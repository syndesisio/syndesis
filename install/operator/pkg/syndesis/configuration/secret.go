package configuration

import (
	"context"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

const (
	SyndesisGlobalConfigSecret          = "syndesis-global-config"
	SyndesisGlobalConfigVersionProperty = "syndesis"
	SyndesisGlobalConfigParamsProperty  = "params"
)

func IsSyndesisConfigurationSecretPresent(ctx context.Context, client client.Client, namespace string) (bool, error) {
	if _, err := GetSyndesisConfigurationSecret(ctx, client, namespace); err != nil && errors.IsNotFound(err) {
		return false, nil
	} else if err != nil {
		return false, err
	} else {
		return true, nil
	}
}

func GetSyndesisConfigurationSecret(ctx context.Context, client client.Client, namespace string) (*v1.Secret, error) {
	secret := v1.Secret{}
	if err := client.Get(ctx, util.NewObjectKey(SyndesisGlobalConfigSecret, namespace), &secret); err != nil {
		return nil, err
	}
	return &secret, nil
}
