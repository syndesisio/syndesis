package operation

import (
	"context"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/template"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

func AttachSyndesisToResource(ctx context.Context, scheme *runtime.Scheme, cl client.Client, syndesis *v1alpha1.Syndesis) error {

	resTypes, err := getAllManagedResourceTypes(scheme)
	if err != nil {
		return err
	}
	selectors := getAllManagerSelectors()

	for _, selector := range selectors {
		for _, metaType := range resTypes {
			options := client.ListOptions{
				Namespace: syndesis.Namespace,
				Raw: &metav1.ListOptions{
					TypeMeta: metaType,
				},
			}
			if err := options.SetLabelSelector(selector); err != nil {
				return err
			}
			list := unstructured.UnstructuredList{
				Object: map[string]interface{}{
					"apiVersion": metaType.APIVersion,
					"kind":       metaType.Kind,
				},
			}
			if err := cl.List(ctx, &options, &list); err != nil {
				return err
			}

			for _, obj := range list.Items {
				SetNamespaceAndOwnerReference(&obj, syndesis)
				if err := cl.Update(ctx, &obj); err != nil {
					return err
				}
			}

		}
	}

	return nil
}

func getAllManagerSelectors() []string {
	return []string{
		"syndesis.io/app=syndesis,syndesis.io/type=infrastructure",
		"syndesis.io/app=todo,app=syndesis",
	}
}

func getAllManagedResourceTypes(scheme *runtime.Scheme) ([]metav1.TypeMeta, error) {
	metas, err := template.GetDeclaredResourceTypes(scheme)
	if err != nil {
		return nil, err
	}
	return appendMissingResourceTypes(metas), nil
}

func appendMissingResourceTypes(metas []metav1.TypeMeta) []metav1.TypeMeta {
	// Add here any resource type that should be attached but may not be present in the template
	metas = appendTypeMeta(metas, "batch/v1beta1", "CronJob")
	metas = appendTypeMeta(metas, "v1", "ServiceAccount")
	return appendTypeMeta(metas, "authorization.openshift.io/v1", "RoleBinding")
}

func appendTypeMeta(metas []metav1.TypeMeta, apiVersion string, kind string) []metav1.TypeMeta {
	return append(metas, metav1.TypeMeta{
		APIVersion: apiVersion,
		Kind:       kind,
	})
}
