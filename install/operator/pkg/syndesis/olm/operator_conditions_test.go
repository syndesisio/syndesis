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
	"os"
	"testing"

	olmapiv1alpha1 "github.com/operator-framework/api/pkg/operators/v1alpha1"
	olmapiv2 "github.com/operator-framework/api/pkg/operators/v2"
	"github.com/stretchr/testify/assert"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	syntesting "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/testing"
	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	rtfake "sigs.k8s.io/controller-runtime/pkg/client/fake"
)

func deployment(name string, owner client.Object, namespace string) *appsv1.Deployment {
	return &appsv1.Deployment{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: namespace,
			Name:      name,
			OwnerReferences: []metav1.OwnerReference{
				{
					Name: owner.GetName(),
					Kind: owner.GetObjectKind().GroupVersionKind().Kind,
				},
			},
		},
	}
}

func csversion(name string, namespace string) *olmapiv1alpha1.ClusterServiceVersion {
	return &olmapiv1alpha1.ClusterServiceVersion{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: namespace,
			Name:      name,
		},
		TypeMeta: metav1.TypeMeta{
			APIVersion: "operators.coreos.com/v1alpha1",
			Kind:       "ClusterServiceVersion",
		},
	}
}

func opCondition(name string, namespace string) *olmapiv2.OperatorCondition {
	return &olmapiv2.OperatorCondition{
		ObjectMeta: metav1.ObjectMeta{
			Name:      name,
			Namespace: namespace,
		},
		Spec: olmapiv2.OperatorConditionSpec{
			Conditions: []metav1.Condition{},
		},
	}
}

func TestConditions_GetOperationConditionName(t *testing.T) {
	configuration.TemplateConfig = "../../../build/conf/config-test.yaml"
	testNS := "test-namespace"

	confName, err := configuration.GetProductName(configuration.TemplateConfig)
	assert.NoError(t, err)
	confVersion, err := configuration.GetVersion(configuration.TemplateConfig)
	assert.NoError(t, err)

	deploymentName := confName + "-operator"
	csvName := confName + ".v" + confVersion
	csv := csversion(csvName, testNS)

	rtClient := rtfake.NewFakeClientWithScheme(syntesting.CreateScheme(),
		csv,
		deployment(deploymentName, csv, testNS),
	)

	clientTools := &clienttools.ClientTools{}
	clientTools.SetRuntimeClient(rtClient)
	clientTools.SetApiClient(syntesting.AllApiClient())
	clientTools.SetCoreV1Client(syntesting.CoreV1Client()) // equipped with syndesis namespace
	clientTools.SetOlmClient(syntesting.OlmClient())

	coreClient, err := clientTools.CoreV1Client()
	assert.NoError(t, err)

	opsNS := &corev1.Namespace{
		ObjectMeta: metav1.ObjectMeta{
			Name: testNS,
		},
	}

	nsi := coreClient.Namespaces()
	nsi.Create(context.TODO(), opsNS, metav1.CreateOptions{})

	name, err := GetConditionName(context.TODO(), clientTools, testNS, confName)
	assert.NoError(t, err)

	assert.Equal(t, csvName, name)
}

func TestConditions_SetOperationCondition(t *testing.T) {
	t.Skip("skipping test due to bug https://github.com/operator-framework/operator-lib/issues/103")

	configuration.TemplateConfig = "../../../build/conf/config-test.yaml"
	testNS := "test-namespace"

	confName, err := configuration.GetProductName(configuration.TemplateConfig)
	assert.NoError(t, err)
	confVersion, err := configuration.GetVersion(configuration.TemplateConfig)
	assert.NoError(t, err)

	deploymentName := confName + "-operator"
	csvName := confName + ".v" + confVersion
	csv := csversion(csvName, testNS)
	opCon := opCondition(csvName, testNS)

	// Set the env var to match the operator condition name
	err = os.Setenv("OPERATOR_CONDITION_NAME", csvName)
	assert.NoError(t, err)

	rtClient := rtfake.NewFakeClientWithScheme(syntesting.CreateScheme(),
		csv, opCon,
		deployment(deploymentName, csv, testNS),
	)

	clientTools := &clienttools.ClientTools{}
	clientTools.SetRuntimeClient(rtClient)
	clientTools.SetApiClient(syntesting.AllApiClient())
	clientTools.SetCoreV1Client(syntesting.CoreV1Client()) // equipped with syndesis namespace
	clientTools.SetOlmClient(syntesting.OlmClient())

	coreClient, err := clientTools.CoreV1Client()
	assert.NoError(t, err)

	opsNS := &corev1.Namespace{
		ObjectMeta: metav1.ObjectMeta{
			Name: testNS,
		},
	}

	nsi := coreClient.Namespaces()
	nsi.Create(context.TODO(), opsNS, metav1.CreateOptions{})

	status := ConditionState{
		Status:  metav1.ConditionFalse,
		Message: "test-turn-off",
		Reason:  "testing the turn off",
	}

	err = SetUpgradeCondition(context.TODO(), clientTools, testNS, confName, status)
	assert.NoError(t, err)
}
