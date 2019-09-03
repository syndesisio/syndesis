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
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	v1 "k8s.io/api/rbac/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/client/fake"
	"testing"
)

const (
	ns      = "namespace"
	succeed = "\u2713"
	failed  = "\u2717"
)

func TestInstallOperator(t *testing.T) {
	if testing.Short() {
		t.Skip("skipping operator install tests in short mode")
	}

	ctx := context.TODO()
	i := &Install{Options: &internal.Options{Namespace: ns, Context: ctx}}

	// Create a fake client to mock API calls and pass it to the cmd
	objs := []runtime.Object{}
	cl := fake.NewFakeClient(objs...)
	i.Client = &cl

	t.Logf("\tTest: When running `operator install`, it should create the role %s", RoleName)
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
	}
}
