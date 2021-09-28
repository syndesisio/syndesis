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
	"strings"
	"time"

	oappsv1 "github.com/openshift/api/apps/v1"
	"github.com/spf13/afero"
	synpkg "github.com/syndesisio/syndesis/install/operator/pkg"
	synapi "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta3"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	appsv1 "k8s.io/api/apps/v1"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/resource"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/apimachinery/pkg/util/intstr"
	"k8s.io/apimachinery/pkg/util/wait"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/remotecommand"
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

// Performs the database upgrade by starting the new database in a
// separate Pod with a separate volume and loading a dump from the
// existing database into it
func (u *databaseUpgrade) run() (err error) {
	u.log.Info("Upgrading database")

	// deploys a new Deployment (`syndesis-db-upgrade`)
	// with the image of the new (target) version
	u.log.V(synpkg.DEBUG_LOGGING_LVL).Info("Deploying the db upgrade")
	if err := u.deployUpgrade(); err != nil {
		return err
	}

	// wait for the `syndesis-db-upgrade` to scale up
	if err := u.awaitScale(upgradeDeploymentName, newDeploymentTracker()); err != nil {
		return err
	}

	// creates the database dump of the current database
	u.log.V(synpkg.DEBUG_LOGGING_LVL).Info("Creating dump of the current database")
	if err := u.createDump(); err != nil {
		return err
	}

	// loads the database dump created above to the new database
	u.log.V(synpkg.DEBUG_LOGGING_LVL).Info("Loading database dump into the new database")
	if err := u.loadDump(); err != nil {
		return err
	}

	// scales down the current (old) database (`syndesis-db`)
	u.log.V(synpkg.DEBUG_LOGGING_LVL).Info("Scaling down the current database")
	if err := u.scaleDownDatabase(); err != nil {
		return err
	}

	// we have the data on the new volume and for transfer to be safe we stop the new database
	u.log.V(synpkg.DEBUG_LOGGING_LVL).Info("Removing the new database used for upgrade")
	if err := u.deleteUpgrade(); err != nil {
		return err
	}

	// now move the new data over to the existing volume, the deployment will be upgraded
	// with the new database version
	u.log.V(synpkg.DEBUG_LOGGING_LVL).Info("Moving migrated data to the database volume")
	if err := u.moveMigratedData(); err != nil {
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

// Deploys the `syndesis-db-upgrade` Deployment with the new version
// of the database backed by a new volume for the data files and a
// separate volume that will be used for the database dump of the
// current database
func (u *databaseUpgrade) deployUpgrade() error {
	config, err := configuration.GetProperties(
		u.context, configuration.TemplateConfig,
		u.clientTools,
		u.syndesis)
	if err != nil {
		return err
	}

	one := int32(1)
	limits := make(corev1.ResourceList)
	if limitMemory, err := resource.ParseQuantity(config.Syndesis.Components.Database.Resources.Limit.Memory); err == nil {
		limits[corev1.ResourceMemory] = limitMemory
	}
	if limitCPU, err := resource.ParseQuantity(config.Syndesis.Components.Database.Resources.Limit.CPU); err == nil {
		limits[corev1.ResourceCPU] = limitCPU
	}

	requests := make(corev1.ResourceList)
	if requestsMemory, err := resource.ParseQuantity(config.Syndesis.Components.Database.Resources.Request.Memory); err == nil {
		requests[corev1.ResourceMemory] = requestsMemory
	}
	if requestsCPU, err := resource.ParseQuantity(config.Syndesis.Components.Database.Resources.Request.CPU); err == nil {
		requests[corev1.ResourceCPU] = requestsCPU
	}

	metadata := upgradeMetadata.DeepCopy()
	metadata.SetNamespace(u.namespace)

	upgradePV := corev1.PersistentVolumeClaim{
		ObjectMeta: metav1.ObjectMeta{
			Name:      upgradeDeploymentName,
			Namespace: u.namespace,
			Labels:    upgradeLabels,
		},
		Spec: corev1.PersistentVolumeClaimSpec{
			AccessModes: []corev1.PersistentVolumeAccessMode{
				corev1.ReadWriteOnce,
			},
			Resources: corev1.ResourceRequirements{
				Requests: corev1.ResourceList{
					corev1.ResourceStorage: resource.MustParse(config.Syndesis.Components.Database.Resources.VolumeCapacity),
				},
			},
		},
	}
	if len(config.Syndesis.Components.Database.Resources.VolumeStorageClass) > 0 {
		upgradePV.Spec.StorageClassName = &config.Syndesis.Components.Database.Resources.VolumeStorageClass
	}

	if _, err = u.apiClient().CoreV1().PersistentVolumeClaims(u.namespace).Create(u.context, &upgradePV, metav1.CreateOptions{}); err != nil {
		return err
	}

	dep := appsv1.Deployment{
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
							Name: "syndesis-db-upgrade-data",
							VolumeSource: corev1.VolumeSource{
								PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
									ClaimName: "syndesis-db-upgrade",
								},
							},
						},
						{
							Name: "syndesis-db-upgrade-dump",
							VolumeSource: corev1.VolumeSource{
								EmptyDir: &corev1.EmptyDirVolumeSource{},
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
									Name:  "PGPASSWORD",
									Value: config.Syndesis.Components.Database.Password,
								},
							},
							ImagePullPolicy: corev1.PullIfNotPresent,
							Image:           config.Syndesis.Components.Database.Image,
							Resources: corev1.ResourceRequirements{
								Limits:   limits,
								Requests: requests,
							},
							VolumeMounts: []corev1.VolumeMount{
								{
									Name:      "syndesis-db-upgrade-data",
									MountPath: "/var/lib/pgsql/data",
								},
								{
									Name:      "syndesis-db-upgrade-dump",
									MountPath: "/dump",
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
	}

	_, err = u.apiClient().AppsV1().Deployments(u.namespace).Create(u.context, &dep, metav1.CreateOptions{})

	return err
}

// Creates the database dump in `/dump/database.dump` of the new database instance
// by executing pg_dump > /dump/database.dump. We're relying on the new psql client
// to be able to access the current database. The password is provided via environment
// variable `PGPASSWORD` above
func (u *databaseUpgrade) createDump() error {
	config, err := configuration.GetProperties(
		u.context, configuration.TemplateConfig,
		u.clientTools,
		u.syndesis)
	if err != nil {
		return err
	}

	return u.executeInDbUpgradePod([]string{
		"pg_dump",
		"--file=/dump/database.dump",
		"--dbname=" + config.Syndesis.Components.Database.Name,
		"--host=syndesis-db",
		"--port=5432",
		"--username=" + config.Syndesis.Components.Database.User,
	})
}

// Loads the database dump from `/dump/database.dump` into the new database instance
// by executing psql -f /dump/database.dump and then runs an ANALYZE to update the
// statistics
func (u *databaseUpgrade) loadDump() error {
	config, err := configuration.GetProperties(
		u.context, configuration.TemplateConfig,
		u.clientTools,
		u.syndesis)
	if err != nil {
		return err
	}

	return u.executeInDbUpgradePod([]string{
		"bash",
		"-c",
		fmt.Sprintf(`set -euxo pipefail
psql --set=ON_ERROR_STOP=on --file /dump/database.dump --dbname=%s
psql --dbname=%s --command 'ANALYZE'
`, config.Syndesis.Components.Database.Name, config.Syndesis.Components.Database.Name),
	})
}

func (u *databaseUpgrade) deleteUpgrade() error {
	return u.apiClient().AppsV1().Deployments(u.namespace).Delete(u.context, upgradeDeploymentName, metav1.DeleteOptions{})
}

// Creates a Job that will transfer the data files from the new database
// into the volume of the current (old) database
func (u *databaseUpgrade) moveMigratedData() error {
	config, err := configuration.GetProperties(
		u.context, configuration.TemplateConfig,
		u.clientTools,
		u.syndesis)
	if err != nil {
		return err
	}

	metadata := upgradeMetadata.DeepCopy()
	metadata.SetNamespace(u.namespace)

	one := int32(1)
	four := int32(4)
	job, err := u.apiClient().BatchV1().Jobs(u.namespace).Create(u.context, &batchv1.Job{
		ObjectMeta: *metadata,
		Spec: batchv1.JobSpec{
			BackoffLimit: &four,
			Parallelism:  &one,
			Template: corev1.PodTemplateSpec{
				ObjectMeta: metav1.ObjectMeta{
					Name:   "syndesis-db-upgrade",
					Labels: metadata.Labels,
				},
				Spec: corev1.PodSpec{
					ServiceAccountName: "syndesis-default",
					RestartPolicy:      corev1.RestartPolicyNever,
					Volumes: []corev1.Volume{
						{
							Name: "syndesis-db-data",
							VolumeSource: corev1.VolumeSource{
								PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
									ClaimName: "syndesis-db",
								},
							},
						},
						{
							Name: "syndesis-db-upgrade-data",
							VolumeSource: corev1.VolumeSource{
								PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSource{
									ClaimName: "syndesis-db-upgrade",
								},
							},
						},
					},
					Containers: []corev1.Container{
						{
							Name: "move-data",
							Command: []string{
								"bash",
								"-c",
								`TS=$(date +%Y%m%d%H%M%S)
function cleanup {
  rm -rf /destination/userdata.${TS}
}
trap cleanup EXIT
set -euxo pipefail
mv /source/userdata/ /destination/userdata.${TS}
rm -rf /destination/userdata
mv /destination/userdata.${TS} /destination/userdata`},
							ImagePullPolicy: corev1.PullIfNotPresent,
							Image:           config.Syndesis.Components.Database.LoggerImage, // reuse image
							VolumeMounts: []corev1.VolumeMount{
								{
									Name:      "syndesis-db-upgrade-data",
									MountPath: "/source",
								},
								{
									Name:      "syndesis-db-data",
									MountPath: "/destination",
								},
							},
						},
					},
				},
			},
		},
	}, metav1.CreateOptions{})
	if err != nil {
		return err
	}

	jobPoller := func() (done bool, err error) {
		if job, err := u.apiClient().BatchV1().Jobs(u.namespace).Get(u.context, job.Name, metav1.GetOptions{}); err != nil {
			return false, err
		} else {
			if job.Status.Failed != 0 {
				return false, fmt.Errorf("failed to move migrated database data")
			} else if job.Status.Succeeded != 0 {
				return true, nil
			} else {
				return false, nil
			}
		}
	}

	return wait.PollImmediate(time.Second, time.Minute, jobPoller)
}

// we'd like to mock this in tests as the RESTClient is nil and the
// remotecommand.NewSPDYExecutor doesn't have a fake or can be (easily)
// mocked not to execute HTTP requests
var exec func(util.ExecOptions) error = util.Exec

// make it easier to mock
func defaultRESTConfig(u *databaseUpgrade) *rest.Config {
	return u.config()
}

var restConfig func(u *databaseUpgrade) *rest.Config = defaultRESTConfig

func (u *databaseUpgrade) executeInDbUpgradePod(cmd []string) error {
	dbUpgradePodName, err := u.dbUpgradePodName()
	if err != nil {
		return err
	}

	if err := exec(util.ExecOptions{
		Config:    restConfig(u),
		Api:       u.apiClient(),
		Namespace: u.namespace,
		Pod:       dbUpgradePodName,
		Container: "postgresql",
		Command:   cmd,
		StreamOptions: remotecommand.StreamOptions{
			Stdin:  strings.NewReader(""),
			Stdout: &logWriter{u.log.WithName("stdout").V(synpkg.DEBUG_LOGGING_LVL).Info},
			Stderr: &logWriter{u.log.WithName("stderr").V(synpkg.DEBUG_LOGGING_LVL).Info},
		},
	}); err != nil {
		return fmt.Errorf("executing: `%v` in the `posgresql` container of the `%v` pod: %v (turn on debug logging to see output)", strings.Join(cmd[:], " "), dbUpgradePodName, err)
	}

	return nil
}

func (u *databaseUpgrade) dbUpgradePodName() (string, error) {
	if pods, err := u.apiClient().CoreV1().Pods(u.namespace).List(u.context, metav1.ListOptions{
		LabelSelector: "syndesis.io/component=syndesis-db-upgrade",
	}); err != nil {
		return "", err
	} else {
		if len(pods.Items) == 0 {
			return "", fmt.Errorf("no Pod with label `syndesis.io/component=syndesis-db-upgrade` found in namespace %s", u.namespace)
		} else if len(pods.Items) != 1 {
			return "", fmt.Errorf("more than one Pod with label `syndesis.io/component=syndesis-db-upgrade` found in namespace %s", u.namespace)
		}

		return pods.Items[0].Name, nil
	}
}

type logWriter struct {
	logfn func(msg string, keysAndValues ...interface{})
}

func (l *logWriter) Write(p []byte) (int, error) {
	l.logfn(string(p[:]))

	return len(p), nil
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

		return 0.0, errors.New("unable to parse PostgreSQL version, no data found in /data/postgresql.txt")
	}

	line := s.Text()

	extracted := postgresVersionRegex.FindStringSubmatch(line)
	if len(extracted) < 2 {
		return 0.0, fmt.Errorf("unable to parse PostgreSQL version from version string: `%s`", line)
	}

	return strconv.ParseFloat(extracted[1], 64)
}
