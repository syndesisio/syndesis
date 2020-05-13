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
	"bytes"
	"context"
	"io/ioutil"
	"net/http"
	"os"
	"reflect"
	"testing"

	"sigs.k8s.io/controller-runtime/pkg/client"

	v1 "k8s.io/api/core/v1"
	"k8s.io/client-go/rest"
	restclient "k8s.io/client-go/rest"
	"k8s.io/client-go/rest/fake"
	"k8s.io/kubectl/pkg/scheme"

	"k8s.io/client-go/kubernetes"

	"github.com/stretchr/testify/assert"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
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
				"PSQL_IMAGE": "PSQL_IMAGE", "S2I_IMAGE": "S2I_IMAGE", "OPERATOR_IMAGE": "OPERATOR_IMAGE",
				"UI_IMAGE": "UI_IMAGE", "SERVER_IMAGE": "SERVER_IMAGE", "META_IMAGE": "META_IMAGE",
				"DV_IMAGE": "DV_IMAGE", "OAUTH_IMAGE": "OAUTH_IMAGE", "PROMETHEUS_IMAGE": "PROMETHEUS_IMAGE",
				"UPGRADE_IMAGE": "UPGRADE_IMAGE", "DATABASE_NAMESPACE": "DATABASE_NAMESPACE", "DATABASE_IMAGE": "DATABASE_IMAGE",
				"PSQL_EXPORTER_IMAGE": "PSQL_EXPORTER_IMAGE", "DEV_SUPPORT": "true", "TEST_SUPPORT": "false",
				"INTEGRATION_LIMIT": "30", "DEPLOY_INTEGRATIONS": "true", "CAMELK_IMAGE": "CAMELK_IMAGE",
				"DATABASE_VOLUME_NAME": "nfs0002", "DATABASE_STORAGE_CLASS": "nfs-storage-class1",
				"DATABASE_VOLUME_ACCESS_MODE": "ReadWriteOnce", "TODO_IMAGE": "TODO_IMAGE", "AMQ_IMAGE": "AMQ_IMAGE",
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
						PublicApi: v1beta1.PublicApiConfiguration{
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
							CamelVersion:  "2.23.2.fuse-760024",
							CamelKRuntime: "0.3.4.fuse-740008",
						},
						PublicApi: PublicApiConfiguration{
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
		OpenShiftConsoleUrl:        "",
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
					CamelVersion:  "2.23.2.fuse-760024",
					CamelKRuntime: "0.3.4.fuse-740008",
					Image:         "fabric8/s2i-java:3.0-java8",
				},
				PublicApi: PublicApiConfiguration{
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

func Test_PostgreVersionParsingRegexp(t *testing.T) {
	tests := []struct {
		version string
		value   string
	}{
		{"postgres (PostgreSQL) 10.6 (Debian 10.6-1.pgdg90+1)", "10.6"},
		{"PostgreSQL 9.5.14", "9.5"},
	}

	for _, test := range tests {
		extracted := postgresVersionRegex.FindStringSubmatch(test.version)
		if len(extracted) < 2 || extracted[1] != test.value {
			t.Errorf("Expecting that version %s would be extracted from %s, but it was %s", test.version, test.value, extracted)
		}
	}
}

func Test_postgreSQLVersionFromInitPod(t *testing.T) {
	os.Setenv("POD_NAME", "syndesis-operator-3-crpjp")
	defer func() { os.Unsetenv("POD_NAME") }()

	// this simply returns the same HTTP response for every request
	fakeClient := &fake.RESTClient{
		GroupVersion:         v1.SchemeGroupVersion,
		NegotiatedSerializer: scheme.Codecs,
		Client: fake.CreateHTTPClient(func(req *http.Request) (*http.Response, error) {
			expected := "http://localhost/apis/v1/namespaces/syndesis/pods/syndesis-operator-3-crpjp/log?container=postgres-version"
			if req.URL.String() != expected {
				t.Errorf("Expecting to fetch pod log via URL like `%s`, but it was `%s`", expected, req.URL.String())
			}
			body := ioutil.NopCloser(bytes.NewReader([]byte("PostgreSQL 9.6.12")))
			return &http.Response{StatusCode: http.StatusOK, Body: body}, nil
		}),
	}
	clientConfig := &restclient.Config{
		APIPath: "/apis",
		ContentConfig: rest.ContentConfig{
			NegotiatedSerializer: scheme.Codecs,
			GroupVersion:         &v1.SchemeGroupVersion,
		},
	}
	restClient, _ := rest.RESTClientFor(clientConfig)
	restClient.Client = fakeClient.Client
	client := kubernetes.New(restClient).CoreV1()

	syndesis := v1beta1.Syndesis{}
	syndesis.SetNamespace("syndesis")

	version, err := postgreSQLVersionFromInitPod(client, &syndesis)
	if err != nil {
		t.Error(err)
	}

	if version != 9.6 {
		t.Errorf("Expecting that version would be 9.6, but it was %f", version)
	}
}
