package template

import (
	"errors"
	templatev1 "github.com/openshift/api/template/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/openshift/template"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/runtime"
)

const (
	SyndesisUpgrateTemplateName	= "syndesis-upgrade"
)

type UpgradeParams struct {
	InstallParams
	SyndesisVersion		string
}

func GetUpgradeResources(syndesis *v1alpha1.Syndesis, params UpgradeParams) ([]runtime.RawExtension, error) {
	resources, err := GetInstallResources(syndesis, params.InstallParams)
	if err != nil {
		return nil, err
	}

	upgrateTempl, err := findUpgradeTemplate(resources)
	if err != nil {
		return nil, err
	}
	processor, err := template.NewTemplateProcessor(syndesis.Namespace)
	if err != nil {
		return nil, err
	}

	paramMap := configuration.GetEnvVars(syndesis)
	paramMap[string(configuration.EnvOpenshiftOauthClientSecret)] = params.OAuthClientSecret
	paramMap[string(configuration.EnvSyndesisVersion)] = params.SyndesisVersion

	return processor.Process(upgrateTempl, paramMap)
}


func findUpgradeTemplate(list []runtime.RawExtension) (*templatev1.Template, error) {
	for _, object := range list {
		res, err := util.LoadKubernetesResource(object.Raw)
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