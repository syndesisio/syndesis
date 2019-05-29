package generator_test

import (
    "fmt"
    "github.com/stretchr/testify/assert"
    "github.com/syndesisio/syndesis/install/operator/pkg/generator"
    "testing"
)

func TestStandardConfig(t *testing.T) {
    context := generator.CreateSyndesisContext();
    file, err := context.GenerateResources()
    assert.NoError(t, err)
    fmt.Println(file)
    //assert.Len(t, config, 3)
    //assert.Contains(t, config, string(EnvOpenshiftProject))
    //assert.Contains(t, config, string(EnvSarNamespace))
    //assert.Contains(t, config, string(EnvExposeVia3Scale))
}
