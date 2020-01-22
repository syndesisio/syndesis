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
	"fmt"
	"reflect"
	"testing"

	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/runtime"

	"github.com/stretchr/testify/assert"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
)

const (
	s       = "syndesis-test"
	ns      = "namespace"
	succeed = "\u2713"
	failed  = "\u2717"
)

func Test_syndesisApi_unstructuredToV1Alpha1(t *testing.T) {

	type args struct {
		obj unstructured.Unstructured
	}
	tests := []struct {
		name    string
		args    args
		wantS   *v1alpha1.Syndesis
		wantErr bool
	}{
		{"It should marshal unstructured from v1alpha1 to v1alpha1", args{getRuntimeObjectAsUnstructured(&v1alpha1.Syndesis{})}, &v1alpha1.Syndesis{}, false},
		{"It should not marshal unstructured from v1beta1 to v1alpha1", args{getRuntimeObjectAsUnstructured(&v1beta1.Syndesis{})}, nil, true},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			api := syndesisApi{}
			gotS, err := api.unstructuredToV1Alpha1(tt.args.obj)
			if (err != nil) != tt.wantErr {
				t.Errorf("unstructuredToV1Alpha1() error = %v, wantErr %v", err, tt.wantErr)
				return
			}
			if !reflect.DeepEqual(gotS, tt.wantS) {
				t.Errorf("unstructuredToV1Alpha1() gotS = %v, want %v", gotS, tt.wantS)
			}
		})
	}
}

func Test_syndesisApi_unstructuredToV1Beta1(t *testing.T) {
	type args struct {
		obj unstructured.Unstructured
	}
	tests := []struct {
		name    string
		args    args
		wantS   *v1beta1.Syndesis
		wantErr bool
	}{
		{
			"An empty instance v1beta1 should be fine",
			args{obj: getRuntimeObjectAsUnstructured(&v1beta1.Syndesis{})},
			&v1beta1.Syndesis{}, false,
		},
		{
			"An empty instance of v1alpha1 should be fine",
			args{obj: getRuntimeObjectAsUnstructured(&v1alpha1.Syndesis{})},
			&v1beta1.Syndesis{}, false,
		},
		{
			"An instance v1alpha1 with mismatching data should return error",
			args{obj: getRuntimeObjectAsUnstructured(&v1alpha1.Syndesis{Spec: v1alpha1.SyndesisSpec{Addons: v1alpha1.AddonsSpec{"ops": {"enabled": "true"}}}})},
			nil, true,
		},
		{
			"An instance v1alpha1 with mismatching data should return error",
			args{obj: getRuntimeObjectAsUnstructured(&v1beta1.Syndesis{Spec: v1beta1.SyndesisSpec{Addons: v1beta1.AddonsSpec{Todo: v1beta1.AddonSpec{Enabled: true}}}})},
			&v1beta1.Syndesis{Spec: v1beta1.SyndesisSpec{Addons: v1beta1.AddonsSpec{Todo: v1beta1.AddonSpec{Enabled: true}}}}, false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			api := syndesisApi{}
			gotS, err := api.unstructuredToV1Beta1(tt.args.obj)
			if (err != nil) != tt.wantErr {
				t.Errorf("unstructuredToV1Beta1() error = %v, wantErr %v", err, tt.wantErr)
				return
			}
			if !reflect.DeepEqual(gotS, tt.wantS) {
				t.Errorf("unstructuredToV1Beta1() gotS = %v, want %v", gotS, tt.wantS)
			}
		})
	}
}

func Test_syndesisApi_v1alpha1ToV1beta1(t *testing.T) {
	il := 5
	ici := 10
	dsc := true
	type fields struct {
		v1alpha1 *v1alpha1.Syndesis
		v1beta1  *v1beta1.Syndesis
	}
	tests := []struct {
		name    string
		fields  fields
		wantErr bool
		wantB   *v1beta1.Syndesis
	}{
		{
			"When ForceMigration is true but phase is not Installed, not changes are applied",
			fields{
				v1alpha1: &v1alpha1.Syndesis{Status: v1alpha1.SyndesisStatus{Phase: v1alpha1.SyndesisPhaseInstalling}},
				v1beta1:  &v1beta1.Syndesis{Spec: v1beta1.SyndesisSpec{ForceMigration: true}},
			},
			false,
			&v1beta1.Syndesis{Spec: v1beta1.SyndesisSpec{ForceMigration: true}},
		},
		{
			"When phase is Installed but ForceMigration is false, not changes are applied",
			fields{
				v1alpha1: &v1alpha1.Syndesis{Status: v1alpha1.SyndesisStatus{Phase: v1alpha1.SyndesisPhaseInstalled}},
				v1beta1:  &v1beta1.Syndesis{Spec: v1beta1.SyndesisSpec{ForceMigration: false}},
			},
			false,
			&v1beta1.Syndesis{Spec: v1beta1.SyndesisSpec{ForceMigration: false}},
		},
		{
			"When v1alpha1 is migrated, spec changes",
			fields{
				v1alpha1: &v1alpha1.Syndesis{Status: v1alpha1.SyndesisStatus{Phase: v1alpha1.SyndesisPhaseInstalled}},
				v1beta1:  &v1beta1.Syndesis{Spec: v1beta1.SyndesisSpec{ForceMigration: true}},
			},
			false,
			&v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{ForceMigration: false},
				Status: v1beta1.SyndesisStatus{
					Phase:       v1beta1.SyndesisPhaseInstalled,
					Reason:      v1beta1.SyndesisStatusReasonMigrated,
					Description: fmt.Sprintf("App migrated from %s to %s", v1alpha1.SchemeGroupVersion.String(), v1beta1.SchemeGroupVersion.String()),
				},
			},
		},
		{
			"Migration, check fields",
			fields{
				v1alpha1: &v1alpha1.Syndesis{
					Status: v1alpha1.SyndesisStatus{Phase: v1alpha1.SyndesisPhaseInstalled},
					Spec: v1alpha1.SyndesisSpec{
						SarNamespace:         "sar namespace",
						ImageStreamNamespace: "imagestream namespace",
						RouteHostname:        "routehostname",
						Addons: v1alpha1.AddonsSpec{
							"ops":    map[string]string{"enabled": "true"},
							"todo":   map[string]string{"enabled": "true"},
							"camelk": map[string]string{"enabled": "false"},
							"komodo": map[string]string{"enabled": "true"},
							"jaeger": map[string]string{"enabled": "false"},
						},
						Integration: v1alpha1.IntegrationSpec{
							Limit:              &il,
							StateCheckInterval: &ici,
						},
						MavenRepositories: map[string]string{
							"repo1": "repo1url",
							"repo2": "repo2url",
						},
						Components: v1alpha1.ComponentsSpec{
							Server: v1alpha1.ServerConfiguration{
								Features: v1alpha1.ServerFeatures{
									ManagementUrlFor3scale: "ManagementUrlFor3scale",
								},
							},
							Db: v1alpha1.DbConfiguration{
								User:     "user",
								Database: "database",
							},
							Oauth: v1alpha1.OauthConfiguration{
								DisableSarCheck: &dsc,
							},
						},
					},
				},
				v1beta1: &v1beta1.Syndesis{Spec: v1beta1.SyndesisSpec{ForceMigration: true}},
			},
			false,
			&v1beta1.Syndesis{
				Status: v1beta1.SyndesisStatus{
					Phase:       v1beta1.SyndesisPhaseInstalled,
					Reason:      v1beta1.SyndesisStatusReasonMigrated,
					Description: fmt.Sprintf("App migrated from %s to %s", v1alpha1.SchemeGroupVersion.String(), v1beta1.SchemeGroupVersion.String()),
				},
				Spec: v1beta1.SyndesisSpec{
					ForceMigration:       false,
					ImageStreamNamespace: "imagestream namespace",
					RouteHostname:        "routehostname",
					Addons: v1beta1.AddonsSpec{
						Jaeger: v1beta1.JaegerConfiguration{Enabled: false},
						Ops:    v1beta1.AddonSpec{Enabled: true},
						Todo:   v1beta1.AddonSpec{Enabled: true},
						DV:     v1beta1.DvConfiguration{Enabled: true},
						CamelK: v1beta1.AddonSpec{Enabled: false},
					},
					Components: v1beta1.ComponentsSpec{
						Oauth: v1beta1.OauthConfiguration{SarNamespace: "sar namespace", DisableSarCheck: dsc},
						Server: v1beta1.ServerConfiguration{
							Features: v1beta1.ServerFeatures{
								IntegrationLimit:              il,
								IntegrationStateCheckInterval: ici,
								ManagementUrlFor3scale:        "ManagementUrlFor3scale",
								MavenRepositories: map[string]string{
									"repo1": "repo1url",
									"repo2": "repo2url",
								},
							},
						},
						Database: v1beta1.DatabaseConfiguration{
							User: "user",
							Name: "database",
						},
					},
				},
			},
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			api := syndesisApi{
				v1alpha1: tt.fields.v1alpha1,
				v1beta1:  tt.fields.v1beta1,
			}
			if err := api.v1alpha1ToV1beta1(); (err != nil) != tt.wantErr {
				t.Errorf("v1alpha1ToV1beta1() error = %v, wantErr %v", err, tt.wantErr)
			} else {
				assert.Equal(t, api.v1beta1, tt.wantB)
			}
		})
	}
}

func getRuntimeObjectAsUnstructured(obj runtime.Object) (r unstructured.Unstructured) {
	s, err := util.ToUnstructured(obj)
	if err != nil {
		return unstructured.Unstructured{}
	} else {
		return *s
	}
}
