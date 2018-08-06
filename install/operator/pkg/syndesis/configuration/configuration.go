package configuration

import (
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"k8s.io/api/core/v1"
	"strconv"
)

type SyndesisEnvVar string

// Location from where the template should be loaded
var TemplateLocation *string

const (
	EnvRouteHostname 					SyndesisEnvVar = "ROUTE_HOSTNAME"
	//EnvOpenshiftMaster 					SyndesisEnvVar = "OPENSHIFT_MASTER"
	EnvOpenshiftConsoleUrl				SyndesisEnvVar = "OPENSHIFT_CONSOLE_URL"
	EnvOpenshiftProject					SyndesisEnvVar = "OPENSHIFT_PROJECT"
	EnvOpenshiftOauthClientSecret		SyndesisEnvVar = "OPENSHIFT_OAUTH_CLIENT_SECRET"
	EnvPostgresqlMemoryLimit			SyndesisEnvVar = "POSTGRESQL_MEMORY_LIMIT"
	EnvPostgresqlImageStreamNamespace	SyndesisEnvVar = "POSTGRESQL_IMAGE_STREAM_NAMESPACE"
	EnvPostgresqlUser					SyndesisEnvVar = "POSTGRESQL_USER"
	//EnvPostgresqlPassword				SyndesisEnvVar = "POSTGRESQL_PASSWORD"
	EnvPostgresqlDatabase				SyndesisEnvVar = "POSTGRESQL_DATABASE"
	EnvPostgresqlVolumeCapacity			SyndesisEnvVar = "POSTGRESQL_VOLUME_CAPACITY"
	//EnvPostgresqlSampledbPassword		SyndesisEnvVar = "POSTGRESQL_SAMPLEDB_PASSWORD"
	EnvTestSupport				SyndesisEnvVar = "TEST_SUPPORT_ENABLED"
	//EnvOauthCookieSecret				SyndesisEnvVar = "OAUTH_COOKIE_SECRET"
	//EnvSyndesisEncryptKey				SyndesisEnvVar = "SYNDESIS_ENCRYPT_KEY"
	EnvPrometheusVolumeCapacity			SyndesisEnvVar = "PROMETHEUS_VOLUME_CAPACITY"
	EnvPrometheusMemoryLimit			SyndesisEnvVar = "PROMETHEUS_MEMORY_LIMIT"
	EnvMetaVolumeCapacity				SyndesisEnvVar = "META_VOLUME_CAPACITY"
	EnvMetaMemoryLimit					SyndesisEnvVar = "META_MEMORY_LIMIT"
	EnvServerMemoryLimit				SyndesisEnvVar = "SERVER_MEMORY_LIMIT"
	//EnvClientStateAuthenticationKey		SyndesisEnvVar = "CLIENT_STATE_AUTHENTICATION_KEY"
	//EnvClientStateEncryptionKey			SyndesisEnvVar = "CLIENT_STATE_ENCRYPTION_KEY"
	EnvImageStreamNamespace          SyndesisEnvVar = "IMAGE_STREAM_NAMESPACE"
	EnvControllersIntegrationEnabled SyndesisEnvVar = "CONTROLLERS_INTEGRATION_ENABLED"
	EnvSyndesisRegistry              SyndesisEnvVar = "SYNDESIS_REGISTRY"
	EnvDemoDataEnabled               SyndesisEnvVar = "DEMO_DATA_ENABLED"
	EnvMaxIntegrationsPerUser        SyndesisEnvVar = "MAX_INTEGRATIONS_PER_USER"
	EnvIntegrationStateCheckInterval SyndesisEnvVar = "INTEGRATION_STATE_CHECK_INTERVAL"

	EnvSyndesisVersion 					SyndesisEnvVar = "SYNDESIS_VERSION"
)

type SyndesisEnvVarConfig struct {
	Var		SyndesisEnvVar
	Value	string
}

type SyndesisEnvVarExtractor func(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig

type SyndesisEnvVarSetter func(config map[string]string, syndesis *v1alpha1.Syndesis)


var (
	extractors = []SyndesisEnvVarExtractor {
		envOpenshiftProject,
		envRouteHostname,
		envSyndesisRegistry,
		envDemoDataEnabled,
		envMaxIntegrationsPerUser,
		envIntegrationStateCheckInterval,

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
	}

	setters = []SyndesisEnvVarSetter {
		routeHostnameFromEnv,
		syndesisRegistryFromEnv,
		demoDataEnabledFromEnv,
		maxIntegrationsPerUserFromEnv,
		integrationStateCheckInterval,

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
	if routeHost := syndesis.Spec.RouteHostName; routeHost != "" {
		return &SyndesisEnvVarConfig{
			Var: EnvRouteHostname,
			Value: routeHost,
		}
	}
	return nil
}
func routeHostnameFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvRouteHostname); ok {
		syndesis.Spec.RouteHostName = v
	}
}

func envOpenShiftConsoleUrl(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if consoleUrl := syndesis.Spec.OpenShiftConsoleUrl; consoleUrl != "" {
		return &SyndesisEnvVarConfig{
			Var: EnvOpenshiftConsoleUrl,
			Value: consoleUrl,
		}
	}
	return nil
}
func openShiftConsoleUrlFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvOpenshiftConsoleUrl); ok {
		syndesis.Spec.OpenShiftConsoleUrl = v
	}
}

func envOpenshiftProject(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	return &SyndesisEnvVarConfig{
		Var: EnvOpenshiftProject,
		Value: syndesis.Namespace,
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
	if v, ok := getString(config, EnvSyndesisRegistry); ok {
		syndesis.Spec.Registry = v
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
	if v, ok := getBool(config, EnvDemoDataEnabled); ok {
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
	if v, ok := getInt(config, EnvMaxIntegrationsPerUser); ok {
		syndesis.Spec.Integration.Limit = &v
	}
}

func integrationStateCheckInterval(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getInt(config, EnvIntegrationStateCheckInterval); ok {
		syndesis.Spec.Integration.StateCheckInterval = &v
	}
}

func envControllersIntegrationsEnabled(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if deploy := syndesis.Spec.DeployIntegrations; deploy != nil {
		return &SyndesisEnvVarConfig{
			Var: EnvControllersIntegrationEnabled,
			Value: strconv.FormatBool(*deploy),
		}
	}
	return nil
}

func envTestSupport(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if deploy := syndesis.Spec.TestSupport; deploy != nil {
		return &SyndesisEnvVarConfig{
			Var: EnvTestSupport,
			Value: strconv.FormatBool(*deploy),
		}
	}
	return nil
}

func controllersIntegrationsEnabledFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getBool(config, EnvControllersIntegrationEnabled); ok {
		syndesis.Spec.DeployIntegrations = &v
	}
}

func testSupportFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getBool(config, EnvTestSupport); ok {
		syndesis.Spec.TestSupport = &v
	}
}

func envImageStreamNamespace(syndesis *v1alpha1.Syndesis) *SyndesisEnvVarConfig {
	if namespace := syndesis.Spec.ImageStreamNamespace; namespace != "" {
		return &SyndesisEnvVarConfig{
			Var: EnvImageStreamNamespace,
			Value: namespace,
		}
	}
	return nil
}
func imageStreamNamespaceFromEnv(config map[string]string, syndesis *v1alpha1.Syndesis) {
	if v, ok := getString(config, EnvImageStreamNamespace); ok {
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
	if v, ok := getQuantity(config, EnvPostgresqlMemoryLimit); ok {
		if syndesis.Spec.Components.Db.Resources.Limits == nil {
			syndesis.Spec.Components.Db.Resources.Limits = make(v1.ResourceList, 0)
		}
		syndesis.Spec.Components.Db.Resources.Limits[v1.ResourceMemory]=v
	}
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
	if v, ok := getString(config, EnvPostgresqlImageStreamNamespace); ok {
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
	if v, ok := getString(config, EnvPostgresqlUser); ok {
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
	if v, ok := getString(config, EnvPostgresqlDatabase); ok {
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
	if v, ok := getString(config, EnvPostgresqlVolumeCapacity); ok {
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
	if v, ok := getQuantity(config, EnvPrometheusMemoryLimit); ok {
		if syndesis.Spec.Components.Prometheus.Resources.Limits == nil {
			syndesis.Spec.Components.Prometheus.Resources.Limits = make(v1.ResourceList, 0)
		}
		syndesis.Spec.Components.Prometheus.Resources.Limits[v1.ResourceMemory]=v
	}
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
	if v, ok := getString(config, EnvPrometheusVolumeCapacity); ok {
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
	if v, ok := getQuantity(config, EnvServerMemoryLimit); ok {
		if syndesis.Spec.Components.Server.Resources.Limits == nil {
			syndesis.Spec.Components.Server.Resources.Limits = make(v1.ResourceList, 0)
		}
		syndesis.Spec.Components.Server.Resources.Limits[v1.ResourceMemory]=v
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
	if v, ok := getQuantity(config, EnvMetaMemoryLimit); ok {
		if syndesis.Spec.Components.Meta.Resources.Limits == nil {
			syndesis.Spec.Components.Meta.Resources.Limits = make(v1.ResourceList, 0)
		}
		syndesis.Spec.Components.Meta.Resources.Limits[v1.ResourceMemory]=v
	}
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
	if v, ok := getString(config, EnvMetaVolumeCapacity); ok {
		syndesis.Spec.Components.Meta.Resources.VolumeCapacity = v
	}
}

