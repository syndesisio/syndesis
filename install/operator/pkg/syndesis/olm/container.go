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

type container struct {
	body []byte
}

func (d *container) build() (err error) {
	c := `operator_manifests:
  manifests_dir: manifests
  enable_digest_pinning: true
  enable_repo_replacements: true
  enable_registry_replacements: true
  repo_replacements:
    - registry: registry.redhat.io
      package_mappings:
        fuse-online-operator-container: fuse7
        fuse-ignite-s2i-container: fuse7
        fuse-ignite-server-container: fuse7
        fuse-ignite-meta-container: fuse7
        fuse-ignite-upgrade-container: fuse7
        fuse-ignite-ui-container: fuse7
        fuse-postgres-exporter-container: fuse7
`

	d.body = []byte(c)
	return
}
