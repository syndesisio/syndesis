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
	"context"
	"strings"
	"testing"

	"github.com/go-logr/zapr"
	oappsv1 "github.com/openshift/api/apps/v1"
	"github.com/spf13/afero"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta2"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"go.uber.org/zap"
	appsv1 "k8s.io/api/apps/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	cgofake "k8s.io/client-go/kubernetes/fake"
	"k8s.io/client-go/kubernetes/scheme"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/client/fake"
)

func TestShouldCreateNewInstance(t *testing.T) {
	if newDatabaseUpgrade(step{}, nil) == nil {
		t.Fatal("Should create new instance, got nil")
	}
}

func TestShouldNotRunForExternalDatabases(t *testing.T) {
	u := newDatabaseUpgrade(step{}, &v1beta2.Syndesis{
		Spec: v1beta2.SyndesisSpec{
			Components: v1beta2.ComponentsSpec{
				Database: v1beta2.DatabaseConfiguration{
					ExternalDbURL: "some url",
				},
			},
		},
	})

	if u.canRun() == true {
		t.Fatal("We do not want to run database upgrade for for external databases")
	}
}

func Test_PostgreVersionParsingRegexp(t *testing.T) {
	tests := []struct {
		version string
		value   string
	}{
		{"postgres (PostgreSQL) 10.6 (Debian 10.6-1.pgdg90+1)", "10.6"},
		{"PostgreSQL 9.5.14", "9.5"},
	}

	for _, test := range tests {
		extracted := postgresVersionRegex.FindStringSubmatch(test.version)
		if len(extracted) < 2 || extracted[1] != test.value {
			t.Fatalf("Expecting that version %s would be extracted from %s, but it was %s", test.version, test.value, extracted)
		}
	}
}

func TestShouldDetermineTargetDatabaseVersion(t *testing.T) {
	tests := []struct {
		version  string
		expected float64
	}{
		{"postgres (PostgreSQL) 10.6 (Debian 10.6-1.pgdg90+1)", 10.6},
		{"PostgreSQL 9.5.14", 9.5},
	}

	fs := afero.NewMemMapFs()
	if err := fs.Mkdir("/data", 0755); err != nil {
		t.Fatal(err)
	}

	s := sharedFileTarget{
		fs: fs,
	}

	for _, test := range tests {
		if err := afero.WriteFile(fs, "/data/postgresql.txt", []byte(test.version), 0644); err != nil {
			t.Fatal(err)
		}

		if got, err := s.version(); err != nil {
			t.Fatal(err)
		} else if got != test.expected {
			t.Fatalf("Wanted version %v, got %v", test.expected, got)
		}
	}
}

func TestShouldRunOnlyWhenTargetVersionIsNewerThanCurrent(t *testing.T) {
	tests := []struct {
		target   float64
		current  float64
		couldRun bool
	}{
		{1.0, 1.0, false},
		{1.0, 2.0, false},
		{2.0, 1.0, true},
	}

	for _, test := range tests {
		u := databaseUpgrade{
			step: step{
				log: zapr.NewLogger(zap.NewNop()),
			},
			syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Components: v1beta2.ComponentsSpec{
						Database: v1beta2.DatabaseConfiguration{},
					},
				},
			},
			target:  func() (float64, error) { return test.target, nil },
			current: func() (float64, error) { return test.current, nil },
		}

		if u.canRun() != test.couldRun {
			t.Fatalf("Expected canRun with target=%v and current=%v to return %v", test.target, test.current, test.couldRun)
		}
	}
}

//
// Provides a wrapper client that overrides the Create function, allowing
// objects that have been requested to be created to be manipulated by the
// test before they are actually created in the fake client.
//
type EvtFakeRuntimeClient struct {
	client.Client
}

func (evt EvtFakeRuntimeClient) Create(ctx context.Context, runtimeObj client.Object, opts ...client.CreateOption) error {
	if dep, ok := runtimeObj.(*appsv1.Deployment); ok {
		//
		// Fake the creation of a replica on the create db-upgrade deployment
		//
		dep.Status.Replicas = 1
		dep.Status.ReadyReplicas = 1
	}

	return evt.Client.Create(ctx, runtimeObj)
}

func TestRunDatabaseUpgrade(t *testing.T) {
	configuration.TemplateConfig = "../../../build/conf/config-test.yaml"

	schemeToUse := scheme.Scheme
	if err := oappsv1.Install(schemeToUse); err != nil {
		t.Fatal(err)
	}

	cl := fake.NewFakeClientWithScheme(schemeToUse, &oappsv1.DeploymentConfig{
		ObjectMeta: metav1.ObjectMeta{
			Name: "syndesis-db",
		},
		Status: oappsv1.DeploymentConfigStatus{
			Replicas:      1,
			ReadyReplicas: 1,
		},
	})

	evtClient := EvtFakeRuntimeClient{cl}

	clientTools := clienttools.ClientTools{}
	clientTools.SetRuntimeClient(evtClient)
	apiClient := cgofake.NewSimpleClientset()
	clientTools.SetApiClient(apiClient)

	u := databaseUpgrade{
		step: step{
			log:         zapr.NewLogger(zap.NewNop()),
			context:     context.TODO(),
			clientTools: &clientTools,
		},
		syndesis: &v1beta2.Syndesis{
			Spec: v1beta2.SyndesisSpec{
				Components: v1beta2.ComponentsSpec{
					Database: v1beta2.DatabaseConfiguration{},
				},
			},
		},
		target:  func() (float64, error) { return 2.0, nil },
		current: func() (float64, error) { return 1.0, nil },
		cleanup: func() error { return nil },
	}

	if err := u.run(); err != nil {
		t.Fatal(err)
	}

	deployments := appsv1.DeploymentList{}
	if err := cl.List(u.context, &deployments); err != nil {
		t.Fatal(err)
	}

	if len(deployments.Items) != 1 {
		t.Fatalf("Expected the database upgrade Deployment to be created, but there are %v deployments", len(deployments.Items))
	}

	deployment := deployments.Items[0]
	if !strings.HasPrefix(deployment.ObjectMeta.Name, "syndesis-db-upgrade") {
		t.Fatalf("Expected the database upgrade deployment to be created, but there's a deployment named: %v", deployment.ObjectMeta.Name)
	}

	for _, container := range deployment.Spec.Template.Spec.Containers {
		if container.Name == "postgresql" {
			for _, env := range container.Env {
				if env.Name == "POSTGRESQL_UPGRADE" && env.Value == "copy" {
					return
				}
			}
		}
	}

	t.Fatalf("Could not find the `postgresql` container with environment variable `POSTGRESQL_UPGRADE=copy` in deployment: %v", deployment)
}

func TestShouldDeleteUpgradeDeployment(t *testing.T) {
	configuration.TemplateConfig = "../../../build/conf/config-test.yaml"
	cl := fake.NewFakeClient(&appsv1.Deployment{
		ObjectMeta: upgradeMetadata,
	})
	clientTools := clienttools.ClientTools{}
	clientTools.SetRuntimeClient(cl)
	u := databaseUpgrade{
		step: step{
			log:         zapr.NewLogger(zap.NewNop()),
			context:     context.TODO(),
			clientTools: &clientTools,
		},
	}

	if err := u.deleteDbUpgrade(); err != nil {
		t.Fatal(err)
	}

	deployments := appsv1.DeploymentList{}
	if err := cl.List(u.context, &deployments); err != nil {
		t.Fatal(err)
	}

	if len(deployments.Items) != 0 {
		t.Fatalf("Expected the database upgrade Deployment to be deleted, but it wasn't, there are %v deployments", len(deployments.Items))
	}
}

func TestOnRollbackShouldDeleteUpgradeDeployment(t *testing.T) {
	configuration.TemplateConfig = "../../../build/conf/config-test.yaml"
	cl := fake.NewFakeClient(&appsv1.Deployment{
		ObjectMeta: upgradeMetadata,
	})
	clientTools := clienttools.ClientTools{}
	clientTools.SetRuntimeClient(cl)
	u := databaseUpgrade{
		step: step{
			log:         zapr.NewLogger(zap.NewNop()),
			context:     context.TODO(),
			clientTools: &clientTools,
		},
	}

	if err := u.rollback(); err != nil {
		t.Fatal(err)
	}

	deployments := appsv1.DeploymentList{}
	if err := cl.List(u.context, &deployments); err != nil {
		t.Fatal(err)
	}

	if len(deployments.Items) != 0 {
		t.Fatalf("Expected the database upgrade Deployment to be deleted, but it wasn't, there are %v deployments", len(deployments.Items))
	}
}
