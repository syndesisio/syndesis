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
	// Set RouteHostname to the hostname of the exposed syndesis service.  Typically the operator can automatically
	// determine this by looking at the result of the Route object it creates.
	RouteHostname      string `json:"routeHostname,omitempty"`
	DemoData           *bool  `json:"demoData,omitempty"`
	DeployIntegrations *bool  `json:"deployIntegrations,omitempty"`
	// Set TestSupport to true to enable a very low level data access API into Syndesis, typically used for
	// integration testing, and should not be enabled in production.
	// +k8s:openapi-gen=false
	TestSupport *bool `json:"testSupport,omitempty"`
	// Set ImageStreamNamespace to the namespace where the operator should store image streams in.  Defaults to match
	// the namespace of the the Syndesis resource.
	ImageStreamNamespace string `json:"imageStreamNamespace,omitempty"`
	// Integration is used to configure settings related to runtime integrations that get deployed.
	Integration IntegrationSpec `json:"integration,omitempty"`

	// Components is used to configure all the core components of Syndesis
	Components ComponentsSpec `json:"components,omitempty"`

	OpenShiftMaster string `json:"openShiftMaster,omitempty"`

	// Set OpenShiftConsoleUrl to the the http URL of your OpenShift console so we can deep link to things like
	// pod logs.
	OpenShiftConsoleUrl string `json:"openShiftConsoleUrl,omitempty"`
	// SarNamespace is the namespace to perform Subject Access Review authorization checks against.  Defaults to match
	// the namespace of the the Syndesis resource.
	SarNamespace string `json:"sarNamespace,omitempty"`

	// Optional add on features that can be enabled.
	// +k8s:openapi-gen=false
	Addons AddonsSpec `json:"addons,omitempty"`

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
	Image string `json:"image,omitempty"`
	// if set to true, then any authenticated user can access the install. otherwise the user
	// needs access to get pods against the SarNamespace
	DisableSarCheck bool `json:"disable-sar-check,omitempty"`
}

type PostgresExporterConfiguration struct {
	Image string `json:"image,omitempty"`
}

type KomodoConfiguration struct {
	Resources Resources `json:"resources,omitempty"`
	Image     string    `json:"image,omitempty"`
}

type S2IConfiguration struct {
	Image string `json:"image,omitempty"`
}

type UIConfiguration struct {
	Image string `json:"image,omitempty"`
}

type DbConfiguration struct {
	Image                string              `json:"image,omitempty"`
	Resources            ResourcesWithVolume `json:"resources,omitempty"`
	User                 string              `json:"user,omitempty"`
	Database             string              `json:"database,omitempty"`
	ImageStreamNamespace string              `json:"imageStreamNamespace,omitempty"`
}

type PrometheusConfiguration struct {
	Image     string              `json:"image,omitempty"`
	Resources ResourcesWithVolume `json:"resources,omitempty"`
}

type GrafanaConfiguration struct {
	Resources Resources `json:"resources,omitempty"`
}

type ServerConfiguration struct {
	Image     string         `json:"image,omitempty"`
	Resources Resources      `json:"resources,omitempty"`
	Features  ServerFeatures `json:"features,omitempty"`
}

type MetaConfiguration struct {
	Image     string              `json:"image,omitempty"`
	Resources ResourcesWithVolume `json:"resources,omitempty"`
}

type UpgradeConfiguration struct {
	Image     string              `json:"image,omitempty"`
	Resources VolumeOnlyResources `json:"resources,omitempty"`
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

// +k8s:openapi-gen=true
type AddonsSpec map[string]Parameters

// +k8s:openapi-gen=false
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
