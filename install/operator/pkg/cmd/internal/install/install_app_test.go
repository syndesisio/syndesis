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

	"github.com/stretchr/testify/assert"

	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime"
	dynfake "k8s.io/client-go/dynamic/fake"
)

const (
	appNS      = "namespace"
	appTag     = "1.9.0"
	appSucceed = "\u2713"
	appFailed  = "\u2717"
)

func TestInstallAppNoCustomResource(t *testing.T) {
	if testing.Short() {
		t.Skip("skipping app install tests in short mode")
	}

	ctx := context.TODO()
	i := &Install{Options: &internal.Options{Namespace: appNS, Context: ctx}, tag: appTag, devSupport: false}

	scheme := runtime.NewScheme()
	dyncl := dynfake.NewSimpleDynamicClient(scheme)
	i.DynamicClient = dyncl

	//
	// Want to compare app resource being installed rather than actually installing
	//
	i.eject = "yaml"
	i.ejectedResources = []unstructured.Unstructured{}

	if err := i.installApplication(); err != nil {
		t.Fatalf("\t%s\t got an error when running the command: [%v]", appFailed, err)
	}

	assert.True(t, len(i.ejectedResources) == 1)

	//
	// No custom resource specified ensures default resource is used
	//
	resource, err := util.LoadUnstructuredObjectFromFile("default_app_test.yml")
	if err != nil {
		t.Fatalf("\t%s\t got an error when loading test app.yml file: [%v]", appFailed, err)
	}

	assert.Equal(t, i.ejectedResources[0], *resource)
}

func TestInstallAppCustomResource(t *testing.T) {
	if testing.Short() {
		t.Skip("skipping app install tests in short mode")
	}

	ctx := context.TODO()
	i := &Install{Options: &internal.Options{Namespace: appNS, Context: ctx}, tag: appTag, devSupport: false}

	scheme := runtime.NewScheme()
	dyncl := dynfake.NewSimpleDynamicClient(scheme)
	i.DynamicClient = dyncl

	//
	// Want to compare app resource being installed rather than actually installing
	//
	i.eject = "yaml"
	i.ejectedResources = []unstructured.Unstructured{}

	i.customResource = "custom_app_test.yml"

	if err := i.installApplication(); err != nil {
		t.Fatalf("\t%s\t got an error when running the command: [%v]", appFailed, err)
	}

	assert.True(t, len(i.ejectedResources) == 1)

	//
	// Custom resource should be rendered and used for install
	//
	resource, err := util.LoadUnstructuredObjectFromFile("custom_app_test.yml")
	if err != nil {
		t.Fatalf("\t%s\t got an error when loading test app.yml file: [%v]", appFailed, err)
	}

	assert.Equal(t, i.ejectedResources[0], *resource)
}
