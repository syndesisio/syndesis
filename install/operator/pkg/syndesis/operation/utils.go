package operation

import (
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime/schema"
)

func SetNamespaceAndOwnerReference(resource interface{}, syndesis *v1alpha1.Syndesis) {
	if kObj, ok := resource.(metav1.Object); ok {
		kObj.SetNamespace(syndesis.Namespace)

		kObj.SetOwnerReferences([]metav1.OwnerReference{
			*metav1.NewControllerRef(syndesis, schema.GroupVersionKind{
				Group:   v1alpha1.SchemeGroupVersion.Group,
				Version: v1alpha1.SchemeGroupVersion.Version,
				Kind:    syndesis.Kind,
			}),
		})
	}
}


