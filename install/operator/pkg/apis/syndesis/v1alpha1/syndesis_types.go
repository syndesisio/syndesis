package v1alpha1

import (
	v1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// EDIT THIS FILE!  THIS IS SCAFFOLDING FOR YOU TO OWN!
// NOTE: json tags are required.  Any new fields you add must have json tags for the fields to be serialized.

// SyndesisSpec defines the desired state of Syndesis
// +k8s:openapi-gen=true
type SyndesisSpec struct {
	RouteHostname        string          `json:"routeHostname,omitempty"`
	DemoData             *bool           `json:"demoData,omitempty"`
	DeployIntegrations   *bool           `json:"deployIntegrations,omitempty"`
	TestSupport          *bool           `json:"testSupport,omitempty"`
	ImageStreamNamespace string          `json:"imageStreamNamespace,omitempty"`
	Integration          IntegrationSpec `json:"integration,omitempty"`
	// The container registry to pull syndesis images from
	Registry            string         `json:"registry,omitempty"`
	Components          ComponentsSpec `json:"components,omitempty"`
	OpenShiftMaster     string         `json:"openshiftMaster,omitempty"`
	OpenShiftConsoleUrl string         `json:"openshiftConsoleUrl,omitempty"`
	SarNamespace        string         `json:"sarNamespace,omitempty"`
	Addons              AddonsSpec     `json:"addons,omitempty"`
	// if true, then the image streams are changed to used local development builds & JAVA_DEBUG is enabled
	DevSupport bool `json:"devSupport,omitempty"`

	MavenRepositories map[string]string `json:"mavenRepositories,omitempty"`
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

type IntegrationSpec struct {
	Limit              *int `json:"limit,omitempty"`
	StateCheckInterval *int `json:"stateCheckInterval,omitempty"`
}

type ComponentsSpec struct {
	ImagePrefix      string                        `json:"imagePrefix,omitempty"`
	Scheduled        bool                          `json:"scheduled,omitempty"`
	Server           ServerConfiguration           `json:"server,omitempty"`
	Meta             MetaConfiguration             `json:"meta,omitempty"`
	UI               UIConfiguration               `json:"ui,omitempty"`
	S2I              S2IConfiguration              `json:"s2i,omitempty"`
	Db               DbConfiguration               `json:"db,omitempty"`
	Oauth            OauthConfiguration            `json:"oauth,omitempty"`
	PostgresExporter PostgresExporterConfiguration `json:"psql,omitempty"`
	Prometheus       PrometheusConfiguration       `json:"prometheus,omitempty"`
	Grafana          GrafanaConfiguration          `json:"grafana,omitempty"`
	Komodo           KomodoConfiguration           `json:"komodo,omitempty"`
	Upgrade          UpgradeConfiguration          `json:"upgrade,omitempty"`
}

type OauthConfiguration struct {
	Tag string `json:"tag,omitempty"`
}

type PostgresExporterConfiguration struct {
	Tag         string `json:"tag,omitempty"`
	Registry    string `json:"registry,omitempty"`
	ImagePrefix string `json:"imagePrefix,omitempty"`
}

type KomodoConfiguration struct {
	Registry    string    `json:"registry,omitempty"`
	ImagePrefix string    `json:"imagePrefix,omitempty"`
	Resources   Resources `json:"resources,omitempty"`
	Tag         string    `json:"tag,omitempty"`
}

type S2IConfiguration struct {
	Registry    string `json:"registry,omitempty"`
	ImagePrefix string `json:"imagePrefix,omitempty"`
	Tag         string `json:"tag,omitempty"`
}

type UIConfiguration struct {
	Registry    string `json:"registry,omitempty"`
	ImagePrefix string `json:"imagePrefix,omitempty"`
	Tag         string `json:"tag,omitempty"`
}

type DbConfiguration struct {
	Tag                  string              `json:"tag,omitempty"`
	Resources            ResourcesWithVolume `json:"resources,omitempty"`
	User                 string              `json:"user,omitempty"`
	Database             string              `json:"database,omitempty"`
	ImageStreamNamespace string              `json:"imageStreamNamespace,omitempty"`
}

type PrometheusConfiguration struct {
	Tag       string              `json:"tag,omitempty"`
	Resources ResourcesWithVolume `json:"resources,omitempty"`
}

type GrafanaConfiguration struct {
	Resources Resources `json:"resources,omitempty"`
}

type ServerConfiguration struct {
	Registry    string         `json:"registry,omitempty"`
	ImagePrefix string         `json:"imagePrefix,omitempty"`
	Tag         string         `json:"tag,omitempty"`
	Resources   Resources      `json:"resources,omitempty"`
	Features    ServerFeatures `json:"features,omitempty"`
}

type MetaConfiguration struct {
	Registry    string              `json:"registry,omitempty"`
	ImagePrefix string              `json:"imagePrefix,omitempty"`
	Tag         string              `json:"tag,omitempty"`
	Resources   ResourcesWithVolume `json:"resources,omitempty"`
}

type UpgradeConfiguration struct {
	Tag         string              `json:"tag,omitempty"`
	Registry    string              `json:"registry,omitempty"`
	ImagePrefix string              `json:"imagePrefix,omitempty"`
	Resources   VolumeOnlyResources `json:"resources,omitempty"`
}

type Resources struct {
	v1.ResourceRequirements `json:",inline,omitempty"`
}

type ResourcesWithVolume struct {
	Resources      `json:",inline,omitempty"`
	VolumeCapacity string `json:"volumeCapacity,omitempty"`
}

type VolumeOnlyResources struct {
	VolumeCapacity string `json:"volumeCapacity,omitempty"`
}

type ServerFeatures struct {
	ManagementUrlFor3scale string `json:"managementUrlFor3scale,omitempty"`
}

type AddonsSpec map[string]Parameters

type Parameters map[string]string

// =============================================================================

type SyndesisPhase string

const (
	SyndesisPhaseMissing               SyndesisPhase = ""
	SyndesisPhaseInstalling            SyndesisPhase = "Installing"
	SyndesisPhaseUpgradingLegacy       SyndesisPhase = "UpgradingLegacy"
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
