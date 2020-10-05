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

package v1beta2

import (
	v1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// EDIT THIS FILE!  THIS IS SCAFFOLDING FOR YOU TO OWN!
// NOTE: json tags are required.  Any new fields you add must have json tags for the fields to be serialized.

// SyndesisSpec defines the desired state of Syndesis
// +k8s:openapi-gen=true
type SyndesisSpec struct {
	// Schedule backup
	// +optional
	Backup BackupConfig `json:"backup,omitempty"`

	// The external hostname to access Syndesis
	RouteHostname string `json:"routeHostname,omitempty"`

	// Enable SampleDB and demo data for Syndesis
	DemoData bool `json:"demoData,omitempty"`

	// Components is used to configure all the core components of Syndesis
	Components ComponentsSpec `json:"components,omitempty"`

	// Optional add on features that can be enabled.
	Addons AddonsSpec `json:"addons,omitempty"`

	// Something
	ForceMigration bool `json:"forceMigration"`

	// Configuration of Affinity and Toleration for infrastructure component pods
	InfraScheduling SchedulingSpec `json:"infraScheduling,omitempty"`

	// Configuration of Affinity and Toleration for integrations pods
	IntegrationScheduling SchedulingSpec `json:"integrationScheduling,omitempty"`

	// INSERT ADDITIONAL SPEC FIELDS - desired state of cluster
	// Important: Run "operator-sdk generate k8s" to regenerate code after modifying this file
	// Add custom validation using kubebuilder tags: https://book.kubebuilder.io/beyond_basics/generating_crd.html
}

// SyndesisStatus defines the observed state of Syndesis
// +k8s:openapi-gen=true
type SyndesisStatus struct {
	Phase              SyndesisPhase        `json:"phase,omitempty"`
	UpgradeAttempts    int                  `json:"upgradeAttempts,omitempty"`
	LastUpgradeFailure *metav1.Time         `json:"lastUpgradeFailure,omitempty"`
	ForceUpgrade       bool                 `json:"forceUpgrade,omitempty"`
	Reason             SyndesisStatusReason `json:"reason,omitempty"`
	Description        string               `json:"description,omitempty"`
	Version            string               `json:"version,omitempty"`
	TargetVersion      string               `json:"targetVersion,omitempty"`
	Backup             BackupStatus         `json:"backup,omitempty"`

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

// +kubebuilder:validation:Enum=hourly;daily;midnight;weekly;monthly;yearly;every 3m
type BackupSchedule string

type BackupConfig struct {
	// Set schedule for backup cronjob
	// +optional
	Schedule BackupSchedule `json:"schedule,omitempty"`
}

type BackupStatus struct {
	// When is the next backup planned
	Next string `json:"next,omitempty"`
	// When was the previous backup executed
	Previous string `json:"previous,omitempty"`
}

type OauthConfiguration struct {
	// Enable or disable SAR checks all together
	DisableSarCheck bool `json:"disableSarCheck,omitempty"`

	// The user needs to have permissions to at least get a list of pods in the given project in order to be granted access to the Syndesis installation
	SarNamespace string `json:"sarNamespace,omitempty"`

	// Using an external auth provider, specify the name of the secret
	// that stores the credentials, ie. provider type, client id, cookie & client secrets
	CredentialsSecret string `json:"credentialsSecret,omitempty"`

	// The name of the secret used to store the TLS certificate for secure HTTPS communication
	CryptoCommsSecret string `json:"cryptoCommsSecret,omitempty"`
}

type DatabaseConfiguration struct {
	// Username for PostgreSQL user that will be used for accessing the database
	User string `json:"user,omitempty"`

	// Name of the PostgreSQL database accessed
	Name string `json:"name,omitempty"`

	// Host and port of the PostgreSQL database to access
	URL string `json:"url,omitempty"`

	// If specified, use an external database instead of the installed by syndesis
	ExternalDbURL string `json:"externalDbURL,omitempty"`

	// Resource provision requirements of the database
	Resources ResourcesWithPersistentVolume `json:"resources,omitempty"`
}

type PrometheusConfiguration struct {
	Resources ResourcesWithPersistentVolume `json:"resources,omitempty"`
}

type GrafanaConfiguration struct {
	Resources Resources `json:"resources,omitempty"`
}

type ServerConfiguration struct {
	Resources      Resources      `json:"resources,omitempty"`
	Features       ServerFeatures `json:"features,omitempty"`
	ConnectionPool ConnectionPool `json:"connectionPool,omitempty"`
	// JAVA_OPTIONS environment variable
	JavaOptions string `json:"javaOptions,omitempty"`
}

type MetaConfiguration struct {
	Resources ResourcesWithPersistentVolume `json:"resources,omitempty"`
	// JAVA_OPTIONS environment variable
	JavaOptions string `json:"javaOptions,omitempty"`
}

type UpgradeConfiguration struct {
	Resources VolumeOnlyResources `json:"resources,omitempty"`
}

type ResourceParams struct {
	Memory string `json:"memory,omitempty"`
	CPU    string `json:"cpu,omitempty"`
}

type Resources struct {
	Limit   ResourceParams `json:"limit,omitempty"`
	Request ResourceParams `json:"request,omitempty"`
}

// +kubebuilder:validation:Enum=ReadWriteOnce;ReadOnlyMany;ReadWriteMany
type VolumeAccessMode string

// Possible VAM can be found here  https://docs.openshift.com/container-platform/4.2/storage/understanding-persistent-storage.html
const (
	ReadWriteOnce VolumeAccessMode = "ReadWriteOnce"
	ReadOnlyMany  VolumeAccessMode = "ReadOnlyMany"
	// ReadWriteMany VolumeAccessMode = "ReadWriteMany"
)

type ResourcesWithPersistentVolume struct {
	Limit              ResourceParams    `json:"limit,omitempty"`
	Request            ResourceParams    `json:"request,omitempty"`
	VolumeCapacity     string            `json:"volumeCapacity,omitempty"`
	VolumeName         string            `json:"volumeName,omitempty"`
	VolumeAccessMode   VolumeAccessMode  `json:"volumeAccessMode,omitempty"`
	VolumeStorageClass string            `json:"volumeStorageClass,omitempty"`
	VolumeLabels       map[string]string `json:"volumeLabels,omitempty"`
}

type ServerFeatures struct {
	// Maximum number of integrations single user can create
	IntegrationLimit int `json:"integrationLimit,omitempty"`

	// Interval for checking the state of the integrations
	IntegrationStateCheckInterval int `json:"integrationStateCheckInterval,omitempty"`

	// Whether we deploy integrations
	DeployIntegrations bool `json:"deployIntegrations,omitempty"`

	// Maven settings
	Maven MavenConfiguration `json:"maven"`

	// 3scale management URL
	ManagementURLFor3scale string `json:"managementUrlFor3scale,omitempty"`
}

// Connection Pool parameters used in syndesis-server to manage the connections to the database
// time values are in milliseconds
type ConnectionPool struct {
	// maximum number of milliseconds that syndesis-server will wait for a connection from the pool
	ConnectionTimeout int `json:"connectionTimeout,omitempty"`
	// maximum amount of time that a connection is allowed to sit idle in the pool
	IdleTimeout int `json:"idleTimeout,omitempty"`
	// amount of time that a connection can be out of the pool before a message is logged indicating a possible connection leak
	LeakDetectionThreshold int `json:"leakDetectionThreshold,omitempty"`
	// maximum size that the pool is allowed to reach, including both idle and in-use connections
	MaximumPoolSize int `json:"maximumPoolSize,omitempty"`
	// maximum lifetime of a connection in the pool
	MaxLifetime int `json:"maxLifetime,omitempty"`
	// minimum number of idle connections maintained in the pool
	MinimumIdle int `json:"minimumIdle,omitempty"`
}

type MavenConfiguration struct {
	// Should we append new repositories
	Append bool `json:"append"`
	// additional maven options to be used in integration builds
	AdditionalArguments string `json:"additionalArguments,omitempty"`
	// Set repositories for maven
	Repositories map[string]string `json:"repositories,omitempty"`
}

type SchedulingSpec struct {
	Affinity    *v1.Affinity    `json:"affinity,omitempty"`
	Tolerations []v1.Toleration `json:"tolerations,omitempty"`
}

type AddonsSpec struct {
	Jaeger    JaegerConfiguration    `json:"jaeger,omitempty"`
	Ops       AddonSpec              `json:"ops,omitempty"`
	Todo      AddonSpec              `json:"todo,omitempty"`
	Knative   AddonSpec              `json:"knative,omitempty"`
	CamelK    AddonSpec              `json:"camelk,omitempty"`
	PublicAPI PublicAPIConfiguration `json:"publicApi,omitempty"`
}

type JaegerConfiguration struct {
	Enabled       bool   `json:"enabled,omitempty"`
	ClientOnly    bool   `json:"clientOnly,omitempty"`
	ImageAgent    string `json:"imageAgent,omitempty"`
	ImageAllInOne string `json:"imageAllInOne,omitempty"`
	ImageOperator string `json:"imageOperator,omitempty"`
	OperatorOnly  bool   `json:"operatorOnly,omitempty"`
	QueryURI      string `json:"queryUri,omitempty"`
	CollectorURI  string `json:"collectorUri,omitempty"`
	SamplerType   string `json:"samplerType,omitempty"`
	SamplerParam  string `json:"samplerParam,omitempty"`
}

type AddonSpec struct {
	Enabled bool `json:"enabled,omitempty"`
}

type PublicAPIConfiguration struct {
	Enabled bool `json:"enabled,omitempty"`
	// Set RouteHostname to the hostname of the exposed syndesis Public API.
	RouteHostname string `json:"routeHostname,omitempty"`
	// if set to true, then any authenticated user can access the API. otherwise the user
	// needs access to get pods against the SarNamespace
	DisableSarCheck bool `json:"disable-sar-check,omitempty"`
}

type SyndesisPhase string

type ResourcesWithVolume struct {
	Limit          ResourceParams `json:"limit,omitempty"`
	Request        ResourceParams `json:"request,omitempty"`
	VolumeCapacity string         `json:"volumeCapacity,omitempty"`
}

type VolumeOnlyResources struct {
	VolumeCapacity string `json:"volumeCapacity,omitempty"`
}

const (
	SyndesisPhaseMissing               SyndesisPhase = ""
	SyndesisPhaseInstalling            SyndesisPhase = "Installing"
	SyndesisPhaseStarting              SyndesisPhase = "Starting"
	SyndesisPhaseStartupFailed         SyndesisPhase = "StartupFailed"
	SyndesisPhaseInstalled             SyndesisPhase = "Installed"
	SyndesisPhaseNotInstalled          SyndesisPhase = "NotInstalled"
	SyndesisPhaseUpgrading             SyndesisPhase = "Upgrading"
	SyndesisPhasePostUpgradeRun        SyndesisPhase = "PostUpgradeRun"
	SyndesisPhasePostUpgradeRunSucceed SyndesisPhase = "PostUpgradeRunSucceed"
	SyndesisPhaseUpgradeFailureBackoff SyndesisPhase = "UpgradeFailureBackoff"
	SyndesisPhaseUpgradeFailed         SyndesisPhase = "UpgradeFailed"
)

type SyndesisStatusReason string

const (
	SyndesisStatusReasonMissing                SyndesisStatusReason = ""
	SyndesisStatusReasonDuplicate              SyndesisStatusReason = "Duplicate"
	SyndesisStatusReasonDeploymentNotReady     SyndesisStatusReason = "DeploymentNotReady"
	SyndesisStatusReasonUpgradeFailed          SyndesisStatusReason = "UpgradeFailed"
	SyndesisStatusReasonTooManyUpgradeAttempts SyndesisStatusReason = "TooManyUpgradeAttempts"
	SyndesisStatusReasonPostUpgradeRun         SyndesisStatusReason = "PostUpgradeRun"
	SyndesisStatusReasonMigrated               SyndesisStatusReason = "Migrated"
)

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// Syndesis is the Schema for the syndeses API
// +k8s:openapi-gen=true
// +kubebuilder:subresource:status
// +kubebuilder:object:root=true
// +kubebuilder:storageversion
// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object
type Syndesis struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   SyndesisSpec   `json:"spec,omitempty"`
	Status SyndesisStatus `json:"status,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object
// +kubebuilder:object:root=true
// SyndesisList contains a list of Syndesis
type SyndesisList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []Syndesis `json:"items"`
}

func init() {
	SchemeBuilder.Register(&Syndesis{}, &SyndesisList{})
}
