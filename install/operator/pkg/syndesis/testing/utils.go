/*
 * Copyright (C) 2020 Red Hat, Inc.
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

package testing

import (
	osappsv1 "github.com/openshift/api/apps/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	discoveryfake "k8s.io/client-go/discovery/fake"
	clientset "k8s.io/client-go/kubernetes"
	gofake "k8s.io/client-go/kubernetes/fake"
	"k8s.io/client-go/kubernetes/scheme"
	"sigs.k8s.io/controller-runtime/pkg/client"
	rtfake "sigs.k8s.io/controller-runtime/pkg/client/fake"
)

func openshiftApiClient() clientset.Interface {
	api := gofake.NewSimpleClientset()
	fd := api.Discovery().(*discoveryfake.FakeDiscovery)

	res1 := metav1.APIResourceList{
		GroupVersion: "image.openshift.io/v1",
	}
	res2 := metav1.APIResourceList{
		GroupVersion: "route.openshift.io/v1",
	}
	res3 := metav1.APIResourceList{
		GroupVersion: "oauth.openshift.io/v1",
	}

	fd.Resources = []*metav1.APIResourceList{&res1, &res2, &res3}

	return api
}

//
// Registers the DeploymentConfig type and adds in a
// mock syndesis-db deployment config runtime object
//
func fakeClient() client.Client {
	scheme := scheme.Scheme
	osappsv1.AddToScheme(scheme)

	synDbDeployment := &osappsv1.DeploymentConfig{
		ObjectMeta: metav1.ObjectMeta{
			Name: "syndesis-db",
		},
		Spec: osappsv1.DeploymentConfigSpec{
			Template: &corev1.PodTemplateSpec{
				Spec: corev1.PodSpec{
					Containers: []corev1.Container{},
				},
			},
		},
	}

	return rtfake.NewFakeClientWithScheme(scheme, synDbDeployment)
}

func FakeClientTools() *clienttools.ClientTools {
	clientTools := &clienttools.ClientTools{}
	clientTools.SetRuntimeClient(fakeClient())
	clientTools.SetApiClient(openshiftApiClient())
	return clientTools
}
