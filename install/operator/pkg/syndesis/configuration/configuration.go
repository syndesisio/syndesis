package configuration

import (
	"strconv"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	v1 "k8s.io/api/core/v1"
)

type SyndesisEnvVar string

// Location from where the template configuration is located
var TemplateConfig string

const (
	EnvRouteHostname                  SyndesisEnvVar = "ROUTE_HOSTNAME"
	EnvOpenshiftMaster                SyndesisEnvVar = "OPENSHIFT_MASTER"
	EnvOpenshiftConsoleUrl            SyndesisEnvVar = "OPENSHIFT_CONSOLE_URL"
	EnvOpenshiftProject               SyndesisEnvVar = "OPENSHIFT_PROJECT"
	EnvOpenshiftOauthClientSecret     SyndesisEnvVar = "OPENSHIFT_OAUTH_CLIENT_SECRET"
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

	EnvUpgradeRegistry        SyndesisEnvVar = "UPGRADE_REGISTRY"
	EnvUpgradeVolumeCapacity  SyndesisEnvVar = "UPGRADE_VOLUME_CAPACITY"
	EnvManagementUrlFor3scale SyndesisEnvVar = "OPENSHIFT_MANAGEMENT_URL_FOR3SCALE"
)

type SyndesisEnvVarConfig struct {
	Var   SyndesisEnvVar
	Value string
}

type SyndesisEnvVarExtractor func(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig

type SyndesisEnvVarSetter func(config map[string]string, syndesis *v1alpha1.Syndesis)

var (
	extractors = []SyndesisEnvVarExtractor{
		envOpenshiftProject,
		envSarNamespace,
		envRouteHostname,
		envSyndesisRegistry,
		envDemoDataEnabled,
		envMaxIntegrationsPerUser,
		envIntegrationStateCheckInterval,
		envManagementUrlFor3scale,

		envControllersIntegrationsEnabled,
		envTestSupport,
		envImageStreamNamespace,
		envOpenShiftConsoleUrl,

		envPostgresqlMemoryLimit,
		envPostgresqlImageStreamNamespace,
		envPostgresqlUser,
		envPostgresqlDatabase,
		envPostgresqlVolumeCapacity,

		envPrometheusMemoryLimit,
		envPrometheusVolumeCapacity,

		envServerMemoryLimit,

		envMetaMemoryLimit,
		envMetaVolumeCapacity,

		envServerTag,
		envMetaTag,
		envUITag,
		envS2ITag,

		envPostgresTag,
		envPostgresExporterTag,
		envKomodoTag,
		envOauthProxyTag,
		envPrometheusTag,

		envUpgradeVolumeCapacity,
		envDatavirtEnabled,
	}

	setters = []SyndesisEnvVarSetter{
		routeHostnameFromEnv,
		syndesisRegistryFromEnv,
		demoDataEnabledFromEnv,
		maxIntegrationsPerUserFromEnv,
		integrationStateCheckInterval,
		managementUrlFor3scale,

		controllersIntegrationsEnabledFromEnv,
		testSupportFromEnv,
		imageStreamNamespaceFromEnv,
		openShiftConsoleUrlFromEnv,

		postgresqlMemoryLimitFromEnv,
		postgresqlImageStreamNamespaceFromEnv,
		postgresqlUserFromEnv,
		postgresqlDatabaseFromEnv,
		postgresqlVolumeCapacityFromEnv,

		prometheusMemoryLimitFromEnv,
		prometheusVolumeCapacityFromEnv,

		serverMemoryLimitFromEnv,

		metaMemoryLimitFromEnv,
		metaVolumeCapacityFromEnv,
		sarNamespaceFromEnv,

		upgradeVolumeCapacityFromEnv,

		syndesisServerTagFromEnv,
		syndesisUITagFromEnv,
		syndesisS2ITagFromEnv,
		syndesisMetaTagFromEnv,

		postgresTagFromEnv,
		postgresExporterTagFromEnv,
		komodoTagFromEnv,
		oauthProxyTagFromEnv,
		prometheusTagFromEnv,
	}
)

func GetEnvVars(syndesis *v1alpha1.Syndesis) map[string]string {
	configs := make(map[string]string)
	for _, extractor := range extractors {
		conf := extractor(syndesis)
		if conf != nil {
			configs[string(conf.Var)] = conf.Value
		}
	}
	return configs
}

func SetConfigurationFromEnvVars(config map[string]string, syndesis *v1alpha1.Syndesis) {
	for _, setter := range setters {
		setter(config, syndesis)
	}
}

// Common
func envRouteHostname(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if routeHost := syndesis.Spec.RouteHostname; routeHost != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvRouteHostname,
			Value: routeHost,
		}
	}
	return nil
}
func routeHostnameFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvRouteHostname); ok && syndesis.Spec.RouteHostname == "" {
		syndesis.Spec.RouteHostname = v
	}
}

func envOpenShiftConsoleUrl(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if consoleUrl := syndesis.Spec.OpenShiftConsoleUrl; consoleUrl != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvOpenshiftConsoleUrl,
			Value: consoleUrl,
		}
	}
	return nil
}
func openShiftConsoleUrlFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvOpenshiftConsoleUrl); ok && syndesis.Spec.OpenShiftConsoleUrl == "" {
		syndesis.Spec.OpenShiftConsoleUrl = v
	}
}

func envOpenshiftProject(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	return &SyndesisEnvVarConfig{
		Var:   EnvOpenshiftProject,
		Value: syndesis.Namespace,
	}
}

func sarNamespaceFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvSarNamespace); ok && syndesis.Spec.SarNamespace == "" {
		syndesis.Spec.SarNamespace = v
	}
}

func envSarNamespace(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	sarNamespace := syndesis.Spec.SarNamespace
	if sarNamespace == "" {
		sarNamespace = syndesis.Namespace
	}

	return &SyndesisEnvVarConfig{
		Var:   EnvSarNamespace,
		Value: sarNamespace,
	}
}

func envSyndesisRegistry(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if registry := syndesis.Spec.Registry; registry != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvSyndesisRegistry,
			Value: registry,
		}
	}
	return nil
}
func syndesisRegistryFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvSyndesisRegistry); ok && syndesis.Spec.Registry == "" {
		syndesis.Spec.Registry = v
	}
}

func envServerTag(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if tag := syndesis.Spec.Components.Server.Tag; tag != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvSyndesisServerTag,
			Value: tag,
		}
	}
	return nil
}
func syndesisServerTagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvSyndesisServerTag); ok && syndesis.Spec.Components.Server.Tag == "" {
		syndesis.Spec.Components.Server.Tag = v
	}
}

func envMetaTag(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if tag := syndesis.Spec.Components.Meta.Tag; tag != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvSyndesisMetaTag,
			Value: tag,
		}
	}
	return nil
}
func syndesisMetaTagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvSyndesisMetaTag); ok && syndesis.Spec.Components.Meta.Tag == "" {
		syndesis.Spec.Components.Meta.Tag = v
	}
}

func envUITag(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if tag := syndesis.Spec.Components.UI.Tag; tag != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvSyndesisUITag,
			Value: tag,
		}
	}
	return nil
}
func syndesisUITagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvSyndesisUITag); ok && syndesis.Spec.Components.UI.Tag == "" {
		syndesis.Spec.Components.UI.Tag = v
	}
}

func envS2ITag(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if tag := syndesis.Spec.Components.S2I.Tag; tag != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvSyndesisS2ITag,
			Value: tag,
		}
	}
	return nil
}
func syndesisS2ITagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvSyndesisS2ITag); ok && syndesis.Spec.Components.S2I.Tag == "" {
		syndesis.Spec.Components.S2I.Tag = v
	}
}

func envPostgresTag(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if tag := syndesis.Spec.Components.Db.Tag; tag != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvPostgresTag,
			Value: tag,
		}
	}
	return nil
}
func postgresTagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvPostgresTag); ok && syndesis.Spec.Components.Db.Tag == "" {
		syndesis.Spec.Components.Db.Tag = v
	}
}

func envPostgresExporterTag(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if tag := syndesis.Spec.Components.PostgresExporter.Tag; tag != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvPostgresExporterTag,
			Value: tag,
		}
	}
	return nil
}
func postgresExporterTagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvPostgresExporterTag); ok && syndesis.Spec.Components.PostgresExporter.Tag == "" {
		syndesis.Spec.Components.PostgresExporter.Tag = v
	}
}

func envKomodoTag(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if tag := syndesis.Spec.Components.Komodo.Tag; tag != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvKomodoTag,
			Value: tag,
		}
	}
	return nil
}
func komodoTagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvKomodoTag); ok && syndesis.Spec.Components.Komodo.Tag == "" {
		syndesis.Spec.Components.Komodo.Tag = v
	}
}

func envOauthProxyTag(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if tag := syndesis.Spec.Components.Oauth.Tag; tag != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvOauthProxyTag,
			Value: tag,
		}
	}
	return nil
}
func oauthProxyTagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvOauthProxyTag); ok && syndesis.Spec.Components.Oauth.Tag == "" {
		syndesis.Spec.Components.Oauth.Tag = v
	}
}

func envPrometheusTag(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if tag := syndesis.Spec.Components.Prometheus.Tag; tag != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvPrometheusTag,
			Value: tag,
		}
	}
	return nil
}
func prometheusTagFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvPrometheusTag); ok && syndesis.Spec.Components.Prometheus.Tag == "" {
		syndesis.Spec.Components.Prometheus.Tag = v
	}
}

func envDemoDataEnabled(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if demodata := syndesis.Spec.DemoData; demodata != nil {
		return &SyndesisEnvVarConfig{
			Var:   EnvDemoDataEnabled,
			Value: strconv.FormatBool(*demodata),
		}
	}
	return nil
}
func demoDataEnabledFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getBool(config, EnvDemoDataEnabled); ok && syndesis.Spec.DemoData == nil {
		syndesis.Spec.DemoData = &v
	}
}

func envMaxIntegrationsPerUser(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if integrations := syndesis.Spec.Integration.Limit; integrations != nil {
		return &SyndesisEnvVarConfig{
			Var:   EnvMaxIntegrationsPerUser,
			Value: strconv.Itoa(*integrations),
		}
	}
	return nil
}

func envIntegrationStateCheckInterval(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if i := syndesis.Spec.Integration.StateCheckInterval; i != nil {
		return &SyndesisEnvVarConfig{
			Var:   EnvIntegrationStateCheckInterval,
			Value: strconv.Itoa(*i),
		}
	}
	return nil
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

func envControllersIntegrationsEnabled(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if deploy := syndesis.Spec.DeployIntegrations; deploy != nil {
		return &SyndesisEnvVarConfig{
			Var:   EnvControllersIntegrationEnabled,
			Value: strconv.FormatBool(*deploy),
		}
	}
	return nil
}

func envTestSupport(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if deploy := syndesis.Spec.TestSupport; deploy != nil {
		return &SyndesisEnvVarConfig{
			Var:   EnvTestSupport,
			Value: strconv.FormatBool(*deploy),
		}
	}
	return nil
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

func envImageStreamNamespace(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if namespace := syndesis.Spec.ImageStreamNamespace; namespace != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvImageStreamNamespace,
			Value: namespace,
		}
	}
	return nil
}
func imageStreamNamespaceFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvImageStreamNamespace); ok && syndesis.Spec.ImageStreamNamespace == "" {
		syndesis.Spec.ImageStreamNamespace = v
	}
}

// Postgresql
func envPostgresqlMemoryLimit(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if limits := syndesis.Spec.Components.Db.Resources.Limits.Memory(); limits != nil && limits.Value() > 0 {
		return &SyndesisEnvVarConfig{
			Var:   EnvPostgresqlMemoryLimit,
			Value: limits.String(),
		}
	}
	return nil
}

func postgresqlMemoryLimitFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	resourceMemoryListFromEnv(&syndesis.Spec.Components.Db.Resources.Resources, config, EnvPostgresqlMemoryLimit)
}

func envPostgresqlImageStreamNamespace(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if ns := syndesis.Spec.Components.Db.ImageStreamNamespace; ns != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvPostgresqlImageStreamNamespace,
			Value: ns,
		}
	}
	return nil
}
func postgresqlImageStreamNamespaceFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvPostgresqlImageStreamNamespace); ok && syndesis.Spec.Components.Db.ImageStreamNamespace == "" {
		syndesis.Spec.Components.Db.ImageStreamNamespace = v
	}
}

func envPostgresqlUser(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if user := syndesis.Spec.Components.Db.User; user != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvPostgresqlUser,
			Value: user,
		}
	}
	return nil
}
func postgresqlUserFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvPostgresqlUser); ok && syndesis.Spec.Components.Db.User == "" {
		syndesis.Spec.Components.Db.User = v
	}
}

func envPostgresqlDatabase(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if database := syndesis.Spec.Components.Db.Database; database != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvPostgresqlDatabase,
			Value: database,
		}
	}
	return nil
}
func postgresqlDatabaseFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvPostgresqlDatabase); ok && syndesis.Spec.Components.Db.Database == "" {
		syndesis.Spec.Components.Db.Database = v
	}
}

func envPostgresqlVolumeCapacity(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if capacity := syndesis.Spec.Components.Db.Resources.VolumeCapacity; capacity != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvPostgresqlVolumeCapacity,
			Value: capacity,
		}
	}
	return nil
}
func postgresqlVolumeCapacityFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvPostgresqlVolumeCapacity); ok && syndesis.Spec.Components.Db.Resources.VolumeCapacity == "" {
		syndesis.Spec.Components.Db.Resources.VolumeCapacity = v
	}
}

// Prometheus
func envPrometheusMemoryLimit(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if limits := syndesis.Spec.Components.Prometheus.Resources.Limits.Memory(); limits != nil && limits.Value() > 0 {
		return &SyndesisEnvVarConfig{
			Var:   EnvPrometheusMemoryLimit,
			Value: limits.String(),
		}
	}
	return nil
}

func prometheusMemoryLimitFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	resourceMemoryListFromEnv(&syndesis.Spec.Components.Prometheus.Resources.Resources, config, EnvPrometheusMemoryLimit)
}

func envPrometheusVolumeCapacity(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if capacity := syndesis.Spec.Components.Prometheus.Resources.VolumeCapacity; capacity != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvPrometheusVolumeCapacity,
			Value: capacity,
		}
	}
	return nil
}
func prometheusVolumeCapacityFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvPrometheusVolumeCapacity); ok && syndesis.Spec.Components.Prometheus.Resources.VolumeCapacity == "" {
		syndesis.Spec.Components.Prometheus.Resources.VolumeCapacity = v
	}
}

// Server
func envServerMemoryLimit(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if limits := syndesis.Spec.Components.Server.Resources.Limits.Memory(); limits != nil && limits.Value() > 0 {
		return &SyndesisEnvVarConfig{
			Var:   EnvServerMemoryLimit,
			Value: limits.String(),
		}
	}
	return nil
}
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
func envMetaMemoryLimit(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if limits := syndesis.Spec.Components.Meta.Resources.Limits.Memory(); limits != nil && limits.Value() > 0 {
		return &SyndesisEnvVarConfig{
			Var:   EnvMetaMemoryLimit,
			Value: limits.String(),
		}
	}
	return nil
}
func metaMemoryLimitFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	resourceMemoryListFromEnv(&syndesis.Spec.Components.Meta.Resources.Resources, config, EnvMetaMemoryLimit)
}

func envMetaVolumeCapacity(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if capacity := syndesis.Spec.Components.Meta.Resources.VolumeCapacity; capacity != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvMetaVolumeCapacity,
			Value: capacity,
		}
	}
	return nil
}
func metaVolumeCapacityFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvMetaVolumeCapacity); ok && syndesis.Spec.Components.Meta.Resources.VolumeCapacity == "" {
		syndesis.Spec.Components.Meta.Resources.VolumeCapacity = v
	}
}

func envUpgradeVolumeCapacity(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if capacity := syndesis.Spec.Components.Upgrade.Resources.VolumeCapacity; capacity != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvUpgradeVolumeCapacity,
			Value: capacity,
		}
	}
	return nil
}

func upgradeVolumeCapacityFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvUpgradeVolumeCapacity); ok && syndesis.Spec.Components.Upgrade.Resources.VolumeCapacity == "" {
		syndesis.Spec.Components.Upgrade.Resources.VolumeCapacity = v
	}
}

func envManagementUrlFor3scale(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if url := syndesis.Spec.Components.Server.Features.ManagementUrlFor3scale; url != "" {
		return &SyndesisEnvVarConfig{
			Var:   EnvManagementUrlFor3scale,
			Value: url,
		}
	}
	return nil
}

func managementUrlFor3scale(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvManagementUrlFor3scale); ok {
		syndesis.Spec.Components.Server.Features.ManagementUrlFor3scale = v
	}
}

func envDatavirtEnabled(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	v := "0"
	if komodo := syndesis.Spec.Addons["komodo"]["enabled"]; komodo == "true" {
		v = "1"
	}
	return &SyndesisEnvVarConfig{
		Var:   EnvDatavirtEnabled,
		Value: v,
	}
}
