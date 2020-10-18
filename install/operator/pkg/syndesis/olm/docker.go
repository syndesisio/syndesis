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

import "gopkg.in/yaml.v2"

type docker struct {
	body []byte
}

func (d *docker) build() (err error) {
	m := map[string]map[string]string{
		"operator_manifests": {
			"manifests_dir":                "manifests",
			"enable_digest_pinning":        "false",
			"enable_repo_replacements":     "false",
			"enable_registry_replacements": "false",
		},
	}
	d.body, err = yaml.Marshal(m)
	return
}
