package operation

import (
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime/schema"
)

func SetNamespaceAndOwnerReference(resource interface{}, syndesis *v1alpha1.Syndesis) {
	object := util.ToMetaObject(resource)
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
	obj := util.ToMetaObject(resource)
	labels := obj.GetLabels()
	if labels == nil {
		labels = map[string]string{}
	}
	labels[key] = value
	obj.SetLabels(labels)
}
