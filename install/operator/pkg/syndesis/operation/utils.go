package operation

import (
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/runtime/schema"
)

func ToObject(resource interface{}) metav1.Object {
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

func SetNamespaceAndOwnerReference(resource interface{}, syndesis *v1alpha1.Syndesis) {
	object := ToObject(resource)
	object.SetNamespace(syndesis.Namespace)
	object.SetOwnerReferences([]metav1.OwnerReference{
		*metav1.NewControllerRef(syndesis, schema.GroupVersionKind{
			Group:   v1alpha1.SchemeGroupVersion.Group,
			Version: v1alpha1.SchemeGroupVersion.Version,
			Kind:    syndesis.Kind,
		}),
	})
	SetLabel(resource, "owner", string(syndesis.GetUID()))
}

func SetLabel(resource interface{}, key string, value string) {
	obj := ToObject(resource)
	labels := obj.GetLabels()
	if labels == nil {
		labels = map[string]string{}
	}
	labels[key] = value
	obj.SetLabels(labels)
}
