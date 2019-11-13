/*
 * Copyright (C) 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package configuration

import (
	"context"
	"errors"
	"strings"

	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	v1 "k8s.io/api/core/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

const (
	SyndesisGlobalConfigSecret         = "syndesis-global-config"
	SyndesisGlobalConfigParamsProperty = "params"
)

func getSyndesisEnvVarsFromOpenShiftNamespace(ctx context.Context, client client.Client, namespace string) (map[string]string, error) {
	secret, err := getSyndesisConfigurationSecret(ctx, client, namespace)
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

func getSyndesisConfigurationSecret(ctx context.Context, client client.Client, namespace string) (*v1.Secret, error) {
	secret := v1.Secret{}
	if err := client.Get(ctx, util.NewObjectKey(SyndesisGlobalConfigSecret, namespace), &secret); err != nil {
		return nil, err
	}
	return &secret, nil
}
