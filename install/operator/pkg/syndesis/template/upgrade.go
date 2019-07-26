package template

import (
	"errors"
	templatev1 "github.com/openshift/api/template/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime"
)

const (
	SyndesisUpgrateTemplateName = "syndesis-upgrade"
)

func GetUpgradeResources(scheme *runtime.Scheme, syndesis *v1alpha1.Syndesis, params ResourceParams) ([]unstructured.Unstructured, error) {
	renderContext, err := GetRenderContext(syndesis, params, map[string]string{})
	if err != nil {
		return nil, err
	}

	// Render the files in the update directory
	res, err := generator.RenderDir("./update/", renderContext)
	if err != nil {
		return nil, err
	}

	return res, nil
}

func findUpgradeTemplate(scheme *runtime.Scheme, list []runtime.RawExtension) (*templatev1.Template, error) {
	for _, object := range list {
		res, err := util.LoadResourceFromYaml(scheme, object.Raw)
		if err != nil {
			return nil, err
		}
		if upgradeTemplate, ok := res.(*templatev1.Template); ok {
			if upgradeTemplate.Name == SyndesisUpgrateTemplateName {
				return upgradeTemplate, nil
			}
		}
	}
	return nil, errors.New("upgrade template not found")
}
