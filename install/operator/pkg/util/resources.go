package util

import (
	"github.com/syndesisio/syndesis/install/operator/resources"

	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime"
	"github.com/operator-framework/operator-sdk/pkg/util/k8sutil"

	"io/ioutil"
	"strings"
	"k8s.io/apimachinery/pkg/util/yaml"
)

func LoadKubernetesResourceFromAsset(path string) (runtime.Object, error) {
	data, err := LoadJSONDataFromAsset(path)
	if err != nil {
		return nil, err
	}

	return LoadKubernetesResource(data)
}

func LoadJSONDataFromAsset(path string) ([]byte, error) {
	data, err := resources.Asset(path)
	if err != nil {
		return nil, err
	}

	return jsonIfYaml(data, path)
}

func LoadKubernetesResourceFromFile(path string) (runtime.Object, error) {
	data, err := ioutil.ReadFile(path)
	if err != nil {
		return nil, err
	}

	data, err = jsonIfYaml(data, path)
	if err != nil {
		return nil, err
	}

	return LoadKubernetesResource(data)
}

func LoadKubernetesResource(jsonData []byte) (runtime.Object, error) {
	u := unstructured.Unstructured{}
	err := u.UnmarshalJSON(jsonData)
	if err != nil {
		return nil, err
	}

	obj := k8sutil.RuntimeObjectFromUnstructured(&u)
	return obj, nil
}

func jsonIfYaml(source []byte, filename string) ([]byte, error) {
	if strings.HasSuffix(filename, ".yaml") || strings.HasSuffix(filename, ".yml") {
		return yaml.ToJSON(source)
	}
	return source, nil
}
