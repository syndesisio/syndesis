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
		// In the 7.0 template, the database does not have syndesis.io/type=infrastructure label
		// It can be removed when it's no longer necessary to upgrade from 7.0 (together with the "legacy" package)
		{
			LabelSelector: "syndesis.io/app=syndesis,syndesis.io/component=syndesis-db",
		},
	}
}

func getAllManagedResourceTypes() ([]metav1.TypeMeta, error) {
	templTypes, err := template.GetDeclaredResourceTypes()
	if err != nil {
		return nil, err
	}
	// Using a map to remove duplicates
	metas := make(map[string]metav1.TypeMeta, 0)
	for _, kType := range templTypes {
		addResourceTypes(metas, kType.APIVersion, kType.Kind)
	}
	addMissingResourceTypes(metas)

	lst := make([]metav1.TypeMeta, 0, len(metas))
	for _, meta := range metas {
		lst = append(lst, meta)
	}
	return lst, nil
}

func addMissingResourceTypes(types map[string]metav1.TypeMeta) {
	// Add here any resource type that should be attached but may not be present in the template
	addResourceTypes(types, "batch/v1beta1", "CronJob")
	addResourceTypes(types, "v1", "ServiceAccount")
	addResourceTypes(types, "authorization.openshift.io/v1", "RoleBinding")
}

func addResourceTypes(types map[string]metav1.TypeMeta, apiVersion string, kind string) {
	types[typeMetaKey(apiVersion, kind)] = metav1.TypeMeta{
		APIVersion: apiVersion,
		Kind: kind,
	}
}

func typeMetaKey(apiVersion string, kind string) string {
	return apiVersion + ";" + kind
}
