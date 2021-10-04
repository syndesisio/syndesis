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
	"errors"
	"fmt"
	"testing"

	"github.com/go-logr/zapr"
	oappsv1 "github.com/openshift/api/apps/v1"
	"github.com/spf13/afero"
	"github.com/stretchr/testify/assert"
	synapi "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta3"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"go.uber.org/zap"
	appsv1 "k8s.io/api/apps/v1"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	cgofake "k8s.io/client-go/kubernetes/fake"
	"k8s.io/client-go/kubernetes/scheme"
	"k8s.io/client-go/rest"
	cgotesting "k8s.io/client-go/testing"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/client/fake"
)

func TestShouldCreateNewInstance(t *testing.T) {
	if newDatabaseUpgrade(step{}, nil) == nil {
		t.Fatal("Should create new instance, got nil")
	}
}

func TestShouldNotRunForExternalDatabases(t *testing.T) {
	u := newDatabaseUpgrade(step{}, &synapi.Syndesis{
		Spec: synapi.SyndesisSpec{
			Components: synapi.ComponentsSpec{
				Database: synapi.DatabaseConfiguration{
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
			syndesis: &synapi.Syndesis{
				Spec: synapi.SyndesisSpec{
					Components: synapi.ComponentsSpec{
						Database: synapi.DatabaseConfiguration{},
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

		evt.Client.Create(ctx, &corev1.Pod{
			ObjectMeta: dep.Spec.Template.ObjectMeta,
		})
	}

	return evt.Client.Create(ctx, runtimeObj)
}

func TestRunDatabaseUpgrade(t *testing.T) {
	configuration.TemplateConfig = "../../../build/conf/config-test.yaml"

	schemeToUse := scheme.Scheme
	if err := oappsv1.Install(schemeToUse); err != nil {
		t.Fatal(err)
	}

	cl := fake.NewClientBuilder().WithObjects(&oappsv1.DeploymentConfig{
		ObjectMeta: metav1.ObjectMeta{
			Name: "syndesis-db",
		},
		Status: oappsv1.DeploymentConfigStatus{
			Replicas:      1,
			ReadyReplicas: 1,
		},
	}).Build()

	evtClient := EvtFakeRuntimeClient{cl}

	clientTools := clienttools.ClientTools{}
	clientTools.SetRuntimeClient(evtClient)
	apiClient := cgofake.NewSimpleClientset()
	clientTools.SetApiClient(apiClient)

	expectedCommands := [][]string{
		{"pg_dump", "--file=/dump/database.dump", "--dbname=syndesis", "--host=syndesis-db", "--port=5432", "--username=syndesis"},
		{"bash", "-c", `set -euxo pipefail
psql --set=ON_ERROR_STOP=on --file /dump/database.dump --dbname=syndesis
psql --dbname=syndesis --command 'ANALYZE'
`},
	}
	cmdIdx := 0
	exec = func(o util.ExecOptions) error {
		if assert.Lessf(t, cmdIdx, len(expectedCommands), "invoked util.Exec with command %v", o.Command) == false {
			return fmt.Errorf("missing expected command for invocation number %d", cmdIdx)
		}
		if assert.Equal(t, expectedCommands[cmdIdx], o.Command) == false {
			return errors.New("assertion failed")
		}

		cmdIdx = cmdIdx + 1

		return nil
	}

	restConfig = func(u *databaseUpgrade) *rest.Config {
		return nil
	}

	apiClient.PrependReactor("create", "persistentvolumeclaims", func(action cgotesting.Action) (bool, runtime.Object, error) {
		obj := action.(cgotesting.CreateAction).GetObject().(*corev1.PersistentVolumeClaim)
		assert.Equal(t, "syndesis-db-upgrade", obj.GetName())
		return true, obj, nil
	})

	apiClient.PrependReactor("create", "deployments", func(action cgotesting.Action) (bool, runtime.Object, error) {
		obj := action.(cgotesting.CreateAction).GetObject().(*appsv1.Deployment)
		assert.Equal(t, "syndesis-db-upgrade", obj.GetName())

		obj.Status.ReadyReplicas = *obj.Spec.Replicas
		cl.Create(context.TODO(), obj)
		apiClient.Tracker().Add(&corev1.Pod{
			ObjectMeta: obj.Spec.Template.ObjectMeta,
		})

		return true, obj, nil
	})

	apiClient.PrependReactor("delete", "deployments", func(action cgotesting.Action) (bool, runtime.Object, error) {
		return true, nil, nil
	})

	apiClient.PrependReactor("create", "jobs", func(action cgotesting.Action) (bool, runtime.Object, error) {
		obj := action.(cgotesting.CreateAction).GetObject().(*batchv1.Job)
		assert.Equal(t, "syndesis-db-upgrade", obj.GetName())
		return true, obj, nil
	})

	apiClient.PrependReactor("get", "jobs", func(action cgotesting.Action) (bool, runtime.Object, error) {
		return true, &batchv1.Job{
			Status: batchv1.JobStatus{
				Succeeded: 1,
			},
		}, nil
	})

	u := databaseUpgrade{
		step: step{
			log:         zapr.NewLogger(zap.NewNop()),
			context:     context.TODO(),
			clientTools: &clientTools,
		},
		syndesis: &synapi.Syndesis{
			Spec: synapi.SyndesisSpec{
				Components: synapi.ComponentsSpec{
					Database: synapi.DatabaseConfiguration{},
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
}

func TestShouldDeleteUpgradeDeployment(t *testing.T) {
	configuration.TemplateConfig = "../../../build/conf/config-test.yaml"
	cl := fake.NewClientBuilder().WithObjects(&appsv1.Deployment{
		ObjectMeta: upgradeMetadata,
	}).Build()
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
	cl := fake.NewClientBuilder().WithObjects(&appsv1.Deployment{
		ObjectMeta: upgradeMetadata,
	}).Build()
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
