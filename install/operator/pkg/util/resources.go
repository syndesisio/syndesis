package util

import (
	"encoding/json"
	"fmt"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/runtime/serializer"
	"sigs.k8s.io/controller-runtime/pkg/client"

	apps "github.com/openshift/api/apps/v1"
	"io/ioutil"
	"k8s.io/apimachinery/pkg/util/yaml"
	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"
	"strings"
)


var (
	// scheme tracks the type registry for the sdk
	// This scheme is used to decode json data into the correct Go type based on the object's GVK
	// All types that the operator watches must be added to this scheme
	scheme = runtime.NewScheme()
)

var log = logf.Log.WithName("resources")

func init() {
	if err := apps.AddToScheme(scheme); err != nil {
		log.Error(err, "Cann't register openshift apps to scheme")
	}
}

func RegisterInScheme(registerFunc func(*runtime.Scheme)) {
	registerFunc(scheme)
}

func NewObjectKey(name string, namespace string) (client.ObjectKey) {
	return client.ObjectKey{
		Name: name,
		Namespace: namespace,
	}
}

func LoadResourceFromFile(path string) (runtime.Object, error) {
	data, err := ioutil.ReadFile(path)
	if err != nil {
		return nil, err
	}

	data, err = jsonIfYaml(data, path)
	if err != nil {
		return nil, err
	}

	return LoadResourceFromYaml(data)
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


func LoadResourceFromYaml(source []byte) (runtime.Object, error) {
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
func RuntimeObjectFromUnstructured(scheme *runtime.Scheme,u *unstructured.Unstructured) (runtime.Object, error) {
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

func jsonIfYaml(source []byte, filename string) ([]byte, error) {
	if strings.HasSuffix(filename, ".yaml") || strings.HasSuffix(filename, ".yml") {
		return yaml.ToJSON(source)
	}
	return source, nil
}
