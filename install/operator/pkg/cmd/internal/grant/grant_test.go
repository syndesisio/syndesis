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

package grant

import (
	"context"
	"fmt"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	"k8s.io/api/rbac/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/client/fake"
	"testing"
)

const (
	ns      = "namespace"
	user    = "test-user"
	succeed = "\u2713"
	failed  = "\u2717"
)

// test grant without --cluster options
func TestGrant(t *testing.T) {
	ctx := context.TODO()
	g := &Grant{Role: RoleName, User: user, Options: &internal.Options{Namespace: ns, Context: ctx}}

	// Create a fake client to mock API calls and pass it to the cmd
	objs := []runtime.Object{}
	cl := fake.NewFakeClient(objs...)
	g.Client = &cl

	t.Logf("\tTest: When running `operator grant --user user`, it should create the role %s and bind it to the user %s", RoleName, user)
	if err := g.grant(); err != nil {
		t.Fatalf("\t%s\t got an error when granting permissions: [%v]", failed, err)
	}
	t.Logf("\t%s\t permissions granted without errors", succeed)

	{
		r := &v1.Role{}
		if err := cl.Get(ctx, client.ObjectKey{Name: RoleName, Namespace: ns}, r); err != nil {
			t.Fatalf("\t%s\t after running the command, a role named [%s] should be created, but got an error [%v]", failed, RoleName, err)
		}
		t.Logf("\t%s\t after running the command, a role named [%s] was created", succeed, RoleName)
	}

	{
		rb := &v1.RoleBinding{}
		rbn := fmt.Sprintf("%s-%s", RoleName, user)

		if err := cl.Get(ctx, client.ObjectKey{Name: rbn, Namespace: ns}, rb); err != nil {
			t.Fatalf("\t%s\t after running the command, a rolebinding named [%s] should be created, but got an error [%v]", failed, rbn, err)
		}
		t.Logf("\t%s\t after running the command, a rolebinding named [%s] was created", succeed, rbn)

		if rb.RoleRef.Name != RoleName {
			t.Fatalf("\t%s\t the role reference in the rolebinding should be [%s], but got [%s]", failed, RoleName, rb.RoleRef.Name)
		}
		t.Logf("\t%s\t the role reference in the rolebinding is [%s]", succeed, RoleName)

		if l := len(rb.Subjects); l != 1 {
			t.Fatalf("\t%s\t the rolebinding should only have one subject associated, but got [%d]", failed, l)
		} else {
			t.Logf("\t%s\t the rolebinding only have one subject associated", succeed)

			if rb.Subjects[0].Name != user {
				t.Fatalf("\t%s\t the rolebinding's associated subject should be [%s], but got [%s]", failed, user, rb.Subjects[0].Name)
			}
			t.Logf("\t%s\t the rolebinding's associated subject is [%s]", succeed, user)
		}
	}
}

// test grant with --cluster options
func TestGrantCluster(t *testing.T) {
	ctx := context.TODO()
	g := &Grant{Role: RoleName, User: user, cluster: true, Options: &internal.Options{Namespace: ns, Context: ctx}}

	// Create a fake client to mock API calls and pass it to the cmd
	objs := []runtime.Object{}
	cl := fake.NewFakeClient(objs...)
	g.Client = &cl

	t.Logf("\tTest: When running `operator grant --user user --cluster`, it should create a clusterrole %s and bind it to the user %s", RoleName, user)
	if err := g.grant(); err != nil {
		t.Fatalf("\t%s\t got an error when granting permissions: [%v]", failed, err)
	}
	t.Logf("\t%s\t permissions granted without errors", succeed)

	{
		cr := &v1.ClusterRole{}
		if err := cl.Get(ctx, client.ObjectKey{Name: RoleName}, cr); err != nil {
			t.Fatalf("\t%s\t after running the command, a clusterrole named [%s] should be created, but got an error [%v]", failed, RoleName, err)
		}
		t.Logf("\t%s\t after running the command, a clusterrole named [%s] was created", succeed, RoleName)
	}

	{
		crb := &v1.ClusterRoleBinding{}
		rbn := fmt.Sprintf("%s-%s", RoleName, user)

		if err := cl.Get(ctx, client.ObjectKey{Name: rbn}, crb); err != nil {
			t.Fatalf("\t%s\t after running the command, a clusterrolebinding named [%s] should be created, but got an error [%v]", failed, rbn, err)
		}
		t.Logf("\t%s\t after running the command, a clusterrolebinding named [%s] was created", succeed, rbn)

		if crb.RoleRef.Name != RoleName {
			t.Fatalf("\t%s\t the role reference in the clusterrolebinding should be [%s], but got [%s]", failed, RoleName, crb.RoleRef.Name)
		}
		t.Logf("\t%s\t the role reference in the clusterrolebinding is [%s]", succeed, RoleName)

		if l := len(crb.Subjects); l != 1 {
			t.Fatalf("\t%s\t the clusterrolebinding should only have one subject associated, but got [%d]", failed, l)
		} else {
			t.Logf("\t%s\t the clusterrolebinding only have one subject associated", succeed)

			if crb.Subjects[0].Name != user {
				t.Fatalf("\t%s\t the clusterrolebinding's associated subject should be [%s], but got [%s]", failed, user, crb.Subjects[0].Name)
			}
			t.Logf("\t%s\t the clusterrolebinding's associated subject is [%s]", succeed, user)
		}
	}
}
