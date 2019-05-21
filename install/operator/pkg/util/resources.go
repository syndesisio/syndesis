package util

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"strings"

	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/runtime/serializer"
	"k8s.io/apimachinery/pkg/util/yaml"
	"sigs.k8s.io/controller-runtime/pkg/client"

	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"
)

var log = logf.Log.WithName("resources")

func NewObjectKey(name string, namespace string) client.ObjectKey {
	return client.ObjectKey{
		Name:      name,
		Namespace: namespace,
	}
}

func LoadResourceFromFile(scheme *runtime.Scheme, path string) (runtime.Object, error) {
	data, err := ioutil.ReadFile(path)
	if err != nil {
		return nil, err
	}

	data, err = jsonIfYaml(data, path)
	if err != nil {
		return nil, err
	}

	return LoadResourceFromYaml(scheme, data)
}

// LoadRawResourceFromYaml loads a k8s resource from a yaml definition without making assumptions on the underlying type
func LoadRawResourceFromYaml(data string) (runtime.Object, error) {
	source := []byte(data)
	jsonSource, err := yaml.ToJSON(source)
	if err != nil {
		return nil, err
	}
	var objmap map[string]interface{}
	if err = json.Unmarshal(jsonSource, &objmap); err != nil {
		return nil, err
	}
	return &unstructured.Unstructured{
		Object: objmap,
	}, nil
}

func LoadResourceFromYaml(scheme *runtime.Scheme, source []byte) (runtime.Object, error) {
	jsonSource, err := yaml.ToJSON(source)
	if err != nil {
		return nil, err
	}
	u := unstructured.Unstructured{}
	err = u.UnmarshalJSON(jsonSource)
	if err != nil {
		return nil, err
	}
	return RuntimeObjectFromUnstructured(scheme, &u)
}

// RuntimeObjectFromUnstructured converts an unstructured to a runtime object
func RuntimeObjectFromUnstructured(scheme *runtime.Scheme, u *unstructured.Unstructured) (runtime.Object, error) {
	gvk := u.GroupVersionKind()
	codecs := serializer.NewCodecFactory(scheme)
	decoder := codecs.UniversalDecoder(gvk.GroupVersion())

	b, err := u.MarshalJSON()
	if err != nil {
		return nil, fmt.Errorf("error running MarshalJSON on unstructured object: %v", err)
	}
	ro, _, err := decoder.Decode(b, &gvk, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to decode json data with gvk(%v): %v", gvk.String(), err)
	}
	return ro, nil
}

func LoadUnstructuredObjectFromFile(path string) (*unstructured.Unstructured, error) {
	data, err := ioutil.ReadFile(path)
	if err != nil {
		return nil, err
	}

	data, err = jsonIfYaml(data, path)
	if err != nil {
		return nil, err
	}

	uo, err := LoadUnstructuredObject(data)
	if err != nil {
		return nil, err
	}

	return uo, nil
}

func LoadUnstructuredObject(data []byte) (*unstructured.Unstructured, error) {
	var uo unstructured.Unstructured
	if err := json.Unmarshal(data, &uo.Object); err != nil {
		return nil, err
	}
	return &uo, nil
}

func jsonIfYaml(source []byte, filename string) ([]byte, error) {
	if strings.HasSuffix(filename, ".yaml") || strings.HasSuffix(filename, ".yml") {
		return yaml.ToJSON(source)
	}
	return source, nil
}
