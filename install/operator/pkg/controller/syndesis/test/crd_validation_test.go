package test

import (
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/RHsyseng/operator-utils/pkg/validation"
	"github.com/ghodss/yaml"
	"github.com/stretchr/testify/assert"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta2"
)

func TestSampleCustomResources(t *testing.T) {
	schema := getSchema(t)
	assert.NotNil(t, schema)

	filePath := getCRFile(t, "../../../../deploy/crds")
	bytes, err := ioutil.ReadFile(filePath)
	assert.NoError(t, err, "Error reading CR yaml %v", filePath)

	var input map[string]interface{}
	assert.NoError(t, yaml.Unmarshal(bytes, &input))
	assert.NoError(t, schema.Validate(input), "File %v does not validate against the CRD schema", filePath)
}

func TestTrialEnvMinimum(t *testing.T) {
	var inputYaml = `
apiVersion: syndesis.io/v1beta2
kind: Syndesis
metadata:
  name: trial
spec:
  addons:
    ops:
      enabled: true
status:
  apiVersion: v1beta2
`
	var input map[string]interface{}
	assert.NoError(t, yaml.Unmarshal([]byte(inputYaml), &input))

	schema := getSchema(t)
	assert.NoError(t, schema.Validate(input))
}

func TestCompleteCRD(t *testing.T) {
	schema := getSchema(t)
	missingEntries := schema.GetMissingEntries(&v1beta2.Syndesis{})
	for _, missing := range missingEntries {
		if strings.HasPrefix(missing.Path, "/status") {
			//Not using subresources, so status is not expected to appear in CRD
		} else if strings.HasPrefix(missing.Path, "/spec") {
			//Not using subresources, so spec is not expected to appear in CRD
		} else {
			assert.Fail(t, "Discrepancy between CRD and Struct", "Missing or incorrect schema validation at %v, expected type %v", missing.Path, missing.Type)
		}
	}
}

func getCRFile(t *testing.T, dir string) string {
	var file string
	err := filepath.Walk(dir,
		func(path string, info os.FileInfo, err error) error {
			if err != nil {
				return err
			}
			if strings.HasPrefix(info.Name(), "syndesis.io_v1beta2_syndesis_cr") {
				file = path
			}
			return nil
		})
	assert.NoError(t, err, "Error finding CR yaml %v", file)
	return file
}

func getSchema(t *testing.T) validation.Schema {
	crdFile := "../../../../deploy/crds/syndesis.io_syndeses_crd.yaml"
	bytes, err := ioutil.ReadFile(crdFile)
	assert.NoError(t, err, "Error reading CRD yaml %v", crdFile)
	schema, err := validation.New(bytes)
	assert.NoError(t, err)
	return schema
}
