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

package util

import (
	"context"
	"strings"

	configv1 "github.com/openshift/api/config/v1"
	operatorv1 "github.com/openshift/api/operator/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime/schema"
	"k8s.io/client-go/dynamic"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

// Only on Openshift 4 - read the consoles/cluster object to retrieve the URL of Web Admin Console
func GetOpenshift4ManagementConsoleUrl(ctx context.Context, rtClient client.Client) (string, error) {
	if !isOpenshift4ConsoleEnabled(ctx, rtClient) {
		log.V(pkg.DEBUG_LOGGING_LVL).Info("Openshift 4 management console is DISABLED.")
		return "", nil
	}
	console := &configv1.Console{}
	err := rtClient.Get(ctx, client.ObjectKey{Name: "cluster"}, console)
	if err != nil {
		return "", err
	}
	return console.Status.ConsoleURL, nil
}

// Only on Openshift 3 - read the configmap/webconsole-config from openshift-web-console namespace to retrieve the URL of Web Admin Console
func GetOpenshift3ManagementConsoleUrl(ctx context.Context, dynClient dynamic.Interface) (string, error) {
	subGvr := schema.GroupVersionResource{
		Version:  "v1",
		Resource: "configmaps",
	}

	// the regular client fails to read objects from diferent namespace, so we use the dynamic client
	unstCm, err := dynClient.Resource(subGvr).Namespace("openshift-web-console").Get(ctx, "webconsole-config", metav1.GetOptions{})
	if err != nil {
		return "", err
	}

	// read the "data" content of the configmap
	dataRaw, exists, err := unstructured.NestedFieldNoCopy(unstCm.UnstructuredContent(), "data")
	if err != nil {
		return "", err
	} else if !exists {
		// don't need to pollute log with a console configmap which doesn't exist
		return "", nil
	}
	cmMap := dataRaw.(map[string]interface{})
	data := cmMap["webconsole-config.yaml"].(string)
	url := extractWebConsoleUrl(data)
	return url, nil
}

// Only on Openshift 4 - check if the management console is enabled
func isOpenshift4ConsoleEnabled(ctx context.Context, rtClient client.Client) bool {
	console := operatorv1.Console{}
	err := rtClient.Get(ctx, client.ObjectKey{Name: "cluster"}, &console)
	if err != nil {
		log.V(pkg.DEBUG_LOGGING_LVL).Info("Error to determine if openshift 4 management console is enabled.", "err", err)
		return false
	}
	return console.Spec.ManagementState != operatorv1.Removed
}

// data is the content of configmap/webconsole-config data property, this is only for openshift 3
// parses and extracts the value of adminConsolePublicURL property contained in the data string
func extractWebConsoleUrl(data string) string {
	url := ""
	if strings.Contains(data, "adminConsolePublicURL:") {
		st1 := strings.Split(data, "adminConsolePublicURL:")[1]
		st1 = strings.Split(st1, "\n")[0]
		url = strings.TrimSpace(st1)
	}
	return url
}