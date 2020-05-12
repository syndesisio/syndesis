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
	"path/filepath"
	"sort"
	"strings"
	"unicode"

	"github.com/spf13/cast"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	conf "github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"

	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"sigs.k8s.io/yaml"
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

var templateParams = map[string]TemplateParam{}

type SyndesisEnvVar string

const EMPTY_FIELD = "<>"

const (
	EnvRouteHostname                 SyndesisEnvVar = "ROUTE_HOSTNAME"
	EnvOpenShiftMaster               SyndesisEnvVar = "OPENSHIFT_MASTER"
	EnvOpenShiftConsoleUrl           SyndesisEnvVar = "OPENSHIFT_CONSOLE_URL"
	EnvOpenShiftProject              SyndesisEnvVar = "OPENSHIFT_PROJECT"
	EnvOpenShiftOauthClientSecret    SyndesisEnvVar = "OPENSHIFT_OAUTH_CLIENT_SECRET"
	EnvPostgresqlMemoryLimit         SyndesisEnvVar = "POSTGRESQL_MEMORY_LIMIT"
	EnvPostgresqlUser                SyndesisEnvVar = "POSTGRESQL_USER"
	EnvPostgresqlPassword            SyndesisEnvVar = "POSTGRESQL_PASSWORD"
	EnvPostgresqlURL                 SyndesisEnvVar = "POSTGRESQL_URL"
	EnvPostgresqlDatabase            SyndesisEnvVar = "POSTGRESQL_DATABASE"
	EnvPostgresqlVolumeCapacity      SyndesisEnvVar = "POSTGRESQL_VOLUME_CAPACITY"
	EnvPostgresqlSampledbPassword    SyndesisEnvVar = "POSTGRESQL_SAMPLEDB_PASSWORD"
	EnvTestSupport                   SyndesisEnvVar = "TEST_SUPPORT_ENABLED"
	EnvOauthCookieSecret             SyndesisEnvVar = "OAUTH_COOKIE_SECRET"
	EnvSyndesisEncryptKey            SyndesisEnvVar = "SYNDESIS_ENCRYPT_KEY"
	EnvPrometheusVolumeCapacity      SyndesisEnvVar = "PROMETHEUS_VOLUME_CAPACITY"
	EnvPrometheusMemoryLimit         SyndesisEnvVar = "PROMETHEUS_MEMORY_LIMIT"
	EnvMetaVolumeCapacity            SyndesisEnvVar = "META_VOLUME_CAPACITY"
	EnvMetaMemoryLimit               SyndesisEnvVar = "META_MEMORY_LIMIT"
	EnvServerMemoryLimit             SyndesisEnvVar = "SERVER_MEMORY_LIMIT"
	EnvClientStateAuthenticationKey  SyndesisEnvVar = "CLIENT_STATE_AUTHENTICATION_KEY"
	EnvClientStateEncryptionKey      SyndesisEnvVar = "CLIENT_STATE_ENCRYPTION_KEY"
	EnvSyndesisRegistry              SyndesisEnvVar = "SYNDESIS_REGISTRY"
	EnvDemoDataEnabled               SyndesisEnvVar = "DEMO_DATA_ENABLED"
	EnvMaxIntegrationsPerUser        SyndesisEnvVar = "MAX_INTEGRATIONS_PER_USER"
	EnvIntegrationStateCheckInterval SyndesisEnvVar = "INTEGRATION_STATE_CHECK_INTERVAL"
	EnvSarNamespace                  SyndesisEnvVar = "SAR_PROJECT"
	EnvKomodoMemoryLimit             SyndesisEnvVar = "KOMODO_MEMORY_LIMIT"
	EnvDatavirtEnabled               SyndesisEnvVar = "DATAVIRT_ENABLED"

	EnvUpgradeVolumeCapacity  SyndesisEnvVar = "UPGRADE_VOLUME_CAPACITY"
	EnvManagementUrlFor3scale SyndesisEnvVar = "OPENSHIFT_MANAGEMENT_URL_FOR3SCALE"

	EnvControllersIntegrationEnabled SyndesisEnvVar = "CONTROLLERS_INTEGRATION_ENABLED"
	EnvImageStreamNamespace          SyndesisEnvVar = "IMAGE_STREAM_NAMESPACE"

	EnvFuseS2iImage        SyndesisEnvVar = "FUSE_S2I_IMAGE"
	EnvFuseMetaImage       SyndesisEnvVar = "FUSE_META_IMAGE"
	EnvFuseServerImage     SyndesisEnvVar = "FUSE_SERVER_IMAGE"
	EnvFuseUIImage         SyndesisEnvVar = "FUSE_UI_IMAGE"
	EnvFuseOauthImage      SyndesisEnvVar = "FUSE_OAUTH_IMAGE"
	EnvPrometheusImage     SyndesisEnvVar = "FUSE_PROMETHEUS_IMAGE"
	EnvFuseDBImage         SyndesisEnvVar = "FUSE_DB_IMAGE"
	EnvFuseDBExporterImage SyndesisEnvVar = "FUSE_DB_EXPORTER_IMAGE"
	EnvFuseDVImage         SyndesisEnvVar = "FUSE_DV_IMAGE"
)

//
// The parameters provided for injecting values
// into the resulting templates
//
var allTemplateParams = map[SyndesisEnvVar]ConfigSpec{
	EnvRouteHostname:                 {Description: "The external hostname to access Syndesis"},
	EnvOpenShiftMaster:               {Value: "https://localhost:8443", Required: true, Description: "Public OpenShift master address"},
	EnvOpenShiftConsoleUrl:           {Value: "https://localhost:8443", Description: "The URL to the OpenShift console"},
	EnvOpenShiftProject:              {Required: true, Description: "The name of the OpenShift project Syndesis is being deployed into"},
	EnvImageStreamNamespace:          {Value: EMPTY_FIELD, Description: "Namespace containing image streams"},
	EnvOpenShiftOauthClientSecret:    {Generate: "expression", FromLen: 64, Required: true, Description: "OpenShift OAuth client secret"},
	EnvPostgresqlMemoryLimit:         {Value: "255Mi", Description: "Maximum amount of memory the PostgreSQL container can use"},
	EnvPostgresqlUser:                {Value: "syndesis", Description: "Username for PostgreSQL user that will be used for accessing the database"},
	EnvPostgresqlPassword:            {Generate: "expression", FromLen: 16, Required: true, Description: "Password for the PostgreSQL connection user"},
	EnvPostgresqlDatabase:            {Value: "syndesis", Required: true, Description: "Name of the PostgreSQL database accessed"},
	EnvPostgresqlURL:                 {Value: "postgresql://syndesis-db:5432/syndesis?sslmode=disable", Required: true, Description: "Host and port of the PostgreSQL database to access"},
	EnvPostgresqlVolumeCapacity:      {Value: "1Gi", Required: true, Description: "Volume space available for PostgreSQL data, e.g. 512Mi, 2Gi"},
	EnvPostgresqlSampledbPassword:    {Generate: "expression", FromLen: 16, Required: true, Description: "Password for the PostgreSQL sampledb user"},
	EnvTestSupport:                   {Value: "false", Required: true, Description: "Enables test-support endpoint on backend API"},
	EnvOauthCookieSecret:             {Generate: "expression", FromLen: 32, Description: "Secret to use to encrypt oauth cookies"},
	EnvSyndesisEncryptKey:            {Generate: "expression", FromLen: 64, Required: true, Description: "The encryption key used to encrypt/decrypt stored secrets"},
	EnvPrometheusVolumeCapacity:      {Value: "1Gi", Required: true, Description: "Volume space available for Prometheus data, e.g. 512Mi, 2Gi"},
	EnvPrometheusMemoryLimit:         {Value: "512Mi", Required: true, Description: "Maximum amount of memory the Prometheus container can use"},
	EnvMetaVolumeCapacity:            {Value: "1Gi", Required: true, Description: "Volume space available for Meta data, e.g. 512Mi, 2Gi"},
	EnvMetaMemoryLimit:               {Value: "512Mi", Required: true, Description: "Maximum amount of memory the syndesis-meta service might use"},
	EnvServerMemoryLimit:             {Value: "800Mi", Required: true, Description: "Maximum amount of memory the syndesis-server service might use"},
	EnvClientStateAuthenticationKey:  {Generate: "expression", FromLen: 32, Required: true, Description: "Key used to perform authentication of client side stored state"},
	EnvClientStateEncryptionKey:      {Generate: "expression", FromLen: 32, Required: true, Description: "Key used to perform encryption of client side stored state"},
	EnvSyndesisRegistry:              {Value: "docker-registry.default.svc:5000", Description: "Registry from where to fetch Syndesis images"},
	EnvDemoDataEnabled:               {Value: "false", Required: true, Description: "Enables starting up with demo data"},
	EnvMaxIntegrationsPerUser:        {Value: "1", Required: true, Description: "Maximum number of integrations single user can create"},
	EnvIntegrationStateCheckInterval: {Value: "60", Required: true, Description: "Interval for checking the state of the integrations"},
	EnvSarNamespace:                  {Required: true, Description: "The user needs to have permissions to at least get a list of pods in the given project in order to be granted access to the Syndesis installation"},
	EnvKomodoMemoryLimit:             {Value: "1024Mi", Required: true, Description: "Maximum amount of memory the data virtualization service might use"},
	EnvDatavirtEnabled:               {Value: "0", Required: true, Description: "Set to 0 to disable data virtualization, set to 1 to enable data virtualization"},
	EnvControllersIntegrationEnabled: {Value: "true", Description: "Should deployment of integrations be enabled?"},
	EnvManagementUrlFor3scale:        {Value: "", Description: "Url to 3scale for exposing services"},

	EnvFuseS2iImage:        {Value: EMPTY_FIELD, Required: true, Description: "The Fuse S2i image and tag"},
	EnvFuseMetaImage:       {Value: EMPTY_FIELD, Required: true, Description: "The Fuse Meta image and tag"},
	EnvFuseServerImage:     {Value: EMPTY_FIELD, Required: true, Description: "The Fuse Server image and tag"},
	EnvFuseUIImage:         {Value: EMPTY_FIELD, Required: true, Description: "The Fuse UI image and tag"},
	EnvFuseOauthImage:      {Value: EMPTY_FIELD, Required: true, Description: "The Fuse Oauth Proxy image and tag"},
	EnvPrometheusImage:     {Value: EMPTY_FIELD, Required: true, Description: "The Fuse Prometheus image and tag"},
	EnvFuseDBImage:         {Value: EMPTY_FIELD, Required: true, Description: "The Fuse Database image and tag"},
	EnvFuseDBExporterImage: {Value: EMPTY_FIELD, Required: true, Description: "The Fuse Database Exporter image and tag"},
	EnvFuseDVImage:         {Value: EMPTY_FIELD, Required: true, Description: "The Fuse DV image and tag"},
}

func (cs ConfigSpec) From() string {
	return fmt.Sprintf("[a-zA-Z0-9]{%d}", cs.FromLen)
}

func (o *Install) installForge() error {

	if o.templateName == "" {
		return errors.New("A template name must be specified")
	}

	// Create an empty syndesis CR which will
	// be filled with parameters placeholder values
	//
	syndesis, _ := v1beta1.NewSyndesis(convertToParam(string(EnvOpenShiftProject)))
	configuration, err := conf.GetProperties(conf.TemplateConfig, o.Context, nil, syndesis)
	if err != nil {
		return err
	}

	// Include broker-amq
	configuration.Productized = true

	synConf := &configuration.Syndesis
	components := &synConf.Components

	// Fix route host name
	configuration.Syndesis.RouteHostname = convertToParam(string(EnvRouteHostname))

	configuration.OpenShiftConsoleUrl = convertToParam(string(EnvOpenShiftConsoleUrl))
	components.Server.Features.OpenShiftMaster = convertToParam(string(EnvOpenShiftMaster))
	components.Server.Features.ManagementUrlFor3scale = convertToParam(string(EnvManagementUrlFor3scale))
	components.Oauth.SarNamespace = convertToParam(string(EnvSarNamespace))

	components.S2I.Image = retargetImage(EnvFuseS2iImage, &components.S2I.Image)
	components.UI.Image = retargetImage(EnvFuseUIImage, &components.UI.Image)
	components.Server.Image = retargetImage(EnvFuseServerImage, &components.Server.Image)
	components.Meta.Image = retargetImage(EnvFuseMetaImage, &components.Meta.Image)
	components.Oauth.Image = retargetImage(EnvFuseOauthImage, &components.Oauth.Image)
	components.Prometheus.Image = retargetImage(EnvPrometheusImage, &components.Prometheus.Image)
	components.Database.Image = retargetImage(EnvFuseDBImage, &components.Database.Image)
	components.Database.Exporter.Image = retargetImage(EnvFuseDBExporterImage, &components.Database.Exporter.Image)
	synConf.Addons.DV.Image = retargetImage(EnvFuseDVImage, &synConf.Addons.DV.Image)

	// Fix Secrets
	components.Database.User = convertToParam(string(EnvPostgresqlUser))
	components.Database.Password = convertToParam(string(EnvPostgresqlPassword))
	components.Database.Name = convertToParam(string(EnvPostgresqlDatabase))
	components.Database.SampledbPassword = convertToParam(string(EnvPostgresqlSampledbPassword))

	configuration.OpenShiftOauthClientSecret = convertToParam(string(EnvOpenShiftOauthClientSecret))
	components.Oauth.CookieSecret = convertToParam(string(EnvOauthCookieSecret))
	components.Server.SyndesisEncryptKey = convertToParam(string(EnvSyndesisEncryptKey))
	components.Server.ClientStateAuthenticationKey = convertToParam(string(EnvClientStateAuthenticationKey))
	components.Server.ClientStateEncryptionKey = convertToParam(string(EnvClientStateEncryptionKey))

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

	db, err := generator.RenderDir("./database/", configuration)
	if err != nil {
		return err
	}
	resources = append(resources, db...)

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
	reqAddons := make([]string, 0)
	if o.addons != "" {
		reqAddons = strings.Split(o.addons, ",")
	}

	addonsPath := "./addons"
	addonsDir, err := generator.GetAssetsFS().Open(addonsPath)
	if err != nil {
		return err
	}
	defer addonsDir.Close()

	addons, err := addonsDir.Readdir(0)
	if err != nil {
		return err
	}

	for _, reqAddon := range reqAddons {
		for _, addonInfo := range addons {
			if reqAddon != addonInfo.Name() {
				continue
			}

			// Found addon so need to Enable it in the configuration
			// before rendering the files in the directory
			switch reqAddon {
			case "jaeger":
				configuration.Syndesis.Addons.Jaeger.Enabled = true
			case "ops":
				configuration.Syndesis.Addons.Ops.Enabled = true
			case "dv":
				configuration.Syndesis.Addons.DV.Enabled = true
			case "camelk":
				configuration.Syndesis.Addons.CamelK.Enabled = true
			case "knative":
				configuration.Syndesis.Addons.Knative.Enabled = true
			case "publicApi":
				configuration.Syndesis.Addons.PublicApi.Enabled = true
			case "todo":
				configuration.Syndesis.Addons.Todo.Enabled = true
			}

			addonDir := filepath.Join(addonsPath, reqAddon)
			f, err := generator.GetAssetsFS().Open(addonDir)
			if err != nil {
				return err
			}
			f.Close()

			addonRes, err := generator.RenderDir(addonDir, configuration)
			if err != nil {
				return err
			}

			resources = append(resources, addonRes...)
		}
	}

	//
	// Perform post process of the resources,
	// substituting variables in place of hard-coded values
	//
	err = postProcess(resources)
	if err != nil {
		return err
	}

	//
	// Finally export all the resources to the template
	//
	if o.eject == "" {
		o.eject = "yaml"
	}

	err = exportTo(resources, o.eject, o.templateName)
	if err != nil {
		return err
	}

	return nil
}

func retargetImage(parameter SyndesisEnvVar, imgPath *string) string {
	image := filepath.Base(*imgPath)

	var param = allTemplateParams[parameter]
	param.Value = image
	allTemplateParams[parameter] = param

	regVar := convertToParam(string(EnvSyndesisRegistry))
	nmVar := convertToParam(string(EnvImageStreamNamespace))
	imgVar := convertToParam(string(parameter))

	return filepath.Join(regVar, nmVar, imgVar)
}

func convertToParam(name string) string {
	//
	// Exceptional situation where the
	// ENDPOINTS_TEST_SUPPORT_ENABLED var
	// is renamed to TEST_SUPPORT_ENABLED
	//
	name = strings.TrimPrefix(name, "ENDPOINTS_")

	evar := "${" + name + "}"
	if _, ok := templateParams[evar]; !ok {
		displayName := strings.ToLower(name)
		displayName = strings.ReplaceAll(displayName, "_", " ")
		displayName = strings.Title(displayName)

		v := TemplateParam{
			Name:        name,
			DisplayName: displayName,
		}

		if cvar, ok := allTemplateParams[SyndesisEnvVar(name)]; ok {
			v.Spec = &cvar
		}
		templateParams[evar] = v
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
	case "builderImageStreamTag":
		convertToParam(string(EnvFuseS2iImage))
	case "imageStreamNamespace":
		return convertToParam(string(EnvImageStreamNamespace))
	}

	if value == "syndesis-s2i:latest" {
		return convertToParam(string(EnvFuseS2iImage))
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

func analyzeType(value interface{}) (int, interface{}) {
	switch v := value.(type) {
	case []interface{}:
		value = processSlice(cast.ToSlice(v))
		return 1, value
	case map[string]interface{}:
		processMap(cast.ToStringMap(value))
		return 2, value
	}

	return 0, value
}

func remove(s []interface{}, i int) []interface{} {
	return append(s[:i], s[i+1:]...)
}

//
// The camel.apache.org rule apiGroup cannot be installed
// in the template by a regular user and not necessary in
// the use-case for this template generation so we need to
// identify it for removal
//
func isCamelRuleMap(value interface{}) bool {
	switch v := value.(type) {
	case map[string]interface{}:
		//
		// map[apiGroups:[camel.apache.org] resources:... ...]]
		//
		m := cast.ToStringMap(v)

		if apiGroup, ok := m["apiGroups"]; ok {
			switch a := apiGroup.(type) {
			case []interface{}:
				arr := cast.ToSlice(a)
				if len(arr) == 1 && arr[0] == "camel.apache.org" {
					// Found it!
					return true
				}
			}
		}
	}

	return false
}

func processSlice(content []interface{}) []interface{} {
	finalizerIdx, camelRuleIdx := -1, -1
	for idx, value := range content {
		if isCamelRuleMap(value) {
			//
			// The camel rules are not applicable and
			// need to be removed since they can only
			// be applied by a cluster admin
			//
			camelRuleIdx = idx
		}
		//
		// Need to remove the finalizers permission
		// as cannot install templates with it
		//
		if value == "deploymentconfigs/finalizers" {
			finalizerIdx = idx
			continue
		}
		_, value = analyzeType(value)
	}

	//
	// Remove the camel group permission
	//
	if camelRuleIdx >= 0 {
		content = remove(content, camelRuleIdx)
	}

	//
	// Remove the finalizer permission
	//
	if finalizerIdx >= 0 {
		content = remove(content, finalizerIdx)
	}

	return content
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

		status, value := analyzeType(value)
		if status > 0 {
			content[key] = value
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

func exportTo(resources []unstructured.Unstructured, format string, templateName string) error {
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
	exportTemplate.SetName(templateName)

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
	for k, v := range templateParams {
		if strings.HasSuffix(k, "_LIMIT}") {
			//
			// LIMIT parameters not used
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
