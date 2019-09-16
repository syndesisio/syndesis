/*
 *
 *  * Copyright (C) 2019 Red Hat, Inc.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package install

import (
	"context"
	"encoding/json"
	"fmt"
	"strings"

	v1 "github.com/openshift/api/route/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/openshift/serviceaccount"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/action"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/template"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/client-go/kubernetes/scheme"
	"sigs.k8s.io/controller-runtime/pkg/client"

	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func (o *Install) installStandalone() error {
	ctx := context.TODO()
	cli, err := o.GetClient()
	if err != nil {
		return err
	}

	s := scheme.Scheme
	if err := v1.AddToScheme(s); err != nil {
		return err
	}

	templateConfig, err := util.LoadJsonFromFile(configuration.TemplateConfig)
	if err != nil {
		return err
	}

	// Parse the config
	gen := &generator.Context{}
	err = json.Unmarshal(templateConfig, gen)
	if err != nil {
		return err
	}

	syndesis := &v1alpha1.Syndesis{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: o.Namespace,
		},
		Spec: v1alpha1.SyndesisSpec{
			DevSupport: o.devSupport,
		},
	}
	gen.Syndesis = syndesis

	// fill in the addons
	params := template.ResourceParams{}

	// Check whether we there is a secret for authentication, and link it to the
	// service accounts
	{
		// Lets create the service account and link it to the secret
		// Check if an image secret exists, to be used to connect to registries that require authentication
		secret := &corev1.Secret{}
		err = cli.Get(ctx, types.NamespacedName{Namespace: syndesis.Namespace, Name: action.SyndesisPullSecret}, secret)
		if err != nil {
			if k8serrors.IsNotFound(err) {
				secret = nil
			} else {
				return err
			}
		}

		serviceAccount, err := installServiceAccount(ctx, cli, syndesis, secret)
		if err != nil {
			return err
		}

		token, err := serviceaccount.GetServiceAccountToken(ctx, cli, serviceAccount.Name, syndesis.Namespace)
		if err != nil {
			return err
		}

		params.OAuthClientSecret = token
	}

	// Get the value of the route, by creating a dummy route and taking it's
	// host value. Delete the route afterwards
	{
		route, err := generator.RenderDir("./route/", gen)
		if err != nil {
			return err
		}

		for _, res := range route {
			res.SetNamespace(o.Namespace)
			_, _, err := util.CreateOrUpdate(ctx, cli, &res)
			if err != nil {
				return err
			}
		}

		syndesisRoute := &v1.Route{}
		err = cli.Get(ctx, util.NewObjectKey("syndesis", o.Namespace), syndesisRoute)
		if err != nil {
			return err
		}
		// Set the right hostname after generating the route
		syndesis.Spec.RouteHostname = syndesisRoute.Spec.Host

		if err := cli.Delete(ctx, syndesisRoute); err != nil {
			return err
		}

	}

	if err = template.SetupRenderContext(gen, syndesis, params, map[string]string{}); err != nil {
		return err
	}

	configuration.SetConfigurationFromEnvVars(gen.Env, syndesis)

	// Render the route resource... this time for real
	all, err := generator.RenderDir("./route/", gen)
	if err != nil {
		return err
	}

	// Render the remaining syndesis resources...
	inf, err := generator.RenderDir("./infrastructure/", gen)
	if err != nil {
		return err
	}

	all = append(all, inf...)

	addons := strings.Split(o.addons, ",")
	for addon := range syndesis.Spec.Addons {
		if !contains(addons, addon) {
			continue
		}

		addonDir := "./addons/" + addon + "/"
		f, err := generator.GetAssetsFS().Open(addonDir)
		if err != nil {
			fmt.Printf("unsuported addon configured: [%s]. [%v]", addon, err)
			continue
		}
		f.Close()

		resources, err := generator.RenderDir(addonDir, gen)
		if err != nil {
			return err
		}

		all = append(all, resources...)
	}

	if o.eject == "" {
		// Install the resources..
		for _, res := range all {
			res.SetNamespace(o.Namespace)
		}

		err := o.install("syndesis was", all)
		if err != nil {
			return err
		}
	} else {
		o.ejectedResources = []unstructured.Unstructured{}
		o.ejectedResources = append(o.ejectedResources, all...)
	}

	return nil
}

func contains(s []string, e string) bool {
	for _, a := range s {
		if a == e {
			return true
		}
	}
	return false
}

func installServiceAccount(ctx context.Context, cl client.Client, syndesis *v1alpha1.Syndesis, secret *corev1.Secret) (*corev1.ServiceAccount, error) {
	sa := newSyndesisServiceAccount(syndesis.Namespace)
	if secret != nil {
		linkImagePullSecret(sa, secret)
	}

	o, _, err := util.CreateOrUpdate(ctx, cl, sa)
	if err != nil {
		return nil, err
	}
	sa.SetUID(o.GetUID())
	return sa, nil
}

func newSyndesisServiceAccount(ns string) *corev1.ServiceAccount {
	sa := corev1.ServiceAccount{
		TypeMeta: metav1.TypeMeta{
			APIVersion: "v1",
			Kind:       "ServiceAccount",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      "syndesis-oauth-client",
			Namespace: ns,
			Labels: map[string]string{
				"app": "syndesis",
			},
			Annotations: map[string]string{
				"serviceaccounts.openshift.io/oauth-redirecturi.local":       "https://localhost:4200",
				"serviceaccounts.openshift.io/oauth-redirecturi.route":       "https://",
				"serviceaccounts.openshift.io/oauth-redirectreference.route": `{"kind": "OAuthRedirectReference", "apiVersion": "v1", "reference": {"kind": "Route","name": "syndesis"}}`,
			},
		},
	}

	return &sa
}

func linkImagePullSecret(sa *corev1.ServiceAccount, secret *corev1.Secret) bool {
	exist := false
	for _, s := range sa.ImagePullSecrets {
		if s.Name == secret.Name {
			exist = true
			break
		}
	}

	if !exist {
		sa.ImagePullSecrets = append(sa.ImagePullSecrets, corev1.LocalObjectReference{
			Name: secret.Name,
		})
		return true
	}

	return false
}
