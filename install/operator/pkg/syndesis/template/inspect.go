package template

import (
	"github.com/openshift/api/template/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
)


func GetDeclaredResourceTypes() ([]metav1.TypeMeta, error) {
	lst := make([]metav1.TypeMeta, 0)
	res, err := util.LoadKubernetesResourceFromAsset("template.yaml")
	if err != nil {
		return nil, err
	}
	if templ, ok := res.(*v1.Template); ok {
		for _, obj := range templ.Objects {
			u := unstructured.Unstructured{}
			err := u.UnmarshalJSON(obj.Raw)
			if err != nil {
				return nil, err
			}
			lst = append(lst, metav1.TypeMeta{
				APIVersion: u.GetAPIVersion(),
				Kind: u.GetKind(),
			})
		}
	}

	return lst, nil
}
