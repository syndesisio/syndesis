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

package install

import (
	"encoding/json"
	"errors"
	"fmt"
	"sort"
	"strings"
	"unicode"

	"github.com/spf13/cast"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1alpha1"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	conf "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"

	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"sigs.k8s.io/yaml"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

var exceptionalItems = []string{
	"ENDPOINTS_TEST_SUPPORT_ENABLED",
	"CONTROLLERS_INTEGRATION_ENABLED",
	"INTEGRATION_STATE_CHECK_INTERVAL",
	"MAX_INTEGRATIONS_PER_USER",
}

type ConfigSpec struct {
	Value       string `json:"value"`
	Required    bool   `json:"required,omitempty"`
	Generate    string `json:"generate,omitempty"`
	FromLen     int    `json:"fromLen,omitempty"`
	Description string `json:"description,omitempty"`
}

type TemplateParam struct {
	Name        string `json:"name,omitempty"`
	DisplayName string `json:"displayName,omitempty"`
	Spec        *ConfigSpec
}

var params = map[string]TemplateParam{}

type SyndesisEnvVar string

const EMPTY_FIELD = "<>"

// Location from where the template configuration is located
var TemplateConfig string

const (
	EnvRouteHostname                  SyndesisEnvVar = "ROUTE_HOSTNAME"
	EnvOpenShiftMaster                SyndesisEnvVar = "OPENSHIFT_MASTER"
	EnvOpenShiftConsoleUrl            SyndesisEnvVar = "OPENSHIFT_CONSOLE_URL"
	EnvOpenShiftProject               SyndesisEnvVar = "OPENSHIFT_PROJECT"
	EnvOpenShiftOauthClientSecret     SyndesisEnvVar = "OPENSHIFT_OAUTH_CLIENT_SECRET"
	EnvPostgresqlMemoryLimit          SyndesisEnvVar = "POSTGRESQL_MEMORY_LIMIT"
	EnvPostgresqlImageStreamNamespace SyndesisEnvVar = "POSTGRESQL_IMAGE_STREAM_NAMESPACE"
	EnvPostgresqlUser                 SyndesisEnvVar = "POSTGRESQL_USER"
	EnvPostgresqlPassword             SyndesisEnvVar = "POSTGRESQL_PASSWORD"
	EnvPostgresqlURL                  SyndesisEnvVar = "POSTGRESQL_URL"
	EnvPostgresqlDatabase             SyndesisEnvVar = "POSTGRESQL_DATABASE"
	EnvPostgresqlVolumeCapacity       SyndesisEnvVar = "POSTGRESQL_VOLUME_CAPACITY"
	EnvPostgresqlSampledbPassword     SyndesisEnvVar = "POSTGRESQL_SAMPLEDB_PASSWORD"
	EnvTestSupport                    SyndesisEnvVar = "TEST_SUPPORT_ENABLED"
	EnvOauthCookieSecret              SyndesisEnvVar = "OAUTH_COOKIE_SECRET"
	EnvSyndesisEncryptKey             SyndesisEnvVar = "SYNDESIS_ENCRYPT_KEY"
	EnvPrometheusVolumeCapacity       SyndesisEnvVar = "PROMETHEUS_VOLUME_CAPACITY"
	EnvPrometheusMemoryLimit          SyndesisEnvVar = "PROMETHEUS_MEMORY_LIMIT"
	EnvMetaVolumeCapacity             SyndesisEnvVar = "META_VOLUME_CAPACITY"
	EnvMetaMemoryLimit                SyndesisEnvVar = "META_MEMORY_LIMIT"
	EnvServerMemoryLimit              SyndesisEnvVar = "SERVER_MEMORY_LIMIT"
	EnvClientStateAuthenticationKey   SyndesisEnvVar = "CLIENT_STATE_AUTHENTICATION_KEY"
	EnvClientStateEncryptionKey       SyndesisEnvVar = "CLIENT_STATE_ENCRYPTION_KEY"
	EnvImageStreamNamespace           SyndesisEnvVar = "IMAGE_STREAM_NAMESPACE"
	EnvControllersIntegrationEnabled  SyndesisEnvVar = "CONTROLLERS_INTEGRATION_ENABLED"
	EnvSyndesisRegistry               SyndesisEnvVar = "SYNDESIS_REGISTRY"
	EnvDemoDataEnabled                SyndesisEnvVar = "DEMO_DATA_ENABLED"
	EnvMaxIntegrationsPerUser         SyndesisEnvVar = "MAX_INTEGRATIONS_PER_USER"
	EnvIntegrationStateCheckInterval  SyndesisEnvVar = "INTEGRATION_STATE_CHECK_INTERVAL"
	EnvSarNamespace                   SyndesisEnvVar = "SAR_PROJECT"
	EnvKomodoMemoryLimit              SyndesisEnvVar = "KOMODO_MEMORY_LIMIT"
	EnvDatavirtEnabled                SyndesisEnvVar = "DATAVIRT_ENABLED"

	EnvSyndesisServerTag   SyndesisEnvVar = "SYNDESIS_SERVER_TAG"
	EnvSyndesisUITag       SyndesisEnvVar = "SYNDESIS_UI_TAG"
	EnvSyndesisS2ITag      SyndesisEnvVar = "SYNDESIS_S2I_TAG"
	EnvSyndesisMetaTag     SyndesisEnvVar = "SYNDESIS_META_TAG"
	EnvPostgresTag         SyndesisEnvVar = "SYNDESIS_POSTGRES_TAG"
	EnvPostgresExporterTag SyndesisEnvVar = "POSTGRES_EXPORTER_TAG"
	EnvKomodoTag           SyndesisEnvVar = "KOMODO_TAG"
	EnvPrometheusTag       SyndesisEnvVar = "PROMETHEUS_TAG"
	EnvOauthProxyTag       SyndesisEnvVar = "OAUTH_PROXY_TAG"

	EnvUpgradeVolumeCapacity  SyndesisEnvVar = "UPGRADE_VOLUME_CAPACITY"
	EnvManagementUrlFor3scale SyndesisEnvVar = "OPENSHIFT_MANAGEMENT_URL_FOR3SCALE"
)

var AllConfigOptions = map[SyndesisEnvVar]ConfigSpec{
	EnvRouteHostname:                  ConfigSpec{Description: "The external hostname to access Syndesis"},
	EnvOpenShiftMaster:                ConfigSpec{Value: "https://localhost:8443", Required: true, Description: "Public OpenShift master address"},
	EnvOpenShiftConsoleUrl:            ConfigSpec{Value: "https://localhost:8443", Description: "The URL to the OpenShift console"},
	EnvOpenShiftProject:               ConfigSpec{Required: true, Description: "The name of the OpenShift project Syndesis is being deployed into"},
	EnvOpenShiftOauthClientSecret:     ConfigSpec{Generate: "expression", FromLen: 64, Required: true, Description: "OpenShift OAuth client secret"},
	EnvPostgresqlMemoryLimit:          ConfigSpec{Value: "255Mi", Description: "Maximum amount of memory the PostgreSQL container can use"},
	EnvPostgresqlImageStreamNamespace: ConfigSpec{Value: "openshift", Description: "The OpenShift Namespace where the PostgreSQL ImageStream resides"},
	EnvPostgresqlUser:                 ConfigSpec{Value: "syndesis", Description: "Username for PostgreSQL user that will be used for accessing the database"},
	EnvPostgresqlPassword:             ConfigSpec{Generate: "expression", FromLen: 16, Required: true, Description: "Password for the PostgreSQL connection user"},
	EnvPostgresqlDatabase:             ConfigSpec{Value: "syndesis", Required: true, Description: "Name of the PostgreSQL database accessed"},
	EnvPostgresqlURL:                  ConfigSpec{Value: "postgresql://syndesis-db:5432/syndesis?sslmode=disable", Required: true, Description: "Host and port of the PostgreSQL database to access"},
	EnvPostgresqlVolumeCapacity:       ConfigSpec{Value: "1Gi", Required: true, Description: "Volume space available for PostgreSQL data, e.g. 512Mi, 2Gi"},
	EnvPostgresqlSampledbPassword:     ConfigSpec{Generate: "expression", FromLen: 16, Required: true, Description: "Password for the PostgreSQL sampledb user"},
	EnvTestSupport:                    ConfigSpec{Value: "false", Required: true, Description: "Enables test-support endpoint on backend API"},
	EnvOauthCookieSecret:              ConfigSpec{Generate: "expression", FromLen: 32, Description: "Secret to use to encrypt oauth cookies"},
	EnvSyndesisEncryptKey:             ConfigSpec{Generate: "expression", FromLen: 64, Required: true, Description: "The encryption key used to encrypt/decrypt stored secrets"},
	EnvPrometheusVolumeCapacity:       ConfigSpec{Value: "1Gi", Required: true, Description: "Volume space available for Prometheus data, e.g. 512Mi, 2Gi"},
	EnvPrometheusMemoryLimit:          ConfigSpec{Value: "512Mi", Required: true, Description: "Maximum amount of memory the Prometheus container can use"},
	EnvMetaVolumeCapacity:             ConfigSpec{Value: "1Gi", Required: true, Description: "Volume space available for Meta data, e.g. 512Mi, 2Gi"},
	EnvMetaMemoryLimit:                ConfigSpec{Value: "512Mi", Required: true, Description: "Maximum amount of memory the syndesis-meta service might use"},
	EnvServerMemoryLimit:              ConfigSpec{Value: "800Mi", Required: true, Description: "Maximum amount of memory the syndesis-server service might use"},
	EnvClientStateAuthenticationKey:   ConfigSpec{Generate: "expression", FromLen: 32, Required: true, Description: "Key used to perform authentication of client side stored state"},
	EnvClientStateEncryptionKey:       ConfigSpec{Generate: "expression", FromLen: 32, Required: true, Description: "Key used to perform encryption of client side stored state"},
	EnvImageStreamNamespace:           ConfigSpec{Value: EMPTY_FIELD, Description: "Namespace containing image streams"},
	EnvControllersIntegrationEnabled:  ConfigSpec{Value: "true", Description: "Should deployment of integrations be enabled?"},
	EnvSyndesisRegistry:               ConfigSpec{Value: "docker.io", Description: "Registry from where to fetch Syndesis images"},
	EnvDemoDataEnabled:                ConfigSpec{Value: "false", Required: true, Description: "Enables starting up with demo data"},
	EnvMaxIntegrationsPerUser:         ConfigSpec{Value: "1", Required: true, Description: "Maximum number of integrations single user can create"},
	EnvIntegrationStateCheckInterval:  ConfigSpec{Value: "60", Required: true, Description: "Interval for checking the state of the integrations"},
	EnvSarNamespace:                   ConfigSpec{Required: true, Description: "The user needs to have permissions to at least get a list of pods in the given project in order to be granted access to the Syndesis installation"},
	EnvKomodoMemoryLimit:              ConfigSpec{Value: "1024Mi", Required: true, Description: "Maximum amount of memory the data virtualization service might use"},
	EnvDatavirtEnabled:                ConfigSpec{Value: "0", Required: true, Description: "Set to 0 to disable data virtualization, set to 1 to enable data virtualization"},

	EnvSyndesisServerTag:   ConfigSpec{},
	EnvSyndesisUITag:       ConfigSpec{},
	EnvSyndesisS2ITag:      ConfigSpec{},
	EnvSyndesisMetaTag:     ConfigSpec{},
	EnvPostgresTag:         ConfigSpec{},
	EnvPostgresExporterTag: ConfigSpec{},
	EnvKomodoTag:           ConfigSpec{},
	EnvPrometheusTag:       ConfigSpec{},
	EnvOauthProxyTag:       ConfigSpec{},

	EnvUpgradeVolumeCapacity:  ConfigSpec{Value: "1Gi", Required: true, Description: "Volume space available for the upgrade process (backup data), e.g. 512Mi, 2Gi"},
	EnvManagementUrlFor3scale: ConfigSpec{Value: "", Description: "Url to 3scale for exposing services"},
}

func (cs ConfigSpec) From() string {
	return fmt.Sprintf("[a-zA-Z0-9]{%d}", cs.FromLen)
}

func (o *Install) installForge() error {
	// Create an empty syndesis CR which will
	// be filled with parameters placeholder values
	//
	syndesis := &v1alpha1.Syndesis{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: convertToParam(string(EnvOpenShiftProject)),
		},
		Spec: v1alpha1.SyndesisSpec{
			Components: v1alpha1.ComponentsSpec{},
			Addons:     v1alpha1.AddonsSpec{},
		},
		Status: v1alpha1.SyndesisStatus{
			TargetVersion: "latest",
		},
	}

	configuration, err := conf.GetProperties(conf.TemplateConfig, o.Context, nil, syndesis)
	if err != nil {
		return err
	}
	//
	// Process the yml template files containing
	// the framework for application
	//
	resources := make([]unstructured.Unstructured, 0)

	route, err := generator.RenderDir("./route/", configuration)
	if err != nil {
		return err
	}
	resources = append(resources, route...)

	// Render the remaining syndesis resources...
	infra, err := generator.RenderDir("./infrastructure/", configuration)
	if err != nil {
		return err
	}
	resources = append(resources, infra...)

	//
	// Determine if any addons specified
	// and include accordingly
	//
	addonArr := make([]string, 0)
	if o.addons != "" {
		addonArr = strings.Split(o.addons, ",")
	}

	addons := map[string]*bool{
		"jaeger":  &configuration.Syndesis.Addons.Jaeger.Enabled,
		"ops":     &configuration.Syndesis.Addons.Ops.Enabled,
		"dv":      &configuration.Syndesis.Addons.DV.Enabled,
		"camelk":  &configuration.Syndesis.Addons.CamelK.Enabled,
		"knative": &configuration.Syndesis.Addons.Knative.Enabled,
		"todo":    &configuration.Syndesis.Addons.Todo.Enabled,
	}

	for _, addon := range addonArr {
		if *addons[addon] != true {
			t := true
			addons[addon] = &t
		}

		addonDir := "./addons/" + addon + "/"
		f, err := generator.GetAssetsFS().Open(addonDir)
		if err != nil {
			fmt.Printf("unsupported addon configured", "addon", addon, "error", err)
			return err
		}
		f.Close()

		addonResources, err := generator.RenderDir(addonDir, configuration)
		if err != nil {
			return err
		}

		resources = append(resources, addonResources...)
	}

	//
	// TODO
	// Consider if this is applicable since it uses the syndesis-operator serviceaccount
	//
	// Render the remaining syndesis resources...
	// upg, err := generator.RenderDir("./upgrade/", gen)
	// if err != nil {
	// 	return err
	// }
	// resources = append(resources, upg...)

	//
	// Perform post process of the resources,
	// substituting variables in place of hard-coded values
	//
	postProcess(resources)

	//
	// Finally export all the resources to the template
	//
	if o.eject == "" {
		o.eject = "yaml"
	}

	err = exportTo(resources, o.eject)
	if err != nil {
		return err
	}

	return nil
}

func convertToParam(name string) string {
	//
	// Exceptional situation where the
	// ENDPOINTS_TEST_SUPPORT_ENABLED var
	// is renamed to TEST_SUPPORT_ENABLED
	//
	name = strings.TrimPrefix(name, "ENDPOINTS_")

	evar := "${" + name + "}"
	if _, ok := params[evar]; !ok {
		displayName := strings.ToLower(name)
		displayName = strings.ReplaceAll(displayName, "_", " ")
		displayName = strings.Title(displayName)

		v := TemplateParam{
			Name:        name,
			DisplayName: displayName,
		}

		if cvar, ok := AllConfigOptions[SyndesisEnvVar(name)]; ok {
			v.Spec = &cvar
		}
		params[evar] = v
	}

	return evar
}

func fixHardcodedExceptions(key string, value interface{}) interface{} {
	switch key {
	case "load-demo-data":
		return convertToParam(string(EnvDemoDataEnabled))
	case "integrationStateCheckInterval":
		return convertToParam(string(EnvIntegrationStateCheckInterval))
	case "maxDeploymentsPerUser":
		return convertToParam(string(EnvMaxIntegrationsPerUser))
	case "maxIntegrationsPerUser":
		return convertToParam(string(EnvMaxIntegrationsPerUser))
	}

	return value
}

func fixDataVirt(value interface{}) interface{} {
	if dvMap, ok := value.(map[string]interface{}); ok {
		dvMap["enabled"] = convertToParam(string(EnvDatavirtEnabled))
		return dvMap
	}

	return value
}

func analyzeType(value interface{}) int {
	switch v := value.(type) {
	case []interface{}:
		processSlice(cast.ToSlice(v))
		return 1
	case map[string]interface{}:
		processMap(cast.ToStringMap(value))
		return 2
	}

	return 0
}

func processSlice(content []interface{}) {
	for _, value := range content {
		analyzeType(value)
	}
}

func isNameValueMap(theMap map[string]interface{}) bool {
	if len(theMap) != 2 {
		return false
	}

	if _, nameok := theMap["name"]; !nameok {
		return false
	}

	if _, valok := theMap["value"]; !valok {
		return false
	}

	return true
}

func hasExceptionalItem(text string) bool {
	for _, item := range exceptionalItems {
		if item == text {
			return true
		}
	}
	return false
}

func processMap(content map[string]interface{}) error {
	//
	// Process maps with only 2 keys (name, value)
	// & update their value with a param version of name
	//
	if isNameValueMap(content) && hasExceptionalItem(content["name"].(string)) {
		content["value"] = convertToParam(content["name"].(string))
		return nil
	}

	for key, value := range content {
		//
		// Really hacky but little choice
		// Datavirt has its own little map that contains
		// enabled key on its own
		//
		if key == "datavirt" {
			fixDataVirt(value)
			continue
		}

		if analyzeType(value) > 0 {
			continue
		}

		if strings.Contains(key, "yml") {
			yamlMap := make(map[string]interface{})
			err := yaml.Unmarshal([]byte(value.(string)), &yamlMap)
			if err != nil {
				return err
			}
			err = processMap(yamlMap)
			if err != nil {
				return err
			}

			newYaml, err := yaml.Marshal(&yamlMap)
			if err != nil {
				return err
			}
			content[key] = string(newYaml)
			continue
		}

		if strings.Contains(key, "json") {
			jsonMap := make(map[string]interface{})
			err := json.Unmarshal([]byte(value.(string)), &jsonMap)
			if err != nil {
				return err
			}
			err = processMap(jsonMap)
			if err != nil {
				return err
			}

			newJson, err := json.Marshal(&jsonMap)
			if err != nil {
				return err
			}
			content[key] = string(newJson)
			continue
		}

		content[key] = fixHardcodedExceptions(key, value)
	}
	return nil
}

/**
 * Cannot include boolean parameter vars in syndesis object since
 * type will not allow it so need to scan resources and replace
 */
func postProcess(resources []unstructured.Unstructured) error {
	for _, resource := range resources {
		content := resource.UnstructuredContent()
		err := processMap(content)
		if err != nil {
			return err
		}
	}
	return nil
}

func lowerCaseFirst(str string) string {
	for i, v := range str {
		return string(unicode.ToLower(v)) + str[i+1:]
	}
	return ""
}

func exportTo(resources []unstructured.Unstructured, format string) error {
	//
	// Strip the resource content out of the unstructured
	//
	resContent := make([]interface{}, 0)
	for _, res := range resources {
		resContent = append(resContent, res.UnstructuredContent())
	}

	exportTemplate := unstructured.Unstructured{}
	exportTemplate.SetKind("Template")
	exportTemplate.SetAPIVersion("template.openshift.io/v1")
	exportTemplate.SetName("fuse-ignite-1.8")

	labels := map[string]string{}
	labels["app"] = "syndesis"
	labels["syndesis.io/app"] = "syndesis"
	labels["syndesis.io/type"] = "infrastructure"
	exportTemplate.SetLabels(labels)

	unstructured.SetNestedField(exportTemplate.UnstructuredContent(), "Syndesis is deployed to ${ROUTE_HOSTNAME}.", "message")

	//
	// Process the parameters and format them into
	// a map[string]interface to conform with unstructured
	// specification
	//
	paramList := make([]interface{}, 0)
	for k, v := range params {
		if strings.HasSuffix(k, "_TAG}") ||
			strings.HasSuffix(k, "_LIMIT}") {
			//
			// TAG & LIMIT parameters not used
			//
			continue
		}

		m := make(map[string]interface{})

		m["name"] = v.Name
		m["displayName"] = v.DisplayName
		if v.Spec != nil {
			spec := v.Spec

			if len(spec.Description) > 0 {
				m["description"] = spec.Description
			}
			if len(spec.Value) > 0 {
				if spec.Value == EMPTY_FIELD {
					m["value"] = ""
				} else {
					m["value"] = spec.Value
				}
			}
			if len(spec.Generate) > 0 {
				m["generate"] = spec.Generate
			}
			if spec.FromLen > 0 {
				m["from"] = spec.From()
			}
			if spec.Required {
				m["required"] = spec.Required
			}
		}

		paramList = append(paramList, m)
	}

	sort.Slice(paramList,
		func(i, j int) bool {
			f := paramList[i].(map[string]interface{})
			g := paramList[j].(map[string]interface{})
			return f["name"].(string) < g["name"].(string)
		})
	unstructured.SetNestedSlice(exportTemplate.UnstructuredContent(), paramList, "parameters")

	//
	// Assign the resources to the objects key
	//
	unstructured.SetNestedSlice(exportTemplate.UnstructuredContent(), resContent, "objects")

	//
	// Convert to runtime object to avoid
	// 'Objects:' being printed
	//
	s := []unstructured.Unstructured{exportTemplate}
	runtimeTemplate := util.UnstructuredsToRuntimeObject(s)

	switch format {
	case "yaml":
		data, err := yaml.Marshal(runtimeTemplate)
		if err != nil {
			return err
		}
		fmt.Print(string(data))
	case "json":
		data, err := json.Marshal(runtimeTemplate)
		if err != nil {
			return err
		}
		fmt.Print(string(data))
	default:
		return errors.New("Illegal format chosen")
	}

	return nil
}
