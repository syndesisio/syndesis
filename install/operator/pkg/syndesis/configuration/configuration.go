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
	"encoding/json"
	"errors"
	"io/ioutil"
	"math/rand"
	"net/url"
	"os"
	"strings"
	"time"

	"github.com/imdario/mergo"

	"k8s.io/apimachinery/pkg/types"

	routev1 "github.com/openshift/api/route/v1"
	corev1 "k8s.io/api/core/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	"sigs.k8s.io/controller-runtime/pkg/client"

	"k8s.io/apimachinery/pkg/util/yaml"

	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
)

var random = rand.New(rand.NewSource(time.Now().UnixNano()))

// Location from where the template configuration is located
var TemplateConfig string

type Config struct {
	AllowLocalHost             bool
	Productized                bool
	DevSupport                 bool
	Scheduled                  bool
	ProductName                string
	ImageStreamNamespace       string
	PrometheusRules            string
	OpenShiftProject           string
	OpenShiftOauthClientSecret string
	RouteHostname              string
	OpenShiftConsoleUrl        string
	ImagePullSecrets           []string
	Syndesis                   SyndesisConfig
}

type SyndesisConfig struct {
	ImageStreamNamespace string
	Components           ComponentsSpec
	Addons               AddonsSpec
}

// Components
type ComponentsSpec struct {
	UI         UIConfiguration
	S2I        S2IConfiguration
	Oauth      OauthConfiguration
	Server     ServerConfiguration
	Meta       MetaConfiguration
	Database   DatabaseConfiguration
	Prometheus PrometheusConfiguration
	Grafana    GrafanaConfiguration
	Upgrade    UpgradeConfiguration
}

type OauthConfiguration struct {
	CookieSecret    string
	Image           string
	DisableSarCheck bool
	SarNamespace    string
}

type UIConfiguration struct {
	Image string
}

type S2IConfiguration struct {
	Image string
}

type DatabaseConfiguration struct {
	User                 string
	Database             string
	URL                  string
	ExternalDbURL        string
	Resources            ResourcesWithVolume
	Exporter             ExporterConfiguration
	Image                string
	ImageStreamNamespace string
	Password             string
	SampledbPassword     string
}

type ExporterConfiguration struct {
	Image string
}

type PrometheusConfiguration struct {
	Image     string
	Rules     string
	Resources ResourcesWithVolume
}

type GrafanaConfiguration struct {
	Resources Resources
}

type ServerConfiguration struct {
	Resources                     Resources
	Features                      ServerFeatures
	Image                         string
	SyndesisEncryptKey            string
	ClientStateAuthenticationKey  string
	ClientStateEncryptionKey      string
	ControllersIntegrationEnabled bool
}

type MetaConfiguration struct {
	Image     string
	Resources ResourcesWithVolume
}

type UpgradeConfiguration struct {
	Image     string
	Resources VolumeOnlyResources
}

type Resources struct {
	Memory string
}

type ResourcesWithVolume struct {
	Memory         string
	VolumeCapacity string
}

type VolumeOnlyResources struct {
	VolumeCapacity string
}

type ServerFeatures struct {
	IntegrationLimit              int
	IntegrationStateCheckInterval int
	DemoData                      bool
	DeployIntegrations            bool
	TestSupport                   bool
	OpenShiftMaster               string
	ManagementUrlFor3scale        string
	MavenRepositories             map[string]string
}

// Addons
type AddonsSpec struct {
	Jaeger JaegerConfiguration
	Ops    AddonConfiguration
	Todo   AddonConfiguration
	DV     DvConfiguration
	CamelK CamelKConfiguration
}

type JaegerConfiguration struct {
	Enabled      bool
	SamplerType  string
	SamplerParam string
}

type DvConfiguration struct {
	Enabled   bool
	Resources Resources
	Image     string
}

type AddonConfiguration struct {
	Enabled bool
}

type CamelKConfiguration struct {
	Enabled       bool
	CamelVersion  string
	CamelKRuntime string
	Image         string
}

/*
/ Returns all processed configurations for Syndesis

 - Default values for configuration are loaded from file
 - Secrets and passwords are loaded from syndesis-global-config Secret if they exits
 and generated if they dont
 - For QE, some fields are loaded from environment variables
 - Users might define fields using the syndesis custom resource
*/
func GetProperties(file string, ctx context.Context, client client.Client, syndesis *v1alpha1.Syndesis) (*Config, error) {
	configuration := &Config{}
	if err := configuration.loadFromFile(file); err != nil {
		return nil, err
	}

	configuration.OpenShiftProject = syndesis.Namespace
	configuration.Syndesis.Components.Oauth.SarNamespace = configuration.OpenShiftProject

	if client != nil {
		if err := configuration.setPasswordsFromSecret(ctx, client, syndesis); err != nil {
			return nil, err
		}
	}
	configuration.generatePasswords()

	if err := configuration.setImagesFromEnv(); err != nil {
		return nil, err
	}

	if err := configuration.setSyndesisFromCustomResource(syndesis); err != nil {
		return nil, err
	}

	return configuration, nil
}

// Load configuration from config file. Config file is expected to be a yaml
// The returned configuration is parsed to JSON and returned as a Config object
func (config *Config) loadFromFile(file string) error {
	data, err := ioutil.ReadFile(file)
	if err != nil {
		return err
	}

	if strings.HasSuffix(file, ".yaml") || strings.HasSuffix(file, ".yml") {
		data, err = yaml.ToJSON(data)
		if err != nil {
			return err
		}
	}

	if err := json.Unmarshal(data, config); err != nil {
		return err
	}

	return nil
}

// Set Config.RouteHostname based on the Spec.Host property of the syndesis route
func (config *Config) SetRoute(ctx context.Context, client client.Client, syndesis *v1alpha1.Syndesis) error {
	syndesisRoute := &routev1.Route{}

	if err := client.Get(ctx, types.NamespacedName{Namespace: syndesis.Namespace, Name: "syndesis"}, syndesisRoute); err != nil {
		if k8serrors.IsNotFound(err) {
			return nil
		} else {
			return err
		}
	}
	config.RouteHostname = syndesisRoute.Spec.Host

	return nil
}

// When an external database is defined, reset connection parameters
func (config *Config) ExternalDatabase(ctx context.Context, client client.Client, syndesis *v1alpha1.Syndesis) error {
	// Handle an external database being defined
	if syndesis.Spec.Components.Database.ExternalDbURL != "" {
		// check to see if password is already provided, check to see if merge is done
		globalCfgSec := &corev1.Secret{}
		if err := client.Get(ctx, types.NamespacedName{Name: "syndesis-global-config", Namespace: syndesis.Namespace}, globalCfgSec); err != nil {
			// the secret doesn't already exist, but it must for external databases
			return err
		}
		postgresPass := string(globalCfgSec.Data["POSTGRESQL_PASSWORD"])
		if postgresPass == "" {
			return errors.New("failed to find postgresql password in global config")
		}

		// setup connection string from provided url
		externalDbURL, err := url.Parse(syndesis.Spec.Components.Database.ExternalDbURL)
		if err != nil {
			return err
		}
		if externalDbURL.Path == "" {
			externalDbURL.Path = syndesis.Spec.Components.Database.Database
		}

		config.Syndesis.Components.Database.URL = externalDbURL.String()
		config.Syndesis.Components.Database.Password = postgresPass
	}

	return nil
}

func (config *Config) setPasswordsFromSecret(ctx context.Context, client client.Client, syndesis *v1alpha1.Syndesis) error {
	secrets, err := getSyndesisEnvVarsFromOpenShiftNamespace(ctx, client, syndesis.Namespace)
	if err != nil {
		if k8serrors.IsNotFound(err) {
			return nil
		} else {
			return err
		}
	}

	config.OpenShiftOauthClientSecret = secrets["OPENSHIFT_OAUTH_CLIENT_SECRET"]
	config.Syndesis.Components.Database.Password = secrets["POSTGRESQL_PASSWORD"]
	config.Syndesis.Components.Database.SampledbPassword = secrets["POSTGRESQL_SAMPLEDB_PASSWORD"]
	config.Syndesis.Components.Oauth.CookieSecret = secrets["OAUTH_COOKIE_SECRET"]
	config.Syndesis.Components.Server.SyndesisEncryptKey = secrets["SYNDESIS_ENCRYPT_KEY"]
	config.Syndesis.Components.Server.ClientStateAuthenticationKey = secrets["CLIENT_STATE_AUTHENTICATION_KEY"]
	config.Syndesis.Components.Server.ClientStateEncryptionKey = secrets["CLIENT_STATE_ENCRYPTION_KEY"]

	return nil
}

// Overwrite operand images with values from ENV if those env are present
func (config *Config) setImagesFromEnv() error {
	img_env := Config{
		Syndesis: SyndesisConfig{
			Addons: AddonsSpec{
				DV: DvConfiguration{Image: os.Getenv("DV_IMAGE")},
			},
			Components: ComponentsSpec{
				Oauth:      OauthConfiguration{Image: os.Getenv("OAUTH_IMAGE")},
				UI:         UIConfiguration{Image: os.Getenv("UI_IMAGE")},
				S2I:        S2IConfiguration{Image: os.Getenv("S2I_IMAGE")},
				Prometheus: PrometheusConfiguration{Image: os.Getenv("PROMETHEUS_IMAGE")},
				Upgrade:    UpgradeConfiguration{Image: os.Getenv("UPGRADE_IMAGE")},
				Meta:       MetaConfiguration{Image: os.Getenv("META_IMAGE")},
				Database: DatabaseConfiguration{
					Image: os.Getenv("DATABASE_IMAGE"), ImageStreamNamespace: os.Getenv("DATABASE_NAMESPACE"),
					Exporter: ExporterConfiguration{Image: os.Getenv("PSQL_EXPORTER_IMAGE")},
				},
				Server: ServerConfiguration{Image: os.Getenv("SERVER_IMAGE")},
			},
		},
	}

	if err := mergo.Merge(config, img_env, mergo.WithOverride); err != nil {
		return err
	}

	return nil
}

// Replace default values with those from custom resource
func (config *Config) setSyndesisFromCustomResource(syndesis *v1alpha1.Syndesis) error {
	c := SyndesisConfig{}
	jsonProperties, err := json.Marshal(syndesis.Spec)
	if err != nil {
		return err
	}

	if err := json.Unmarshal(jsonProperties, &c); err != nil {
		return err
	}

	if err := mergo.Merge(&config.Syndesis, c, mergo.WithOverride); err != nil {
		return err
	}
	return nil
}

// Generate random expressions for passwords and secrets
func (config *Config) generatePasswords() {

	if config.OpenShiftOauthClientSecret == "" {
		config.OpenShiftOauthClientSecret = generatePassword(64)
	}

	if config.Syndesis.Components.Database.Password == "" {
		config.Syndesis.Components.Database.Password = generatePassword(16)
	}

	if config.Syndesis.Components.Database.SampledbPassword == "" {
		config.Syndesis.Components.Database.SampledbPassword = generatePassword(16)
	}

	if config.Syndesis.Components.Oauth.CookieSecret == "" {
		config.Syndesis.Components.Oauth.CookieSecret = generatePassword(32)
	}

	if config.Syndesis.Components.Server.SyndesisEncryptKey == "" {
		config.Syndesis.Components.Server.SyndesisEncryptKey = generatePassword(64)
	}

	if config.Syndesis.Components.Server.ClientStateAuthenticationKey == "" {
		config.Syndesis.Components.Server.ClientStateAuthenticationKey = generatePassword(32)
	}

	if config.Syndesis.Components.Server.ClientStateEncryptionKey == "" {
		config.Syndesis.Components.Server.ClientStateEncryptionKey = generatePassword(32)
	}
}

func generatePassword(size int) string {
	alphabet := make([]rune, (26*2)+10)
	i := 0
	for c := 'a'; c <= 'z'; c++ {
		alphabet[i] = c
		i += 1
	}
	for c := 'A'; c <= 'Z'; c++ {
		alphabet[i] = c
		i += 1
	}
	for c := '0'; c <= '9'; c++ {
		alphabet[i] = c
		i += 1
	}

	result := make([]rune, size)
	for i := 0; i < size; i++ {
		result[i] = alphabet[random.Intn(len(alphabet))]
	}
	s := string(result)
	return s
}
