package addons

import (
	"io/ioutil"
	"path/filepath"
	"strings"

	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
)

func loadAndProcessFile(path string, syndesis *v1alpha1.Syndesis) (*unstructured.Unstructured, error) {
	data, err := ioutil.ReadFile(path)
	if err != nil {
		return nil, err
	}

	//
	// Loop through the available EnvVars & if there are values the substitute them in the content
	// Effectively plugs the EnvVars into the addons before being returned as Unstructured objects
	//
	content := string(data)
	config := configuration.GetEnvVars(syndesis)
	for k, v := range config {
		content = strings.Replace(content, strings.Join([]string{"${", k, "}"}, ""), v, -1)
	}

	data = []byte(content)
	data, err = util.JsonIfYaml(data, path)
	if err != nil {
		return nil, err
	}

	uo, err := util.LoadUnstructuredObject(data)
	if err != nil {
		return nil, err
	}

	return uo, nil
}

func GetAddonID(addon *unstructured.Unstructured) string {
	labels := addon.GetLabels()
	if labels == nil {
		return ""
	}

	return labels["syndesis.io/addon"]
}

func GetAddonsResources(addonsDir string, syndesis *v1alpha1.Syndesis) ([]*unstructured.Unstructured, error) {
	files, err := ioutil.ReadDir(addonsDir)
	if err != nil {
		return nil, err
	}

	res := make([]*unstructured.Unstructured, 0)
	for _, f := range files {
		if f.IsDir() {
			// we may want to recursively install addons
			continue
		}

		addon, err := loadAndProcessFile(filepath.Join(addonsDir, f.Name()), syndesis)
		if err != nil {
			return nil, err
		}
		res = append(res, addon)
	}

	return res, nil
}
