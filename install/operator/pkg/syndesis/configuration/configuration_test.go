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
	"os"
	"reflect"
	"testing"

	"k8s.io/apimachinery/pkg/version"

	"sigs.k8s.io/controller-runtime/pkg/client"

	gofake "k8s.io/client-go/kubernetes/fake"

	"github.com/stretchr/testify/assert"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/capabilities"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	discoveryfake "k8s.io/client-go/discovery/fake"
)

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
						DV: DvConfiguration{
							Enabled: true,
							Image:   "DV_IMAGE",
						},
						CamelK: CamelKConfiguration{Image: "CAMELK_IMAGE"},
						Todo:   TodoConfiguration{Image: "TODO_IMAGE"},
					},
					Components: ComponentsSpec{
						Oauth:      OauthConfiguration{Image: "OAUTH_IMAGE"},
						UI:         UIConfiguration{Image: "UI_IMAGE"},
						S2I:        S2IConfiguration{Image: "S2I_IMAGE"},
						Prometheus: PrometheusConfiguration{Image: "PROMETHEUS_IMAGE"},
						Upgrade:    UpgradeConfiguration{Image: "UPGRADE_IMAGE"},
						Meta:       MetaConfiguration{Image: "META_IMAGE"},
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
					Addons: AddonsSpec{
						DV: DvConfiguration{
							Enabled: true,
							Image:   "docker.io/teiid/syndesis-dv:latest",
						},
					},
					Components: ComponentsSpec{
						Oauth:      OauthConfiguration{Image: "quay.io/openshift/origin-oauth-proxy:v4.0.0"},
						UI:         UIConfiguration{Image: "docker.io/syndesis/syndesis-ui:latest"},
						S2I:        S2IConfiguration{Image: "docker.io/syndesis/syndesis-s2i:latest"},
						Prometheus: PrometheusConfiguration{Image: "docker.io/prom/prometheus:v2.1.0"},
						Upgrade:    UpgradeConfiguration{Image: "docker.io/syndesis/syndesis-upgrade:latest"},
						Meta:       MetaConfiguration{Image: "docker.io/syndesis/syndesis-meta:latest"},
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
				"RELATED_IMAGE_DV": "DV_IMAGE", "RELATED_IMAGE_OAUTH": "OAUTH_IMAGE", "RELATED_IMAGE_PROMETHEUS": "PROMETHEUS_IMAGE",
				"RELATED_IMAGE_UPGRADE": "UPGRADE_IMAGE", "DATABASE_NAMESPACE": "DATABASE_NAMESPACE", "RELATED_IMAGE_DATABASE": "DATABASE_IMAGE",
				"RELATED_IMAGE_PSQL_EXPORTER": "PSQL_EXPORTER_IMAGE", "DEV_SUPPORT": "true", "TEST_SUPPORT": "false",
				"INTEGRATION_LIMIT": "30", "DEPLOY_INTEGRATIONS": "true", "RELATED_IMAGE_CAMELK": "CAMELK_IMAGE",
				"DATABASE_VOLUME_NAME": "nfs0002", "DATABASE_STORAGE_CLASS": "nfs-storage-class1",
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
		syndesis *v1beta1.Syndesis
	}
	tests := []struct {
		name       string
		args       args
		wantConfig *Config
		wantErr    bool
	}{
		{
			name:       "When using an empty syndesis custom resource, the config values from template should remain",
			args:       args{syndesis: &v1beta1.Syndesis{}},
			wantConfig: getConfigLiteral(),
			wantErr:    false,
		},
		{
			name: "When using a syndesis custom resource with values, those values should replace the template values",
			args: args{syndesis: &v1beta1.Syndesis{
				Spec: v1beta1.SyndesisSpec{
					Addons: v1beta1.AddonsSpec{
						Jaeger: v1beta1.JaegerConfiguration{
							Enabled:       true,
							SamplerType:   "const",
							SamplerParam:  "0",
							ImageAgent:    "jaegertracing/jaeger-agent:1.13",
							ImageAllInOne: "jaegertracing/all-in-one:1.13",
							ImageOperator: "jaegertracing/jaeger-operator:1.13",
						},
						Todo: v1beta1.AddonSpec{Enabled: true},
						DV: v1beta1.DvConfiguration{
							Enabled: true,
						},
						CamelK: v1beta1.AddonSpec{Enabled: true},
						PublicAPI: v1beta1.PublicAPIConfiguration{
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
							Enabled:       true,
							SamplerType:   "const",
							SamplerParam:  "0",
							ImageAgent:    "jaegertracing/jaeger-agent:1.13",
							ImageAllInOne: "jaegertracing/all-in-one:1.13",
							ImageOperator: "jaegertracing/jaeger-operator:1.13",
						},
						Ops: AddonConfiguration{Enabled: false},
						Todo: TodoConfiguration{
							Enabled: true,
							Image:   "docker.io/centos/php-71-centos7",
						},
						Knative: AddonConfiguration{Enabled: false},
						DV: DvConfiguration{
							Enabled:   true,
							Resources: Resources{Memory: "1024Mi"},
							Image:     "docker.io/teiid/syndesis-dv:latest",
						},
						CamelK: CamelKConfiguration{
							Enabled:       true,
							Image:         "fabric8/s2i-java:3.0-java8",
							CamelVersion:  "3.1.0",
							CamelKRuntime: "1.1.0",
						},
						PublicAPI: PublicAPIConfiguration{
							Enabled:       true,
							RouteHostname: "mypublichost.com",
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
					Enabled:       false,
					SamplerType:   "const",
					SamplerParam:  "0",
					ImageAgent:    "jaegertracing/jaeger-agent:1.13",
					ImageAllInOne: "jaegertracing/all-in-one:1.13",
					ImageOperator: "jaegertracing/jaeger-operator:1.13",
				},
				Ops: AddonConfiguration{Enabled: false},
				Todo: TodoConfiguration{
					Enabled: false,
					Image:   "docker.io/centos/php-71-centos7",
				},
				DV: DvConfiguration{
					Enabled:   false,
					Image:     "docker.io/teiid/syndesis-dv:latest",
					Resources: Resources{Memory: "1024Mi"},
				},
				CamelK: CamelKConfiguration{
					Enabled:       false,
					CamelVersion:  "3.1.0",
					CamelKRuntime: "1.1.0",
					Image:         "fabric8/s2i-java:3.0-java8",
				},
				PublicAPI: PublicAPIConfiguration{
					Enabled:       true,
					RouteHostname: "mypublichost.com",
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
					Image:     "docker.io/syndesis/syndesis-server:latest",
					Resources: Resources{Memory: "800Mi"},
					Features: ServerFeatures{
						IntegrationLimit:              0,
						IntegrationStateCheckInterval: 60,
						DeployIntegrations:            true,
						TestSupport:                   false,
						OpenShiftMaster:               "https://localhost:8443",
						MavenRepositories: map[string]string{
							"central":           "https://repo.maven.apache.org/maven2/",
							"repo-02-redhat-ga": "https://maven.repository.redhat.com/ga/",
							"repo-03-jboss-ea":  "https://repository.jboss.org/nexus/content/groups/ea/",
						},
					},
				},
				Meta: MetaConfiguration{
					Image: "docker.io/syndesis/syndesis-meta:latest",
					Resources: ResourcesWithVolume{
						Memory:         "512Mi",
						VolumeCapacity: "1Gi",
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
						Memory:           "255Mi",
						VolumeCapacity:   "1Gi",
						VolumeAccessMode: string(v1beta1.ReadWriteOnce),
					},
				},
				Prometheus: PrometheusConfiguration{
					Image: "docker.io/prom/prometheus:v2.1.0",
					Resources: ResourcesWithVolume{
						Memory:         "512Mi",
						VolumeCapacity: "1Gi",
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
		ctx      context.Context
		client   client.Client
		syndesis *v1beta1.Syndesis
	}
	tests := []struct {
		name    string
		args    args
		env     map[string]string
		wantErr bool
		want    string
	}{
		{
			name: "If ROUTE_HOSTNAME environment variable is set, config.RouteHostname should take that value",
			args: args{
				ctx:      context.TODO(),
				client:   nil,
				syndesis: nil,
			},
			wantErr: false,
			env:     map[string]string{"ROUTE_HOSTNAME": "some_value"},
			want:    "some_value",
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			for k, v := range tt.env {
				os.Setenv(k, v)
			}

			config := getConfigLiteral()
			if err := config.SetRoute(tt.args.ctx, tt.args.client, tt.args.syndesis); (err != nil) != tt.wantErr {
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

func Test_ApiCapabilities(t *testing.T) {

	res1 := metav1.APIResourceList{
		GroupVersion: "image.openshift.io/v1",
	}
	res2 := metav1.APIResourceList{
		GroupVersion: "route.openshift.io/v1",
	}
	res3 := metav1.APIResourceList{
		GroupVersion: "oauth.openshift.io/v1",
	}

	res4 := metav1.APIResourceList{
		GroupVersion: "something.openshift.io/v1",
	}
	res5 := metav1.APIResourceList{
		GroupVersion: "not.anything.io/v1",
	}
	res6 := metav1.APIResourceList{
		GroupVersion: "something.else.io/v1",
	}

	testCases := []struct {
		name     string
		resList  []*metav1.APIResourceList
		expected capabilities.ApiServerSpec
	}{
		{
			"Relevant APIs available for fully true api spec",
			[]*metav1.APIResourceList{&res1, &res2, &res3},
			capabilities.ApiServerSpec{
				Version:          "1.16",
				Routes:           true,
				ImageStreams:     true,
				EmbeddedProvider: true,
			},
		},
		{
			"No relevant resources so expect false",
			[]*metav1.APIResourceList{&res4, &res5, &res6},
			capabilities.ApiServerSpec{
				Version:          "1.16",
				Routes:           false,
				ImageStreams:     false,
				EmbeddedProvider: false,
			},
		},
		{
			"No resources so everything false",
			[]*metav1.APIResourceList{},
			capabilities.ApiServerSpec{
				Version:          "1.16",
				Routes:           false,
				ImageStreams:     false,
				EmbeddedProvider: false,
			},
		},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			api := gofake.NewSimpleClientset()
			fd := api.Discovery().(*discoveryfake.FakeDiscovery)
			fd.Resources = tc.resList
			fd.FakedServerVersion = &version.Info{
				Major: "1",
				Minor: "16",
			}

			apiSpec, err := capabilities.ApiCapabilities(api)
			if err != nil {
				t.Error(err)
			}

			if apiSpec == nil {
				t.Error("Failed to return an api specification")
			}

			if apiSpec.Version != tc.expected.Version {
				t.Error("Expected api specification version not expected")
			}

			if apiSpec.Routes != tc.expected.Routes {
				t.Error("Expected api specification routes not expected")
			}

			if apiSpec.ImageStreams != tc.expected.ImageStreams {
				t.Error("Expected api specification image streams not expected")
			}

			if apiSpec.EmbeddedProvider != tc.expected.EmbeddedProvider {
				t.Error("Expected api specification embedded provider not returned")
			}
		})
	}
}
