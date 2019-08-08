package generator_test

import (
	"encoding/json"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/build"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/template"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"path/filepath"
	"testing"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func TestGenerator(t *testing.T) {

	templateConfig, err := util.LoadJsonFromFile(filepath.Join(build.GO_MOD_DIRECTORY, "build", "conf", "config.yaml"))
	require.NoError(t, err)

	// Parse the config
	gen := &generator.Context{}
	err = json.Unmarshal(templateConfig, gen)
	require.NoError(t, err)

	syndesis := &v1alpha1.Syndesis{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: "TEST",
		},
		Spec: v1alpha1.SyndesisSpec{},
	}
	gen.Syndesis = syndesis

	err = template.SetupRenderContext(gen, syndesis, template.ResourceParams{}, map[string]string{} )
	require.NoError(t, err)

	configuration.SetConfigurationFromEnvVars(gen.Env, syndesis)

	resources, err := generator.RenderFSDir(generator.GetAssetsFS(), "./infrastructure/", gen)
	require.NoError(t, err)
	assert.True(t, len(resources) > 0)

	resources, err = generator.RenderFSDir(generator.GetAssetsFS(), "./upgrade/", gen)
	require.NoError(t, err)
	assert.True(t, len(resources) > 0)
}

func TestConfigYAML(t *testing.T)  {
	templateConfig, err := util.LoadJsonFromFile(filepath.Join(build.GO_MOD_DIRECTORY, "build", "conf", "config.yaml"))
	require.NoError(t, err)

	// Parse the config
	gen := &generator.Context{}
	err = json.Unmarshal(templateConfig, gen)
	require.NoError(t, err)

	// Tags are mandatory as fallback in case CR and Secret dont have them defined
	assert.NotNil(t, gen.Tags.Syndesis, "Tags.Syndesis is a mandatory field in config.yaml file")
	assert.NotEmpty(t, gen.Tags.Syndesis, "Tags.Syndesis is a mandatory field in config.yaml file")

	assert.NotNil(t, gen.Tags.Komodo, "Tags.Komodo is a mandatory field in config.yaml file")
	assert.NotEmpty(t, gen.Tags.Komodo, "Tags.Komodo is a mandatory field in config.yaml file")

	assert.NotNil(t, gen.Tags.OAuthProxy, "Tags.OAuthProxy is a mandatory field in config.yaml file")
	assert.NotEmpty(t, gen.Tags.OAuthProxy, "Tags.OAuthProxy is a mandatory field in config.yaml file")

	assert.NotNil(t, gen.Tags.PostgresExporter, "Tags.PostgresExporter is a mandatory field in config.yaml file")
	assert.NotEmpty(t, gen.Tags.PostgresExporter, "Tags.PostgresExporter is a mandatory field in config.yaml file")

	assert.NotNil(t, gen.Tags.Postgresql, "Tags.Postgresql is a mandatory field in config.yaml file")
	assert.NotEmpty(t, gen.Tags.Postgresql, "Tags.Postgresql is a mandatory field in config.yaml file")

	assert.NotNil(t, gen.Tags.Prometheus, "Tags.Prometheus is a mandatory field in config.yaml file")
	assert.NotEmpty(t, gen.Tags.Prometheus, "Tags.Prometheus is a mandatory field in config.yaml file")

	assert.NotNil(t, gen.TagMajor, "TagMajor is a mandatory field in config.yaml file")
	assert.NotEmpty(t, gen.TagMajor, "TagMajor is a mandatory field in config.yaml file")

	assert.NotNil(t, gen.TagMinor, "TagMinor is a mandatory field in config.yaml file")
	assert.NotEmpty(t, gen.TagMinor, "TagMinor is a mandatory field in config.yaml file")
}