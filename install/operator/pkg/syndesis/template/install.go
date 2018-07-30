package template

import (
	"github.com/openshift/api/template/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/openshift/template"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/runtime"
)


type InstallParams struct {
	OAuthClientSecret	string
}

func GetInstallResourcesAsRuntimeObjects(syndesis *v1alpha1.Syndesis, params InstallParams) ([]runtime.Object, error) {
	rawExtensions, err := GetInstallResources(syndesis, params)
	if err != nil {
		return nil, err
	}

	objects := make([]runtime.Object, 0)

	for _, rawObj := range rawExtensions {
		res, err := util.LoadKubernetesResource(rawObj.Raw)
		if err != nil {
			return nil, err
		}
		objects = append(objects, res)
	}

	return objects, nil
}

func GetInstallResources(syndesis *v1alpha1.Syndesis, params InstallParams) ([]runtime.RawExtension, error) {
	res, err := util.LoadKubernetesResourceFromFile(*configuration.TemplateLocation)
	if err != nil {
		return nil, err
	}

	templ := res.(*v1.Template)
	processor, err := template.NewTemplateProcessor(syndesis.Namespace)
	if err != nil {
		return nil, err
	}

	config := configuration.GetEnvVars(syndesis)
	config[string(configuration.EnvOpenshiftOauthClientSecret)] = params.OAuthClientSecret

	return processor.Process(templ, config)
}
