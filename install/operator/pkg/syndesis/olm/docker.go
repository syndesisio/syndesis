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
)

type docker struct {
	config *configuration.Config
	body   []byte
}

func (c *docker) build() (err error) {
	channel := fmt.Sprintf("fuse-online-%s", semver.MajorMinor("v"+c.config.Version))

	m := `FROM scratch

LABEL operators.operatorframework.io.bundle.mediatype.v1=registry+v1
LABEL operators.operatorframework.io.bundle.manifests.v1=manifests/
LABEL operators.operatorframework.io.bundle.metadata.v1=metadata/
LABEL operators.operatorframework.io.bundle.package.v1=fuse-online
LABEL operators.operatorframework.io.bundle.channels.v1=%s
LABEL operators.operatorframework.io.bundle.channel.default.v1=%s
LABEL com.redhat.delivery.operator.bundle=true
LABEL com.redhat.openshift.versions="%s"

COPY manifests /manifests/
COPY metadata/annotations.yaml /metadata/annotations.yaml

LABEL name="fuse7/fuse-online-operator-metadata" \
      version="%s" \
      maintainer="Otavio Piske <opiske@redhat.com>" \
      summary="Operator which manages the lifecycle of the Fuse Online application." \
      description="Operator which manages the lifecycle of the Fuse Online application." \
      com.redhat.component="fuse-online-operator-metadata-container" \
      io.k8s.description="Operator which manages the lifecycle of the Fuse Online application." \
      io.k8s.display-name="Red Hat Fuse Online Operator" \
      io.openshift.tags="fuse"
`
	m = fmt.Sprintf(m, channel, channel, c.config.SupportedOpenShiftVersions, c.config.Version)
	c.body = []byte(m)
	return
}
