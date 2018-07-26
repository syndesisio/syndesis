package configuration

import (
	"github.com/operator-framework/operator-sdk/pkg/sdk"
	"k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

const (
	SyndesisGlobalConfigSecret			= "syndesis-global-config"
	SyndesisGlobalConfigVersionProperty	= "syndesis"
	SyndesisGlobalConfigParamsProperty	= "params"
)

func IsSyndesisConfigurationSecretPresent(namespace string) (bool, error) {
	if _, err := GetSyndesisConfigurationSecret(namespace); err != nil && errors.IsNotFound(err) {
		return false, nil
	} else if err != nil {
		return false, err
	} else {
		return true, nil
	}
}

func GetSyndesisConfigurationSecret(namespace string) (*v1.Secret, error) {
	secret := v1.Secret{
		TypeMeta: metav1.TypeMeta{
			APIVersion: "v1",
			Kind: "Secret",
		},
		ObjectMeta: metav1.ObjectMeta{
			Namespace: namespace,
			Name: SyndesisGlobalConfigSecret,
		},
	}
	if err := sdk.Get(&secret); err != nil {
		return nil, err
	}
	return &secret, nil
}