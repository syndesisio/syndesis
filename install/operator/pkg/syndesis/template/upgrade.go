package template

import (
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/runtime"
)

const (
	SyndesisUpgrateTemplateName = "syndesis-upgrade"
)

func GetUpgradeResources(scheme *runtime.Scheme, syndesis *v1alpha1.Syndesis, params ResourceParams) ([]unstructured.Unstructured, error) {
	renderContext, err := GetTemplateContext()
	if err != nil {
		return nil, err
	}

	err = SetupRenderContext(renderContext, syndesis, params, map[string]string{})
	if err != nil {
		return nil, err
	}

	// Render the files in the update directory
	res, err := generator.RenderDir("./upgrade/", renderContext)
	if err != nil {
		return nil, err
	}

	return res, nil
}
