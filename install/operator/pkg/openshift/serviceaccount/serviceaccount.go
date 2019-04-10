package serviceaccount

import (
	"context"
	"errors"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	corev1 "k8s.io/api/core/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

func GetServiceAccountToken(ctx context.Context, client client.Client, saName string, namespace string) (string, error) {

	sa := corev1.ServiceAccount{}
	if err := client.Get(ctx, util.NewObjectKey(saName, namespace), &sa); err != nil {
		return "", err
	}

	for _, reference := range sa.Secrets {
		secret := corev1.Secret{}
		if err := client.Get(ctx, util.NewObjectKey(reference.Name, namespace), &secret); err != nil {
			return "", err
		}

		if isValidServiceAccountToken(&sa, &secret) {
			token, exists := secret.Data[corev1.ServiceAccountTokenKey]
			if !exists {
				return "", errors.New("service account token did not contain token data")
			}

			return string(token), nil
		}
	}

	return "", errors.New("service account token not found")
}

func isValidServiceAccountToken(serviceAccount *corev1.ServiceAccount, secret *corev1.Secret) bool {
	if secret.Type != corev1.SecretTypeServiceAccountToken {
		return false
	}
	if secret.Namespace != serviceAccount.Namespace {
		return false
	}
	if secret.Annotations[corev1.ServiceAccountNameKey] != serviceAccount.Name {
		return false
	}
	if secret.Annotations[corev1.ServiceAccountUIDKey] != string(serviceAccount.UID) {
		return false
	}
	if len(secret.Data[corev1.ServiceAccountTokenKey]) == 0 {
		return false
	}
	return true
}
