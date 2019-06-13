package operation

import (
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime/schema"
)

func SetNamespaceAndOwnerReference(resource interface{}, syndesis *v1alpha1.Syndesis) {

	var kObj metav1.Object = nil
	if x, ok := resource.(metav1.Object); ok {
		kObj = x
	} else if x, ok := resource.(unstructured.Unstructured); ok {
		kObj = &x
	} else {
		return
	}

	kObj.SetNamespace(syndesis.Namespace)
	kObj.SetOwnerReferences([]metav1.OwnerReference{
		*metav1.NewControllerRef(syndesis, schema.GroupVersionKind{
			Group:   v1alpha1.SchemeGroupVersion.Group,
			Version: v1alpha1.SchemeGroupVersion.Version,
			Kind:    syndesis.Kind,
		}),
	})
}

func SetLabel(resource interface{}, key string, value string) {
	if obj, ok := resource.(metav1.Object); ok {
		labels := obj.GetLabels()
		if labels == nil {
			labels = map[string]string{}
		}
		labels[key] = value
		obj.SetLabels(labels)
	}
}
