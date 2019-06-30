package template

import (
	"encoding/json"
	"fmt"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime"
	"math/rand"
	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"
	"time"
)

var log = logf.Log.WithName("template")
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
	templateConfig, err := util.LoadJsonFromFile(*configuration.TemplateConfig)
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

func GetRenderContext(syndesis *v1alpha1.Syndesis, params ResourceParams) (*generator.Context, error) {
	// Parse the config
	renderContext, err := GetTemplateContext()
	if err != nil {
		return nil, err
	}

	// Setup the config..
	config := configuration.GetEnvVars(syndesis)
	config[string(configuration.EnvOpenshiftOauthClientSecret)] = params.OAuthClientSecret
	if _, ok := syndesis.Spec.Addons["komodo"]; ok {
		config["DATAVIRT_ENABLED"] = "1"
	}

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

	ifMissingSet(config, configuration.EnvSyndesisRegistry, renderContext.Registry)

	ifMissingSet(config, configuration.EnvControllersIntegrationEnabled, "true")
	ifMissingSet(config, configuration.EnvImageStreamNamespace, renderContext.Images.ImageStreamNamespace)
	ifMissingSet(config, configuration.EnvPrometheusVolumeCapacity, "1Gi")
	ifMissingSet(config, configuration.EnvPrometheusMemoryLimit, "512Mi")
	ifMissingSet(config, configuration.EnvMetaVolumeCapacity, "1Gi")
	ifMissingSet(config, configuration.EnvMetaMemoryLimit, "512Mi")
	ifMissingSet(config, configuration.EnvServerMemoryLimit, "800Mi")
	ifMissingSet(config, configuration.EnvKomodoMemoryLimit, "1024Mi")
	ifMissingSet(config, configuration.EnvDatavirtEnabled, "0")
	maxIntegrations := "0"
	if renderContext.Ocp {
		maxIntegrations = "1"
	}
	ifMissingSet(config, configuration.EnvMaxIntegrationsPerUser, maxIntegrations)
	ifMissingSet(config, configuration.EnvIntegrationStateCheckInterval, "60")
	ifMissingSet(config, configuration.EnvUpgradeVolumeCapacity, "1Gi")
	ifMissingSet(config, configuration.EnvExposeVia3Scale, "false")
	if config[string(configuration.EnvOpenshiftProject)] == "" {
		return nil, fmt.Errorf("required config var not set: %s", configuration.EnvOpenshiftProject)
	}
	if config[string(configuration.EnvSarNamespace)] == "" {
		return nil, fmt.Errorf("required config var not set: %s", configuration.EnvSarNamespace)
	}

	renderContext.Syndesis = syndesis.DeepCopy()
	renderContext.Env = config
	return renderContext, nil
}

func GetInstallResources(scheme *runtime.Scheme, syndesis *v1alpha1.Syndesis, params ResourceParams) ([]unstructured.Unstructured, error) {
	renderContext, err := GetRenderContext(syndesis, params)
	if err != nil {
		return nil, err
	}

	// Render the files in the install directory
	res, err := generator.RenderDir("./install/", renderContext)
	if err != nil {
		return nil, err
	}

	return res, nil
}
