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
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/template"
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

type TemplateParam struct {
	Name        string `json:"name,omitempty"`
	DisplayName string `json:"displayName,omitempty"`
	Spec        *conf.ConfigSpec
}

var params = map[string]TemplateParam{}

func (o *Install) installForge() error {
	templateConfig, err := util.LoadJsonFromFile(conf.TemplateConfig)
	if err != nil {
		return err
	}

	//
	// Parse the template config
	//
	gen := &generator.Context{}
	err = json.Unmarshal(templateConfig, gen)
	if err != nil {
		return err
	}

	//
	// Create an empty syndesis CR which will
	// be filled with parameters placeholder values
	//
	syndesis := &v1alpha1.Syndesis{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: convertToParam(string(conf.EnvOpenShiftProject)),
		},
		Spec: v1alpha1.SyndesisSpec{
			DevSupport: false,
			Components: v1alpha1.ComponentsSpec{
				Db: v1alpha1.DbConfiguration{},
			},
			Addons: v1alpha1.AddonsSpec{},
		},
		Status: v1alpha1.SyndesisStatus{
			TargetVersion: "latest",
		},
	}

	//
	// Applies the placeholder config to the syndesis CR
	//
	if err := template.SetupRenderContext(gen, syndesis, template.ResourceParams{}, createConfig()); err != nil {
		return err
	}

	//
	// Process the yml template files containing
	// the framework for application
	//
	resources := make([]unstructured.Unstructured, 0)

	route, err := generator.RenderDir("./route/", gen)
	if err != nil {
		return err
	}
	resources = append(resources, route...)

	// Render the remaining syndesis resources...
	infra, err := generator.RenderDir("./infrastructure/", gen)
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
	for _, addon := range addonArr {
		if syndesis.Spec.Addons[addon] == nil {
			syndesis.Spec.Addons[addon] = v1alpha1.Parameters{}
		}
		syndesis.Spec.Addons[addon]["enabled"] = "true"

		addonDir := "./addons/" + addon + "/"
		f, err := generator.GetAssetsFS().Open(addonDir)
		if err != nil {
			fmt.Printf("Unsupported addon configured: [%s]. [%v]", addon, err)
			return err
		}
		f.Close()

		addonResources, err := generator.RenderDir(addonDir, gen)
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

		if cvar, ok := conf.AllConfigOptions[conf.SyndesisEnvVar(name)]; ok {
			v.Spec = &cvar
		}
		params[evar] = v
	}

	return evar
}

func createConfig() map[string]string {
	config := map[string]string{}

	for k, cs := range conf.AllConfigOptions {
		if (conf.ConfigSpec{} == cs) {
			// Ignore empty parameters as these should
			// not be converted to variables
			continue
		}
		config[string(k)] = convertToParam(string(k))
	}

	return config
}

func fixHardcodedExceptions(key string, value interface{}) interface{} {
	switch key {
	case "load-demo-data":
		return convertToParam(string(conf.EnvDemoDataEnabled))
	case "integrationStateCheckInterval":
		return convertToParam(string(conf.EnvIntegrationStateCheckInterval))
	case "maxDeploymentsPerUser":
		return convertToParam(string(conf.EnvMaxIntegrationsPerUser))
	case "maxIntegrationsPerUser":
		return convertToParam(string(conf.EnvMaxIntegrationsPerUser))
	}

	return value
}

func fixDataVirt(value interface{}) interface{} {
	if dvMap, ok := value.(map[string]interface{}); ok {
		dvMap["enabled"] = convertToParam(string(conf.EnvDatavirtEnabled))
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
				if spec.Value == conf.EMPTY_FIELD {
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
