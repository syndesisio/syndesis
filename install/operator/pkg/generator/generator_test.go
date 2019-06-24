package generator_test

import (
	"encoding/json"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"github.com/syndesisio/syndesis/install/operator/pkg/build"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
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

	resources, err := generator.RenderFSDir(generator.GetAssetsFS(), "./install/", gen)
	require.NoError(t, err)
	assert.True(t, len(resources) > 0)

	resources, err = generator.RenderFSDir(generator.GetAssetsFS(), "./upgrade/", gen)
	require.NoError(t, err)
	assert.True(t, len(resources) > 0)
}
