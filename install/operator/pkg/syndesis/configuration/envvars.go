package configuration

import (
	"context"
	"errors"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"strings"
)

func GetSyndesisEnvVarsFromOpenShiftNamespace(ctx context.Context, client client.Client, namespace string) (map[string]string, error) {
	secret, err := GetSyndesisConfigurationSecret(ctx, client, namespace)
	if err != nil {
		return nil, err
	}

	if envBlob, present := secret.Data[SyndesisGlobalConfigParamsProperty]; present {
		return parseConfigurationBlob(envBlob), nil
	} else {
		return nil, errors.New("no configuration found")
	}

}

func parseConfigurationBlob(blob []byte) map[string]string {
	strs := strings.Split(string(blob), "\n")
	configs := make(map[string]string, 0)
	for _, conf := range strs {
		conf := strings.Trim(conf, " \r\t")
		if conf == "" {
			continue
		}
		kv := strings.SplitAfterN(conf, "=", 2)
		if len(kv) == 2 {
			key := strings.TrimRight(kv[0], "=")
			value := kv[1]
			configs[key] = value
		}
	}
	return configs
}
