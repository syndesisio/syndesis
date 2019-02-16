// Provides information on Syndesis versions.
package configuration

import (
	"context"
	"errors"
	templatev1 "github.com/openshift/api/template/v1"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"strings"
)

// Each operator instance is bound to a single version currently that can be retrieved from this method.
func GetSyndesisVersionFromOperatorTemplate(scheme *runtime.Scheme) (string, error) {

	templateRes, err := util.LoadResourceFromFile(scheme, *TemplateLocation)
	if err != nil {
		return "", err
	}

	template, ok := templateRes.(*templatev1.Template)
	if !ok {
		return "", errors.New("asset is not a template")
	}

	configSecret, err := findConfigSecret(scheme, template)
	if err != nil {
		return "", err
	}

	return GetSyndesisVersion(configSecret)
}

// Retrieves the version of syndesis installed in the namespace.
func GetSyndesisVersionFromNamespace(ctx context.Context, client client.Client, namespace string) (string, error) {
	secret, err := GetSyndesisConfigurationSecret(ctx, client, namespace)
	if err != nil {
		return "", err
	}

	return GetSyndesisVersion(secret)
}

// Extracts the Syndesis version from the configuration secret.
func GetSyndesisVersion(secret *v1.Secret) (string, error) {
	version, ok := secret.StringData[SyndesisGlobalConfigVersionProperty]
	if !ok {
		versionBin, ok := secret.Data[SyndesisGlobalConfigVersionProperty]
		if !ok {
			return "", errors.New("syndesis version not found in secret")
		}

		version = string(versionBin)
	}

	// cleanup characters (TODO fix the upgrade pod)
	version = strings.Replace(version, "\t", "", -1)
	version = strings.Replace(version, "\n", "", -1)

	return version, nil
}

func findConfigSecret(scheme *runtime.Scheme, template *templatev1.Template) (*v1.Secret, error) {
	for _, object := range template.Objects {
		res, err := util.LoadResourceFromYaml(scheme, object.Raw)
		if err != nil {
			return nil, err
		}
		if secret, ok := res.(*v1.Secret); ok {
			if secret.Name == SyndesisGlobalConfigSecret {
				return secret, nil
			}
		}
	}
	return nil, errors.New("global config secret not found")
}
