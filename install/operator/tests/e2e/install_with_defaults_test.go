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
    "k8s.io/apimachinery/pkg/util/wait"
    "testing"
    "time"

    "github.com/syndesisio/syndesis/install/operator/pkg/generator"

    "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"

    "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/template"

    "golang.org/x/net/context"
    "sigs.k8s.io/controller-runtime/pkg/client"

    imagev1 "github.com/openshift/api/image/v1"

    framework "github.com/operator-framework/operator-sdk/pkg/test"
    "github.com/operator-framework/operator-sdk/pkg/test/e2eutil"
    "github.com/syndesisio/syndesis/install/operator/pkg/apis"
    "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
    v1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

var (
    retryInterval          = time.Second * 5
    operatorTimeout        = time.Second * 60
    startingPhaseTimeout   = time.Second * 300
    installingPhaseTimeout = time.Second * 500
    cleanupRetryInterval   = time.Second * 1
    cleanupTimeout         = time.Second * 5
    cr                     = "syndesis-test"
    configLocation         = "build/conf/config.yaml"
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
        t.Run("install-empty-cr", installWithDefaultsTest)
    })
}

// Test that syndesis is installed correctly by reaching the Installed phase. Use an empty CR, test
// generated values
func installWithDefaultsTest(t *testing.T) {
    t.Parallel()
    ctx := framework.NewTestCtx(t)
    defer ctx.Cleanup()

    namespace, err := ctx.GetNamespace()
    if err != nil {
        t.Fatal(err)
    }

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

    configuration.TemplateConfig = configLocation
    renderContext, err := template.GetTemplateContext()
    if err != nil {
        t.Fatalf("unable to render config content: %v", err)
    }

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

    is := &imagev1.ImageStream{}
    if err := framework.AddToFrameworkScheme(imagev1.AddToScheme, is); err != nil {
        t.Fatalf("could not add scheme to framework scheme: %v", err)
    }

    imageStreamsTest(t, f, renderContext, namespace)
    changeImageStreamTagTest(t, f, renderContext, namespace)

}

func imageStreamsTest(t *testing.T, f *framework.Framework, config *generator.Context, namespace string) {
    t.Log("Checking ImageStreams for syndesis components")

    is := &imagev1.ImageStream{}

    var flagtests = []struct {
        name      string
        imageName string
    }{
        {"Server", "syndesis-server"},
        {"Meta", "syndesis-meta"},
        {"S2I", "syndesis-s2i"},
        {"UI", "syndesis-ui"},
    }

    for _, tt := range flagtests {
        t.Logf("checking imagestream for %s", tt.name)
        if err := f.Client.Get(context.TODO(), client.ObjectKey{Name: tt.imageName, Namespace: namespace}, is); err != nil {
            t.Fatalf("an imagestream named [%s] should be created, but got an error [%v]", tt.imageName, err)
        }

        if l := len(is.Spec.Tags); l != 1 {
            t.Fatalf("the imagestream should have only one tag, but got %d", l)
        } else {
            t.Logf("the imagestream has only one tag")

            if is.Spec.Tags[0].Name != config.Tags.Syndesis {
                t.Fatalf("the imagestream tag should be named [%s], but got %s", config.Tags.Syndesis, is.Spec.Tags[0].Name)
            }
            t.Logf("the imagestream tag is named [%s]", config.Tags.Syndesis)
        }
    }
}

func changeImageStreamTagTest(t *testing.T, f *framework.Framework, config *generator.Context, namespace string) {
    t.Log("Changing ImageStreams for syndesis components")

    ctx := goctx.TODO()
    syndesis := &v1alpha1.Syndesis{}
    if err := f.Client.Get(ctx, client.ObjectKey{Namespace: namespace, Name: cr}, syndesis); err != nil {
        t.Fatalf("unable to fetch syndesis cr name [%s], got error [%v]", cr, err)
    }

    var flagtests = []struct {
        name      string
        imageName string
        value     string
    }{
        {"Server", "syndesis-server", "1.8"},
        {"Meta", "syndesis-meta", "1.8"},
        {"S2I", "syndesis-s2i", "1.8"},
        {"UI", "syndesis-ui", "1.8"},
    }

    syndesis.Spec.Components.Server.Tag = flagtests[0].value
    syndesis.Spec.Components.Meta.Tag = flagtests[1].value
    syndesis.Spec.Components.S2I.Tag = flagtests[2].value
    syndesis.Spec.Components.UI.Tag = flagtests[3].value

    if err := f.Client.Update(ctx, syndesis); err != nil {
        t.Fatalf("unable to update syndesis cr name [%s], got error [%v]", cr, err)
    }

    // lets make sure that changes are applied to the custom resource before we continue
    err := wait.Poll(5, 30, func() (done bool, err error) {
        syndesis := &v1alpha1.Syndesis{}
        if err := f.Client.Get(ctx, client.ObjectKey{Namespace: namespace, Name: cr}, syndesis); err != nil {
            t.Logf("unable to fetch syndesis custom resource [%s]", cr)
            return false, err
        }

        if syndesis.Spec.Components.Server.Tag == flagtests[0].value {
            t.Logf("tag [%s] for imagestream [%s] found in syndesis custom resource", flagtests[0].value, flagtests[0].imageName)
            return true, nil
        }

        t.Logf("tag [%s] for imagestream [%s] still not found in syndesis custom resource", flagtests[0].value, flagtests[0].imageName)
        return false, nil
    })
    if err != nil {
        t.Fatalf("syndesis custom resource didn't update correctly: %v", err)
    }

    // lets give the operator some time to pickup ithe changes
    t.Log("waiting 30 seconds for the operator to pick up the changes")
    time.Sleep(30 * time.Second)
    err = wait.Poll(5, 60, func() (done bool, err error) {
        is := &imagev1.ImageStream{}

        for _, tt := range flagtests {
            err = f.Client.Get(goctx.TODO(), client.ObjectKey{Namespace: namespace, Name: tt.imageName}, is)
            if err != nil {
                t.Logf("unable to fetch syndesis custom resource [%s]", cr)
                return false, err
            }

            t.Logf("imagestream %s has %d tags", tt.imageName, len(is.Spec.Tags))
            if is.Spec.Tags[0].Name == tt.value {
                t.Logf("tag [%s] for imagestream [%s] matches [%s]", is.Spec.Tags[0].Name, tt.imageName, tt.value)
            } else {
                t.Logf("tag [%s] for imagestream [%s] doesnt match [%s]", is.Spec.Tags[0].Name, tt.imageName, tt.value)
                return false, nil
            }
        }

        t.Log("all imagestreams checked and all conditions were met")
        return true, nil
    })
    if err != nil {
        t.Fatalf("imagestreams didnt update correctly: %v", err)
    }
}
