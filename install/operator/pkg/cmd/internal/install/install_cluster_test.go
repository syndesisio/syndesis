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
	synapi "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta3"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	syntesting "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/testing"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	discoveryfake "k8s.io/client-go/discovery/fake"
	gofake "k8s.io/client-go/kubernetes/fake"
	clfake "sigs.k8s.io/controller-runtime/pkg/client/fake"
)

func TestIsExtensionsApiPresentOrNot(t *testing.T) {
	apiResource := metav1.APIResourceList{
		GroupVersion: "apiextensions.k8s.io/v1",
		APIResources: []metav1.APIResource{
			{Name: "CustomResourceDefinition"},
		},
	}

	testCases := []struct {
		name     string
		apiList  []*metav1.APIResourceList
		expected bool
	}{
		{
			"Extension v1 API Present",
			[]*metav1.APIResourceList{&apiResource},
			true,
		},
		{
			"Extension v1 API Not Present",
			[]*metav1.APIResourceList{},
			false,
		},
	}

	api := gofake.NewSimpleClientset()
	fd := api.Discovery().(*discoveryfake.FakeDiscovery)
	client := clfake.NewFakeClient()
	i := &Install{Options: &internal.Options{}}
	i.ClientTools().SetRuntimeClient(client)
	i.ClientTools().SetApiClient(api)

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			fd.Resources = tc.apiList
			status, err := i.isExtensionsV1APISupported()
			assert.NoError(t, err)
			assert.Equal(t, tc.expected, status)
		})
	}
}

func TestDowngradeExtensionsApi(t *testing.T) {
	syndesis := &synapi.Syndesis{}
	client := clfake.NewFakeClient()
	i := &Install{Options: &internal.Options{}}
	i.ClientTools().SetRuntimeClient(client)
	i.ClientTools().SetApiClient(syntesting.AllApiClient())

	configuration, err := configuration.GetProperties(context.TODO(), "../../../../build/conf/config-test.yaml", i.ClientTools(), syndesis)
	assert.NoError(t, err)

	resources, err := generator.RenderDir("./install/cluster", configuration)
	assert.NoError(t, err)
	assert.True(t, len(resources) > 0)

	newresources, err := i.downgradeApiExtensions(resources)
	assert.NoError(t, err)
	assert.Equal(t, len(resources), len(newresources))
	assert.NotEqual(t, resources, newresources)

	for _, r := range newresources {
		assert.Equal(t, "CustomResourceDefinition", r.GetKind())
		assert.Equal(t, "apiextensions.k8s.io/v1beta1", r.GetAPIVersion())
		version, exists, _ := unstructured.NestedFieldNoCopy(r.UnstructuredContent(), "spec", "version")
		assert.True(t, exists)
		assert.Equal(t, "v1beta3", version)
	}
}
