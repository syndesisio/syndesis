/*
 * Copyright (C) 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package upgrade

import (
	"bufio"
	"errors"
	"fmt"
	"regexp"
	"strconv"

	oappsv1 "github.com/openshift/api/apps/v1"
	"github.com/spf13/afero"
	synpkg "github.com/syndesisio/syndesis/install/operator/pkg"
	synapi "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta3"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/resource"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/apimachinery/pkg/util/intstr"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

var upgradeMetadata = metav1.ObjectMeta{
	Name:   upgradeDeploymentName,
	Labels: upgradeLabels,
}

// matches anything followed by space followed by number.number followed (optionally) by another .number and an optional space
// meant to parse strings like "PostgreSQL 9.5.14" to "9.5" and "postgres (PostgreSQL) 10.6 (Debian 10.6-1.pgdg90+1)" to "10.6"
var postgresVersionRegex = regexp.MustCompile(`^.* (\d+\.\d+)(?:\.d+)? ?`)

// upgrades the database by leveraging the builtin functionality of the PostgreSQL image from Software Collections
// key functionality is that the upgrade can be triggered by specifying the `POSTGRESQL_UPGRADE=copy` environment
// variable
type databaseUpgrade struct {
	step
	syndesis *synapi.Syndesis
	target   func() (float64, error) // target version of PostgreSQL as detected at runtime from the file left by the init container
	current  func() (float64, error) // current version of PostgreSQL as detected at runtime by querying the running database
	cleanup  func() error            // how to perform cleanup, that is what to do in case of rollback or when we're done with the upgrade
}

func newDatabaseUpgrade(base step, s *synapi.Syndesis) stepRunner {
	sharedFile := &sharedFileTarget{
		fs: afero.NewOsFs(),
	}

	u := databaseUpgrade{
		step:     base,
		syndesis: s,
		target:   sharedFile.version,
	}

	u.current = u.currentFromRunningDatabase
	u.cleanup = u.deleteDbUpgrade

	return &u
}

func (u *databaseUpgrade) run() (err error) {
	u.log.Info("Upgrading database")

	// scales down the database (`syndesis-db`)
	if err := u.scaleDownDatabase(); err != nil {
		return err
	}

	// deploys a new Deployment (`syndesis-db-upgrade`)
	// with the image of the new (target) version with
	// the environment variable set to perform the
	// upgrade by running pg_upgrade
	u.log.V(synpkg.DEBUG_LOGGING_LVL).Info("Deploying the db upgrade")
	if err := u.deployUpgrade(); err != nil {
		return err
	}

	// wait for the `syndesis-db-upgrade` to scale up
	// this marks the end of the upgrade
	if err := u.awaitScale(upgradeDeploymentName, newDeploymentTracker()); err != nil {
		return err
	}

	return nil
}

func (u *databaseUpgrade) rollback() (err error) {
	if err := u.deleteDbUpgrade(); err != nil {
		u.log.Error(err, "Unable to delete database upgrade Deployment during rollback")
	}

	return nil
}

func (u *databaseUpgrade) canRun() bool {
	if len(u.syndesis.Spec.Components.Database.ExternalDbURL) > 0 {
		return false
	}

	target, err := u.target()
	if err != nil {
		u.log.Error(err, "Unable to determine target PostgreSQL version")
		return false
	}

	current, err := u.current()
	if err != nil {
		u.log.Error(err, "Unable to determine current PostgreSQL version")
		return false
	}

	u.name = fmt.Sprintf("Database upgrade from %f to %f", current, target)

	u.log.Info("Determined versions of PostgreSQL database", "target", target, "current", current)
	return target > current
}

// Connects to the running version of the database and queries it's version
func (u *databaseUpgrade) currentFromRunningDatabase() (float64, error) {
	config, err := configuration.GetProperties(
		u.context,
		configuration.TemplateConfig,
		u.clientTools,
		u.syndesis)

	if err != nil {
		return 0.0, err
	}

	databaseConfiguration := config.Syndesis.Components.Database

	return util.PostgreSQLVersionAt(databaseConfiguration.User,
		config.Syndesis.Components.Database.Password,
		databaseConfiguration.Name,
		databaseConfiguration.URL)
}

// Patches the `syndesis-db` DeploymentConfig setting the number
// of replicas to 0
func (u *databaseUpgrade) scaleDownDatabase() error {
	u.log.Info("Scaling down the database deployment", "deployment", "syndesis-db")
	if err := u.client().Patch(u.context, &oappsv1.DeploymentConfig{
		ObjectMeta: metav1.ObjectMeta{
			Name:      "syndesis-db",
			Namespace: u.namespace,
		},
	}, client.RawPatch(types.MergePatchType, []byte(`{"spec":{"replicas":0}}`))); err != nil {
		return err
	}

	if err := u.awaitScale("syndesis-db", newDeploymentConfigTracker()); err != nil {
		return err
	}

	return nil
}

// Deploys the `syndesis-db-upgrade` Deployment with the same `syndesis-db-data` volume
// as the current `syndesis-db` DeploymentConfig. Specifying `POSTGRESQL_UPGRADE=copy`
// instructs the startup scripts within the centos/postgresql image to run pg_upgrade
// to migrate the data files
func (u *databaseUpgrade) deployUpgrade() error {
	config, err := configuration.GetProperties(
		u.context, configuration.TemplateConfig,
		u.clientTools,
		u.syndesis)
	if err != nil {
		return err
	}

	one := int32(1)
	limitMemory, err := resource.ParseQuantity(config.Syndesis.Components.Database.Resources.Limit.Memory)
	if err != nil {
		return err
	}

	requestMemory, err := resource.ParseQuantity(config.Syndesis.Components.Database.Resources.Request.Memory)
	if err != nil {
		return err
	}

	metadata := upgradeMetadata.DeepCopy()
	metadata.SetNamespace(u.namespace)

	return u.client().Create(u.context, &appsv1.Deployment{
		ObjectMeta: *metadata,
		Spec: appsv1.DeploymentSpec{
			Replicas: &one,
			Selector: &metav1.LabelSelector{
				MatchLabels: upgradeLabels,
			},
			Template: corev1.PodTemplateSpec{
				ObjectMeta: upgradeMetadata,
				Spec: corev1.PodSpec{
					ServiceAccountName: "syndesis-default",
					Volumes: []corev1.Volume{
						{
							Name: "syndesis-db-data",
							VolumeSource: corev1.VolumeSource{
								PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
									ClaimName: "syndesis-db",
								},
							},
						},
					},
					Containers: []corev1.Container{
						{
							Name: "postgresql",
							Env: []corev1.EnvVar{
								{
									Name:  "POSTGRESQL_USER",
									Value: config.Syndesis.Components.Database.User,
								},
								{
									Name:  "POSTGRESQL_PASSWORD",
									Value: config.Syndesis.Components.Database.Password,
								},
								{
									Name:  "POSTGRESQL_DATABASE",
									Value: config.Syndesis.Components.Database.Name,
								},
								{
									Name:  "POSTGRESQL_UPGRADE",
									Value: "copy",
								},
							},
							Ports: []corev1.ContainerPort{
								{
									ContainerPort: 5432,
									Protocol:      corev1.ProtocolTCP,
								},
							},
							ImagePullPolicy: corev1.PullIfNotPresent,
							Image:           config.Syndesis.Components.Database.Image,
							Resources: corev1.ResourceRequirements{
								Limits: corev1.ResourceList{
									corev1.ResourceMemory: limitMemory,
								},
								Requests: corev1.ResourceList{
									corev1.ResourceMemory: requestMemory,
								},
							},
							VolumeMounts: []corev1.VolumeMount{
								{
									Name:      "syndesis-db-data",
									MountPath: "/var/lib/pgsql/data",
								},
							},
							ReadinessProbe: &corev1.Probe{
								Handler: corev1.Handler{
									Exec: &corev1.ExecAction{
										Command: []string{
											"/bin/sh",
											"-i",
											"-c",
											"psql -h 127.0.0.1 -U $POSTGRESQL_USER -q -d $POSTGRESQL_DATABASE -c 'SELECT 1'",
										},
									},
								},
								InitialDelaySeconds: 5,
								PeriodSeconds:       10,
								SuccessThreshold:    1,
							},
							LivenessProbe: &corev1.Probe{
								Handler: corev1.Handler{
									TCPSocket: &corev1.TCPSocketAction{
										Port: intstr.FromInt(5432),
									},
								},
								InitialDelaySeconds: 60,
								PeriodSeconds:       10,
								SuccessThreshold:    1,
							},
						},
					},
				},
			},
		},
	})
}

// simple strategy to load the version of the database left in /data/postgresql.txt by the init container
// we use afero filesystem to mock the filesystem in tests
type sharedFileTarget struct {
	fs afero.Fs
}

func (sharedFile *sharedFileTarget) version() (float64, error) {
	f, err := sharedFile.fs.Open("/data/postgresql.txt")
	if err != nil {
		return 0.0, err
	}
	defer f.Close()

	s := bufio.NewScanner(f)
	if !s.Scan() {
		if s.Err() != nil {
			return 0.0, s.Err()
		}

		return 0.0, errors.New("Unable to parse PostgreSQL version, no data found in /data/postgresql.txt")
	}

	line := s.Text()

	extracted := postgresVersionRegex.FindStringSubmatch(line)
	if len(extracted) < 2 {
		return 0.0, fmt.Errorf("Unable to parse PostgreSQL version from version string: `%s`", line)
	}

	return strconv.ParseFloat(extracted[1], 64)
}
