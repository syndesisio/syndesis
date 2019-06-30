package generator_test

import (
	"encoding/json"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/build"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"path/filepath"
	"testing"
)

func TestGenerator(t *testing.T) {

	templateConfig, err := util.LoadJsonFromFile(filepath.Join(build.GO_MOD_DIRECTORY, "build", "conf", "template-config.yaml"))
	require.NoError(t, err)

	// Parse the config
	gen := &generator.Context{}
	err = json.Unmarshal(templateConfig, gen)
	require.NoError(t, err)

	gen.Syndesis = &v1alpha1.Syndesis{
		Spec: v1alpha1.SyndesisSpec{},
	}

	resources, err := generator.RenderFSDir(generator.GetAssetsFS(), "./install/", gen)
	require.NoError(t, err)
	assert.True(t, len(resources) > 0)
	assert.True(t, contains(resources, "DeploymentConfig", "todo"))

	// Verifiy we can disable deploying the todo app.
	gen.Syndesis.Spec.Addons = v1alpha1.AddonsSpec{
		"todo": v1alpha1.Parameters{
			"enabled": "false",
		},
	}
	resources, err = generator.RenderFSDir(generator.GetAssetsFS(), "./install/", gen)
	assert.False(t, contains(resources, "DeploymentConfig", "todo"))

	resources, err = generator.RenderFSDir(generator.GetAssetsFS(), "./upgrade/", gen)
	require.NoError(t, err)
	assert.True(t, len(resources) > 0)
}

func contains(unstructureds []unstructured.Unstructured, kind string, name string) bool {
	for _, value := range unstructureds {
		if value.GetKind() == kind && value.GetName() == name {
			return true
		}
	}
	return false
}
