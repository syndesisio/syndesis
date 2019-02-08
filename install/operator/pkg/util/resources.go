package util

import (
	"fmt"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/runtime/serializer"
	"sigs.k8s.io/controller-runtime/pkg/client"

	apps "github.com/openshift/api/apps/v1"
	"io/ioutil"
	"k8s.io/apimachinery/pkg/util/yaml"
	"strings"
)


var (
	// scheme tracks the type registry for the sdk
	// This scheme is used to decode json data into the correct Go type based on the object's GVK
	// All types that the operator watches must be added to this scheme
	scheme = runtime.NewScheme()
	codecs = serializer.NewCodecFactory(scheme)
)

func init() {
	apps.AddToScheme(scheme)
}

func NewObjectKey(name string, namespace string) (client.ObjectKey) {
	return client.ObjectKey{
		Name: name,
		Namespace: namespace,
	}
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

	obj := runtimeObjectFromUnstructured(&u)
	return obj, nil
}

// RuntimeObjectFromUnstructured converts an unstructured to a runtime object
func runtimeObjectFromUnstructured(u *unstructured.Unstructured) runtime.Object {
	gvk := u.GroupVersionKind()
	decoder := codecs.UniversalDecoder(gvk.GroupVersion());

	b, err := u.MarshalJSON()
	if err != nil {
		panic(err)
	}
	ro, _, err := decoder.Decode(b, &gvk, nil)
	if err != nil {
		err = fmt.Errorf("failed to decode json data with gvk(%v): %v", gvk.String(), err)
		panic(err)
	}
	return ro
}

func jsonIfYaml(source []byte, filename string) ([]byte, error) {
	if strings.HasSuffix(filename, ".yaml") || strings.HasSuffix(filename, ".yml") {
		return yaml.ToJSON(source)
	}
	return source, nil
}
