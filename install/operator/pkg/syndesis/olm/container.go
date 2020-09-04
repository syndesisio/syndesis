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

	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
)

type container struct {
	config *configuration.Config
	body   []byte
}

func (c *container) build() (err error) {
	m := "FROM scratch"
	m += "LABEL operators.operatorframework.io.bundle.mediatype.v1=registry+v1"
	m += "LABEL operators.operatorframework.io.bundle.manifests.v1=manifests/"
	m += "LABEL operators.operatorframework.io.bundle.metadata.v1=metadata/"
	m += "LABEL operators.operatorframework.io.bundle.package.v1=fuse-online"
	m += "LABEL operators.operatorframework.io.bundle.channels.v1=alpha"
	m += "LABEL operators.operatorframework.io.bundle.channel.default.v1=alpha"
	m += "LABEL com.redhat.delivery.operator.bundle=true"
	m += "LABEL com.redhat.openshift.versions=\"v4.5,v4.6\""

	m += "COPY manifests /manifests/"
	m += "COPY metadata/annotations.yaml /metadata/annotations.yaml"

	m += "LABEL name=\"fuse7/fuse-online-operator-metadata\""
	m += fmt.Sprintf("LABEL version=\"%s\"", c.config.Version)
	m += "LABEL maintainer=\"Otavio Piske <opiske@redhat.com>\""
	m += "LABEL summary=\"Operator which manages the lifecycle of the Fuse Online application.\""
	m += "LABEL description=\"Operator which manages the lifecycle of the Fuse Online application.\""
	m += "LABEL com.redhat.component=\"fuse-online-operator-metadata-container\""
	m += "LABEL io.k8s.description=\"Operator which manages the lifecycle of the Fuse Online application.\""
	m += "LABEL io.k8s.display-name=\"Red Hat Fuse Online Operator\""
	m += "LABEL io.openshift.tags=\"fuse\""

	c.body = []byte(m)
	return
}
