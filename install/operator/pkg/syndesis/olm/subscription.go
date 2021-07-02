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
	"errors"
	"fmt"
	"time"

	olmapiv1 "github.com/operator-framework/api/pkg/operators/v1"
	olmapiv1alpha1 "github.com/operator-framework/api/pkg/operators/v1alpha1"
	olmcli "github.com/operator-framework/operator-lifecycle-manager/pkg/api/client/clientset/versioned"
	olmpkgsvr "github.com/operator-framework/operator-lifecycle-manager/pkg/package-server/apis/operators/v1"
	synpkg "github.com/syndesisio/syndesis/install/operator/pkg"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	conf "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	k8serr "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime/schema"
	"k8s.io/apimachinery/pkg/util/wait"
	"k8s.io/client-go/dynamic"
	"sigs.k8s.io/controller-runtime/pkg/client"
	logf "sigs.k8s.io/controller-runtime/pkg/log"
)

var pollTimeout = 180 * time.Second
var pollInterval = 15 * time.Second
var sublog = logf.Log.WithName("subscription")

//
// Finds any existing subscriptions for the operator package. If there are none
// then it attempts to create both a subscription and operatorgroup in the
// namespace so that the operator can be initialised.
//
// Returns only those artifacts that are owned by this operator, ie. those
// that have been given ownership by syndesis, allowing these to be tracked
// by the operator and tidied up. If they were not created by this operator
// then they remain independent and will not be tidied up if the CR is removed.
//
func SubscribeOperator(ctx context.Context, clientTools *clienttools.ClientTools, configuration *conf.Config, olmSpec *conf.OlmSpec) error {
	rtClient, err := clientTools.RuntimeClient()
	if err != nil {
		return err
	}

	//
	// Is there Operator-Lifecyle-Manager support?
	//
	if !configuration.ApiServer.OlmSupport {
		return errors.New("Cluster does not support operation-lifecycle-manager")
	}

	sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("Subscribing to operator", "Package", olmSpec.Package)

	//
	// 1. Look for the packageName in the packageManifest
	//
	pkgManifest, err := findPackageManifest(ctx, rtClient, olmSpec)
	if err != nil {
		return err
	}

	//
	// 2. Check the package has the correct channel
	//
	channel, err := findChannel(ctx, pkgManifest, olmSpec.Channel)

	//
	// 3. Find the CSV supported by the package
	//
	csv, err := findPackageCSV(ctx, rtClient, channel, configuration.OpenShiftProject)
	if err != nil {
		return err
	}

	//
	// 4a. If csv listed with our namespace then an operatorgroup & subscription already installed so RETURN
	//
	if csv != nil {
		//
		// A subscription & operatorgroup exist that will detect our namespace so nothing more to do
		//
		return nil
	}

	olmClient, err := clientTools.OlmClient()
	if err != nil {
		return err
	}

	dynClient, err := clientTools.DynamicClient()
	if err != nil {
		return err
	}

	//
	// 4b. No csv listed so try and install an operator-group or use an existing one if available
	//
	ns, err := findOrCreateOperatorGroup(ctx, rtClient, olmClient, configuration, pkgManifest, channel)
	if err != nil {
		return err
	}

	//
	// 4c. Create the subscription
	//
	sub, err := createSubscription(ctx, rtClient, ns, pkgManifest, channel)
	if err != nil {
		return err
	}

	err = waitForSubscription(ctx, dynClient, sub)
	if err != nil {
		return err
	}

	return nil
}

func findPackageManifest(ctx context.Context, rtClient client.Client, olmSpec *conf.OlmSpec) (*olmpkgsvr.PackageManifest, error) {
	sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("Finding package manifest for package", "Package", olmSpec.Package)

	//
	// Find the list of package manifests
	//
	pkgs := olmpkgsvr.PackageManifestList{}
	if err := rtClient.List(ctx, &pkgs, &client.ListOptions{Namespace: ""}); err != nil {
		return nil, err
	}

	if len(pkgs.Items) == 0 {
		return nil, fmt.Errorf("No package manifests available for Package %s", olmSpec.Package)
	}

	//
	// Find the packagemanifest for the package
	//
	for _, pkg := range pkgs.Items {
		if pkg.Name == olmSpec.Package {
			sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("Identified package manifest for package", "Package", olmSpec.Package)
			return &pkg, nil
		}
	}

	return nil, fmt.Errorf("No package manifest available for package %s", olmSpec.Package)
}

func findChannel(ctx context.Context, pkgManifest *olmpkgsvr.PackageManifest, chnlName string) (*olmpkgsvr.PackageChannel, error) {
	for _, channel := range pkgManifest.Status.Channels {
		if channel.Name == chnlName {
			return &channel, nil
		}
	}

	return nil, fmt.Errorf("The package manifest for %s has no channel %s", pkgManifest.Name, chnlName)
}

func findPackageCSV(ctx context.Context, rtClient client.Client, channel *olmpkgsvr.PackageChannel, namespace string) (*olmapiv1alpha1.ClusterServiceVersion, error) {
	sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("Finding csv for package in namespace", "Channel", channel.Name, "Namespace", namespace)

	csv := olmapiv1alpha1.ClusterServiceVersion{}
	if err := rtClient.Get(ctx, client.ObjectKey{Namespace: namespace, Name: channel.CurrentCSV}, &csv); err != nil {
		if k8serr.IsNotFound(err) {
			return nil, nil // No csvs in namespace
		}

		// A real error occurred
		return nil, err
	}

	sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("Identified csv for package in namespace", "Channel", channel.Name, "Namespace", namespace)
	return &csv, nil
}

func createOperatorGroup(ctx context.Context, rtClient client.Client, configuration *conf.Config, pkgName string, channel *olmpkgsvr.PackageChannel) (*olmapiv1.OperatorGroup, error) {
	sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("Creating operator group for package in namespace", "Channel", channel.Name, "Namespace", configuration.OpenShiftProject)

	ogName := fmt.Sprintf("%s-%s-og", configuration.OpenShiftProject, pkgName)
	csvDesc := channel.CurrentCSVDesc

	//
	// Create an operator group allowing the OLM to see the namespace
	//
	og := &olmapiv1.OperatorGroup{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: configuration.OpenShiftProject,
			Name:      ogName,
			Labels:    map[string]string{configuration.ProductName: configuration.OpenShiftProject},
		},
		Spec: olmapiv1.OperatorGroupSpec{}, // all namespaces by default
	}

	// Determine install mode and add target ns to group if install mode does not allow all namespaces
	if !hasInstallMode(csvDesc.InstallModes, olmapiv1alpha1.InstallModeTypeAllNamespaces) {
		og.Spec.TargetNamespaces = []string{configuration.OpenShiftProject}
	}

	err := rtClient.Create(ctx, og)
	if err != nil && !k8serr.IsAlreadyExists(err) {
		return nil, err
	}

	return og, nil
}

//
// Find or create a compatible operator-group and
// return the namespace in which is it located
//
func findOrCreateOperatorGroup(ctx context.Context, rtClient client.Client, olmClient olmcli.Interface, configuration *conf.Config, pkgManifest *olmpkgsvr.PackageManifest, channel *olmpkgsvr.PackageChannel) (string, error) {

	//
	// 1. Check the install mode of the packagemanifest to see if its ALL
	//
	// 2a. ALL
	//     Look for an og with no target-namespaces, eg. Openshift-Operators, & return its namespace
	//     + Request installation of the subscription in that namespace
	//     - No namespace then check if there are other ogs installed in this namespace
	//       + Other ogs installed (cannot be compatible otherwise would have returned above) so fail with error - cannot install due to incompatible operator-groups: user should install elsewhere
	//     (remember is csv installed then no subscription needed)
	//       - No other og so create an og for ALL
	//
	// 2b. OWN
	//     Check if a compatible og already installed in this namespace
	//     + og already available so return it
	//     - no og so create one
	//     - incompatible og so fail with error - operator-group conflict (need to move ALL operator somewhere else)
	//
	//
	// Use-Cases
	// 1. Pkg = ALL; Namespace exists w/ ALL og;                                Return og / namespace
	// 2. Pkg = ALL; No namespace w/ ALL og;     No og in our namespace;        create og
	// 3. Pkg = ALL; No namespace w/ ALL og;     og installed in our namespace; fail with error
	// 4. Pkg = OWN;                             No og in our namespace;        create og
	// 5. Pkg = OWN;                             og installed in our namespace; incompatible og; fail with error
	// 6. Pkg = OWN;                             og installed in our namespace; compatible og; Return og / namespace
	//

	csvDesc := channel.CurrentCSVDesc

	//
	// Use-cases: 1, 2, 3
	//
	if hasInstallMode(csvDesc.InstallModes, olmapiv1alpha1.InstallModeTypeAllNamespaces) {
		sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("All-Namespace install mode found for package", "Package", pkgManifest.Name)

		//
		// Locate all operator groups in the cluster
		//
		ogs, err := olmClient.OperatorsV1().OperatorGroups("").List(ctx, metav1.ListOptions{})
		if err != nil {
			sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("Error: Cannot get any global namespace operator-groups", "error", err.Error())
		}

		//
		// Found some operator-groups in the cluster
		//
		for _, og := range ogs.Items {
			if isAllNamespace(og) {
				sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("Located All-Namespace Operator-Group", "name", og.Name)
				//
				// Use-case: 1
				// Found a global operator-group so return its namespace for
				// installing the subscription
				//
				return og.Namespace, nil
			}
		}

		//
		// Failed to find an operator-group for some reason.
		// Attempt to create an operator-group in our namespace
		//
	}

	//
	// Use-cases: 2, 3, 4, 5, 6
	//

	//
	// Find if there are any operator-groups already installed in this namespace.
	//
	ogs := olmapiv1.OperatorGroupList{}
	if err := rtClient.List(ctx, &ogs, &client.ListOptions{Namespace: configuration.OpenShiftProject}); err != nil {
		sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("Cannot get any own-namespace operator-groups", "error", err.Error())
		return "", err
	}

	if len(ogs.Items) == 0 {
		//
		// Use-case: 2, 4
		// No operator groups installed so can create one
		//
		if og, err := createOperatorGroup(ctx, rtClient, configuration, pkgManifest.Status.PackageName, channel); err != nil {
			return "", err
		} else {
			//
			// This namespace now has the operator-group
			//
			return og.Namespace, nil
		}
	}

	//
	// Already have some operator-groups in this namespace.
	// This could be a problem due to conflicts so need to
	// check their compatibility.
	//
	for _, og := range ogs.Items {
		sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("Found existing operator-group in namespace. Testing compatibility with operator install mode",
			"Namespace", configuration.OpenShiftProject, "Operator-Group", og.Name)

		//
		// Test for compatibility of install mode & operator-group
		//
		if hasInstallMode(csvDesc.InstallModes, olmapiv1alpha1.InstallModeTypeAllNamespaces) && !isAllNamespace(og) {
			//
			// Use-case: 3
			//
			return "", fmt.Errorf("Existing operator-group %s is incompatible with installing subscription for operator %s",
				og.Name, pkgManifest.Status.PackageName)
		} else if !hasInstallMode(csvDesc.InstallModes, olmapiv1alpha1.InstallModeTypeAllNamespaces) && isAllNamespace(og) {
			//
			// Use-case: 5
			//
			return "", fmt.Errorf("Existing operator-group %s is incompatible with installing subscription for operator %s",
				og.Name, pkgManifest.Status.PackageName)
		}
	}

	//
	// Use-case: 6
	//
	// Have existing operator-groups and all are compatible with install mode.
	// Therefore, no need to create another one and so return this namespace
	//
	return configuration.OpenShiftProject, nil
}

func createSubscription(ctx context.Context, rtClient client.Client, namespace string, pkgManifest *olmpkgsvr.PackageManifest, channel *olmpkgsvr.PackageChannel) (*olmapiv1alpha1.Subscription, error) {
	sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("Creating subscription for package in namespace", "Channel", channel.Name, "Package", pkgManifest.Name, "Namespace", namespace)

	//
	// Create a subscription for the install
	//
	sub := &olmapiv1alpha1.Subscription{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: namespace,
			Name:      pkgManifest.Status.PackageName,
		},
		Spec: &olmapiv1alpha1.SubscriptionSpec{
			InstallPlanApproval:    olmapiv1alpha1.ApprovalAutomatic,
			Package:                pkgManifest.Status.PackageName,
			CatalogSourceNamespace: pkgManifest.Status.CatalogSourceNamespace,
			CatalogSource:          pkgManifest.Status.CatalogSource,
			Channel:                channel.Name,
			StartingCSV:            channel.CurrentCSV, // Add CSV to subscription
		},
	}

	err := rtClient.Create(ctx, sub)
	if err != nil && !k8serr.IsAlreadyExists(err) {
		return nil, err
	}

	sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("Created subscription for package in namespace", "Name", sub.Name, "Namespace", sub.Namespace)
	return sub, nil
}

func isAllNamespace(og olmapiv1.OperatorGroup) bool {
	return len(og.Spec.TargetNamespaces) == 0 && (og.Spec.Selector == nil || len(og.Spec.Selector.MatchLabels) == 0)
}

func hasInstallMode(installModes []olmapiv1alpha1.InstallMode, tgtModeTypes ...olmapiv1alpha1.InstallModeType) bool {
	if len(installModes) == 0 {
		return false
	}

	for _, installMode := range installModes {
		for _, tgtModeType := range tgtModeTypes {
			if installMode.Type == tgtModeType {
				return installMode.Supported
			}
		}
	}

	return false
}

func waitForSubscription(ctx context.Context, dynClient dynamic.Interface, sub *olmapiv1alpha1.Subscription) error {
	subGvr := schema.GroupVersionResource{
		Group:    "operators.coreos.com",
		Version:  "v1alpha1",
		Resource: "subscriptions",
	}
	insGvr := schema.GroupVersionResource{
		Group:    "operators.coreos.com",
		Version:  "v1alpha1",
		Resource: "installplans",
	}

	sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("Waiting on subscription install plan to complete for package in namespace", "Name", sub.Name, "Namespace", sub.Namespace)
	//
	// Wait for the subscription to install the operator
	//
	err := wait.Poll(pollInterval, pollTimeout, func() (done bool, err error) {

		//
		// Fetch latest information for subscription using dynamic client
		// (the runtime client seems incapable of finding resources in other namespaces)
		//
		unsSub, err := dynClient.Resource(subGvr).Namespace(sub.Namespace).Get(ctx, sub.Name, metav1.GetOptions{})
		if err != nil {
			sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("Error: Cannot locate subscription", "Subscription", sub.Name, "Namespace", sub.Namespace, "Error", err)
			if k8serr.IsNotFound(err) {
				// Might not have been created yet so need to wait longer
				return false, nil
			}
			return false, err
		}

		iPlanRefUns, exists, err := unstructured.NestedFieldNoCopy(unsSub.UnstructuredContent(), "status", "installPlanRef")
		if !exists {
			//
			// No install plan reference so something has gone wrong
			//
			return false, fmt.Errorf("Subscription %s in namespace %s does not have an install plan", sub.Name, sub.Namespace)
		} else if err != nil {
			//
			// Another error occurred so return to that effect
			//
			return false, err
		}

		//
		// Convert the successful reference
		//
		iPlanRef, ok := iPlanRefUns.(map[string]interface{})
		if !ok {
			return false, fmt.Errorf("Failed to convert install plan reference")
		}

		installPlanUns, err := dynClient.Resource(insGvr).Namespace(sub.Namespace).Get(ctx, iPlanRef["name"].(string), metav1.GetOptions{})
		if err != nil {
			sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("Error: Cannot find install plan", "InstallPlan", iPlanRef["name"].(string), "Namespace", sub.Namespace)
			return false, err
		}

		phase, exists, err := unstructured.NestedString(installPlanUns.UnstructuredContent(), "status", "phase")
		if !exists {
			//
			// No install plan phase. Should not happen.
			//
			return false, fmt.Errorf("Subscription %s in namespace %s contains an invalid install plan phase", sub.Name, sub.Namespace)
		} else if err != nil {
			return false, err
		}

		if phase == string(olmapiv1alpha1.InstallPlanPhaseRequiresApproval) {
			return false, fmt.Errorf("Subscription %s in %s requires install approval to complete installation", sub.Name, sub.Namespace)
		} else if phase == string(olmapiv1alpha1.InstallPlanPhaseFailed) {
			return false, fmt.Errorf("Subscription %s in %s failed to install the operator", sub.Name, sub.Namespace)
		} else if phase == string(olmapiv1alpha1.InstallPlanPhaseComplete) {
			sublog.V(synpkg.DEBUG_LOGGING_LVL).Info("Install plan for subscription complete", "Subscription Name", sub.Name, "Subscription Namespace", sub.Namespace)
			return true, nil
		}

		//
		// Install plan is still to complete so wait
		//
		return false, nil
	})

	if err != nil {
		return err
	}

	return nil
}
