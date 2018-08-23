package v1alpha1

import (
	"k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

type SyndesisList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata"`
	Items           []Syndesis `json:"items"`
}

func NewSyndesisList() *SyndesisList {
	return &SyndesisList{
		TypeMeta: metav1.TypeMeta{
			APIVersion: groupName + "/" + version,
			Kind:       "Syndesis",
		},
	}
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

type Syndesis struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata"`
	Spec              SyndesisSpec   `json:"spec"`
	Status            SyndesisStatus `json:"status,omitempty"`
}

type SyndesisSpec struct {
	RouteHostName        string          `json:"routeHostname,omitempty"`
	DemoData             *bool           `json:"demoData,omitempty"`
	DeployIntegrations   *bool           `json:"deployIntegrations,omitempty"`
	TestSupport          *bool           `json:"testSupport,omitempty"`
	ImageStreamNamespace string          `json:"imageStreamNamespace,omitempty"`
	Integration          IntegrationSpec `json:"integration,omitempty"`
	Registry             string          `json:"registry,omitempty"`
	Components           ComponentsSpec  `json:"components,omitempty"`
	OpenShiftConsoleUrl  string          `json:"openShiftConsoleUrl,omitempty"`
	SarNamespace         string          `json:"sarNamespace,omitempty"`
}

type IntegrationSpec struct {
	Limit              *int `json:"limit,omitempty"`
	StateCheckInterval *int `json:"stateCheckInterval,omitempty"`
}

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

type SyndesisStatus struct {
	Phase              SyndesisPhase        `json:"phase,omitempty"`
	UpgradeAttempts    int32                `json:"upgradeAttempts,omitempty"`
	LastUpgradeFailure *metav1.Time         `json:"lastUpgradeFailure,omitempty"`
	ForceUpgrade       bool                 `json:"forceUpgrade,omitempty"`
	Reason             SyndesisStatusReason `json:"reason,omitempty"`
	Description        string               `json:"description,omitempty"`
	Version            string               `json:"version,omitempty"`
	TargetVersion      string               `json:"targetVersion,omitempty"`
}

type ComponentsSpec struct {
	Db         DbConfiguration         `json:"db,omitempty"`
	Prometheus PrometheusConfiguration `json:"prometheus,omitempty"`
	Server     ServerConfiguration     `json:"server,omitempty"`
	Meta       MetaConfiguration       `json:"meta,omitempty"`
}

type DbConfiguration struct {
	Resources            ResourcesWithVolume `json:"resources,omitempty"`
	User                 string              `json:"user,omitempty"`
	Database             string              `json:"database,omitempty"`
	ImageStreamNamespace string              `json:"imageStreamNamespace,omitempty"`
}

type PrometheusConfiguration struct {
	Resources ResourcesWithVolume `json:"resources,omitempty"`
}

type ServerConfiguration struct {
	Resources Resources `json:"resources,omitempty"`
}

type MetaConfiguration struct {
	Resources ResourcesWithVolume `json:"resources,omitempty"`
}

type Resources struct {
	v1.ResourceRequirements `json:",inline,omitempty"`
}

type ResourcesWithVolume struct {
	v1.ResourceRequirements `json:",inline,omitempty"`
	VolumeCapacity          string `json:"volumeCapacity,omitempty"`
}
