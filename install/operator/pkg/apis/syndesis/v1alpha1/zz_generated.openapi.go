// +build !ignore_autogenerated

// This file was autogenerated by openapi-gen. Do not edit it manually!

package v1alpha1

import (
	spec "github.com/go-openapi/spec"
	common "k8s.io/kube-openapi/pkg/common"
)

func GetOpenAPIDefinitions(ref common.ReferenceCallback) map[string]common.OpenAPIDefinition {
	return map[string]common.OpenAPIDefinition{
		"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.ComponentsSpec": schema_pkg_apis_syndesis_v1alpha1_ComponentsSpec(ref),
		"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.Syndesis":       schema_pkg_apis_syndesis_v1alpha1_Syndesis(ref),
		"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.SyndesisSpec":   schema_pkg_apis_syndesis_v1alpha1_SyndesisSpec(ref),
		"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.SyndesisStatus": schema_pkg_apis_syndesis_v1alpha1_SyndesisStatus(ref),
	}
}

func schema_pkg_apis_syndesis_v1alpha1_ComponentsSpec(ref common.ReferenceCallback) common.OpenAPIDefinition {
	return common.OpenAPIDefinition{
		Schema: spec.Schema{
			SchemaProps: spec.SchemaProps{
				Type: []string{"object"},
				Properties: map[string]spec.Schema{
					"oauth": {
						SchemaProps: spec.SchemaProps{
							Ref: ref("github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.OauthConfiguration"),
						},
					},
					"server": {
						SchemaProps: spec.SchemaProps{
							Ref: ref("github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.ServerConfiguration"),
						},
					},
					"meta": {
						SchemaProps: spec.SchemaProps{
							Ref: ref("github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.MetaConfiguration"),
						},
					},
					"database": {
						SchemaProps: spec.SchemaProps{
							Ref: ref("github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.DatabaseConfiguration"),
						},
					},
					"prometheus": {
						SchemaProps: spec.SchemaProps{
							Ref: ref("github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.PrometheusConfiguration"),
						},
					},
					"grafana": {
						SchemaProps: spec.SchemaProps{
							Ref: ref("github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.GrafanaConfiguration"),
						},
					},
					"upgrade": {
						SchemaProps: spec.SchemaProps{
							Ref: ref("github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.UpgradeConfiguration"),
						},
					},
				},
			},
		},
		Dependencies: []string{
			"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.DatabaseConfiguration", "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.GrafanaConfiguration", "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.MetaConfiguration", "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.OauthConfiguration", "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.PrometheusConfiguration", "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.ServerConfiguration", "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.UpgradeConfiguration"},
	}
}

func schema_pkg_apis_syndesis_v1alpha1_Syndesis(ref common.ReferenceCallback) common.OpenAPIDefinition {
	return common.OpenAPIDefinition{
		Schema: spec.Schema{
			SchemaProps: spec.SchemaProps{
				Description: "Syndesis is the Schema for the syndeses API",
				Type:        []string{"object"},
				Properties: map[string]spec.Schema{
					"kind": {
						SchemaProps: spec.SchemaProps{
							Description: "Kind is a string value representing the REST resource this object represents. Servers may infer this from the endpoint the client submits requests to. Cannot be updated. In CamelCase. More info: https://git.k8s.io/community/contributors/devel/api-conventions.md#types-kinds",
							Type:        []string{"string"},
							Format:      "",
						},
					},
					"apiVersion": {
						SchemaProps: spec.SchemaProps{
							Description: "APIVersion defines the versioned schema of this representation of an object. Servers should convert recognized schemas to the latest internal value, and may reject unrecognized values. More info: https://git.k8s.io/community/contributors/devel/api-conventions.md#resources",
							Type:        []string{"string"},
							Format:      "",
						},
					},
					"metadata": {
						SchemaProps: spec.SchemaProps{
							Ref: ref("k8s.io/apimachinery/pkg/apis/meta/v1.ObjectMeta"),
						},
					},
					"spec": {
						SchemaProps: spec.SchemaProps{
							Ref: ref("github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.SyndesisSpec"),
						},
					},
					"status": {
						SchemaProps: spec.SchemaProps{
							Ref: ref("github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.SyndesisStatus"),
						},
					},
				},
			},
		},
		Dependencies: []string{
			"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.SyndesisSpec", "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.SyndesisStatus", "k8s.io/apimachinery/pkg/apis/meta/v1.ObjectMeta"},
	}
}

func schema_pkg_apis_syndesis_v1alpha1_SyndesisSpec(ref common.ReferenceCallback) common.OpenAPIDefinition {
	return common.OpenAPIDefinition{
		Schema: spec.Schema{
			SchemaProps: spec.SchemaProps{
				Description: "SyndesisSpec defines the desired state of Syndesis",
				Type:        []string{"object"},
				Properties: map[string]spec.Schema{
					"imageStreamNamespace": {
						SchemaProps: spec.SchemaProps{
							Type:   []string{"string"},
							Format: "",
						},
					},
					"components": {
						SchemaProps: spec.SchemaProps{
							Description: "Components is used to configure all the core components of Syndesis",
							Ref:         ref("github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.ComponentsSpec"),
						},
					},
					"addons": {
						SchemaProps: spec.SchemaProps{
							Description: "Optional add on features that can be enabled.",
							Ref:         ref("github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.AddonsSpec"),
						},
					},
				},
			},
		},
		Dependencies: []string{
			"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.AddonsSpec", "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1.ComponentsSpec"},
	}
}

func schema_pkg_apis_syndesis_v1alpha1_SyndesisStatus(ref common.ReferenceCallback) common.OpenAPIDefinition {
	return common.OpenAPIDefinition{
		Schema: spec.Schema{
			SchemaProps: spec.SchemaProps{
				Description: "SyndesisStatus defines the observed state of Syndesis",
				Type:        []string{"object"},
				Properties: map[string]spec.Schema{
					"phase": {
						SchemaProps: spec.SchemaProps{
							Type:   []string{"string"},
							Format: "",
						},
					},
					"upgradeAttempts": {
						SchemaProps: spec.SchemaProps{
							Type:   []string{"integer"},
							Format: "int32",
						},
					},
					"lastUpgradeFailure": {
						SchemaProps: spec.SchemaProps{
							Ref: ref("k8s.io/apimachinery/pkg/apis/meta/v1.Time"),
						},
					},
					"forceUpgrade": {
						SchemaProps: spec.SchemaProps{
							Type:   []string{"boolean"},
							Format: "",
						},
					},
					"reason": {
						SchemaProps: spec.SchemaProps{
							Type:   []string{"string"},
							Format: "",
						},
					},
					"description": {
						SchemaProps: spec.SchemaProps{
							Type:   []string{"string"},
							Format: "",
						},
					},
					"version": {
						SchemaProps: spec.SchemaProps{
							Type:   []string{"string"},
							Format: "",
						},
					},
					"targetVersion": {
						SchemaProps: spec.SchemaProps{
							Type:   []string{"string"},
							Format: "",
						},
					},
				},
			},
		},
		Dependencies: []string{
			"k8s.io/apimachinery/pkg/apis/meta/v1.Time"},
	}
}
