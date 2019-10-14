package configuration

import (
	"fmt"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	v1 "k8s.io/api/core/v1"
)

type SyndesisEnvVar string

// Location from where the template configuration is located
var TemplateConfig string

const (
	EnvRouteHostname                  SyndesisEnvVar = "ROUTE_HOSTNAME"
	EnvOpenShiftMaster                SyndesisEnvVar = "OPENSHIFT_MASTER"
	EnvOpenShiftConsoleUrl            SyndesisEnvVar = "OPENSHIFT_CONSOLE_URL"
	EnvOpenShiftProject               SyndesisEnvVar = "OPENSHIFT_PROJECT"
	EnvOpenShiftOauthClientSecret     SyndesisEnvVar = "OPENSHIFT_OAUTH_CLIENT_SECRET"
	EnvPostgresqlMemoryLimit          SyndesisEnvVar = "POSTGRESQL_MEMORY_LIMIT"
	EnvPostgresqlImageStreamNamespace SyndesisEnvVar = "POSTGRESQL_IMAGE_STREAM_NAMESPACE"
	EnvPostgresqlUser                 SyndesisEnvVar = "POSTGRESQL_USER"
	EnvPostgresqlPassword             SyndesisEnvVar = "POSTGRESQL_PASSWORD"
	EnvPostgresqlDatabase             SyndesisEnvVar = "POSTGRESQL_DATABASE"
	EnvPostgresqlVolumeCapacity       SyndesisEnvVar = "POSTGRESQL_VOLUME_CAPACITY"
	EnvPostgresqlSampledbPassword     SyndesisEnvVar = "POSTGRESQL_SAMPLEDB_PASSWORD"
	EnvTestSupport                    SyndesisEnvVar = "TEST_SUPPORT_ENABLED"
	EnvOauthCookieSecret              SyndesisEnvVar = "OAUTH_COOKIE_SECRET"
	EnvSyndesisEncryptKey             SyndesisEnvVar = "SYNDESIS_ENCRYPT_KEY"
	EnvPrometheusVolumeCapacity       SyndesisEnvVar = "PROMETHEUS_VOLUME_CAPACITY"
	EnvPrometheusMemoryLimit          SyndesisEnvVar = "PROMETHEUS_MEMORY_LIMIT"
	EnvMetaVolumeCapacity             SyndesisEnvVar = "META_VOLUME_CAPACITY"
	EnvMetaMemoryLimit                SyndesisEnvVar = "META_MEMORY_LIMIT"
	EnvServerMemoryLimit              SyndesisEnvVar = "SERVER_MEMORY_LIMIT"
	EnvClientStateAuthenticationKey   SyndesisEnvVar = "CLIENT_STATE_AUTHENTICATION_KEY"
	EnvClientStateEncryptionKey       SyndesisEnvVar = "CLIENT_STATE_ENCRYPTION_KEY"
	EnvImageStreamNamespace           SyndesisEnvVar = "IMAGE_STREAM_NAMESPACE"
	EnvControllersIntegrationEnabled  SyndesisEnvVar = "CONTROLLERS_INTEGRATION_ENABLED"
	EnvSyndesisRegistry               SyndesisEnvVar = "SYNDESIS_REGISTRY"
	EnvDemoDataEnabled                SyndesisEnvVar = "DEMO_DATA_ENABLED"
	EnvMaxIntegrationsPerUser         SyndesisEnvVar = "MAX_INTEGRATIONS_PER_USER"
	EnvIntegrationStateCheckInterval  SyndesisEnvVar = "INTEGRATION_STATE_CHECK_INTERVAL"
	EnvSarNamespace                   SyndesisEnvVar = "SAR_PROJECT"
	EnvKomodoMemoryLimit              SyndesisEnvVar = "KOMODO_MEMORY_LIMIT"
	EnvDatavirtEnabled                SyndesisEnvVar = "DATAVIRT_ENABLED"

	EnvSyndesisServerTag   SyndesisEnvVar = "SYNDESIS_SERVER_TAG"
	EnvSyndesisUITag       SyndesisEnvVar = "SYNDESIS_UI_TAG"
	EnvSyndesisS2ITag      SyndesisEnvVar = "SYNDESIS_S2I_TAG"
	EnvSyndesisMetaTag     SyndesisEnvVar = "SYNDESIS_META_TAG"
	EnvPostgresTag         SyndesisEnvVar = "SYNDESIS_POSTGRES_TAG"
	EnvPostgresExporterTag SyndesisEnvVar = "POSTGRES_EXPORTER_TAG"
	EnvKomodoTag           SyndesisEnvVar = "KOMODO_TAG"
	EnvPrometheusTag       SyndesisEnvVar = "PROMETHEUS_TAG"
	EnvOauthProxyTag       SyndesisEnvVar = "OAUTH_PROXY_TAG"

	EnvUpgradeVolumeCapacity  SyndesisEnvVar = "UPGRADE_VOLUME_CAPACITY"
	EnvManagementUrlFor3scale SyndesisEnvVar = "OPENSHIFT_MANAGEMENT_URL_FOR3SCALE"
)

const EMPTY_FIELD = "<>"

type ConfigSpec struct {
	Value       string `json:"value"`
	Required    bool   `json:"required,omitempty"`
	Generate    string `json:"generate,omitempty"`
	FromLen     int    `json:"fromLen,omitempty"`
	Description string `json:"description,omitempty"`
}

func (cs ConfigSpec) From() string {
	return fmt.Sprintf("[a-zA-Z0-9]{%d}", cs.FromLen)
}

var AllConfigOptions = map[SyndesisEnvVar]ConfigSpec{
	EnvRouteHostname:                  ConfigSpec{Description: "The external hostname to access Syndesis"},
	EnvOpenShiftMaster:                ConfigSpec{Value: "https://localhost:8443", Required: true, Description: "Public OpenShift master address"},
	EnvOpenShiftConsoleUrl:            ConfigSpec{Value: "https://localhost:8443", Description: "The URL to the OpenShift console"},
	EnvOpenShiftProject:               ConfigSpec{Required: true, Description: "The name of the OpenShift project Syndesis is being deployed into"},
	EnvOpenShiftOauthClientSecret:     ConfigSpec{Generate: "expression", FromLen: 64, Required: true, Description: "OpenShift OAuth client secret"},
	EnvPostgresqlMemoryLimit:          ConfigSpec{Value: "255Mi", Description: "Maximum amount of memory the PostgreSQL container can use"},
	EnvPostgresqlImageStreamNamespace: ConfigSpec{Value: "openshift", Description: "The OpenShift Namespace where the PostgreSQL ImageStream resides"},
	EnvPostgresqlUser:                 ConfigSpec{Value: "syndesis", Description: "Username for PostgreSQL user that will be used for accessing the database"},
	EnvPostgresqlPassword:             ConfigSpec{Generate: "expression", FromLen: 16, Required: true, Description: "Password for the PostgreSQL connection user"},
	EnvPostgresqlDatabase:             ConfigSpec{Value: "syndesis", Required: true, Description: "Name of the PostgreSQL database accessed"},
	EnvPostgresqlVolumeCapacity:       ConfigSpec{Value: "1Gi", Required: true, Description: "Volume space available for PostgreSQL data, e.g. 512Mi, 2Gi"},
	EnvPostgresqlSampledbPassword:     ConfigSpec{Generate: "expression", FromLen: 16, Required: true, Description: "Password for the PostgreSQL sampledb user"},
	EnvTestSupport:                    ConfigSpec{Value: "false", Required: true, Description: "Enables test-support endpoint on backend API"},
	EnvOauthCookieSecret:              ConfigSpec{Generate: "expression", FromLen: 32, Description: "Secret to use to encrypt oauth cookies"},
	EnvSyndesisEncryptKey:             ConfigSpec{Generate: "expression", FromLen: 64, Required: true, Description: "The encryption key used to encrypt/decrypt stored secrets"},
	EnvPrometheusVolumeCapacity:       ConfigSpec{Value: "1Gi", Required: true, Description: "Volume space available for Prometheus data, e.g. 512Mi, 2Gi"},
	EnvPrometheusMemoryLimit:          ConfigSpec{Value: "512Mi", Required: true, Description: "Maximum amount of memory the Prometheus container can use"},
	EnvMetaVolumeCapacity:             ConfigSpec{Value: "1Gi", Required: true, Description: "Volume space available for Meta data, e.g. 512Mi, 2Gi"},
	EnvMetaMemoryLimit:                ConfigSpec{Value: "512Mi", Required: true, Description: "Maximum amount of memory the syndesis-meta service might use"},
	EnvServerMemoryLimit:              ConfigSpec{Value: "800Mi", Required: true, Description: "Maximum amount of memory the syndesis-server service might use"},
	EnvClientStateAuthenticationKey:   ConfigSpec{Generate: "expression", FromLen: 32, Required: true, Description: "Key used to perform authentication of client side stored state"},
	EnvClientStateEncryptionKey:       ConfigSpec{Generate: "expression", FromLen: 32, Required: true, Description: "Key used to perform encryption of client side stored state"},
	EnvImageStreamNamespace:           ConfigSpec{Value: EMPTY_FIELD, Description: "Namespace containing image streams"},
	EnvControllersIntegrationEnabled:  ConfigSpec{Value: "true", Description: "Should deployment of integrations be enabled?"},
	EnvSyndesisRegistry:               ConfigSpec{Value: "docker.io", Description: "Registry from where to fetch Syndesis images"},
	EnvDemoDataEnabled:                ConfigSpec{Value: "false", Required: true, Description: "Enables starting up with demo data"},
	EnvMaxIntegrationsPerUser:         ConfigSpec{Value: "1", Required: true, Description: "Maximum number of integrations single user can create"},
	EnvIntegrationStateCheckInterval:  ConfigSpec{Value: "60", Required: true, Description: "Interval for checking the state of the integrations"},
	EnvSarNamespace:                   ConfigSpec{Required: true, Description: "The user needs to have permissions to at least get a list of pods in the given project in order to be granted access to the Syndesis installation"},
	EnvKomodoMemoryLimit:              ConfigSpec{Value: "1024Mi", Required: true, Description: "Maximum amount of memory the data virtualization service might use"},
	EnvDatavirtEnabled:                ConfigSpec{Value: "0", Required: true, Description: "Set to 0 to disable data virtualization, set to 1 to enable data virtualization"},

	EnvSyndesisServerTag:   ConfigSpec{},
	EnvSyndesisUITag:       ConfigSpec{},
	EnvSyndesisS2ITag:      ConfigSpec{},
	EnvSyndesisMetaTag:     ConfigSpec{},
	EnvPostgresTag:         ConfigSpec{},
	EnvPostgresExporterTag: ConfigSpec{},
	EnvKomodoTag:           ConfigSpec{},
	EnvPrometheusTag:       ConfigSpec{},
	EnvOauthProxyTag:       ConfigSpec{},

	EnvUpgradeVolumeCapacity:  ConfigSpec{Value: "1Gi", Required: true, Description: "Volume space available for the upgrade process (backup data), e.g. 512Mi, 2Gi"},
	EnvManagementUrlFor3scale: ConfigSpec{Value: "", Description: "Url to 3scale for exposing services"},
}

func DefaultValue(envVar SyndesisEnvVar) string {
	return AllConfigOptions[envVar].Value
}

type SyndesisEnvVarSetter func(config map[string]string, syndesis *v1alpha1.Syndesis)

var (
	setters = []SyndesisEnvVarSetter{
		routeHostnameFromEnv,
		openshiftMasterFromEnv,
		openshiftConsoleUrlFromEnv,

		postgresqlMemoryLimitFromEnv,
		postgresqlImageStreamNamespaceFromEnv,
		postgresqlUserFromEnv,
		postgresqlDatabaseFromEnv,
		postgresqlVolumeCapacityFromEnv,

		testSupportFromEnv,

		prometheusVolumeCapacityFromEnv,
		prometheusMemoryLimitFromEnv,
		metaVolumeCapacityFromEnv,
		metaMemoryLimitFromEnv,
		serverMemoryLimitFromEnv,

		imageStreamNamespaceFromEnv,
		controllersIntegrationsEnabledFromEnv,
		syndesisRegistryFromEnv,
		demoDataEnabledFromEnv,
		maxIntegrationsPerUserFromEnv,
		integrationStateCheckInterval,

		komodoMemoryLimitFromEnv,
		sarNamespaceFromEnv,

		syndesisServerTagFromEnv,
		syndesisUITagFromEnv,
		syndesisS2ITagFromEnv,
		syndesisMetaTagFromEnv,

		postgresTagFromEnv,
		postgresExporterTagFromEnv,

		komodoTagFromEnv,
		oauthProxyTagFromEnv,
		prometheusTagFromEnv,

		upgradeVolumeCapacityFromEnv,
		managementUrlFor3scale,
	}
)

func SetConfigurationFromEnvVars(config map[string]string, syndesis *v1alpha1.Syndesis) {
	for _, setter := range setters {
		setter(config, syndesis)
	}
}

// Common
func routeHostnameFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvRouteHostname); ok && syndesis.Spec.RouteHostname == "" {
		syndesis.Spec.RouteHostname = v
	}
}

func openshiftMasterFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvOpenShiftMaster); ok && syndesis.Spec.OpenShiftMaster == "" {
		syndesis.Spec.OpenShiftMaster = v
	}
}

func openshiftConsoleUrlFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvOpenShiftConsoleUrl); ok && syndesis.Spec.OpenShiftConsoleUrl == "" {
		syndesis.Spec.OpenShiftConsoleUrl = v
	}
}

func sarNamespaceFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvSarNamespace); ok && syndesis.Spec.SarNamespace == "" {
		syndesis.Spec.SarNamespace = v
	}
}

func syndesisRegistryFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvSyndesisRegistry); ok && syndesis.Spec.Registry == "" {
		syndesis.Spec.Registry = v
	}
}

func syndesisServerTagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvSyndesisServerTag); ok && syndesis.Spec.Components.Server.Tag == "" {
		syndesis.Spec.Components.Server.Tag = v
	}
}

func syndesisMetaTagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvSyndesisMetaTag); ok && syndesis.Spec.Components.Meta.Tag == "" {
		syndesis.Spec.Components.Meta.Tag = v
	}
}

func syndesisUITagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvSyndesisUITag); ok && syndesis.Spec.Components.UI.Tag == "" {
		syndesis.Spec.Components.UI.Tag = v
	}
}

func syndesisS2ITagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvSyndesisS2ITag); ok && syndesis.Spec.Components.S2I.Tag == "" {
		syndesis.Spec.Components.S2I.Tag = v
	}
}

func postgresTagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvPostgresTag); ok && syndesis.Spec.Components.Db.Tag == "" {
		syndesis.Spec.Components.Db.Tag = v
	}
}

func postgresExporterTagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvPostgresExporterTag); ok && syndesis.Spec.Components.PostgresExporter.Tag == "" {
		syndesis.Spec.Components.PostgresExporter.Tag = v
	}
}

func komodoTagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvKomodoTag); ok && syndesis.Spec.Components.Komodo.Tag == "" {
		syndesis.Spec.Components.Komodo.Tag = v
	}
}

func oauthProxyTagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvOauthProxyTag); ok && syndesis.Spec.Components.Oauth.Tag == "" {
		syndesis.Spec.Components.Oauth.Tag = v
	}
}

func prometheusTagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvPrometheusTag); ok && syndesis.Spec.Components.Prometheus.Tag == "" {
		syndesis.Spec.Components.Prometheus.Tag = v
	}
}

// Komodo
func komodoMemoryLimitFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	resourceMemoryListFromEnv(&syndesis.Spec.Components.Komodo.Resources, config, EnvKomodoMemoryLimit)
}

func demoDataEnabledFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getBool(config, EnvDemoDataEnabled); ok && syndesis.Spec.DemoData == nil {
		syndesis.Spec.DemoData = &v
	}
}

func maxIntegrationsPerUserFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getInt(config, EnvMaxIntegrationsPerUser); ok && syndesis.Spec.Integration.Limit == nil {
		syndesis.Spec.Integration.Limit = &v
	}
}

func integrationStateCheckInterval(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getInt(config, EnvIntegrationStateCheckInterval); ok && syndesis.Spec.Integration.StateCheckInterval == nil {
		syndesis.Spec.Integration.StateCheckInterval = &v
	}
}

func controllersIntegrationsEnabledFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getBool(config, EnvControllersIntegrationEnabled); ok && syndesis.Spec.DeployIntegrations != nil {
		syndesis.Spec.DeployIntegrations = &v
	}
}

func testSupportFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getBool(config, EnvTestSupport); ok && syndesis.Spec.TestSupport == nil {
		syndesis.Spec.TestSupport = &v
	}
}

func imageStreamNamespaceFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvImageStreamNamespace); ok && syndesis.Spec.ImageStreamNamespace == "" {
		syndesis.Spec.ImageStreamNamespace = v
	}
}

func postgresqlMemoryLimitFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	resourceMemoryListFromEnv(&syndesis.Spec.Components.Db.Resources.Resources, config, EnvPostgresqlMemoryLimit)
}

func postgresqlImageStreamNamespaceFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvPostgresqlImageStreamNamespace); ok && syndesis.Spec.Components.Db.ImageStreamNamespace == "" {
		syndesis.Spec.Components.Db.ImageStreamNamespace = v
	}
}

func postgresqlUserFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvPostgresqlUser); ok && syndesis.Spec.Components.Db.User == "" {
		syndesis.Spec.Components.Db.User = v
	}
}

func postgresqlDatabaseFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvPostgresqlDatabase); ok && syndesis.Spec.Components.Db.Database == "" {
		syndesis.Spec.Components.Db.Database = v
	}
}

func postgresqlVolumeCapacityFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvPostgresqlVolumeCapacity); ok && syndesis.Spec.Components.Db.Resources.VolumeCapacity == "" {
		syndesis.Spec.Components.Db.Resources.VolumeCapacity = v
	}
}

// Prometheus
func prometheusMemoryLimitFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	resourceMemoryListFromEnv(&syndesis.Spec.Components.Prometheus.Resources.Resources, config, EnvPrometheusMemoryLimit)
}

func prometheusVolumeCapacityFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvPrometheusVolumeCapacity); ok && syndesis.Spec.Components.Prometheus.Resources.VolumeCapacity == "" {
		syndesis.Spec.Components.Prometheus.Resources.VolumeCapacity = v
	}
}

// Server
func serverMemoryLimitFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	resourceMemoryListFromEnv(&syndesis.Spec.Components.Server.Resources, config, EnvServerMemoryLimit)
}

func resourceMemoryListFromEnv(r *v1alpha1.Resources, config map[string]string, envVar SyndesisEnvVar) {
	if r.Limits == nil {
		r.Limits = make(v1.ResourceList, 0)
	}
	if v, found := getQuantity(config, envVar); found {
		if _, found := r.Limits[v1.ResourceMemory]; !found {
			r.Limits[v1.ResourceMemory] = v
		}
	}
}

// Meta
func metaMemoryLimitFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	resourceMemoryListFromEnv(&syndesis.Spec.Components.Meta.Resources.Resources, config, EnvMetaMemoryLimit)
}

func metaVolumeCapacityFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvMetaVolumeCapacity); ok && syndesis.Spec.Components.Meta.Resources.VolumeCapacity == "" {
		syndesis.Spec.Components.Meta.Resources.VolumeCapacity = v
	}
}

func upgradeVolumeCapacityFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvUpgradeVolumeCapacity); ok && syndesis.Spec.Components.Upgrade.Resources.VolumeCapacity == "" {
		syndesis.Spec.Components.Upgrade.Resources.VolumeCapacity = v
	}
}

func managementUrlFor3scale(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvManagementUrlFor3scale); ok && syndesis.Spec.Components.Server.Features.ManagementUrlFor3scale == "" {
		syndesis.Spec.Components.Server.Features.ManagementUrlFor3scale = v
	}
}
