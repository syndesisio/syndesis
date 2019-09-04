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

package install

import (
	"context"
	"testing"

	v12 "github.com/openshift/api/image/v1"

	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	v1 "k8s.io/api/rbac/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/client-go/kubernetes/scheme"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/client/fake"
)

const (
	ns      = "namespace"
	tag     = "1.8.0"
	succeed = "\u2713"
	failed  = "\u2717"
)

func TestInstallOperator(t *testing.T) {
	if testing.Short() {
		t.Skip("skipping operator install tests in short mode")
	}

	ctx := context.TODO()
	i := &Install{Options: &internal.Options{Namespace: ns, Context: ctx}, tag: tag}

	s := scheme.Scheme
	if err := v12.AddToScheme(s); err != nil {
		t.Fatalf("Unable to add route scheme: (%v)", err)
	}

	// Create a fake client to mock API calls and pass it to the cmd
	objs := []runtime.Object{}
	cl := fake.NewFakeClient(objs...)
	i.Client = &cl

	t.Logf("\tTest: When running `operator install --tag`, it should create the role %s", RoleName)
	if err := i.installOperatorResources(); err != nil {
		t.Fatalf("\t%s\t got an error when running the command: [%v]", failed, err)
	}
	t.Logf("\t%s\t command ran without errors", succeed)

	{
		r := &v1.Role{}
		if err := cl.Get(ctx, client.ObjectKey{Name: RoleName, Namespace: ns}, r); err != nil {
			t.Fatalf("\t%s\t after running the command, a role named [%s] should be created, but got an error [%v]", failed, RoleName, err)
		}
		t.Logf("\t%s\t after running the command, a role named [%s] was created", succeed, RoleName)

		is := &v12.ImageStream{}
		if err := cl.Get(ctx, client.ObjectKey{Name: RoleName, Namespace: ns}, is); err != nil {
			t.Fatalf("\t%s\t after running the command, an imagestream named [%s] should be created, but got an error [%v]", failed, RoleName, err)
		}
		t.Logf("\t%s\t after running the command, an imagestream named [%s] was created", succeed, RoleName)

		if len(is.Spec.Tags) != 1 {
			t.Fatalf("\t%s\t the imagestream should have only one tag, but got %d", failed, 1)
		} else {
			t.Logf("\t%s\t the imagestream has only one tag", succeed)

			if is.Spec.Tags[0].Name != tag {
				t.Fatalf("\t%s\t the imagestream tag should be named [%s], but got %s", failed, tag, is.Spec.Tags[0].Name)
			}
			t.Logf("\t%s\t the imagestream tag is named [%s]", succeed, tag)
		}
	}
}
