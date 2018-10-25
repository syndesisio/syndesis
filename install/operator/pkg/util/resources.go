package util

import (
	"github.com/operator-framework/operator-sdk/pkg/util/k8sutil"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime"

	"io/ioutil"
	"k8s.io/apimachinery/pkg/util/yaml"
	"strings"
)

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
