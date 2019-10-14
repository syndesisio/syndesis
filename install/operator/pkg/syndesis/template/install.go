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
	if secret, ok := config[string(cf.EnvOpenShiftOauthClientSecret)]; !ok || secret == "" {
		config[string(cf.EnvOpenShiftOauthClientSecret)] = params.OAuthClientSecret
	}

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

	ifMissingSet(&syndesis.Spec.Components.Meta.Tag, renderContext.Tags.Syndesis)
	ifMissingSet(&syndesis.Spec.Components.Server.Tag, renderContext.Tags.Syndesis)
	ifMissingSet(&syndesis.Spec.Components.UI.Tag, renderContext.Tags.Syndesis)
	ifMissingSet(&syndesis.Spec.Components.S2I.Tag, renderContext.Tags.Syndesis)

	ifMissingSet(&syndesis.Spec.Components.Db.Tag, renderContext.Tags.Postgresql)
	ifMissingSet(&syndesis.Spec.Components.PostgresExporter.Tag, renderContext.Tags.PostgresExporter)
	ifMissingSet(&syndesis.Spec.Components.Komodo.Tag, renderContext.Tags.Komodo)
	ifMissingSet(&syndesis.Spec.Components.Oauth.Tag, renderContext.Tags.OAuthProxy)
	ifMissingSet(&syndesis.Spec.Components.Prometheus.Tag, renderContext.Tags.Prometheus)

	ifMissingSet(&syndesis.Spec.Registry, renderContext.Registry)
	ifMissingSet(&syndesis.Spec.Components.Meta.Registry, syndesis.Spec.Registry)
	ifMissingSet(&syndesis.Spec.Components.Server.Registry, syndesis.Spec.Registry)
	ifMissingSet(&syndesis.Spec.Components.UI.Registry, syndesis.Spec.Registry)
	ifMissingSet(&syndesis.Spec.Components.S2I.Registry, syndesis.Spec.Registry)
	ifMissingSet(&syndesis.Spec.Components.Upgrade.Registry, syndesis.Spec.Registry)
	ifMissingSet(&syndesis.Spec.Components.Komodo.Registry, syndesis.Spec.Registry)
	ifMissingSet(&syndesis.Spec.Components.PostgresExporter.Registry, syndesis.Spec.Registry)

	ifMissingSet(&syndesis.Spec.Components.ImagePrefix, renderContext.Images.SyndesisImagesPrefix)
	ifMissingSet(&syndesis.Spec.Components.Meta.ImagePrefix, syndesis.Spec.Components.ImagePrefix)
	ifMissingSet(&syndesis.Spec.Components.Server.ImagePrefix, syndesis.Spec.Components.ImagePrefix)
	ifMissingSet(&syndesis.Spec.Components.UI.ImagePrefix, syndesis.Spec.Components.ImagePrefix)
	ifMissingSet(&syndesis.Spec.Components.S2I.ImagePrefix, syndesis.Spec.Components.ImagePrefix)
	ifMissingSet(&syndesis.Spec.Components.Upgrade.ImagePrefix, syndesis.Spec.Components.ImagePrefix)
	ifMissingSet(&syndesis.Spec.Components.Komodo.ImagePrefix, renderContext.Images.KomodoImagesPrefix)
	ifMissingSet(&syndesis.Spec.Components.PostgresExporter.ImagePrefix, renderContext.Images.PostgresExporterImagePrefix)

	if syndesis.Spec.DeployIntegrations == nil {
		v := true
		syndesis.Spec.DeployIntegrations = &v
	}
	ifMissingSet(&syndesis.Spec.ImageStreamNamespace, renderContext.Images.ImageStreamNamespace)

	ifMissingSet(&syndesis.Spec.Components.Db.Resources.VolumeCapacity, cf.DefaultValue(cf.EnvPostgresqlVolumeCapacity))
	ifMissingSet(&syndesis.Spec.Components.Prometheus.Resources.VolumeCapacity, cf.DefaultValue(cf.EnvPrometheusVolumeCapacity))
	ifMissingSet(&syndesis.Spec.Components.Meta.Resources.VolumeCapacity, cf.DefaultValue(cf.EnvMetaVolumeCapacity))
	ifMissingSetResource(syndesis.Spec.Components.Db.Resources.Resources.Limits, "memory", cf.DefaultValue(cf.EnvPostgresqlMemoryLimit))
	ifMissingSetResource(syndesis.Spec.Components.Prometheus.Resources.Resources.Limits, "memory", cf.DefaultValue(cf.EnvPrometheusMemoryLimit))
	ifMissingSetResource(syndesis.Spec.Components.Meta.Resources.Resources.Limits, "memory", cf.DefaultValue(cf.EnvMetaMemoryLimit))
	ifMissingSetResource(syndesis.Spec.Components.Server.Resources.Limits, "memory", cf.DefaultValue(cf.EnvServerMemoryLimit))
	ifMissingSetResource(syndesis.Spec.Components.Komodo.Resources.Limits, "memory", cf.DefaultValue(cf.EnvKomodoMemoryLimit))

	maxIntegrations := 0
	if renderContext.Ocp {
		maxIntegrations = 1
	}
	if syndesis.Spec.Integration.Limit == nil {
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

	// Maven settings
	if len(syndesis.Spec.MavenRepositories) == 0 {
		if syndesis.Spec.MavenRepositories == nil {
			syndesis.Spec.MavenRepositories = make(map[string]string)
		}
		if renderContext.Productized {
			syndesis.Spec.MavenRepositories["central"] = "https://repo.maven.apache.org/maven2/"
			if renderContext.EarlyAccess {
				syndesis.Spec.MavenRepositories["repo-02-redhat-ea"] = "https://maven.repository.redhat.com/earlyaccess/all/"
				syndesis.Spec.MavenRepositories["repo-03-jboss-ea"] = "https://repository.jboss.org/nexus/content/groups/ea/"
			} else {
				syndesis.Spec.MavenRepositories["repo-02-redhat-ga"] = "https://maven.repository.redhat.com/ga/"
				syndesis.Spec.MavenRepositories["repo-03-jboss-ga"] = "https://repository.jboss.org/"
			}
		} else {
			// Repositories needed for community builds
			syndesis.Spec.MavenRepositories["central"] = "https://repo.maven.apache.org/maven2/"
			syndesis.Spec.MavenRepositories["repo-02-redhat-ga"] = "https://maven.repository.redhat.com/ga/"
			syndesis.Spec.MavenRepositories["repo-03-jboss-ea"] = "https://repository.jboss.org/nexus/content/groups/ea/"
		}
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
