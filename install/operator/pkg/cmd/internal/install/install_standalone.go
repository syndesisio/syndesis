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

package install

import (
	"context"
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/action"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/types"

	v1 "github.com/openshift/api/route/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/openshift/serviceaccount"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/template"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes/scheme"
)

// Parse the templates and apply the resources
// There are two hacky bits here. It is needed to create the syndesis
// route and the syndesis-oauth-client service account in advance, before
// the rest of the resources are parsed. The host of the route and the token of the serviceaccount
// are passed down to the templates.
func (o *Install) installStandalone() error {
	if o.ejectedResources != nil {
		o.ejectedResources = nil
	}

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

	// create custom resource
	syndesis := &v1alpha1.Syndesis{}

	if o.customResource == "" {
		syndesis = &v1alpha1.Syndesis{
			ObjectMeta: metav1.ObjectMeta{
				Namespace: o.Namespace,
			},
			Spec: v1alpha1.SyndesisSpec{
				DevSupport: o.devSupport,
				Addons:     v1alpha1.AddonsSpec{},
			},
		}
	} else {
		customResource, err := util.LoadJsonFromFile(o.customResource)
		if err != nil {
			return err
		}

		err = json.Unmarshal(customResource, syndesis)
		if err != nil {
			return err
		}
	}

	gen.Syndesis = syndesis

	addons := make([]string, 0)
	if o.addons != "" {
		addons = strings.Split(o.addons, ",")
	}

	for _, addon := range addons {
		if contains(addons, addon) {
			if syndesis.Spec.Addons[addon] == nil {
				syndesis.Spec.Addons[addon] = v1alpha1.Parameters{}
			}
			syndesis.Spec.Addons[addon]["enabled"] = "true"
		}
	}

	// Get pull secret in case it was created, and link it to the serviceaccounts
	secret := &corev1.Secret{}
	err = cli.Get(ctx, types.NamespacedName{Namespace: syndesis.Namespace, Name: action.SyndesisPullSecret}, secret)
	if err != nil {
		if k8serrors.IsNotFound(err) {
			secret = nil
		} else {
			return err
		}
	}
	if secret != nil {
		gen.ImagePullSecrets = append(gen.ImagePullSecrets, secret.Name)
	}

	params := template.ResourceParams{}

	// Create the syndesis-oauth-client serviceaccount to grab the token
	{
		if sa, err := generator.RenderDir("./serviceaccount/", gen); err == nil {
			for _, res := range sa {
				res.SetNamespace(o.Namespace)
			}

			if err := o.install("serviceaccount syndesis-oauth-client was", sa); err != nil {
				o.Println("unable to create syndesis-oauth-client service account")
				return err
			}
		} else {
			o.Println("unable to parse syndesis-oauth-client service account")
			return err
		}

		time.Sleep(2 * time.Second)
		token, err := serviceaccount.GetServiceAccountToken(ctx, cli, "syndesis-oauth-client", syndesis.Namespace)
		if err != nil {
			return err
		}

		if secret != nil {
			sa := &corev1.ServiceAccount{}
			if err := cli.Get(ctx, util.NewObjectKey("syndesis-oauth-client", o.Namespace), sa); err == nil {
				linkSecret(sa, secret.Name)
			} else {
				return err
			}
		}
		params.OAuthClientSecret = token
	}

	// Get the value of the route, by creating syndesis route first and taking it's
	// host value
	{
		if route, err := generator.RenderDir("./route/", gen); err == nil {
			for _, res := range route {
				res.SetNamespace(o.Namespace)
			}

			if err := o.install("syndesis route was", route); err != nil {
				return err
			}
		} else {
			return err
		}

		syndesisRoute := &v1.Route{}
		err = cli.Get(ctx, util.NewObjectKey("syndesis", o.Namespace), syndesisRoute)
		if err != nil {
			return err
		}
		// Set the right hostname after generating the route
		syndesis.Spec.RouteHostname = syndesisRoute.Spec.Host
	}

	if err = template.SetupRenderContext(gen, syndesis, params, map[string]string{}); err != nil {
		return err
	}

	configuration.SetConfigurationFromEnvVars(gen.Env, syndesis)

	// Render the remaining syndesis resources...
	all, err := generator.RenderDir("./infrastructure/", gen)
	if err != nil {
		return err
	}

	// Render addons
	for addon, properties := range syndesis.Spec.Addons {
		if properties["enabled"] != "true" {
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

		o.Println("addon " + addon + " enabled")
		all = append(all, resources...)
	}

	// Install the resources..
	for _, res := range all {
		res.SetNamespace(o.Namespace)
	}

	err = o.install("syndesis was", all)
	if err != nil {
		return err
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

func linkSecret(sa *corev1.ServiceAccount, secret string) bool {
	exist := false
	for _, s := range sa.Secrets {
		if s.Name == secret {
			exist = true
			break
		}
	}

	if !exist {
		sa.Secrets = append(sa.Secrets, corev1.ObjectReference{Namespace: sa.Namespace, Name: action.SyndesisPullSecret})
		return true
	}

	return false
}
