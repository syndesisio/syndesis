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

package v1alpha1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// EDIT THIS FILE!  THIS IS SCAFFOLDING FOR YOU TO OWN!
// NOTE: json tags are required.  Any new fields you add must have json tags for the fields to be serialized.

// SyndesisSpec defines the desired state of Syndesis
// +k8s:openapi-gen=true
type SyndesisSpec struct {
	ImageStreamNamespace string `json:"imageStreamNamespace,omitempty"`

	// Components is used to configure all the core components of Syndesis
	Components ComponentsSpec `json:"components,omitempty"`

	// Optional add on features that can be enabled.
	Addons AddonsSpec `json:"addons,omitempty"`

	// INSERT ADDITIONAL SPEC FIELDS - desired state of cluster
	// Important: Run "operator-sdk generate k8s" to regenerate code after modifying this file
	// Add custom validation using kubebuilder tags: https://book.kubebuilder.io/beyond_basics/generating_crd.html
}

// SyndesisStatus defines the observed state of Syndesis
// +k8s:openapi-gen=true
type SyndesisStatus struct {
	Phase              SyndesisPhase        `json:"phase,omitempty"`
	UpgradeAttempts    int32                `json:"upgradeAttempts,omitempty"`
	LastUpgradeFailure *metav1.Time         `json:"lastUpgradeFailure,omitempty"`
	ForceUpgrade       bool                 `json:"forceUpgrade,omitempty"`
	Reason             SyndesisStatusReason `json:"reason,omitempty"`
	Description        string               `json:"description,omitempty"`
	Version            string               `json:"version,omitempty"`
	TargetVersion      string               `json:"targetVersion,omitempty"`
	// INSERT ADDITIONAL STATUS FIELD - define observed state of cluster
	// Important: Run "operator-sdk generate k8s" to regenerate code after modifying this file
	// Add custom validation using kubebuilder tags: https://book.kubebuilder.io/beyond_basics/generating_crd.html
}

// =============================================================================

// +k8s:openapi-gen=true
type ComponentsSpec struct {
	Oauth      OauthConfiguration      `json:"oauth,omitempty"`
	Server     ServerConfiguration     `json:"server,omitempty"`
	Meta       MetaConfiguration       `json:"meta,omitempty"`
	Database   DatabaseConfiguration   `json:"database,omitempty"`
	Prometheus PrometheusConfiguration `json:"prometheus,omitempty"`
	Grafana    GrafanaConfiguration    `json:"grafana,omitempty"`
	Upgrade    UpgradeConfiguration    `json:"upgrade,omitempty"`
}

type OauthConfiguration struct {
	DisableSarCheck bool   `json:"disable-sar-check,omitempty"`
	SarNamespace    string `json:"sarNamespace,omitempty"`
}

type DvConfiguration struct {
	Enabled   bool      `json:"enabled,omitempty"`
	Resources Resources `json:"resources,omitempty"`
}

type DatabaseConfiguration struct {
	User          string              `json:"user,omitempty"`
	Name          string              `json:"name,omitempty"`
	URL           string              `url:"url,omitempty"`
	ExternalDbURL string              `json:"externalDbURL,omitempty"`
	Resources     ResourcesWithVolume `json:"resources,omitempty"`
}

type PrometheusConfiguration struct {
	Rules     string              `json:"rules,omitempty"`
	Resources ResourcesWithVolume `json:"resources,omitempty"`
}

type GrafanaConfiguration struct {
	Resources Resources `json:"resources,omitempty"`
}

type ServerConfiguration struct {
	Resources Resources      `json:"resources,omitempty"`
	Features  ServerFeatures `json:"features,omitempty"`
}

type MetaConfiguration struct {
	Resources ResourcesWithVolume `json:"resources,omitempty"`
}

type UpgradeConfiguration struct {
	Resources VolumeOnlyResources `json:"resources,omitempty"`
}

type Resources struct {
	Memory string `json:",inline,omitempty"`
}

type ResourcesWithVolume struct {
	Memory         string `json:",inline,omitempty"`
	VolumeCapacity string `json:"volumeCapacity,omitempty"`
}

type VolumeOnlyResources struct {
	VolumeCapacity string `json:"volumeCapacity,omitempty"`
}

type ServerFeatures struct {
	MavenRepositories map[string]string `json:"mavenRepositories,omitempty"`
}

type AddonsSpec struct {
	Jaeger  JaegerConfiguration `json:"jaeger,omitempty"`
	Ops     AddonSpec           `json:"ops,omitempty"`
	Todo    AddonSpec           `json:"todo,omitempty"`
	Knative AddonSpec           `json:"knative,omitempty"`
	DV      DvConfiguration     `json:"dv,omitempty"`
	CamelK  CamelKConfiguration `json:"camelk,omitempty"`
}

type JaegerConfiguration struct {
	Enabled      bool   `json:"enabled,omitempty"`
	SamplerType  string `json:"samplerType,omitempty"`
	SamplerParam string `json:"samplerParam,omitempty"`
}

type AddonSpec struct {
	Enabled bool `json:"enabled,omitempty"`
}

type CamelKConfiguration struct {
	Enabled       bool   `json:"enabled,omitempty"`
	CamelVersion  string `json:"camelVersion,omitempty"`
	CamelKRuntime string `json:"camelkRuntime,omitempty"`
	Image         string `json:"image,omitempty"`
}

// =============================================================================

type SyndesisPhase string

const (
	SyndesisPhaseMissing               SyndesisPhase = ""
	SyndesisPhaseInstalling            SyndesisPhase = "Installing"
	SyndesisPhaseStarting              SyndesisPhase = "Starting"
	SyndesisPhaseStartupFailed         SyndesisPhase = "StartupFailed"
	SyndesisPhaseInstalled             SyndesisPhase = "Installed"
	SyndesisPhaseNotInstalled          SyndesisPhase = "NotInstalled"
	SyndesisPhaseUpgrading             SyndesisPhase = "Upgrading"
	SyndesisPhaseUpgradeFailureBackoff SyndesisPhase = "UpgradeFailureBackoff"
	SyndesisPhaseUpgradeFailed         SyndesisPhase = "UpgradeFailed"
)

type SyndesisStatusReason string

const (
	SyndesisStatusReasonMissing                SyndesisStatusReason = ""
	SyndesisStatusReasonDuplicate              SyndesisStatusReason = "Duplicate"
	SyndesisStatusReasonDeploymentNotReady     SyndesisStatusReason = "DeploymentNotReady"
	SyndesisStatusReasonUpgradePodFailed       SyndesisStatusReason = "UpgradePodFailed"
	SyndesisStatusReasonTooManyUpgradeAttempts SyndesisStatusReason = "TooManyUpgradeAttempts"
)

// =============================================================================

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// Syndesis is the Schema for the syndeses API
// +k8s:openapi-gen=true
// +kubebuilder:subresource:status
// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object
type Syndesis struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   SyndesisSpec   `json:"spec,omitempty"`
	Status SyndesisStatus `json:"status,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// SyndesisList contains a list of Syndesis
type SyndesisList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []Syndesis `json:"items"`
}

func init() {
	SchemeBuilder.Register(&Syndesis{}, &SyndesisList{})
}
