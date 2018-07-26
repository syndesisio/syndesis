package serviceaccount

import (
	"errors"
	"github.com/operator-framework/operator-sdk/pkg/sdk"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func GetServiceAccountToken(serviceAccountName string, namespace string) (string, error) {

	sa := corev1.ServiceAccount{
		TypeMeta: metav1.TypeMeta{
			Kind: "ServiceAccount",
			APIVersion: "v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name: serviceAccountName,
			Namespace: namespace,
		},
	}
	if err := sdk.Get(&sa); err != nil {
		return "", err
	}

	for _, reference := range sa.Secrets {
		secret := corev1.Secret{
			TypeMeta: metav1.TypeMeta{
				Kind: "Secret",
				APIVersion: "v1",
			},
			ObjectMeta: metav1.ObjectMeta{
				Name: reference.Name,
				Namespace: namespace,
			},
		}
		if err := sdk.Get(&secret); err != nil {
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