package template

import (
	"encoding/json"
	"fmt"
	"math/rand"
	"time"

	"k8s.io/apimachinery/pkg/api/resource"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	cf "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	v1 "k8s.io/api/core/v1"
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

func ifMissingGeneratePwd(config map[string]string, name cf.SyndesisEnvVar) {
	if value, found := config[string(name)]; !found || value == "" {
		size := cf.AllConfigOptions[name].FromLen
		config[string(name)] = randomPassword(size)
	}
}

func ifMissingSet(str *string, value string) {
	if str != nil && *str == "" {
		*str = value
	}
}

func ifMissingSetResource(list v1.ResourceList, name string, value string) {
	if _, found := list[v1.ResourceName(name)]; !found {
		q, err := resource.ParseQuantity(value)
		if err != nil {
			panic(err)
		}
		list[v1.ResourceName(name)] = q
	}
}

func GetTemplateContext() (*generator.Context, error) {
	templateConfig, err := util.LoadJsonFromFile(cf.TemplateConfig)
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

func SetupRenderContext(renderContext *generator.Context, syndesis *v1alpha1.Syndesis, env map[string]string) error {

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

	ifMissingGeneratePwd(config, cf.EnvOpenShiftOauthClientSecret)
	ifMissingGeneratePwd(config, cf.EnvPostgresqlPassword)
	ifMissingGeneratePwd(config, cf.EnvPostgresqlSampledbPassword)
	ifMissingGeneratePwd(config, cf.EnvOauthCookieSecret)
	ifMissingGeneratePwd(config, cf.EnvSyndesisEncryptKey)
	ifMissingGeneratePwd(config, cf.EnvClientStateAuthenticationKey)
	ifMissingGeneratePwd(config, cf.EnvClientStateEncryptionKey)
	cf.SetConfigurationFromEnvVars(config, syndesis)

	ifMissingSet(&syndesis.Spec.OpenShiftMaster, cf.DefaultValue(cf.EnvOpenShiftMaster))
	ifMissingSet(&syndesis.Spec.Components.Db.ImageStreamNamespace, cf.DefaultValue(cf.EnvPostgresqlImageStreamNamespace))
	ifMissingSet(&syndesis.Spec.Components.Db.User, cf.DefaultValue(cf.EnvPostgresqlUser))
	ifMissingSet(&syndesis.Spec.Components.Db.Database, cf.DefaultValue(cf.EnvPostgresqlDatabase))

	if syndesis.Spec.TestSupport == nil {
		v := false
		syndesis.Spec.TestSupport = &v
	}
	if syndesis.Spec.DemoData == nil {
		v := false
		syndesis.Spec.DemoData = &v
	}

	ifMissingSet(&syndesis.Spec.Components.Meta.Image, renderContext.SpecDefaults.Components.Meta.Image)
	ifMissingSet(&syndesis.Spec.Components.Server.Image, renderContext.SpecDefaults.Components.Server.Image)
	ifMissingSet(&syndesis.Spec.Components.UI.Image, renderContext.SpecDefaults.Components.UI.Image)
	ifMissingSet(&syndesis.Spec.Components.UI.Image, renderContext.SpecDefaults.Components.UI.Image)
	ifMissingSet(&syndesis.Spec.Components.S2I.Image, renderContext.SpecDefaults.Components.S2I.Image)
	ifMissingSet(&syndesis.Spec.Components.Db.Image, renderContext.SpecDefaults.Components.Db.Image)
	ifMissingSet(&syndesis.Spec.Components.PostgresExporter.Image, renderContext.SpecDefaults.Components.PostgresExporter.Image)
	ifMissingSet(&syndesis.Spec.Components.Komodo.Image, renderContext.SpecDefaults.Components.Komodo.Image)
	ifMissingSet(&syndesis.Spec.Components.Oauth.Image, renderContext.SpecDefaults.Components.Oauth.Image)
	ifMissingSet(&syndesis.Spec.Components.Prometheus.Image, renderContext.SpecDefaults.Components.Prometheus.Image)
	ifMissingSet(&syndesis.Spec.Components.Upgrade.Image, renderContext.SpecDefaults.Components.Upgrade.Image)

	if syndesis.Spec.DeployIntegrations == nil {
		v := true
		syndesis.Spec.DeployIntegrations = &v
	}
	ifMissingSet(&syndesis.Spec.ImageStreamNamespace, renderContext.SpecDefaults.ImageStreamNamespace)

	ifMissingSet(&syndesis.Spec.Components.Prometheus.Resources.VolumeCapacity, cf.DefaultValue(cf.EnvPrometheusVolumeCapacity))
	ifMissingSet(&syndesis.Spec.Components.Meta.Resources.VolumeCapacity, cf.DefaultValue(cf.EnvMetaVolumeCapacity))
	ifMissingSet(&syndesis.Spec.Components.Db.Resources.VolumeCapacity, cf.DefaultValue(cf.EnvPostgresqlVolumeCapacity))

	ifMissingSetResource(syndesis.Spec.Components.Prometheus.Resources.Resources.Limits, "memory", cf.DefaultValue(cf.EnvPrometheusMemoryLimit))
	ifMissingSetResource(syndesis.Spec.Components.Db.Resources.Resources.Limits, "memory", cf.DefaultValue(cf.EnvPostgresqlMemoryLimit))
	ifMissingSetResource(syndesis.Spec.Components.Meta.Resources.Resources.Limits, "memory", cf.DefaultValue(cf.EnvMetaMemoryLimit))
	ifMissingSetResource(syndesis.Spec.Components.Server.Resources.Limits, "memory", cf.DefaultValue(cf.EnvServerMemoryLimit))
	ifMissingSetResource(syndesis.Spec.Components.Komodo.Resources.Limits, "memory", cf.DefaultValue(cf.EnvKomodoMemoryLimit))

	if syndesis.Spec.Integration.Limit == nil {
		maxIntegrations := 0
		syndesis.Spec.Integration.Limit = &maxIntegrations
	}
	if syndesis.Spec.Integration.StateCheckInterval == nil {
		v := 60
		syndesis.Spec.Integration.StateCheckInterval = &v
	}

	ifMissingSet(&syndesis.Spec.Components.Upgrade.Resources.VolumeCapacity, cf.DefaultValue(cf.EnvUpgradeVolumeCapacity))
	if syndesis.Namespace == "" {
		return fmt.Errorf("required config var not set: %s", cf.EnvOpenShiftProject)
	}
	if syndesis.Spec.SarNamespace == "" {
		syndesis.Spec.SarNamespace = syndesis.Namespace
	}

	// Copy Maven settings
	if len(syndesis.Spec.MavenRepositories) == 0 {
		syndesis.Spec.MavenRepositories = renderContext.SpecDefaults.MavenRepositories
	}

	renderContext.Syndesis = syndesis
	renderContext.Env = config
	return nil
}

func copyMap(dst map[string]string, src map[string]string) {
	for key, value := range src {
		dst[key] = value
	}
}
