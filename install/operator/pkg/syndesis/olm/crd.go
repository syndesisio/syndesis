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

package olm

import (
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"gopkg.in/yaml.v2"
)

type crd struct {
	body []byte
}

// Build the content of the crd file
func (c *crd) build() (err error) {
	g, err := generator.Render("assets/install/cluster/syndesis.yml", nil)
	if err != nil {
		return err
	}

	mjson, err := g[0].MarshalJSON()
	if err != nil {
		return err
	}

	m := make(map[string]interface{})
	if err := yaml.Unmarshal(mjson, &m); err != nil {
		return err
	}

	c.body, err = yaml.Marshal(m)
	return
}
