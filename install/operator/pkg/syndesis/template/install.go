package template

import (
	"encoding/json"
	"fmt"
	"math/rand"
	"time"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/runtime"
)

var random = rand.New(rand.NewSource(time.Now().UnixNano()))

type ResourceParams struct {
	OAuthClientSecret string
	UpgradeRegistry   string
}

func randomPassword(size int) string {
	alphabet := make([]rune, (26*2)+10)
	i := 0
	for c := 'a'; c <= 'z'; c++ {
		alphabet[i] = c
		i += 1
	}
	for c := 'A'; c <= 'Z'; c++ {
		alphabet[i] = c
		i += 1
	}
	for c := '0'; c <= '9'; c++ {
		alphabet[i] = c
		i += 1
	}

	result := make([]rune, size)
	for i := 0; i < size; i++ {
		result[i] = alphabet[random.Intn(len(alphabet))]
	}
	s := string(result)
	return s
}

func ifMissingGeneratePwd(config map[string]string, name configuration.SyndesisEnvVar, size int) {
	if value, found := config[string(name)]; !found || value == "" {
		config[string(name)] = randomPassword(size)
	}
}

func ifMissingSet(config map[string]string, name configuration.SyndesisEnvVar, value string) {
	if _, found := config[string(name)]; !found || value == "" {
		config[string(name)] = value
	}
}

func GetTemplateContext() (*generator.Context, error) {
	templateConfig, err := util.LoadJsonFromFile(configuration.TemplateConfig)
	if err != nil {
		return nil, err
	}

	// Parse the config
	gen := &generator.Context{}
	err = json.Unmarshal(templateConfig, gen)
	if err != nil {
		return nil, err
	}
	return gen, nil
}

// Each operator instance is bound to a single version currently that can be retrieved from this method.
func GetSyndesisVersionFromOperatorTemplate(scheme *runtime.Scheme) (string, error) {
	ctx, err := GetTemplateContext()
	if err != nil {
		return "", err
	}
	return ctx.Tags.Syndesis, nil
}

func SetupRenderContext(renderContext *generator.Context, syndesis *v1alpha1.Syndesis, params ResourceParams, env map[string]string) error {

	// Lets fill in all the addons we know about...
	if syndesis.Spec.Addons == nil {
		syndesis.Spec.Addons = v1alpha1.AddonsSpec{}
	}

	addonFiles, err := generator.GetAssetsFS().Open("./addons/")
	if err != nil {
		return err
	}
	defer addonFiles.Close()
	addonFileInfos, err := addonFiles.Readdir(-1)
	if err != nil {
		return err
	}
	for _, f := range addonFileInfos {
		if !f.IsDir() {
			continue
		}

		if syndesis.Spec.Addons[f.Name()] == nil {
			syndesis.Spec.Addons[f.Name()] = v1alpha1.Parameters{}
		}

		params := syndesis.Spec.Addons[f.Name()]
		if params["enabled"] != "" {
			continue
		}

		switch f.Name() {
		case "todo":
			params["enabled"] = "true"
		default:
			params["enabled"] = "false"
		}
	}

	// Setup the config..
	config := make(map[string]string)
	copyMap(config, env)
	copyMap(config, configuration.GetEnvVars(syndesis))
	config[string(configuration.EnvOpenshiftOauthClientSecret)] = params.OAuthClientSecret

	ifMissingGeneratePwd(config, configuration.EnvOpenshiftOauthClientSecret, 64)
	ifMissingGeneratePwd(config, configuration.EnvPostgresqlPassword, 16)
	ifMissingGeneratePwd(config, configuration.EnvPostgresqlSampledbPassword, 16)
	ifMissingGeneratePwd(config, configuration.EnvOauthCookieSecret, 32)
	ifMissingGeneratePwd(config, configuration.EnvSyndesisEncryptKey, 64)
	ifMissingGeneratePwd(config, configuration.EnvClientStateAuthenticationKey, 32)
	ifMissingGeneratePwd(config, configuration.EnvClientStateEncryptionKey, 32)

	ifMissingSet(config, configuration.EnvOpenshiftMaster, "https://localhost:8443")
	ifMissingSet(config, configuration.EnvPostgresqlMemoryLimit, "255Mi")
	ifMissingSet(config, configuration.EnvPostgresqlImageStreamNamespace, "openshift")
	ifMissingSet(config, configuration.EnvPostgresqlUser, "syndesis")
	ifMissingSet(config, configuration.EnvPostgresqlDatabase, "syndesis")

	ifMissingSet(config, configuration.EnvPostgresqlVolumeCapacity, "1Gi")
	ifMissingSet(config, configuration.EnvTestSupport, "false")
	ifMissingSet(config, configuration.EnvDemoDataEnabled, "false")

	ifMissingSet(config, configuration.EnvSyndesisMetaTag, renderContext.Tags.Syndesis)
	ifMissingSet(config, configuration.EnvSyndesisServerTag, renderContext.Tags.Syndesis)
	ifMissingSet(config, configuration.EnvSyndesisUITag, renderContext.Tags.Syndesis)
	ifMissingSet(config, configuration.EnvSyndesisS2ITag, renderContext.Tags.Syndesis)

	ifMissingSet(config, configuration.EnvPostgresTag, renderContext.Tags.Postgresql)
	ifMissingSet(config, configuration.EnvPostgresExporterTag, renderContext.Tags.PostgresExporter)
	ifMissingSet(config, configuration.EnvKomodoTag, renderContext.Tags.Komodo)
	ifMissingSet(config, configuration.EnvOauthProxyTag, renderContext.Tags.OAuthProxy)
	ifMissingSet(config, configuration.EnvPrometheusTag, renderContext.Tags.Prometheus)

	ifMissingSet(config, configuration.EnvSyndesisRegistry, renderContext.Registry)

	ifMissingSet(config, configuration.EnvControllersIntegrationEnabled, "true")
	ifMissingSet(config, configuration.EnvImageStreamNamespace, renderContext.Images.ImageStreamNamespace)
	ifMissingSet(config, configuration.EnvPrometheusVolumeCapacity, "1Gi")
	ifMissingSet(config, configuration.EnvPrometheusMemoryLimit, "512Mi")
	ifMissingSet(config, configuration.EnvMetaVolumeCapacity, "1Gi")
	ifMissingSet(config, configuration.EnvMetaMemoryLimit, "512Mi")
	ifMissingSet(config, configuration.EnvServerMemoryLimit, "800Mi")
	ifMissingSet(config, configuration.EnvKomodoMemoryLimit, "1024Mi")
	maxIntegrations := "0"
	if renderContext.Ocp {
		maxIntegrations = "1"
	}
	ifMissingSet(config, configuration.EnvMaxIntegrationsPerUser, maxIntegrations)
	ifMissingSet(config, configuration.EnvIntegrationStateCheckInterval, "60")
	ifMissingSet(config, configuration.EnvUpgradeVolumeCapacity, "1Gi")
	if config[string(configuration.EnvOpenshiftProject)] == "" {
		return fmt.Errorf("required config var not set: %s", configuration.EnvOpenshiftProject)
	}
	if config[string(configuration.EnvSarNamespace)] == "" {
		return fmt.Errorf("required config var not set: %s", configuration.EnvSarNamespace)
	}

	//
	// Apply DevSupport flag value to Debug
	//
	// If using dev images then doing development so
	// useful to have JAVA_DEBUG set to allow for image debugging
	//
	renderContext.Debug = syndesis.Spec.DevSupport

	renderContext.Syndesis = syndesis
	renderContext.Env = config
	return nil
}

func copyMap(dst map[string]string, src map[string]string) {
	for key, value := range src {
		dst[key] = value
	}
}
