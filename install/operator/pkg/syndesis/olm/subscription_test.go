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

package olm

import (
	"context"
	"fmt"
	"testing"
	"time"

	semver "github.com/blang/semver/v4"
	osappsv1 "github.com/openshift/api/apps/v1"
	"github.com/operator-framework/api/pkg/lib/version"
	olmapiv1 "github.com/operator-framework/api/pkg/operators/v1"
	olmapiv1alpha1 "github.com/operator-framework/api/pkg/operators/v1alpha1"
	olmfake "github.com/operator-framework/operator-lifecycle-manager/pkg/api/client/clientset/versioned/fake"
	olmpkgsvr "github.com/operator-framework/operator-lifecycle-manager/pkg/package-server/apis/operators/v1"
	"github.com/stretchr/testify/assert"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta2"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	syntesting "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/testing"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime"
	dynfake "k8s.io/client-go/dynamic/fake"
	"k8s.io/client-go/kubernetes/scheme"
	"sigs.k8s.io/controller-runtime/pkg/client"
	rtfake "sigs.k8s.io/controller-runtime/pkg/client/fake"
)

func createScheme() *runtime.Scheme {
	scheme := scheme.Scheme
	osappsv1.AddToScheme(scheme)
	olmapiv1.AddToScheme(scheme)
	return scheme
}

func packageManifest(installModes []olmapiv1alpha1.InstallMode) (*olmpkgsvr.PackageManifest, *olmpkgsvr.PackageChannel) {
	channel := olmpkgsvr.PackageChannel{
		Name:       "1.0.0-stable",
		CurrentCSV: "jaeger-operator.v1.0.0",
		CurrentCSVDesc: olmpkgsvr.CSVDescription{
			DisplayName: "Jaeger Operator",
			Version: version.OperatorVersion{
				semver.Version{
					Major: 1, Minor: 0, Patch: 0,
				},
			},
			InstallModes: installModes,
		},
	}

	pkgManifest := &olmpkgsvr.PackageManifest{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: "some-namespace",
			Name:      "jaeger-product",
		},
		TypeMeta: metav1.TypeMeta{
			APIVersion: "packages.operators.coreos.com/v1",
			Kind:       "PackageManifest",
		},
		Status: olmpkgsvr.PackageManifestStatus{
			CatalogSource:            "operators",
			CatalogSourceDisplayName: "Test Operators",
			CatalogSourceNamespace:   "test-marketplace",
			CatalogSourcePublisher:   "Syndesis Corp",
			DefaultChannel:           "1.0.0-stable",
			PackageName:              "jaeger-product",
			Channels:                 []olmpkgsvr.PackageChannel{channel},
		},
	}

	return pkgManifest, &channel
}

func installMode(installType olmapiv1alpha1.InstallModeType, supported bool) olmapiv1alpha1.InstallMode {
	return olmapiv1alpha1.InstallMode{
		Type:      installType,
		Supported: supported,
	}
}

func operatorGroup(namespace string, name string, targetNamespaces ...string) *olmapiv1.OperatorGroup {
	og := &olmapiv1.OperatorGroup{
		TypeMeta: metav1.TypeMeta{
			Kind:       "OperatorGroup",
			APIVersion: "operators.coreos.com/v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Namespace:       namespace,
			Name:            name,
			ResourceVersion: "1",
			Labels:          map[string]string{namespace: namespace},
		},
		Spec: olmapiv1.OperatorGroupSpec{}, // all namespaces by default
	}

	if len(targetNamespaces) > 0 {
		og.Spec = olmapiv1.OperatorGroupSpec{
			TargetNamespaces: targetNamespaces,
		}
	}

	return og
}

//
// Dynamic Client requires unstructured objects to be loaded in
// so need to convert the operator-groups accordingly.
//
// Bug in OperatorGroup causes a panic when calling ToUnstructered so
// have to convert manually for purposes of test
//
func operatorGroupAsUnstructured(t *testing.T, og *olmapiv1.OperatorGroup) *unstructured.Unstructured {
	uns := &unstructured.Unstructured{
		Object: map[string]interface{}{
			"apiVersion": og.TypeMeta.APIVersion,
			"kind":       og.TypeMeta.Kind,
			"metadata": map[string]interface{}{
				"namespace":       og.Namespace,
				"name":            og.Name,
				"resourceVersion": og.ResourceVersion,
			},
			"spec": map[string]interface{}{},
		},
	}

	if len(og.Spec.TargetNamespaces) > 0 {
		//
		// Convert the target namespaces to interface{}
		//
		toAdd := make([]interface{}, len(og.Spec.TargetNamespaces))
		for i, v := range og.Spec.TargetNamespaces {
			toAdd[i] = v
		}

		//
		// Add the target namespaces to spec as a slice
		//
		err := unstructured.SetNestedSlice(uns.UnstructuredContent(), toAdd, "spec", "targetNamespaces")
		assert.NoError(t, err)
	}
	return uns
}

type Expect struct {
	Error     bool
	Namespace string
}

func Test_FindOperatorGroups(t *testing.T) {
	synns := "syndesis"
	oons := "openshift-operators"

	testCases := []struct {
		name         string
		og           *olmapiv1.OperatorGroup
		installModes []olmapiv1alpha1.InstallMode
		expect       Expect
	}{
		{
			// - No operator group exists -> install mode is All-Namespaces -> new operator group created
			"No-OG---IM-AllNS---NewOG-Created",
			nil,
			[]olmapiv1alpha1.InstallMode{
				installMode(olmapiv1alpha1.InstallModeTypeOwnNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeSingleNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeMultiNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeAllNamespaces, true),
			},
			Expect{false, synns},
		},
		{
			// - No operator group exists -> install mode is All-Namespaces -> existing og returned
			"OG-OONS---IM-AllNS---ExistingOG-Returned",
			operatorGroup(oons, "global-operators"), // All-Namespace Operator Group
			[]olmapiv1alpha1.InstallMode{
				installMode(olmapiv1alpha1.InstallModeTypeOwnNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeSingleNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeMultiNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeAllNamespaces, true),
			},
			Expect{false, oons},
		},
		{
			// - 1 operator group: All-Namespaces -> install mode is All-Namespaces -> existing og returned
			"One-OG---IM-AllNS---ExistingOG-Returned",
			operatorGroup(synns, "existing-og-1"), // All-Namespace Operator Group
			[]olmapiv1alpha1.InstallMode{
				installMode(olmapiv1alpha1.InstallModeTypeOwnNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeSingleNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeMultiNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeAllNamespaces, true), // All-Namespace Install Mode
			},
			Expect{false, synns},
		},
		{
			// - No operator group exists -> install mode is Own-Namespace  -> new operator group created
			"No-OG---IM-OwnNS---NewOG-Created",
			nil,
			[]olmapiv1alpha1.InstallMode{
				installMode(olmapiv1alpha1.InstallModeTypeOwnNamespace, true),
				installMode(olmapiv1alpha1.InstallModeTypeSingleNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeMultiNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeAllNamespaces, false),
			},
			Expect{false, synns},
		},
		{
			// - 1 operator group: Own-Namespace -> install mode is Own-Namespaces -> existing og returned
			"One-OG-OwnNS---IM-OwnNS---ExistingOG-Returned",
			operatorGroup(synns, "existing-og-1", synns), // Own-Namespace Operator Group
			[]olmapiv1alpha1.InstallMode{
				installMode(olmapiv1alpha1.InstallModeTypeOwnNamespace, true),
				installMode(olmapiv1alpha1.InstallModeTypeSingleNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeMultiNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeAllNamespaces, false),
			},
			Expect{false, synns},
		},
		{
			// - 1 operator group: All-Namespaces  -> install mode is Own-Namespace  -> error
			"One-OG-AllNS---IM-OwnNS---Error",
			operatorGroup(synns, "existing-og-1"), // Own-Namespace Operator Group
			[]olmapiv1alpha1.InstallMode{
				installMode(olmapiv1alpha1.InstallModeTypeOwnNamespace, true),
				installMode(olmapiv1alpha1.InstallModeTypeSingleNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeMultiNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeAllNamespaces, false),
			},
			Expect{true, ""},
		},
		{
			// = 1 operator group: Own-Namespace  -> install mode is All-Namespaces  -> error
			"One-OG-OwnNS---IM-AllNS---Error",
			operatorGroup(synns, "existing-og-1", synns), // Own-Namespace Operator Group
			[]olmapiv1alpha1.InstallMode{
				installMode(olmapiv1alpha1.InstallModeTypeOwnNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeSingleNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeMultiNamespace, false),
				installMode(olmapiv1alpha1.InstallModeTypeAllNamespaces, true),
			},
			Expect{true, ""},
		},
	}

	scheme := createScheme()

	clientTools := &clienttools.ClientTools{}
	clientTools.SetApiClient(syntesting.AllApiClient())
	clientTools.SetCoreV1Client(syntesting.CoreV1Client()) // equipped with syndesis namespace
	clientTools.SetOlmClient(syntesting.OlmClient())

	coreClient, err := clientTools.CoreV1Client()
	assert.NoError(t, err)

	olmClient, err := clientTools.OlmClient()
	assert.NoError(t, err)

	opsNS := &corev1.Namespace{
		ObjectMeta: metav1.ObjectMeta{
			Name: oons,
		},
	}

	//
	// Create a openshift-operators namespace
	//
	nsi := coreClient.Namespaces()
	nsi.Create(context.TODO(), opsNS, metav1.CreateOptions{})

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {

			var rtClient client.Client
			if tc.og == nil {
				rtClient = rtfake.NewFakeClientWithScheme(scheme)
			} else {
				rtClient = rtfake.NewFakeClientWithScheme(scheme, tc.og)
				olmClient = olmfake.NewSimpleClientset(tc.og)
			}
			clientTools.SetRuntimeClient(rtClient)
			clientTools.SetOlmClient(olmClient)

			syndesis, err := v1beta2.NewSyndesis(synns)
			conf, err := configuration.GetProperties(context.TODO(), "../../../build/conf/config-test.yaml", clientTools, syndesis)
			assert.NoError(t, err)

			pkgManifest, channel := packageManifest(tc.installModes)
			ns, err := findOrCreateOperatorGroup(context.TODO(), rtClient, olmClient, conf, pkgManifest, channel)
			assert.Equal(t, tc.expect.Error, err != nil)
			// Compare operator group and expected result
			assert.Equal(t, tc.expect.Namespace, ns)
		})
	}
}

func Test_WaitForSubscription(t *testing.T) {
	oons := "openshift-operators"
	testPkg := "jaeger-product"
	testPkgVersion := "v1.17.6"

	installPlan := &olmapiv1alpha1.InstallPlan{
		TypeMeta: metav1.TypeMeta{
			Kind:       "InstallPlan",
			APIVersion: "operators.coreos.com/v1alpha1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Namespace:       oons,
			Name:            "install-w6kdm",
			ResourceVersion: "1",
			Labels:          map[string]string{"namespace": oons},
		},
		Spec: olmapiv1alpha1.InstallPlanSpec{
			Approval:                   olmapiv1alpha1.ApprovalAutomatic,
			Approved:                   true,
			ClusterServiceVersionNames: []string{fmt.Sprintf("%s.%s", testPkg, testPkgVersion)},
			Generation:                 1,
		},
		Status: olmapiv1alpha1.InstallPlanStatus{
			Phase: olmapiv1alpha1.InstallPlanPhaseComplete,
		},
	}

	subscription := &olmapiv1alpha1.Subscription{
		TypeMeta: metav1.TypeMeta{
			Kind:       "Subscription",
			APIVersion: "operators.coreos.com/v1alpha1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Namespace:       oons,
			Name:            testPkg,
			ResourceVersion: "1",
			Labels:          map[string]string{"namespace": oons},
		},
		Spec: &olmapiv1alpha1.SubscriptionSpec{
			Channel:                "stable",
			InstallPlanApproval:    olmapiv1alpha1.ApprovalAutomatic,
			Package:                testPkg,
			CatalogSource:          "test-operators",
			CatalogSourceNamespace: "test-marketplace",
			StartingCSV:            fmt.Sprintf("%s.%s", testPkg, testPkgVersion),
		},
		Status: olmapiv1alpha1.SubscriptionStatus{
			CurrentCSV:   fmt.Sprintf("%s.%s", testPkg, testPkgVersion),
			InstalledCSV: fmt.Sprintf("%s.%s", testPkg, testPkgVersion),
			InstallPlanRef: &corev1.ObjectReference{
				APIVersion: "operators.coreos.com/v1alpha1",
				Kind:       "InstallPlan",
				Name:       installPlan.Name,
				Namespace:  oons,
			},
		},
	}

	//
	// Quicken up for test
	//
	pollTimeout = 10 * time.Second
	pollInterval = 1 * time.Second

	scheme := createScheme()
	ip, err := util.ToUnstructured(installPlan)
	assert.NoError(t, err)
	s, err := util.ToUnstructured(subscription)
	assert.NoError(t, err)

	dynClient := dynfake.NewSimpleDynamicClient(scheme, ip, s)

	assert.NoError(t, err)

	err = waitForSubscription(context.TODO(), dynClient, subscription)
	assert.NoError(t, err)
}
