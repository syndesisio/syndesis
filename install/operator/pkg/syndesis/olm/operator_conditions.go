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

	olmapiv2 "github.com/operator-framework/api/pkg/operators/v2"
	conditions "github.com/operator-framework/operator-lib/conditions"
	errs "github.com/pkg/errors"
	synpkg "github.com/syndesisio/syndesis/install/operator/pkg"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/capabilities"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	appsv1 "k8s.io/api/apps/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	logf "sigs.k8s.io/controller-runtime/pkg/log"
)

var opCondLog = logf.Log.WithName("operator-condition-log")

type ConditionState struct {
	// The value of the condition, either metav1.ConditionTrue or metav1.ConditionFalse
	Status metav1.ConditionStatus
	// The single word reason for the condition setting
	// Must start with a letter
	// Rest of the world can include letters, numbers, commas and colons
	// Cannot end with comma or colon
	Reason string
	// The description of the reason for the condition change.
	Message string
}

func GetConditionName(ctx context.Context, clientTools *clienttools.ClientTools, namespace string) (string, error) {
	opCondLog.V(synpkg.DEBUG_LOGGING_LVL).Info("Finding OLM Operator Condition")

	apiSpec, err := capabilities.ApiCapabilities(clientTools)
	if err != nil {
		return "", err
	}

	if !apiSpec.OlmSupport {
		//
		// This cluster does not support OLM so nothing to do
		//
		opCondLog.V(synpkg.DEBUG_LOGGING_LVL).Info("No OLM support ... aborting Operation Condition Search")
		return "", nil
	}

	rtClient, err := clientTools.RuntimeClient()
	if err != nil {
		return "", errs.Wrap(err, "Failed to initialise runtime client")
	}

	//
	// Find the operator condition associated with this operator
	//
	// deployment -> owned by CSV -> operator condition has the same name
	//
	configName, err := configuration.GetProductName(configuration.TemplateConfig)
	if err != nil {
		return "", errs.Wrap(err, "Failed to determine product name")
	}
	deploymentName := configName + "-operator"

	opCondLog.V(synpkg.DEBUG_LOGGING_LVL).Info("Finding Operator Deployment", "name", deploymentName)
	deployment := &appsv1.Deployment{}
	if err = rtClient.Get(ctx, types.NamespacedName{Namespace: namespace, Name: deploymentName}, deployment); err != nil {
		return "", err // Should find the deployment of this operator
	}

	opCondLog.V(synpkg.DEBUG_LOGGING_LVL).Info("Operator Deployment found", "name", deploymentName)
	ownerRefs := deployment.GetOwnerReferences()
	if len(ownerRefs) > 1 || len(ownerRefs) == 0 {
		// No operator condition as this is not owned by a CSV
		return "", nil
	}

	if ownerRefs[0].Kind != "ClusterServiceVersion" {
		// No operator condition as this is not owned by a CSV
		return "", nil
	}

	opCondLog.V(synpkg.DEBUG_LOGGING_LVL).Info("CSV Owned Deployment", "name", deploymentName, "owner", ownerRefs[0].Name)
	return ownerRefs[0].Name, nil
}

//
// Creates the condition if it does not already exist
//
func SetUpgradeCondition(ctx context.Context, clientTools *clienttools.ClientTools, namespace string, state ConditionState) error {

	conditionName, err := GetConditionName(ctx, clientTools, namespace)
	if err != nil {
		return err
	} else if conditionName == "" {
		return nil
	}

	rtClient, err := clientTools.RuntimeClient()
	if err != nil {
		return errs.Wrap(err, "Failed to initialise runtime client")
	}

	clusterFactory := conditions.InClusterFactory{rtClient}
	err = os.Setenv("OPERATOR_CONDITION_NAME", conditionName)
	if err != nil {
		return err
	}
	uc, err := clusterFactory.NewCondition(olmapiv2.ConditionType(olmapiv2.Upgradeable))
	if err != nil {
		return err
	}

	err = uc.Set(ctx,
		state.Status,
		conditions.WithReason(state.Reason),
		conditions.WithMessage(state.Message))
	if err != nil {
		return err
	}

	return nil
}
