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

package clienttools

import (
	osappsv1 "github.com/openshift/api/apps/v1"
	projectv1 "github.com/openshift/api/project/v1"
	olmapiv1 "github.com/operator-framework/api/pkg/operators/v1"
	olmapiv1alpha1 "github.com/operator-framework/api/pkg/operators/v1alpha1"
	olmapiv1alpha2 "github.com/operator-framework/api/pkg/operators/v1alpha2"
	olmcli "github.com/operator-framework/operator-lifecycle-manager/pkg/api/client/clientset/versioned"
	olmpkgsvr "github.com/operator-framework/operator-lifecycle-manager/pkg/package-server/apis/operators/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/client-go/dynamic"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/kubernetes/scheme"
	corev1 "k8s.io/client-go/kubernetes/typed/core/v1"
	"k8s.io/client-go/rest"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/client/config"
)

type ClientTools struct {
	restConfig    *rest.Config
	runtimeClient *client.Client
	scheme        *runtime.Scheme
	dynamicClient dynamic.Interface
	apiClient     kubernetes.Interface
	coreV1Client  corev1.CoreV1Interface
	olmClient     olmcli.Interface
}

func (ck *ClientTools) RestConfig() (c *rest.Config) {
	if ck.restConfig == nil {
		config, err := config.GetConfig()
		util.ExitOnError(err)
		ck.restConfig = config
	}

	return ck.restConfig
}

func (ck *ClientTools) GetScheme() *runtime.Scheme {
	if ck.scheme == nil {
		//
		// Add schemes that the client should be capable of retrieving
		// scheme.Scheme provides most of the fundamental types
		// whilst runtime.Scheme is the empty equivalent.
		//
		ck.scheme = scheme.Scheme

		// Openshift types such as DeploymentConfig
		osappsv1.AddToScheme(ck.scheme)

		//
		// AddToScheme is deprecated in the OS api but schemeBuilder is still private
		// whereas operator-marketplace has SchemeBuilder as public.
		//
		olmapiv1alpha2.SchemeBuilder.AddToScheme(ck.scheme)
		olmapiv1alpha1.SchemeBuilder.AddToScheme(ck.scheme)
		olmapiv1.SchemeBuilder.AddToScheme(ck.scheme)
		olmpkgsvr.SchemeBuilder.AddToScheme(ck.scheme)
		projectv1.AddToScheme(ck.scheme)
	}

	return ck.scheme
}

func (ck *ClientTools) RuntimeClient() (c client.Client, err error) {
	if ck.runtimeClient == nil {

		s := ck.GetScheme()

		// Register
		options := client.Options{
			Scheme: s,
		}

		cl, err := client.New(ck.RestConfig(), options)
		if err != nil {
			return nil, err
		}
		ck.runtimeClient = &cl
	}

	return *ck.runtimeClient, nil
}

func (ck *ClientTools) SetRuntimeClient(c client.Client) {
	ck.runtimeClient = &c
}

func (ck *ClientTools) DynamicClient() (c dynamic.Interface, err error) {
	if ck.dynamicClient == nil {
		dyncl, err := dynamic.NewForConfig(ck.RestConfig())
		if err != nil {
			return nil, err
		}
		ck.dynamicClient = dyncl
	}
	return ck.dynamicClient, nil
}

func (ck *ClientTools) SetDynamicClient(d dynamic.Interface) {
	ck.dynamicClient = d
}

func (ck *ClientTools) ApiClient() (kubernetes.Interface, error) {
	if ck.apiClient == nil {
		apicl, err := kubernetes.NewForConfig(ck.RestConfig())
		if err != nil {
			return nil, err
		}
		ck.apiClient = apicl
	}
	return ck.apiClient, nil
}

func (ck *ClientTools) SetApiClient(a kubernetes.Interface) {
	ck.apiClient = a
}

func (ck *ClientTools) CoreV1Client() (corev1.CoreV1Interface, error) {
	if ck.coreV1Client == nil {
		client, err := corev1.NewForConfig(ck.RestConfig())
		if err != nil {
			return nil, err
		}
		ck.coreV1Client = client
	}

	return ck.coreV1Client, nil
}

func (ck *ClientTools) SetCoreV1Client(c corev1.CoreV1Interface) {
	ck.coreV1Client = c
}

func (ck *ClientTools) OlmClient() (olmcli.Interface, error) {
	if ck.olmClient == nil {
		client, err := olmcli.NewForConfig(ck.RestConfig())
		if err != nil {
			return nil, err
		}
		ck.olmClient = client
	}

	return ck.olmClient, nil
}

func (ck *ClientTools) SetOlmClient(c olmcli.Interface) {
	ck.olmClient = c
}
