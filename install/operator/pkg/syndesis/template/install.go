package template

import (
	"encoding/json"
	"fmt"
	"github.com/openshift/api/template/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/openshift/template"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/runtime"
	"math/rand"
	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"
	"time"
)

var log = logf.Log.WithName("template")
var random = rand.New(rand.NewSource(time.Now().UnixNano()))

type InstallParams struct {
	OAuthClientSecret string
}

func GetInstallResourcesAsRuntimeObjects(scheme *runtime.Scheme, syndesis *v1alpha1.Syndesis, params InstallParams) ([]runtime.Object, error) {
	rawExtensions, err := GetInstallResources(scheme, syndesis, params)
	if err != nil {
		return nil, err
	}

	objects := make([]runtime.Object, 0)

	for _, rawObj := range rawExtensions {
		res, err := util.LoadResourceFromYaml(scheme, rawObj.Raw)
		if err != nil {
			return nil, err
		}
		objects = append(objects, res)
	}

	if log.V(5).Enabled() {
		log.V(5).Info("Number of objects to create", "number_of_objects", len(objects))
		for _, obj := range objects {
			log.V(5).Info("Object to create", "object", obj)
		}
	}

	return objects, nil
}

func randomPassword(size int) string {
	alphabet := make([]byte, (26*2)+10)
	for i := 0; i < 26; i++ {
		alphabet[i] = 'a'+1
	}
	for i := 0; i < 26; i++ {
		alphabet[i] = 'A'+1
	}
	for i := 0; i < 10; i++ {
		alphabet[i] = '0'+1
	}

	result := make([]byte, size)
	for i := 0; i < size; i++ {
		result[i] = alphabet[random.Intn(len(alphabet))]
	}
	return string(result)
}

func ifMissingGeneratePwd(config map[string]string, name configuration.SyndesisEnvVar, size int){
	if value, found := config[string(name)]; !found || value=="" {
		config[string(name)] = randomPassword(size)
	}
}

func ifMissingSet(config map[string]string, name configuration.SyndesisEnvVar, value string){
	if _, found := config[string(name)]; !found || value=="" {
		config[string(name)] = value
	}
}

func GetInstallResources(scheme *runtime.Scheme, syndesis *v1alpha1.Syndesis, params InstallParams) ([]runtime.RawExtension, error) {

	// Load the template config..
	templateConfig, err := util.LoadJsonFromFile(*configuration.TemplateConfig)
	if err != nil {
		return nil, err
	}

	// Parse the config
	gen := generator.Context{}
	err = json.Unmarshal(templateConfig, &gen)
	if err != nil {
		return nil, err
	}

	// Generate the OpenShift Template
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
	ifMissingSet(config, configuration.EnvPostgresqlVolumeCapacity, "1Gi")
	ifMissingSet(config, configuration.EnvTestSupport, "false")
	ifMissingSet(config, configuration.EnvDemoDataEnabled, "false")
	ifMissingSet(config, configuration.EnvSyndesisRegistry, gen.Registry)
	ifMissingSet(config, configuration.EnvControllersIntegrationEnabled, "true")
	ifMissingSet(config, configuration.EnvImageStreamNamespace, gen.Images.ImageStreamNamespace)
	ifMissingSet(config, configuration.EnvPrometheusVolumeCapacity, "1Gi")
	ifMissingSet(config, configuration.EnvPrometheusMemoryLimit, "512Mi")
	ifMissingSet(config, configuration.EnvMetaVolumeCapacity, "1Gi")
	ifMissingSet(config, configuration.EnvMetaMemoryLimit, "512Mi")
	ifMissingSet(config, configuration.EnvServerMemoryLimit, "800Mi")
	ifMissingSet(config, configuration.EnvKomodoMemoryLimit, "1024Mi")
	ifMissingSet(config, configuration.EnvDatavirtEnabled, "0")
	maxIntegrations := "0"
	if gen.Ocp {
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

	gen.Params = config
	generated, err := gen.GenerateResources()
	if err != nil {
		return nil, err
	}

	// Parse the template
	res, err := util.LoadRawResourceFromYaml(generated)
	if err != nil {
		return nil, err
	}

	templ := res.(*v1.Template)
	processor, err := template.NewTemplateProcessor(scheme, syndesis.Namespace)
	if err != nil {
		return nil, err
	}

	return processor.Process(templ, config)
}

