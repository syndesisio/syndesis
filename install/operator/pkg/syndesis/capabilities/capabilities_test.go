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

package capabilities

import (
	"testing"

	"k8s.io/apimachinery/pkg/version"

	gofake "k8s.io/client-go/kubernetes/fake"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	discoveryfake "k8s.io/client-go/discovery/fake"
)

func Test_ApiCapabilities(t *testing.T) {

	res1 := metav1.APIResourceList{
		GroupVersion: "image.openshift.io/v1",
		APIResources: []metav1.APIResource{
			{Name: "imagestreams"},
		},
	}
	res2 := metav1.APIResourceList{
		GroupVersion: "route.openshift.io/v1",
		APIResources: []metav1.APIResource{
			{Name: "routes"},
		},
	}
	res3 := metav1.APIResourceList{
		GroupVersion: "oauth.openshift.io/v1",
		APIResources: []metav1.APIResource{
			{Name: "oauthclientauthorizations"},
		},
	}
	res3a := metav1.APIResourceList{
		GroupVersion: "console.openshift.io/v1",
		APIResources: []metav1.APIResource{
			{Name: "consolelinks"},
		},
	}

	res4 := metav1.APIResourceList{
		GroupVersion: "something.openshift.io/v1",
	}
	res5 := metav1.APIResourceList{
		GroupVersion: "not.anything.io/v1",
	}
	res6 := metav1.APIResourceList{
		GroupVersion: "something.else.io/v1",
	}

	testCases := []struct {
		name     string
		resList  []*metav1.APIResourceList
		expected ApiServerSpec
	}{
		{
			"Relevant APIs available for fully true api spec",
			[]*metav1.APIResourceList{&res1, &res2, &res3, &res3a},
			ApiServerSpec{
				Version:          "1.16",
				Routes:           true,
				ImageStreams:     true,
				EmbeddedProvider: true,
				ConsoleLink:      true,
				Openshift4:       true,
			},
		},
		{
			"No relevant resources so expect false",
			[]*metav1.APIResourceList{&res4, &res5, &res6},
			ApiServerSpec{
				Version:          "1.16",
				Routes:           false,
				ImageStreams:     false,
				EmbeddedProvider: false,
				Openshift4:       false,
			},
		},
		{
			"No resources so everything false",
			[]*metav1.APIResourceList{},
			ApiServerSpec{
				Version:          "1.16",
				Routes:           false,
				ImageStreams:     false,
				EmbeddedProvider: false,
				Openshift4:       false,
			},
		},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			api := gofake.NewSimpleClientset()
			fd := api.Discovery().(*discoveryfake.FakeDiscovery)
			fd.Resources = tc.resList
			fd.FakedServerVersion = &version.Info{
				Major: "1",
				Minor: "16",
			}

			clientTools := &clienttools.ClientTools{}
			clientTools.SetApiClient(api)
			apiSpec, err := ApiCapabilities(clientTools)
			if err != nil {
				t.Error(err)
			}

			if apiSpec == nil {
				t.Error("Failed to return an api specification")
			}

			if apiSpec.Version != tc.expected.Version {
				t.Error("Expected api specification version not expected")
			}

			if apiSpec.Routes != tc.expected.Routes {
				t.Error("Expected api specification routes not expected")
			}

			if apiSpec.ImageStreams != tc.expected.ImageStreams {
				t.Error("Expected api specification image streams not expected")
			}

			if apiSpec.EmbeddedProvider != tc.expected.EmbeddedProvider {
				t.Error("Expected api specification embedded provider not returned")
			}
			if apiSpec.Openshift4 != tc.expected.Openshift4 {
				t.Error("Not Expected openshift4", "expected", apiSpec.Openshift4, "actual", tc.expected.Openshift4)
			}
		})
	}
}

func Test_ApiCapabilitiesOpenshift3(t *testing.T) {

	res1 := metav1.APIResourceList{
		GroupVersion: "image.openshift.io/v1",
		APIResources: []metav1.APIResource{
			{Name: "imagestreams"},
		},
	}

	testCases := []struct {
		name     string
		resList  []*metav1.APIResourceList
		expected ApiServerSpec
	}{
		{
			"Openshift 3 parse version 1.11",
			[]*metav1.APIResourceList{&res1},
			ApiServerSpec{
				Version:          "1.11",
				Routes:           true,
				ImageStreams:     true,
				EmbeddedProvider: true,
				Openshift4:       false,
			},
		},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			api := gofake.NewSimpleClientset()
			fd := api.Discovery().(*discoveryfake.FakeDiscovery)
			fd.Resources = tc.resList
			fd.FakedServerVersion = &version.Info{
				Major: "1",
				Minor: "11",
			}

			clientTools := &clienttools.ClientTools{}
			clientTools.SetApiClient(api)
			apiSpec, err := ApiCapabilities(clientTools)
			if err != nil {
				t.Error(err)
			}

			if apiSpec == nil {
				t.Error("Failed to return an api specification")
			}

			if apiSpec.Version != tc.expected.Version {
				t.Error("Expected api specification version not expected", "Actual", apiSpec.Version, "Expected", tc.expected.Version)
			}

			if apiSpec.ImageStreams != tc.expected.ImageStreams {
				t.Error("Expected api specification image streams not expected")
			}

			if apiSpec.Openshift4 {
				t.Error("Expected openshift platform to version 3")
			}
		})
	}
}
