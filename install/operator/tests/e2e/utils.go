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

package e2e

import (
	goctx "context"
	framework "github.com/operator-framework/operator-sdk/pkg/test"
	v1alpha1 "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	v1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/wait"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"testing"
	"time"
)

func WaitForSyndesisPhase(t *testing.T, f *framework.Framework, namespace, name string, phase v1alpha1.SyndesisPhase, retryInterval, timeout time.Duration) error {
	err := wait.Poll(retryInterval, timeout, func() (done bool, err error) {
		syndesis := &v1alpha1.Syndesis{}

		err = f.Client.Get(goctx.TODO(), client.ObjectKey{Namespace: namespace, Name: name}, syndesis)
		if err != nil {
			t.Logf("waiting for availability of %s syndesis\n", name)
			return false, err
		}

		if syndesis.Status.Phase == phase {
			t.Logf("syndesis phase for %s transitioned to %s\n", name, syndesis.Status.Phase)
			return true, nil
		}

		t.Logf("waiting for full availability of %s cr, phase is currently %s\n", name, syndesis.Status.Phase)
		return false, nil
	})
	if err != nil {
		return err
	}

	return nil
}

func CreateEmpyCR(name string, namespace string) *v1alpha1.Syndesis {
	return &v1alpha1.Syndesis{
		ObjectMeta: v1.ObjectMeta{
			Name:      name,
			Namespace: namespace,
		},
		Spec: v1alpha1.SyndesisSpec{},
	}
}

func CreateCR(name string, namespace string) *v1alpha1.Syndesis {
	return &v1alpha1.Syndesis{
		ObjectMeta: v1.ObjectMeta{
			Name:      name,
			Namespace: namespace,
		},
		Spec: v1alpha1.SyndesisSpec{
			Components: v1alpha1.ComponentsSpec{
				Db: v1alpha1.DbConfiguration{
					Tag:      "9.5",
					User:     "syndesis",
					Database: "syndesis",
					Resources: v1alpha1.ResourcesWithVolume{
						Resources:      v1alpha1.Resources{},
						VolumeCapacity: "512Mi",
					},
				},
				Server: v1alpha1.ServerConfiguration{
					Tag: "latest",
				},
				Meta: v1alpha1.MetaConfiguration{
					Tag: "latest",
				},
				Prometheus: v1alpha1.PrometheusConfiguration{
					Tag: "v2.1.0",
				},
			},
			Addons: v1alpha1.AddonsSpec{
				"komodo": {
					"installed": "true",
				},
			},
		},
	}
}
