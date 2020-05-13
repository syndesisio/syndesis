/*
 * Copyright (C) 2020 Red Hat, Inc.
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

//
//go:generate go run assets/assets_generate.go
package generator

import (
	"bytes"
	"fmt"
	"io/ioutil"
	"net/http"
	"path/filepath"
	"reflect"
	"sort"
	"strings"
	"text/template"

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"

	"github.com/pkg/errors"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
)

func AssetAsBytes(path string) ([]byte, error) {
	file, err := GetAssetsFS().Open(path)
	if err != nil {
		return nil, err
	}
	defer file.Close()
	prometheusRules, err := ioutil.ReadAll(file)
	if err != nil {
		return nil, err
	}
	return prometheusRules, nil
}

func isDirectory(path string) bool {
	f, err := GetAssetsFS().Open(path)
	if err != nil {
		return false
	}
	defer f.Close()

	info, err := f.Stat()
	if err != nil {
		return false
	}

	return info.IsDir()
}

func RenderDir(directory string, context interface{}) ([]unstructured.Unstructured, error) {
	return RenderFSDir(GetAssetsFS(), directory, context)
}

var templateFunctions = template.FuncMap{
	"mapHasKey": func(item reflect.Value, key reflect.Value) (bool, error) {
		if item.Kind() != reflect.Map {
			return false, fmt.Errorf("mapHasKey requires a map type")
		}
		if x := item.MapIndex(key); x.IsValid() {
			return true, nil
		} else {
			return false, nil
		}
	},
	"tagOf": func(image string) string {
		splits := strings.Split(image, ":")
		if len(splits) == 1 {
			return "latest"
		}
		return splits[len(splits)-1]
	},
}

func RenderFSDir(assets http.FileSystem, directory string, context interface{}) ([]unstructured.Unstructured, error) {
	f, err := assets.Open(directory)
	if err != nil {
		return nil, err
	}
	defer f.Close()

	files, err := f.Readdir(-1)
	if err != nil {
		return nil, err
	}
	sort.Slice(files, func(i, j int) bool {
		return files[i].Name() < files[j].Name()
	})

	var response []unstructured.Unstructured
	for _, f := range files {
		filePath := directory + "/" + f.Name()
		r, err := Render(filePath, context)
		if err != nil {
			return nil, err
		}
		response = append(response, r...)
	}
	return response, nil
}

/*
 * Can render a file or directory of files, the latter being
 * done by delegating to renderDir if file is a directory.
 */
func Render(filePath string, context interface{}) ([]unstructured.Unstructured, error) {
	if isDirectory(filePath) {
		return RenderDir(filePath, context)
	}

	var obj interface{} = nil
	var response []unstructured.Unstructured

	// We can load plain yml files..
	if strings.HasSuffix(filePath, ".yml") || strings.HasSuffix(filePath, ".yaml") {
		if !skipFile(filePath, context) {
			fileData, err := AssetAsBytes(filePath)
			if err != nil {
				return nil, err
			}

			err = util.UnmarshalYaml(fileData, &obj)
			if err != nil {
				return nil, errors.Errorf("%s:\n%s\n", err, string(fileData))
			}
		}
	}

	// We can process go lang templates.
	if strings.HasSuffix(filePath, ".yml.tmpl") || strings.HasSuffix(filePath, ".yaml.tmpl") {
		fileData, err := AssetAsBytes(filePath)
		if err != nil {
			return nil, err
		}

		tmpl, err := template.New(filePath).Funcs(templateFunctions).Parse(string(fileData))
		if err != nil {
			return nil, err
		}

		buffer := &bytes.Buffer{}
		err = tmpl.Execute(buffer, context)
		if err != nil {
			return nil, err
		}
		rawYaml := buffer.Bytes()

		err = util.UnmarshalYaml(rawYaml, &obj)
		if err != nil {
			return nil, errors.Errorf("%s: %s: \n%s\n", filePath, err, string(rawYaml))
		}
	}

	switch v := obj.(type) {
	case []interface{}:
		for _, value := range v {
			if x, ok := value.(map[string]interface{}); ok {
				u := unstructured.Unstructured{x}
				//annotatedForDebugging(u, name, rawYaml)
				response = append(response, u)
			} else {
				return nil, errors.New("list did not contain objects")
			}
		}
	case map[string]interface{}:
		u := unstructured.Unstructured{v}
		//annotatedForDebugging(u, name, rawYaml)
		response = append(response, u)
	case nil:
		// It's ok if a template chooses not to generate any resources..

	default:
		return nil, fmt.Errorf("Unexptected yaml unmarshal type: %v", obj)
	}

	return response, nil
}

// Skip file if conditions are met
func skipFile(file string, config interface{}) bool {
	if x, ok := config.(*configuration.Config); ok {
		if x.Syndesis.Components.Database.ExternalDbURL != "" {
			for _, wfile := range []string{"addon-ops-db-alerting-rules.yml", "addon-ops-db-dashboard.yml"} {
				if wfile == filepath.Base(file) {
					return true
				}
			}
		}
	}

	return false
}
