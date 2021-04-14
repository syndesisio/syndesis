package operation

import (
	"strings"

	"github.com/syndesisio/syndesis/install/operator/pkg"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta2"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime/schema"
)

func SetNamespaceAndOwnerReference(resource interface{}, syndesis *v1beta2.Syndesis) {
	object := util.ToMetaObject(resource)
	if r, ok := resource.(unstructured.Unstructured); ok {
		//
		// Cluster-level resources do not require a namespace to be set
		//
		if !strings.HasPrefix(r.GetKind(), "Cluster") {
			object.SetNamespace(syndesis.Namespace)
		}
	} else {
		//
		// Ensures any other resources still have a namespace set
		//
		object.SetNamespace(syndesis.Namespace)
	}

	object.SetOwnerReferences([]metav1.OwnerReference{
		*metav1.NewControllerRef(syndesis, schema.GroupVersionKind{
			Group:   v1beta2.SchemeGroupVersion.Group,
			Version: v1beta2.SchemeGroupVersion.Version,
			Kind:    syndesis.Kind,
		}),
	})

	//
	// Jobs do not like being labelled with an owner but have to have
	// controller-uid instead. If "owner" is used then the job is not
	// held around but garbage-collected within a few seconds of creation.
	// Adding owner in addition to controller-uid yields the same "bug"
	//
	if r, ok := resource.(unstructured.Unstructured); ok && r.GetKind() == "Job" {
		setLabel(resource, pkg.ControllerUidLabel, string(syndesis.GetUID()))
	} else {
		setLabel(resource, "owner", string(syndesis.GetUID()))
	}
}

func setLabel(resource interface{}, key string, value string) {
	obj := util.ToMetaObject(resource)
	labels := obj.GetLabels()
	if labels == nil {
		labels = map[string]string{}
	}
	labels[key] = value
	obj.SetLabels(labels)
}
