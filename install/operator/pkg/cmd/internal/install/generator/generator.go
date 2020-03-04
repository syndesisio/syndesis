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
package main

import (
	"fmt"
	"io/ioutil"
	"log"

	"gopkg.in/yaml.v2"
)

type config struct {
	Syndesis struct {
		Components struct {
			Database struct {
				Image string `yaml:"Image"`
			} `yaml:"Database"`
		} `yaml:"Components"`
	} `yaml:"Syndesis"`
}

func main() {
	data, err := ioutil.ReadFile("../../../../build/conf/config.yaml")
	if err != nil {
		log.Fatal(err)
	}

	c := config{}

	err = yaml.Unmarshal(data, &c)
	if err != nil {
		log.Fatal(err)
	}

	code := fmt.Sprintf(`package install

const defaultDatabaseImage = "%s"
`, c.Syndesis.Components.Database.Image)

	err = ioutil.WriteFile("install_defaults.go", []byte(code), 0644)
	if err != nil {
		log.Fatal(err)
	}
}
