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
	"encoding/base64"
	"os"
	"reflect"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta2"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/capabilities"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func Test_GetAddons(t *testing.T) {
	config := getConfigLiteral()
	addons := GetAddonsInfo(*config)

	assert.True(t, len(addons) > 0)

	for _, addon := range addons {
		switch addon.Name() {
		case "jaeger":
			assert.Equal(t, config.Syndesis.Addons.Jaeger.Name(), addon.Name())
			assert.Equal(t, config.Syndesis.Addons.Jaeger.Enabled, addon.IsEnabled())
			assert.Equal(t, config.Syndesis.Addons.Jaeger.Olm, *addon.GetOlmSpec())
		case "ops":
			assert.Equal(t, config.Syndesis.Addons.Ops.Name(), addon.Name())
			assert.Equal(t, config.Syndesis.Addons.Ops.Enabled, addon.IsEnabled())
		case "camelk":
			assert.Equal(t, config.Syndesis.Addons.CamelK.Name(), addon.Name())
			assert.Equal(t, config.Syndesis.Addons.CamelK.Enabled, addon.IsEnabled())
		case "knative":
			assert.Equal(t, config.Syndesis.Addons.Knative.Name(), addon.Name())
			assert.Equal(t, config.Syndesis.Addons.Knative.Enabled, addon.IsEnabled())
		case "todo":
			assert.Equal(t, config.Syndesis.Addons.Todo.Name(), addon.Name())
			assert.Equal(t, config.Syndesis.Addons.Todo.Enabled, addon.IsEnabled())
		case "publicApi":
			assert.Equal(t, config.Syndesis.Addons.PublicAPI.Name(), addon.Name())
			assert.Equal(t, config.Syndesis.Addons.PublicAPI.Enabled, addon.IsEnabled())
		default:
			t.Errorf("addon name %s not recognised", addon.Name())
		}
	}
}

func Test_loadFromFile(t *testing.T) {
	type args struct {
		file string
	}
	tests := []struct {
		name    string
		args    args
		want    *Config
		wantErr bool
	}{
		{
			name:    "When loading the from file, a valid configuration should be loaded",
			args:    args{file: "../../../build/conf/config-test.yaml"},
			want:    getConfigLiteral(),
			wantErr: false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got := &Config{}
			err := got.loadFromFile(tt.args.file)
			if (err != nil) != tt.wantErr {
				t.Errorf("loadFromFile() error = %v, wantErr %v", err, tt.wantErr)
				return
			}

			if !reflect.DeepEqual(got, tt.want) {
				t.Errorf("loadFromFile() got = %v, want %v", got, tt.want)
			}
		})
	}
}

func Test_setConfigFromEnv(t *testing.T) {
	tests := []struct {
		name    string
		conf    *Config
		want    *Config
		env     map[string]string
		wantErr bool
	}{
		{
			name: "When all environment variables are set for images, a valid configuration with all values should be created",
			want: &Config{
				Productized: true,
				ProductName: "something",
				DevSupport:  true,
				ApiServer: capabilities.ApiServerSpec{
					Version:          "1.16",
					Routes:           true,
					ImageStreams:     true,
					EmbeddedProvider: true,
				},
				Syndesis: SyndesisConfig{
					RouteHostname: "route",
					Addons: AddonsSpec{
						CamelK: CamelKConfiguration{Image: "CAMELK_IMAGE"},
						Todo:   TodoConfiguration{Image: "TODO_IMAGE"},
					},
					Components: ComponentsSpec{
						Oauth:      OauthConfiguration{Image: "OAUTH_IMAGE"},
						UI:         UIConfiguration{Image: "UI_IMAGE"},
						S2I:        S2IConfiguration{Image: "S2I_IMAGE"},
						Prometheus: PrometheusConfiguration{Image: "PROMETHEUS_IMAGE"},
						Upgrade:    UpgradeConfiguration{Image: "UPGRADE_IMAGE"},
						Meta: MetaConfiguration{
							Image: "META_IMAGE",
							Resources: ResourcesWithPersistentVolume{
								VolumeAccessMode:   "ReadWriteOnce",
								VolumeStorageClass: "nfs-storage-class10",
								VolumeName:         "nfs0020",
							},
						},
						Database: DatabaseConfiguration{
							Image:    "DATABASE_IMAGE",
							Exporter: ExporterConfiguration{Image: "PSQL_EXPORTER_IMAGE"},
							Resources: ResourcesWithPersistentVolume{
								VolumeAccessMode:   "ReadWriteOnce",
								VolumeStorageClass: "nfs-storage-class1",
								VolumeName:         "nfs0002",
							},
						},
						Server: ServerConfiguration{
							Image: "SERVER_IMAGE",
							Features: ServerFeatures{
								TestSupport: false,
							},
						},
						AMQ: AMQConfiguration{Image: "AMQ_IMAGE"},
					},
				},
			},
			conf: &Config{
				Productized: true,
				ProductName: "something",
				DevSupport:  true,
				ApiServer: capabilities.ApiServerSpec{
					Version:          "1.16",
					Routes:           true,
					ImageStreams:     true,
					EmbeddedProvider: true,
				},
				Syndesis: SyndesisConfig{
					RouteHostname: "route",
					Addons:        AddonsSpec{},
					Components: ComponentsSpec{
						Oauth:      OauthConfiguration{Image: "quay.io/openshift/origin-oauth-proxy:v4.0.0"},
						UI:         UIConfiguration{Image: "docker.io/syndesis/syndesis-ui:latest"},
						S2I:        S2IConfiguration{Image: "docker.io/syndesis/syndesis-s2i:latest"},
						Prometheus: PrometheusConfiguration{Image: "docker.io/prom/prometheus:v2.1.0"},
						Upgrade:    UpgradeConfiguration{Image: "docker.io/syndesis/syndesis-upgrade:latest"},
						Meta: MetaConfiguration{
							Image: "docker.io/syndesis/syndesis-meta:latest",
							Resources: ResourcesWithPersistentVolume{
								VolumeAccessMode:   "ReadWriteMany",
								VolumeStorageClass: "nfs-storage-class",
								VolumeName:         "nfs0011",
							},
						},
						Database: DatabaseConfiguration{
							Exporter: ExporterConfiguration{Image: "docker.io/wrouesnel/postgres_exporter:v0.4.7"},
							Resources: ResourcesWithPersistentVolume{
								VolumeAccessMode:   "ReadWriteMany",
								VolumeStorageClass: "nfs-storage-class",
								VolumeName:         "nfs0001",
							},
						},
						Server: ServerConfiguration{Image: "docker.io/syndesis/syndesis-server:latest"},
					},
				},
			},
			env: map[string]string{
				"RELATED_PSQL": "PSQL_IMAGE", "RELATED_IMAGE_S2I": "S2I_IMAGE", "RELATED_IMAGE_OPERATOR": "OPERATOR_IMAGE",
				"RELATED_IMAGE_UI": "UI_IMAGE", "RELATED_IMAGE_SERVER": "SERVER_IMAGE", "RELATED_IMAGE_META": "META_IMAGE",
				"RELATED_IMAGE_OAUTH": "OAUTH_IMAGE", "RELATED_IMAGE_PROMETHEUS": "PROMETHEUS_IMAGE",
				"RELATED_IMAGE_UPGRADE": "UPGRADE_IMAGE", "DATABASE_NAMESPACE": "DATABASE_NAMESPACE", "RELATED_IMAGE_DATABASE": "DATABASE_IMAGE",
				"RELATED_IMAGE_PSQL_EXPORTER": "PSQL_EXPORTER_IMAGE", "DEV_SUPPORT": "true", "TEST_SUPPORT": "false",
				"INTEGRATION_LIMIT": "30", "DEPLOY_INTEGRATIONS": "true", "RELATED_IMAGE_CAMELK": "CAMELK_IMAGE",
				"META_VOLUME_NAME": "nfs0020", "META_STORAGE_CLASS": "nfs-storage-class10",
				"META_VOLUME_ACCESS_MODE": "ReadWriteOnce",
				"DATABASE_VOLUME_NAME":    "nfs0002", "DATABASE_STORAGE_CLASS": "nfs-storage-class1",
				"DATABASE_VOLUME_ACCESS_MODE": "ReadWriteOnce", "RELATED_IMAGE_TODO": "TODO_IMAGE", "RELATED_IMAGE_AMQ": "AMQ_IMAGE",
			},
			wantErr: false,
		},
		{
			name:    "When no environment variables are set for images, a valid configuration with the original images should be created",
			want:    getConfigLiteral(),
			conf:    getConfigLiteral(),
			env:     map[string]string{},
			wantErr: false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			for k, v := range tt.env {
				os.Setenv(k, v)
			}

			err := tt.conf.setConfigFromEnv()
			if (err != nil) != tt.wantErr {
				t.Errorf("loadFromFile() error = %v, wantErr %v", err, tt.wantErr)
				return
			}
			if !reflect.DeepEqual(tt.conf, tt.want) {
				t.Errorf("loadFromFile() got = %v, want %v", tt.conf, tt.want)
			}

			for k := range tt.env {
				os.Unsetenv(k)
			}
		})
	}
}

func Test_setSyndesisFromCustomResource(t *testing.T) {
	type args struct {
		syndesis *v1beta2.Syndesis
	}
	tests := []struct {
		name       string
		args       args
		wantConfig *Config
		wantErr    bool
	}{
		{
			name:       "When using an empty syndesis custom resource, the config values from template should remain",
			args:       args{syndesis: &v1beta2.Syndesis{}},
			wantConfig: getConfigLiteral(),
			wantErr:    false,
		},
		{
			name: "When using a syndesis custom resource with values, those values should replace the template values",
			args: args{syndesis: &v1beta2.Syndesis{
				Spec: v1beta2.SyndesisSpec{
					Addons: v1beta2.AddonsSpec{
						Jaeger: v1beta2.JaegerConfiguration{
							Enabled:       true,
							SamplerType:   "const",
							SamplerParam:  "0",
							ImageAgent:    "jaegertracing/jaeger-agent:1.13",
							ImageAllInOne: "jaegertracing/all-in-one:1.13",
							ImageOperator: "jaegertracing/jaeger-operator:1.13",
						},
						Todo:   v1beta2.AddonSpec{Enabled: true},
						CamelK: v1beta2.AddonSpec{Enabled: true},
						PublicAPI: v1beta2.PublicAPIConfiguration{
							Enabled:       true,
							RouteHostname: "mypublichost.com",
						},
					},
				},
			}},
			wantConfig: &Config{
				Syndesis: SyndesisConfig{
					Addons: AddonsSpec{
						Jaeger: JaegerConfiguration{
							Enabled: true,
							Olm: OlmSpec{
								Package: "jaeger",
								Channel: "stable",
							},
							SamplerType:   "const",
							SamplerParam:  "0",
							ImageAgent:    "jaegertracing/jaeger-agent:1.13",
							ImageAllInOne: "jaegertracing/all-in-one:1.13",
							ImageOperator: "jaegertracing/jaeger-operator:1.13",
						},
						Ops: OpsConfiguration{
							AddonConfiguration: AddonConfiguration{Enabled: false},
						},
						Todo: TodoConfiguration{
							AddonConfiguration: AddonConfiguration{Enabled: true},
							Image:              "docker.io/centos/php-71-centos7",
						},
						Knative: KnativeConfiguration{
							AddonConfiguration: AddonConfiguration{Enabled: false},
						},
						CamelK: CamelKConfiguration{
							AddonConfiguration: AddonConfiguration{Enabled: true},
							Image:              "fabric8/s2i-java:3.0-java8",
							CamelVersion:       "3.4.4",
							CamelKRuntime:      "1.3.0",
						},
						PublicAPI: PublicAPIConfiguration{
							AddonConfiguration: AddonConfiguration{Enabled: true},
							RouteHostname:      "mypublichost.com",
						},
					},
				},
			},
			wantErr: false,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got := getConfigLiteral()
			err := got.setSyndesisFromCustomResource(tt.args.syndesis)
			if (err != nil) != tt.wantErr {
				t.Errorf("setSyndesisFromCustomResource() error = %v, wantErr %v", err, tt.wantErr)
				return
			}

			if !reflect.DeepEqual(got.Syndesis.Addons, tt.wantConfig.Syndesis.Addons) {
				t.Errorf("setSyndesisFromCustomResource() gotConfig = %v, want %v", got.Syndesis.Addons, tt.wantConfig.Syndesis.Addons)
			}
		})
	}
}

func Test_MavenSettings(t *testing.T) {
	c := getConfigLiteral()
	s := &v1beta2.Syndesis{
		Spec: v1beta2.SyndesisSpec{
			Components: v1beta2.ComponentsSpec{
				Server: v1beta2.ServerConfiguration{
					Features: v1beta2.ServerFeatures{
						Maven: v1beta2.MavenConfiguration{
							Append: false,
							Repositories: map[string]string{
								"rep1": "http://rep1",
								"rep2": "http://rep2",
							},
						},
					},
				},
			},
		},
	}

	c.setSyndesisFromCustomResource(s)
	assert.Equal(t, c.Syndesis.Components.Server.Features.Maven.Repositories, s.Spec.Components.Server.Features.Maven.Repositories)

	s.Spec.Components.Server.Features.Maven.Append = true
	c.Syndesis.Components.Server.Features.Maven.Repositories = map[string]string{"rep0": "http://rep0"}
	c.setSyndesisFromCustomResource(s)
	assert.Equal(t, c.Syndesis.Components.Server.Features.Maven.Repositories, map[string]string{
		"rep0": "http://rep0",
		"rep1": "http://rep1",
		"rep2": "http://rep2",
	})
}

func Test_generatePasswords(t *testing.T) {
	tests := []struct {
		name   string
		got    *Config
		length [7]int
	}{
		{
			name:   "Passwords and secrets should be generated when they values are empty",
			got:    &Config{},
			length: [7]int{64, 16, 16, 32, 64, 32, 32},
		},
		{
			name: "Passwords and secrets should be generated when they values are empty",
			got: &Config{
				OpenShiftOauthClientSecret: "swer",
				Syndesis: SyndesisConfig{
					Components: ComponentsSpec{
						Oauth: OauthConfiguration{CookieSecret: "qwerqwer"},
						Database: DatabaseConfiguration{
							Password:         "1234qwer",
							SampledbPassword: "12ed",
						},
						Server: ServerConfiguration{
							SyndesisEncryptKey:           "poyotu",
							ClientStateAuthenticationKey: "pogkth",
							ClientStateEncryptionKey:     "12",
						},
					},
				},
			},
			length: [7]int{4, 8, 4, 8, 6, 6, 2},
		},
	}
	for _, tt := range tests {
		tt.got.generatePasswords()
		t.Run(tt.name, func(t *testing.T) {
			assert.Len(t, tt.got.OpenShiftOauthClientSecret, tt.length[0])
			assert.Len(t, tt.got.Syndesis.Components.Database.Password, tt.length[1])
			assert.Len(t, tt.got.Syndesis.Components.Database.SampledbPassword, tt.length[2])
			assert.Len(t, tt.got.Syndesis.Components.Oauth.CookieSecret, tt.length[3])
			assert.Len(t, tt.got.Syndesis.Components.Server.SyndesisEncryptKey, tt.length[4])
			assert.Len(t, tt.got.Syndesis.Components.Server.ClientStateAuthenticationKey, tt.length[5])
			assert.Len(t, tt.got.Syndesis.Components.Server.ClientStateEncryptionKey, tt.length[6])
		})
	}
}

// Return a config object as loaded from config file,
// but without using the loadFromFile function
func getConfigLiteral() *Config {
	return &Config{
		Version:                    "7.7.0",
		ProductName:                "syndesis",
		AllowLocalHost:             false,
		Productized:                false,
		DevSupport:                 false,
		Scheduled:                  true,
		PrometheusRules:            "",
		OpenShiftProject:           "",
		OpenShiftOauthClientSecret: "",
		OpenShiftConsoleURL:        "",
		Syndesis: SyndesisConfig{
			RouteHostname: "",
			SHA:           false,
			Addons: AddonsSpec{
				Jaeger: JaegerConfiguration{
					Enabled: false,
					Olm: OlmSpec{
						Package: "jaeger",
						Channel: "stable",
					},
					SamplerType:   "const",
					SamplerParam:  "0",
					ImageAgent:    "jaegertracing/jaeger-agent:1.13",
					ImageAllInOne: "jaegertracing/all-in-one:1.13",
					ImageOperator: "jaegertracing/jaeger-operator:1.13",
				},
				Ops: OpsConfiguration{
					AddonConfiguration: AddonConfiguration{Enabled: false},
				},
				Todo: TodoConfiguration{
					AddonConfiguration: AddonConfiguration{Enabled: false},
					Image:              "docker.io/centos/php-71-centos7",
				},
				Knative: KnativeConfiguration{
					AddonConfiguration: AddonConfiguration{Enabled: false},
				},
				CamelK: CamelKConfiguration{
					AddonConfiguration: AddonConfiguration{Enabled: false},
					CamelVersion:       "3.4.4",
					CamelKRuntime:      "1.3.0",
					Image:              "fabric8/s2i-java:3.0-java8",
				},
				PublicAPI: PublicAPIConfiguration{
					AddonConfiguration: AddonConfiguration{Enabled: true},
					RouteHostname:      "mypublichost.com",
				},
			},
			Components: ComponentsSpec{
				Oauth: OauthConfiguration{
					Image: "quay.io/openshift/origin-oauth-proxy:v4.0.0",
				},
				UI: UIConfiguration{
					Image: "docker.io/syndesis/syndesis-ui:latest",
				},
				S2I: S2IConfiguration{
					Image: "docker.io/syndesis/syndesis-s2i:latest",
				},
				Server: ServerConfiguration{
					Image: "docker.io/syndesis/syndesis-server:latest",
					Resources: Resources{
						Limit: ResourceParams{
							Memory: "800Mi",
							CPU:    "750m",
						},
						Request: ResourceParams{
							Memory: "256Mi",
							CPU:    "450m",
						},
					},
					Features: ServerFeatures{
						IntegrationLimit:              0,
						IntegrationStateCheckInterval: 60,
						DeployIntegrations:            true,
						TestSupport:                   false,
						OpenShiftMaster:               "https://localhost:8443",
						Maven: MavenConfiguration{
							Append:              false,
							AdditionalArguments: "-Daaaaa=bbbbb",
							Repositories: map[string]string{
								"central":           "https://repo.maven.apache.org/maven2/",
								"repo-02-redhat-ga": "https://maven.repository.redhat.com/ga/",
								"repo-03-jboss-ea":  "https://repository.jboss.org/nexus/content/groups/ea/",
							},
						},
					},
				},
				Meta: MetaConfiguration{
					Image: "docker.io/syndesis/syndesis-meta:latest",
					Resources: ResourcesWithPersistentVolume{
						Limit: ResourceParams{
							Memory: "512Mi",
						},
						Request: ResourceParams{
							Memory: "280Mi",
						},
						VolumeCapacity:   "1Gi",
						VolumeAccessMode: string(v1beta2.ReadWriteOnce),
					},
				},
				Database: DatabaseConfiguration{
					Image: "postgresql:9.6",
					User:  "syndesis",
					Name:  "syndesis",
					URL:   "postgresql://syndesis-db:5432/syndesis?sslmode=disable",
					Exporter: ExporterConfiguration{
						Image: "docker.io/wrouesnel/postgres_exporter:v0.4.7",
					},
					Resources: ResourcesWithPersistentVolume{
						Limit: ResourceParams{
							Memory: "255Mi",
						},
						Request: ResourceParams{
							Memory: "255Mi",
						},
						VolumeCapacity:   "1Gi",
						VolumeAccessMode: string(v1beta2.ReadWriteOnce),
					},
				},
				Prometheus: PrometheusConfiguration{
					Image: "docker.io/prom/prometheus:v2.1.0",
					Resources: ResourcesWithPersistentVolume{
						Limit: ResourceParams{
							Memory: "512Mi",
						},
						Request: ResourceParams{
							Memory: "512Mi",
						},
						VolumeCapacity:   "1Gi",
						VolumeAccessMode: string(v1beta2.ReadWriteOnce),
					},
				},
				Upgrade: UpgradeConfiguration{
					Image:     "docker.io/syndesis/syndesis-upgrade:latest",
					Resources: VolumeOnlyResources{VolumeCapacity: "1Gi"},
				},
				AMQ: AMQConfiguration{
					Image: "registry.access.redhat.com/jboss-amq-6/amq63-openshift:1.3",
				},
			},
		},
	}
}

func Test_setBoolFromEnv(t *testing.T) {
	type args struct {
		env     string
		current bool
	}
	tests := []struct {
		name string
		args args
		want bool
		env  map[string]string
	}{
		{"With no env, false value should stay false", args{"NOT_EXISTING_ENV", false}, false, map[string]string{}},
		{"With no env, true value should stay true", args{"NOT_EXISTING_ENV", true}, true, map[string]string{}},
		{"With env set to true, a value of true should stay true", args{"EXISTING_ENV", true}, true, map[string]string{"EXISTING_ENV": "true"}},
		{"With env set to true, a value of false should change to true", args{"EXISTING_ENV", false}, true, map[string]string{"EXISTING_ENV": "true"}},
		{"With env set to false, a value of true should change to false", args{"EXISTING_ENV", true}, false, map[string]string{"EXISTING_ENV": "false"}},
		{"With env set to false, a value of false should stay false", args{"EXISTING_ENV", false}, false, map[string]string{"EXISTING_ENV": "false"}},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			for k, v := range tt.env {
				os.Setenv(k, v)
			}

			if got := setBoolFromEnv(tt.args.env, tt.args.current); got != tt.want {
				t.Errorf("setBoolFromEnv() = %v, want %v", got, tt.want)
			}

			for k := range tt.env {
				os.Unsetenv(k)
			}
		})
	}
}

func TestConfig_SetRoute(t *testing.T) {
	type args struct {
		ctx           context.Context
		routeHostname string
	}
	tests := []struct {
		name    string
		args    args
		env     map[string]string
		wantErr bool
		want    string
	}{
		{
			name: "ROUTE_HOSTNAME environment variable NOT set, config.RouteHostname should take the value as given",
			args: args{
				ctx:           context.TODO(),
				routeHostname: "my_route_name",
			},
			env:     map[string]string{},
			wantErr: false,
			want:    "my_route_name",
		},
		{
			name: "If ROUTE_HOSTNAME environment variable is set, config.RouteHostname should take that value",
			args: args{
				ctx:           context.TODO(),
				routeHostname: "my_route_name",
			},
			env:     map[string]string{"ROUTE_HOSTNAME": "some_value"},
			wantErr: false,
			want:    "some_value",
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			for k, v := range tt.env {
				os.Setenv(k, v)
			}

			config := getConfigLiteral()
			if err := config.SetRoute(tt.args.ctx, tt.args.routeHostname); (err != nil) != tt.wantErr {
				t.Errorf("SetRoute() error = %v, wantErr %v", err, tt.wantErr)
			}
			assert.Equal(t, config.Syndesis.RouteHostname, tt.want)

			for k := range tt.env {
				os.Unsetenv(k)
			}
		})
	}
}

func Test_setIntFromEnv(t *testing.T) {
	type args struct {
		env     string
		current int
	}
	tests := []struct {
		name string
		args args
		want int
		env  map[string]string
	}{
		{"With no env, default value should not change", args{"NOT_EXISTING_ENV", 10}, 10, map[string]string{}},
		{"With env set to a value, the default should take that value", args{"EXISTING_ENV", 10}, 30, map[string]string{"EXISTING_ENV": "30"}},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			for k, v := range tt.env {
				os.Setenv(k, v)
			}

			if got := setIntFromEnv(tt.args.env, tt.args.current); got != tt.want {
				t.Errorf("setIntFromEnv() = %v, want %v", got, tt.want)
			}

			for k := range tt.env {
				os.Unsetenv(k)
			}
		})
	}
}

func Test_secretToEnvVars(t *testing.T) {
	secret := &corev1.Secret{
		ObjectMeta: metav1.ObjectMeta{
			Name:      "my-secret",
			Namespace: "default",
		},
		Data: map[string][]byte{
			"KEY_1": []byte(base64.StdEncoding.EncodeToString([]byte("example1key1"))),
			"KEY_2": []byte(base64.StdEncoding.EncodeToString([]byte("example1key2"))),
			"KEY_3": []byte(base64.StdEncoding.EncodeToString([]byte("example1key3"))),
		},
	}

	//
	// Note indenting by 2 tabs or 4 spaces
	//
	data, err := SecretToEnvVars(secret.Name, secret.Data, 2)
	require.NoError(t, err)

	expected := "" +
		"    - name: KEY_1\n" +
		"      valueFrom:\n" +
		"        secretKeyRef:\n" +
		"          key: KEY_1\n" +
		"          name: my-secret\n" +
		"    - name: KEY_2\n" +
		"      valueFrom:\n" +
		"        secretKeyRef:\n" +
		"          key: KEY_2\n" +
		"          name: my-secret\n" +
		"    - name: KEY_3\n" +
		"      valueFrom:\n" +
		"        secretKeyRef:\n" +
		"          key: KEY_3\n" +
		"          name: my-secret\n"

	assert.Equal(t, expected, string(data))
}
