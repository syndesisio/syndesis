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
	"context"
	"fmt"
	"reflect"
	"strings"
	"time"

	osappsv1 "github.com/openshift/api/apps/v1"
	olmcli "github.com/operator-framework/operator-lifecycle-manager/pkg/api/client/clientset/versioned"
	olmfake "github.com/operator-framework/operator-lifecycle-manager/pkg/api/client/clientset/versioned/fake"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/capabilities"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	discoveryfake "k8s.io/client-go/discovery/fake"
	clientset "k8s.io/client-go/kubernetes"
	corefake "k8s.io/client-go/kubernetes/fake"
	gofake "k8s.io/client-go/kubernetes/fake"
	"k8s.io/client-go/kubernetes/scheme"
	corev1client "k8s.io/client-go/kubernetes/typed/core/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	rtfake "sigs.k8s.io/controller-runtime/pkg/client/fake"
)

//
// A fake API client that supports all required api
//
func AllApiClient() clientset.Interface {
	api := gofake.NewSimpleClientset()
	fd := api.Discovery().(*discoveryfake.FakeDiscovery)

	reqApi := capabilities.RequiredApi
	v := reflect.ValueOf(reqApi)

	fd.Resources = []*metav1.APIResourceList{}
	for i := 0; i < v.NumField(); i++ {
		pkg := fmt.Sprintf("%v", v.Field(i))
		nameGroup := strings.SplitN(pkg, ".", 2)

		resList := &metav1.APIResourceList{
			GroupVersion: nameGroup[1],
			APIResources: []metav1.APIResource{
				{
					Name: nameGroup[0],
				},
			},
		}

		fd.Resources = append(fd.Resources, resList)
	}

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

func CoreV1Client(initObjs ...runtime.Object) corev1client.CoreV1Interface {
	clientset := corefake.NewSimpleClientset(initObjs...)

	coreV1Client := clientset.CoreV1()

	//
	// Create a syndesis namespace
	//
	nsi := coreV1Client.Namespaces()
	nsi.Create(context.TODO(), &corev1.Namespace{
		ObjectMeta: metav1.ObjectMeta{
			Name:              "syndesis",
			CreationTimestamp: metav1.Date(2009, time.November, 10, 23, 0, 0, 0, time.UTC),
		},
	}, metav1.CreateOptions{})

	return coreV1Client
}

func OlmClient(initObjs ...runtime.Object) olmcli.Interface {
	clientset := olmfake.NewSimpleClientset(initObjs...)
	return clientset
}

func FakeClientTools() *clienttools.ClientTools {
	clientTools := &clienttools.ClientTools{}
	clientTools.SetRuntimeClient(fakeClient())
	clientTools.SetApiClient(AllApiClient())
	clientTools.SetCoreV1Client(CoreV1Client())
	clientTools.SetOlmClient(OlmClient())
	return clientTools
}
