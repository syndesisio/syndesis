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

package e2e

import (
    goctx "context"
    framework "github.com/operator-framework/operator-sdk/pkg/test"
    "github.com/operator-framework/operator-sdk/pkg/test/e2eutil"
    "github.com/syndesisio/syndesis/install/operator/pkg/apis"
    "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
    "k8s.io/apimachinery/pkg/apis/meta/v1"
    "testing"
    "time"
)

var (
    retryInterval          = time.Second * 5
    operatorTimeout        = time.Second * 60
    startingPhaseTimeout   = time.Second * 300
    installingPhaseTimeout = time.Second * 500
    cleanupRetryInterval   = time.Second * 1
    cleanupTimeout         = time.Second * 5
)

func TestSyndesis(t *testing.T) {
    syndesis := &v1alpha1.SyndesisList{
        TypeMeta: v1.TypeMeta{
            Kind:       "Syndesis",
            APIVersion: "syndeses.syndesis.io",
        },
    }

    if err := framework.AddToFrameworkScheme(apis.AddToScheme, syndesis); err != nil {
        t.Fatalf("Failed to add custom resource scheme to framework: %v", err)
    }

    // run subtests
    t.Run("e2e", func(t *testing.T) {
        t.Run("install-default", installWithDefaultsTest)
        t.Run("install-full", installTest)
    })
}

// Test that syndesis is installed correctly by reaching the Installed phase. Use an empty CR, test
// generated values
func installWithDefaultsTest(t *testing.T)  {
    t.Parallel()
    ctx := framework.NewTestCtx(t)
    defer ctx.Cleanup()

    namespace, err := ctx.GetNamespace()
    cr := "syndesis-test"

    co := framework.CleanupOptions{
        TestContext:   ctx,
        Timeout:       cleanupTimeout,
        RetryInterval: cleanupRetryInterval,
    }

    // Initialize cluster resources from yaml, including operator deployment
    // and needed roles / rbac
    if err := ctx.InitializeClusterResources(&co); err != nil {
        t.Fatalf("failed to initialize cluster resources: %v", err)
    }
    t.Log("initialized cluster resources from yaml")

    n, err := ctx.GetNamespace()
    if err != nil {
        t.Fatal(err)
    }

    // get global framework variables
    f := framework.Global

    // wait for syndesis-operator to be ready
    if err := e2eutil.WaitForOperatorDeployment(t, f.KubeClient, n, "syndesis-operator", 1, retryInterval, operatorTimeout); err != nil {
        t.Fatal(err)
    }

    syndesis := CreateEmpyCR(cr, namespace)

    t.Logf("creating empty syndesis cr")

    if err := f.Client.Create(goctx.TODO(), syndesis, &co); err != nil {
        t.Fatal(err)
    }

    // wait for syndesis operator to reach 1 replicas
    t.Logf("waiting syndesis to be initialized")
    err = WaitForSyndesisPhase(t, f, namespace, cr, v1alpha1.SyndesisPhaseInstalling, retryInterval, startingPhaseTimeout)
    if err != nil {
        t.Fatal(err)
    }

    t.Logf("waiting syndesis to be installed")
    err = WaitForSyndesisPhase(t, f, namespace, cr, v1alpha1.SyndesisPhaseInstalled, retryInterval, installingPhaseTimeout)
    if err != nil {
        t.Fatal(err)
    }
}

// Install syndesis with a fully CR, verify some of the generated values
func installTest(t *testing.T)  {
    t.Parallel()
    ctx := framework.NewTestCtx(t)
    defer ctx.Cleanup()

    namespace, err := ctx.GetNamespace()
    cr := "syndesis-test"

    co := framework.CleanupOptions{
        TestContext:   ctx,
        Timeout:       cleanupTimeout,
        RetryInterval: cleanupRetryInterval,
    }

    // Initialize cluster resources from yaml, including operator deployment
    // and needed roles / rbac
    if err := ctx.InitializeClusterResources(&co); err != nil {
        t.Fatalf("failed to initialize cluster resources: %v", err)
    }
    t.Log("initialized cluster resources from yaml")

    n, err := ctx.GetNamespace()
    if err != nil {
        t.Fatal(err)
    }

    // get global framework variables
    f := framework.Global

    // wait for syndesis-operator to be ready
    if err := e2eutil.WaitForOperatorDeployment(t, f.KubeClient, n, "syndesis-operator", 1, retryInterval, operatorTimeout); err != nil {
        t.Fatal(err)
    }

    syndesis := CreateCR(cr, namespace)

    t.Logf("creating syndesis cr")

    if err := f.Client.Create(goctx.TODO(), syndesis, &co); err != nil {
        t.Fatal(err)
    }

    // wait for syndesis operator to reach 1 replicas
    t.Logf("waiting syndesis to be initialized")
    err = WaitForSyndesisPhase(t, f, namespace, cr, v1alpha1.SyndesisPhaseInstalling, retryInterval, startingPhaseTimeout)
    if err != nil {
        t.Fatal(err)
    }

    t.Logf("waiting syndesis to be installed")
    err = WaitForSyndesisPhase(t, f, namespace, cr, v1alpha1.SyndesisPhaseInstalled, retryInterval, installingPhaseTimeout)
    if err != nil {
        t.Fatal(err)
    }
}