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

package main

import (
	"context"
	"fmt"
	"os"

	gerrors "github.com/pkg/errors"
	synapis "github.com/syndesisio/syndesis/install/operator/pkg/apis"
	synapi "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta3"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/olm"
	kerrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

func main() {
	clientTools := &clienttools.ClientTools{}

	//
	// Ensure runtime client knows about syndesis apis
	//
	scheme := clientTools.GetScheme()
	if err := synapis.AddToScheme(scheme); err != nil {
		fmt.Println(err, "Error occurred")
		os.Exit(1)
	}

	ctx := context.TODO()

	nm, found := os.LookupEnv("POD_NAMESPACE")
	if !found {
		fmt.Println("Error: No POD_NAMESPACE has been set")
		os.Exit(1)
	} else {
		fmt.Println("Info: Using POD_NAMESPACE: ", nm)
	}

	if err := setUpgradeCondition(ctx, clientTools, nm); err != nil {
		fmt.Println(err, "Error occurred")
		os.Exit(1)
	}
}

func setUpgradeCondition(ctx context.Context, clientTools *clienttools.ClientTools, nm string) error {
	found, err := hasSyndesis(ctx, clientTools, nm)
	if err != nil {
		return gerrors.Wrap(err, "Failed to get Syndesis resource")
	} else if !found {
		fmt.Println("Info: No Syndesis Custom Resource. Upgrade disablement not required")
		return nil
	}

	//
	// Disable the upgrade until enabled
	//
	state := olm.ConditionState{
		Status:  metav1.ConditionFalse,
		Reason:  "NotReady",
		Message: "Disable any operator upgrade until reconciliation allows it",
	}
	if upgErr := olm.SetUpgradeCondition(ctx, clientTools, nm, state); upgErr != nil {
		return gerrors.Wrap(upgErr, "Failed to set the upgrade condition on the operator")
	}

	return nil
}

func hasSyndesis(ctx context.Context, clientTools *clienttools.ClientTools, nm string) (bool, error) {
	synList := synapi.SyndesisList{}
	rtclient, err := clientTools.RuntimeClient()
	if err != nil {
		return false, err
	}

	err = rtclient.List(ctx, &synList, &client.ListOptions{Namespace: nm})
	if err != nil {
		if kerrors.IsNotFound(err) {
			return false, nil
		}
		return false, err
	}

	return len(synList.Items) > 0, nil
}
