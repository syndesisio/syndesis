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

package uninstall

import (
	"context"
	"testing"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"

	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	v1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/client-go/kubernetes/scheme"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/client/fake"
)

const (
	s       = "syndesis-test"
	ns      = "namespace"
	succeed = "\u2713"
	failed  = "\u2717"
)

func TestUninstall(t *testing.T) {
	if testing.Short() {
		t.Skip("skipping operator uninstall tests in short mode")
	}

	ctx := context.TODO()
	s := v1beta1.Syndesis{ObjectMeta: v1.ObjectMeta{Name: s, Namespace: ns}}
	sl := v1beta1.SyndesisList{}

	sch := scheme.Scheme
	sch.AddKnownTypes(v1beta1.SchemeGroupVersion, &s)
	sch.AddKnownTypes(v1beta1.SchemeGroupVersion, &sl)

	u := &Uninstall{Options: &internal.Options{Namespace: ns, Context: ctx}}
	{
		t.Logf("\tTest: When run without any CRs, `operator uninstall` should not fail")
		if err := u.uninstall(); err != nil {
			t.Fatalf("\t%s\t got an error when uninstalling the app: [%v]", failed, err)
		}
		t.Logf("\t%s\t syndesis app uninstalled correctly", succeed)

	}

	// Create a fake client to mock API calls and pass it to the cmd
	objs := []runtime.Object{&s}
	cl := fake.NewFakeClient(objs...)
	listOpts := []client.ListOption{
		client.InNamespace(ns),
	}
	cl.List(ctx, &sl, listOpts...)
	u.ClientTools().SetRuntimeClient(cl)

	{
		t.Logf("\tTest: When running `operator uninstall`, it should remove the exiting syndesis CRs")
		if l := len(sl.Items); l != 1 {
			t.Fatalf("\t%s\t before deleting, there should be a total of 1 syndesis CRs, but got [%d] instead", failed, l)
		}
		t.Logf("\t%s\t before deleting, there should be a total of 1 syndesis CRs", succeed)

		if err := u.uninstall(); err != nil {
			t.Fatalf("\t%s\t got an error when uninstalling the app: [%v]", failed, err)
		}
		t.Logf("\t%s\t syndesis CRs deleted correctly", succeed)

		cl.List(ctx, &sl, listOpts...)
		if l := len(sl.Items); l != 0 {
			t.Fatalf("\t%s\t after deleting, there should be a total of 0 syndesis CRs, but got [%d] instead", failed, l)
		}
		t.Logf("\t%s\t after deleting, there should be a total of 0 syndesis CRs", succeed)
	}
}
