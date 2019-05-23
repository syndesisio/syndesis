package template

import (
	"github.com/openshift/api/template/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/openshift/template"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/runtime"
	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"
)

var log = logf.Log.WithName("template")

type InstallParams struct {
	OAuthClientSecret string
	DataVirtEnabled   bool
}

func GetInstallResourcesAsRuntimeObjects(scheme *runtime.Scheme, syndesis *v1alpha1.Syndesis, params InstallParams) ([]runtime.Object, error) {
	rawExtensions, err := GetInstallResources(scheme, syndesis, params)
	if err != nil {
		return nil, err
	}

	objects := make([]runtime.Object, 0)

	for _, rawObj := range rawExtensions {
		res, err := util.LoadResourceFromYaml(scheme, rawObj.Raw)
		if err != nil {
			return nil, err
		}
		objects = append(objects, res)
	}

	if log.V(5).Enabled() {
		log.V(5).Info("Number of objects to create", "number_of_objects", len(objects))
		for _, obj := range objects {
			log.V(5).Info("Object to create", "object", obj)
		}
	}

	return objects, nil
}

func GetInstallResources(scheme *runtime.Scheme, syndesis *v1alpha1.Syndesis, params InstallParams) ([]runtime.RawExtension, error) {
	res, err := util.LoadResourceFromFile(scheme, *configuration.TemplateLocation)
	if err != nil {
		return nil, err
	}

	templ := res.(*v1.Template)
	processor, err := template.NewTemplateProcessor(scheme, syndesis.Namespace)
	if err != nil {
		return nil, err
	}

	config := configuration.GetEnvVars(syndesis)
	config[string(configuration.EnvOpenshiftOauthClientSecret)] = params.OAuthClientSecret
	if params.DataVirtEnabled {
		config["DATAVIRT_ENABLED"] = "1"
	}

	return processor.Process(templ, config)
}
