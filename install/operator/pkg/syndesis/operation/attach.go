package operation

import (
	"github.com/operator-framework/operator-sdk/pkg/sdk"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/template"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func AttachSyndesisToResource(syndesis *v1alpha1.Syndesis) error {

	resTypes, err := getAllManagedResourceTypes()
	if err != nil {
		return err
	}
	selectors := getAllManagerSelectors()

	for _, selector := range selectors {
		for _, metaType := range resTypes {

			options := sdk.WithListOptions(&selector)
			list := metav1.List{
				TypeMeta: metaType,
			}
			if err := sdk.List(syndesis.Namespace, &list, options); err != nil {
				return err
			}

			for _, obj := range list.Items {
				res, err := util.LoadKubernetesResource(obj.Raw)
				if err != nil {
					return err
				}
				SetNamespaceAndOwnerReference(res, syndesis)
				if err := sdk.Update(res); err != nil {
					return err
				}
			}

		}
	}

	return nil
}

func getAllManagerSelectors() []metav1.ListOptions {
	return []metav1.ListOptions {
		{
			LabelSelector: "syndesis.io/app=syndesis,syndesis.io/type=infrastructure",
		},
		{
			LabelSelector: "app=syndesis,syndesis.io/app=todo",
		},
	}
}

func getAllManagedResourceTypes() ([]metav1.TypeMeta, error) {
	metas, err := template.GetDeclaredResourceTypes()
	if err != nil {
		return nil, err
	}
	return appendMissingResourceTypes(metas), nil
}

func appendMissingResourceTypes(metas []metav1.TypeMeta) ([]metav1.TypeMeta){
	// Add here any resource type that should be attached but may not be present in the template
	metas = appendTypeMeta(metas, "batch/v1beta1", "CronJob")
	metas = appendTypeMeta(metas, "v1", "ServiceAccount")
	return appendTypeMeta(metas, "authorization.openshift.io/v1", "RoleBinding")
}

func appendTypeMeta(metas []metav1.TypeMeta, apiVersion string, kind string) ([]metav1.TypeMeta) {
	return append(metas,metav1.TypeMeta{
		APIVersion: apiVersion,
		Kind: kind,
	})
}
