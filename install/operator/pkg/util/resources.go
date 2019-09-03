package util

import (
	"encoding/json"
	"fmt"
	"github.com/pkg/errors"
	"io/ioutil"
	"k8s.io/apimachinery/pkg/api/meta"
	"strings"

	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/runtime/serializer"
	"k8s.io/apimachinery/pkg/util/yaml"
	"sigs.k8s.io/controller-runtime/pkg/client"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
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

func UnmarshalYaml(data []byte, target interface{}) error {
	data, err := yaml.ToJSON(data)
	if err != nil {
		return err
	}
	return json.Unmarshal(data, target)
}

// LoadRawResourceFromYaml loads a k8s resource from a yaml definition without making assumptions on the underlying type
func LoadRawResourceFromYaml(data string) (*unstructured.Unstructured, error) {
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

func SeperateStructuredAndUnstructured(scheme *runtime.Scheme, in []unstructured.Unstructured) ([]runtime.Object, []unstructured.Unstructured) {
	runtimes := []runtime.Object{}
	unstructureds := []unstructured.Unstructured{}
	for _, value := range in {
		if r, err := RuntimeObjectFromUnstructured(scheme, &value); err == nil {
			runtimes = append(runtimes, r)
		} else {
			unstructureds = append(unstructureds, value)
		}
	}
	return runtimes, unstructureds
}

func LoadJsonFromFile(path string) ([]byte, error) {
	data, err := ioutil.ReadFile(path)
	if err != nil {
		return nil, err
	}

	data, err = jsonIfYaml(data, path)
	if err != nil {
		return nil, err
	}
	return data, nil
}

func LoadUnstructuredObjectFromFile(path string) (*unstructured.Unstructured, error) {
	data, err := LoadJsonFromFile(path)
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

func ToUnstructured(obj runtime.Object) (*unstructured.Unstructured, error) {

	// It might be already Unstructured..
	if u, ok := obj.(*unstructured.Unstructured); ok {
		return u, nil
	}

	// Convert it..
	fields, err := runtime.DefaultUnstructuredConverter.ToUnstructured(obj)
	if err != nil {
		return nil, errors.WithStack(err)
	}
	return &unstructured.Unstructured{fields}, nil
}

func ToMetaObject(resource interface{}) metav1.Object {
	switch resource := resource.(type) {
	case metav1.Object:
		return resource
	case unstructured.Unstructured:
		return &resource
	}
	panic("Not a metav1.Object")
}

func ToRuntimeObject(resource interface{}) runtime.Object {
	switch resource := resource.(type) {
	case runtime.Object:
		return resource
	case unstructured.Unstructured:
		return &resource
	}
	panic("Not a runtime.Object")
}

func UnstructuredsToRuntimeObject(items []unstructured.Unstructured) runtime.Object {
	switch len(items) {
	case 0:
		return nil
	case 1:
		return &items[0]
	default:
		list := unstructured.UnstructuredList{
			Items: items,
		}
		list.SetAPIVersion("v1")
		list.SetKind("List")
		return &list
	}
}

func IsNoKindMatchError(err error) bool {
	if err == nil {
		return false
	}
	switch err.(type) {
	case *meta.NoKindMatchError:
		return true
	default:
		return false
	}
}
