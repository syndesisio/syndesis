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

	logf "sigs.k8s.io/controller-runtime/pkg/log"

	"github.com/go-logr/logr"
	"k8s.io/apimachinery/pkg/api/meta"
	"k8s.io/apimachinery/pkg/types"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta2"
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
	v1beta2          *v1beta2.Syndesis
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

	api := syndesisApi{
		client:           c,
		context:          ctx,
		log:              logf.Log.WithName("versions").WithValues("version to", "v1beta2"),
		unstructuredApis: list,
		v1beta2: &v1beta2.Syndesis{
			TypeMeta: metav1.TypeMeta{Kind: "Syndesis", APIVersion: "syndesis.io/v1beta2"},
		},
		v1beta1: &v1beta1.Syndesis{
			TypeMeta: metav1.TypeMeta{Kind: "Syndesis", APIVersion: "syndesis.io/v1beta1"},
		},
		v1alpha1: &v1alpha1.Syndesis{
			TypeMeta: metav1.TypeMeta{Kind: "Syndesis", APIVersion: "syndesis.io/v1alpha1"},
		},
	}

	if err := c.List(ctx, list, options); err != nil {
		if meta.IsNoMatchError(err) {
			// Returns api but with empty list
			return api, nil
		}
		return nil, err
	} else {
		api.unstructuredApis = list
	}

	v1alpha1s := []*v1alpha1.Syndesis{}
	v1beta1s := []*v1beta1.Syndesis{}
	v1beta2s := []*v1beta2.Syndesis{}
	for _, a := range api.unstructuredApis.Items {
		sb2, err := api.unstructuredToV1Beta2(a)
		if err == nil {
			v1beta2s = append(v1beta2s, sb2)
		} else {
			sb1, err := api.unstructuredToV1Beta1(a)
			if err == nil {
				v1beta1s = append(v1beta1s, sb1)
			} else {
				sa, err := api.unstructuredToV1Alpha1(a)
				if err == nil {
					v1alpha1s = append(v1alpha1s, sa)
				}
			}
		}
	}

	/*
	 * We support at most, one instance of each api. We can have:
	 * - 1x v1alpha1 0x v1beta1 0x v1beta2.
	 * - 0x v1alpha1 1x v1beta1 0x v1beta2.
	 * It can be an upgrade where the administrator installing the operator didn't create
	 * a v1beta2. In this case, we will create an empty v1beta2 and migrate
	 *
	 * - 0x v1alpha1 0x v1beta1 1x v1beta2. This is the desired state and we do nothing
	 */
	if len(v1alpha1s)+len(v1beta1s)+len(v1beta2s) > 1 {
		return nil, fmt.Errorf("unsupported number of apis v1alpha: %d, v1beta1: %d, v1beta2: %d", len(v1alpha1s), len(v1beta1s), len(v1beta2s))
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
		api.v1alpha1.TypeMeta = metav1.TypeMeta{Kind: "Syndesis", APIVersion: "syndesis.io/v1alpha1"}
	} else {
		api.v1alpha1 = nil
	}

	// Fetch v1beta1 from kubernetes if it exists
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
		api.v1beta1 = nil
	}

	// Fetch v1beta2 from kubernetes if it exists, otherwise create a new one
	if len(v1beta2s) == 1 {
		if err = api.client.Get(
			api.context,
			types.NamespacedName{
				Namespace: v1beta2s[0].Namespace,
				Name:      v1beta2s[0].Name,
			},
			api.v1beta2); err != nil {
			return nil, err
		}
		api.v1beta2.TypeMeta = metav1.TypeMeta{Kind: "Syndesis", APIVersion: "syndesis.io/v1beta2"}
	} else {
		// When creating a new v1beta2 api, try to use existing name for it
		name := "app"
		if len(v1alpha1s) == 1 {
			name = v1alpha1s[0].Name
		} else if len(v1beta1s) == 1 {
			name = v1beta1s[0].Name
		}

		//
		// Not fetched from cluster so create a new one and turn on ForceMigration
		//
		api.v1beta2 = &v1beta2.Syndesis{
			Spec:     v1beta2.SyndesisSpec{ForceMigration: true},
			TypeMeta: metav1.TypeMeta{Kind: "Syndesis", APIVersion: "syndesis.io/v1beta2"},
			ObjectMeta: metav1.ObjectMeta{
				Name:      name,
				Namespace: n,
			},
		}
	}

	return api, nil
}

func (api syndesisApi) Migrate() (err error) {
	if len(api.unstructuredApis.Items) == 0 {
		// Nothing to do
		return nil
	}

	if api.v1alpha1 != nil {
		if err = api.v1alpha1ToV1beta2(); err != nil {
			return err
		}
	} else if api.v1beta1 != nil {
		if err = api.v1beta1ToV1beta2(); err != nil {
			return err
		}
	}

	if err = api.updateApis(); err != nil {
		return err
	}

	return nil
}

// Migrates from old v1alpha1 api to v1beta2. This overwrite v1alpha1
func (api syndesisApi) v1alpha1ToV1beta2() error {
	// We migrate only if v1alpha1 wasn't migrated before and v1beta2 explicitly indicates to be migrated
	if api.v1alpha1 != nil && api.v1alpha1.Status.Phase == v1alpha1.SyndesisPhaseInstalled && api.v1beta2.Spec.ForceMigration {

		//
		// Upgrade to v1beta1 first
		//

		//
		// If there is no v1beta1 defined then create one and migrate the v1alpha1 to it
		//
		if api.v1beta1 == nil {
			api.v1beta1 = &v1beta1.Syndesis{
				Spec:     v1beta1.SyndesisSpec{ForceMigration: true},
				TypeMeta: metav1.TypeMeta{Kind: "Syndesis", APIVersion: "syndesis.io/v1beta1"},
				ObjectMeta: metav1.ObjectMeta{
					Name:      api.v1alpha1.Name,
					Namespace: api.v1alpha1.Namespace,
				},
			}
		}

		if err := api.v1alpha1ToV1beta1(); err != nil {
			return err
		}

		//
		// Now upgrade to v1beta2
		//

		// Ensure that beta1 is in upgrade mode
		api.v1beta1.Spec.ForceMigration = true

		if err := api.v1beta1ToV1beta2(); err != nil {
			return err
		}
	}

	return nil
}

// Migrates from old v1beta1 api to v1beta2. This overwrite v1beta1
func (api syndesisApi) v1beta1ToV1beta2() error {
	// We migrate only if v1beta1 wasn't migrated before and v1beta2 explicitly indicates to be migrated
	if api.v1beta1 != nil && api.v1beta1.Status.Phase == v1beta1.SyndesisPhaseInstalled && api.v1beta2.Spec.ForceMigration {

		//
		// Copy any common fields by marhsalling to json
		// then unmarshalling into the new struct
		//
		beta1Json, err := json.Marshal(api.v1beta1)
		if err != nil {
			return err
		}

		//
		// Not interested in marshalling errors as only copying common fields
		// The rest will be explicitly copied in the remaining code
		//
		_ = json.Unmarshal(beta1Json, &api.v1beta2)

		//
		// This will have been overwritten so correct it
		//
		api.v1beta2.TypeMeta.APIVersion = "syndesis.io/v1beta2"

		// Server
		if api.v1beta1.Spec.Components.Server.Resources.Memory != "" {
			api.v1beta2.Spec.Components.Server.Resources.Limit = v1beta2.ResourceParams{Memory: api.v1beta1.Spec.Components.Server.Resources.Memory}
			api.v1beta2.Spec.Components.Server.Resources.Request = v1beta2.ResourceParams{Memory: api.v1beta1.Spec.Components.Server.Resources.Memory}
		}

		// Database
		if api.v1beta1.Spec.Components.Database.Resources.Memory != "" {
			api.v1beta2.Spec.Components.Database.Resources.Limit = v1beta2.ResourceParams{Memory: api.v1beta1.Spec.Components.Database.Resources.Memory}
			api.v1beta2.Spec.Components.Database.Resources.Request = v1beta2.ResourceParams{Memory: api.v1beta1.Spec.Components.Database.Resources.Memory}
		}

		// Meta
		if api.v1beta1.Spec.Components.Meta.Resources.Memory != "" {
			api.v1beta2.Spec.Components.Meta.Resources.Limit = v1beta2.ResourceParams{Memory: api.v1beta1.Spec.Components.Meta.Resources.Memory}
			api.v1beta2.Spec.Components.Meta.Resources.Request = v1beta2.ResourceParams{Memory: api.v1beta1.Spec.Components.Meta.Resources.Memory}
		}

		// Prometheus
		if api.v1beta1.Spec.Components.Prometheus.Resources.Memory != "" {
			api.v1beta2.Spec.Components.Prometheus.Resources.Limit = v1beta2.ResourceParams{Memory: api.v1beta1.Spec.Components.Prometheus.Resources.Memory}
			api.v1beta2.Spec.Components.Prometheus.Resources.Request = v1beta2.ResourceParams{Memory: api.v1beta1.Spec.Components.Prometheus.Resources.Memory}
		}

		// Grafana
		if api.v1beta1.Spec.Components.Grafana.Resources.Memory != "" {
			api.v1beta2.Spec.Components.Grafana.Resources.Limit = v1beta2.ResourceParams{Memory: api.v1beta1.Spec.Components.Grafana.Resources.Memory}
			api.v1beta2.Spec.Components.Grafana.Resources.Request = v1beta2.ResourceParams{Memory: api.v1beta1.Spec.Components.Grafana.Resources.Memory}
		}

		// We dont want to migrate again more than once
		api.v1beta2.Spec.ForceMigration = false

		// We need the same status and version in the target as in the origin
		api.v1beta2.Status.Version = api.v1beta1.Status.Version
		api.v1beta2.Status.Phase = v1beta2.SyndesisPhaseInstalled
		api.v1beta2.Status.Reason = v1beta2.SyndesisStatusReasonMigrated

		srcVersion := ""
		if api.v1alpha1 != nil {
			srcVersion = v1alpha1.SchemeGroupVersion.String()
		} else {
			srcVersion = v1beta1.SchemeGroupVersion.String()
		}

		api.v1beta2.Status.Description = fmt.Sprintf("App migrated from %s to %s", srcVersion, v1beta2.SchemeGroupVersion.String())
	}

	return nil
}

// Migrates from old v1alpha1 api to v1beta1. This overwrite v1alpha1
func (api syndesisApi) v1alpha1ToV1beta1() error {
	// We migrate only if v1alpha1 wasn't migrated before and v1beta1 explicitly indicates to be migrated
	if api.v1alpha1 != nil && api.v1alpha1.Status.Phase == v1alpha1.SyndesisPhaseInstalled && api.v1beta1.Spec.ForceMigration {

		//
		// Copy any common fields by marhsalling to json
		// then unmarshalling into the new struct
		//
		alphaJson, err := json.Marshal(api.v1alpha1)
		if err != nil {
			return err
		}

		//
		// Not interested in marshalling errors as only copying common fields
		// The rest will be explicitly copied in the remaining code
		//
		_ = json.Unmarshal(alphaJson, &api.v1beta1)

		//
		// This will have been overwritten so correct it
		//
		api.v1beta1.TypeMeta.APIVersion = "syndesis.io/v1beta1"

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
	fromName := ""
	fromVersion := ""
	if api.v1alpha1 != nil {
		fromName = api.v1alpha1.Name
		fromVersion = api.v1alpha1.Status.Version
	} else if api.v1beta1 != nil {
		fromName = api.v1beta1.Name
		fromVersion = api.v1beta1.Status.Version
	} else {
		return nil // nothing to do
	}

	api.log.Info("updating syndesis api",
		"from name", fromName, "from version", fromVersion,
		"to name", api.v1beta2.Name, "to version", api.v1beta2.Status.Version)
	if _, _, err := util.CreateOrUpdate(api.context, api.client, api.v1beta2); err != nil {
		return err
	}

	return nil
}

// Attempt to convert from unstructured to v1beta2.Syndesis
func (api syndesisApi) unstructuredToV1Beta2(obj unstructured.Unstructured) (s *v1beta2.Syndesis, err error) {
	s = &v1beta2.Syndesis{}

	objM, err := json.Marshal(obj.Object)
	if err != nil {
		return nil, err
	}

	if err := json.Unmarshal(objM, s); err != nil {
		return nil, err
	}

	return
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
