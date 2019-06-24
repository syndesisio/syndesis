// Provides information on Syndesis versions.
package configuration

import (
	"context"
	"errors"
	"k8s.io/api/core/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"strings"
)

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
