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

package versions

import (
	"context"
	"encoding/json"
	"fmt"

	v1 "k8s.io/api/core/v1"

	"github.com/syndesisio/syndesis/install/operator/pkg/util"

	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"

	"github.com/go-logr/logr"

	"k8s.io/apimachinery/pkg/types"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type SyndesisApiMigrator interface {
	// If Syndesis API version is v1alpha1, migrate it to v1beta1
	Migrate() error
}

type syndesisApi struct {
	client           client.Client
	context          context.Context
	log              logr.Logger
	unstructuredApis *unstructured.UnstructuredList
	v1alpha1         *v1alpha1.Syndesis
	v1beta1          *v1beta1.Syndesis
}

// Build and return an SyndesisApiMigrator interface
func ApiMigrator(ctx context.Context, c client.Client, n string) (r SyndesisApiMigrator, err error) {
	// Fetch all existing apis in an unstructured list. It is necessary to use an unstructured list
	// because different apis might have a different structure
	list := &unstructured.UnstructuredList{
		Object: map[string]interface{}{
			"kind":       "Syndesis",
			"apiVersion": "syndesis.io/v1beta1",
		},
	}

	options := &client.ListOptions{
		Namespace: n,
		Raw: &metav1.ListOptions{
			TypeMeta: metav1.TypeMeta{
				Kind:       "Syndesis",
				APIVersion: "syndesis.io/v1beta1",
			},
		},
	}
	if err := c.List(ctx, list, options); err != nil {
		return nil, err
	}

	api := syndesisApi{
		client:           c,
		context:          ctx,
		log:              logf.Log.WithName("versions").WithValues("version from", "v1alpha1", "version to", "v1beta1"),
		unstructuredApis: list,
		v1beta1: &v1beta1.Syndesis{
			TypeMeta: metav1.TypeMeta{Kind: "Syndesis", APIVersion: "syndesis.io/v1beta1"},
		},
		v1alpha1: &v1alpha1.Syndesis{
			TypeMeta: metav1.TypeMeta{Kind: "Syndesis", APIVersion: "syndesis.io/v1alpha1"},
		},
	}

	v1alpha1s := []*v1alpha1.Syndesis{}
	v1beta1s := []*v1beta1.Syndesis{}
	for _, a := range api.unstructuredApis.Items {
		sb, err := api.unstructuredToV1Beta1(a)
		if err != nil {
			sa, err := api.unstructuredToV1Alpha1(a)
			if err == nil {
				v1alpha1s = append(v1alpha1s, sa)
			}
		} else {
			v1beta1s = append(v1beta1s, sb)
		}
	}

	/*
	 * We support at most, one instance of each api. We can have:
	 * - 1x v1alpha1 0x v1beta1. It can be an upgrade where the administrator installing the operator didn't create
	 * a v1beta1. In this case, we will create an empty v1beta1 and migrate from v1alpha1
	 *
	 * - 0x v1alpha1 1x v1beta1. This is the desired state and we do nothing
	 */
	if len(v1alpha1s)+len(v1beta1s) > 1 {
		return nil, fmt.Errorf("unsupported ammount of apis v1alpha: %d, v1beta1: %d", len(v1alpha1s), len(v1beta1s))
	}

	// Fetch v1alpha1 from kubernetes if it exists
	if len(v1alpha1s) == 1 {
		if err = api.client.Get(
			api.context,
			types.NamespacedName{
				Namespace: v1alpha1s[0].Namespace,
				Name:      v1alpha1s[0].Name,
			},
			api.v1alpha1); err != nil {
			return nil, err
		}
		api.v1alpha1.TypeMeta = metav1.TypeMeta{Kind: "Syndesis", APIVersion: "syndesis.io/v1beta1"}
	} else {
		api.v1alpha1 = nil
	}

	// Fetch v1beta1 from kubernetes if it exists, otherwise create a new one
	if len(v1beta1s) == 1 {
		if err = api.client.Get(
			api.context,
			types.NamespacedName{
				Namespace: v1beta1s[0].Namespace,
				Name:      v1beta1s[0].Name,
			},
			api.v1beta1); err != nil {
			return nil, err
		}
		api.v1beta1.TypeMeta = metav1.TypeMeta{Kind: "Syndesis", APIVersion: "syndesis.io/v1beta1"}
	} else {
		// When creating a new v1beta1 api, try to use v1alpha1 name for it
		name := "app"
		if len(v1alpha1s) == 1 {
			name = v1alpha1s[0].Name
		}

		api.v1beta1 = &v1beta1.Syndesis{
			Spec:     v1beta1.SyndesisSpec{ForceMigration: true},
			TypeMeta: metav1.TypeMeta{Kind: "Syndesis", APIVersion: "syndesis.io/v1beta1"},
			ObjectMeta: metav1.ObjectMeta{
				Name:      name,
				Namespace: n,
			},
		}
	}

	return api, nil
}

func (api syndesisApi) Migrate() (err error) {
	if api.v1alpha1 != nil {
		if err = api.v1alpha1ToV1beta1(); err != nil {
			return err
		}

		if err = api.updateApis(); err != nil {
			return err
		}
	}

	return nil
}

// Migrates from old v1alpha1 api to v1beta1. This overwrite v1alpha1
func (api syndesisApi) v1alpha1ToV1beta1() error {
	// We migrate only if v1alpha1 wasn't migrated before and v1beta1 explicitly indicates to be migrated
	if api.v1alpha1 != nil && api.v1alpha1.Status.Phase == v1alpha1.SyndesisPhaseInstalled && api.v1beta1.Spec.ForceMigration {
		// Migrate addons
		for k, addon := range api.v1alpha1.Spec.Addons {
			switch k {
			case "ops":
				api.v1beta1.Spec.Addons.Ops.Enabled = addon["enabled"] == "true"
			case "todo":
				api.v1beta1.Spec.Addons.Todo.Enabled = addon["enabled"] == "true"
			case "jaeger":
				api.v1beta1.Spec.Addons.Jaeger.Enabled = addon["enabled"] == "true"
			}
		}

		// Migrate maven repositories
		if len(api.v1alpha1.Spec.MavenRepositories) != 0 {
			api.v1beta1.Spec.Components.Server.Features.Maven.Repositories = map[string]string{}
			for k, v := range api.v1alpha1.Spec.MavenRepositories {
				api.v1beta1.Spec.Components.Server.Features.Maven.Repositories[k] = v
			}
		}

		// Migrate Integrations
		if api.v1alpha1.Spec.Integration.Limit != nil {
			api.v1beta1.Spec.Components.Server.Features.IntegrationLimit = *api.v1alpha1.Spec.Integration.Limit
		}
		if api.v1alpha1.Spec.Integration.StateCheckInterval != nil {
			api.v1beta1.Spec.Components.Server.Features.IntegrationStateCheckInterval = *api.v1alpha1.Spec.Integration.StateCheckInterval
		}

		// Server
		if api.v1alpha1.Spec.Components.Server.Features.ManagementUrlFor3scale != "" {
			api.v1beta1.Spec.Components.Server.Features.ManagementUrlFor3scale = api.v1alpha1.Spec.Components.Server.Features.ManagementUrlFor3scale
		}
		if api.v1alpha1.Spec.Components.Server.Resources.Limits != nil {
			if m, ok := api.v1alpha1.Spec.Components.Server.Resources.Limits[v1.ResourceMemory]; ok {
				api.v1beta1.Spec.Components.Server.Resources.Memory = m.String()
			}
		}

		// Database
		if api.v1alpha1.Spec.Components.Db.Database != "" {
			api.v1beta1.Spec.Components.Database.Name = api.v1alpha1.Spec.Components.Db.Database
		}
		if api.v1alpha1.Spec.Components.Db.Resources.Limits != nil {
			if m, ok := api.v1alpha1.Spec.Components.Db.Resources.Limits[v1.ResourceMemory]; ok {
				api.v1beta1.Spec.Components.Database.Resources.Memory = m.String()
			}
		}
		if api.v1alpha1.Spec.Components.Db.Resources.VolumeCapacity != "" {
			api.v1beta1.Spec.Components.Database.Resources.VolumeCapacity = api.v1alpha1.Spec.Components.Db.Resources.VolumeCapacity
		}
		if api.v1alpha1.Spec.Components.Db.User != "" {
			api.v1beta1.Spec.Components.Database.User = api.v1alpha1.Spec.Components.Db.User
		}

		// Oauth
		if api.v1alpha1.Spec.Components.Oauth.DisableSarCheck != nil {
			api.v1beta1.Spec.Components.Oauth.DisableSarCheck = *api.v1alpha1.Spec.Components.Oauth.DisableSarCheck
		}

		// Meta
		if api.v1alpha1.Spec.Components.Meta.Resources.Limits != nil {
			if m, ok := api.v1alpha1.Spec.Components.Meta.Resources.Limits[v1.ResourceMemory]; ok {
				api.v1beta1.Spec.Components.Meta.Resources.Memory = m.String()
			}
		}
		if api.v1alpha1.Spec.Components.Meta.Resources.VolumeCapacity != "" {
			api.v1beta1.Spec.Components.Meta.Resources.VolumeCapacity = api.v1alpha1.Spec.Components.Meta.Resources.VolumeCapacity
		}

		// Prometheus
		if api.v1alpha1.Spec.Components.Prometheus.Resources.Limits != nil {
			if m, ok := api.v1alpha1.Spec.Components.Prometheus.Resources.Limits[v1.ResourceMemory]; ok {
				api.v1beta1.Spec.Components.Prometheus.Resources.Memory = m.String()
			}
		}
		if api.v1alpha1.Spec.Components.Prometheus.Resources.VolumeCapacity != "" {
			api.v1beta1.Spec.Components.Prometheus.Resources.VolumeCapacity = api.v1alpha1.Spec.Components.Prometheus.Resources.VolumeCapacity
		}

		// Grafana
		if api.v1alpha1.Spec.Components.Grafana.Resources.Limits != nil {
			if m, ok := api.v1alpha1.Spec.Components.Grafana.Resources.Limits[v1.ResourceMemory]; ok {
				api.v1beta1.Spec.Components.Grafana.Resources.Memory = m.String()
			}
		}

		// General
		if api.v1alpha1.Spec.RouteHostname != "" {
			api.v1beta1.Spec.RouteHostname = api.v1alpha1.Spec.RouteHostname
		}

		if api.v1alpha1.Spec.SarNamespace != "" {
			api.v1beta1.Spec.Components.Oauth.SarNamespace = api.v1alpha1.Spec.SarNamespace
		}

		// We dont want to migrate again more than once
		api.v1beta1.Spec.ForceMigration = false

		// We need the same status and version in the target as in the origin
		api.v1beta1.Status.Version = api.v1alpha1.Status.Version
		api.v1beta1.Status.Phase = v1beta1.SyndesisPhaseInstalled
		api.v1beta1.Status.Reason = v1beta1.SyndesisStatusReasonMigrated
		api.v1beta1.Status.Description = fmt.Sprintf("App migrated from %s to %s", v1alpha1.SchemeGroupVersion.String(), v1beta1.SchemeGroupVersion.String())
	}

	return nil
}

// Write back apis
func (api syndesisApi) updateApis() error {
	api.log.Info("updating syndesis api",
		"from name", api.v1alpha1.Name, "from version", api.v1alpha1.Status.Version,
		"to name", api.v1beta1.Name, "to version", api.v1beta1.Status.Version)
	if _, _, err := util.CreateOrUpdate(api.context, api.client, api.v1beta1); err != nil {
		return err
	}

	return nil
}

// Attempt to convert from unstructured to v1beta1.Syndesis
func (api syndesisApi) unstructuredToV1Beta1(obj unstructured.Unstructured) (s *v1beta1.Syndesis, err error) {
	s = &v1beta1.Syndesis{}

	objM, err := json.Marshal(obj.Object)
	if err != nil {
		return nil, err
	}

	if err := json.Unmarshal(objM, s); err != nil {
		return nil, err
	}

	return
}

// Attempt to convert from unstructured to v1alpha1.Syndesis
func (api syndesisApi) unstructuredToV1Alpha1(obj unstructured.Unstructured) (s *v1alpha1.Syndesis, err error) {
	s = &v1alpha1.Syndesis{}

	objM, err := json.Marshal(obj.Object)
	if err != nil {
		return nil, err
	}

	err = json.Unmarshal(objM, s)
	if err != nil {
		return nil, err
	}

	return s, nil
}
