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
	"fmt"

	"github.com/rogpeppe/go-internal/semver"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"gopkg.in/yaml.v2"
)

type annotation struct {
	config *configuration.Config
	body   []byte
}

func (c *annotation) build() (err error) {
	name := "fuse-online"
	if !c.config.Productized {
		name = "syndesis-operator"
	}

	channel := fmt.Sprintf("fuse-online-%s.x", semver.MajorMinor("v"+c.config.Version))

	m := map[string]map[string]string{
		"annotations": {
			"operators.operatorframework.io.bundle.mediatype.v1":       "registry+v1",
			"operators.operatorframework.io.bundle.manifests.v1":       "manifests/",
			"operators.operatorframework.io.bundle.metadata.v1":        "metadata/",
			"operators.operatorframework.io.bundle.package.v1":         name,
			"operators.operatorframework.io.bundle.channels.v1":        channel,
			"operators.operatorframework.io.bundle.channel.default.v1": channel,
		},
	}
	c.body, err = yaml.Marshal(m)
	return
}
