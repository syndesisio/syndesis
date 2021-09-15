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

package util

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

 func TestOpenshift3ExtractWebConsoleUrl(t *testing.T) {

	t.Run("Parse and extract Web Console Url", func(t *testing.T) {
		expectedUrl := "https://console.apps.7d1d.example.opentlc.com/"
		ocp3WebConsole := fmt.Sprintf("apiVersion: webconsole.config.openshift.io/v1\nclusterInfo:\n  adminConsolePublicURL: %s\n  consolePublicURL: https://master.7d1d.example.opentlc.com/console/\n  loggingPublicURL: https://kibana.apps.7d1d.example.opentlc.com\n  logoutPublicURL: ''\n  masterPublicURL: https://master.7d1d.example.opentlc.com:443\n  metricsPublicURL: https://hawkular-metrics.apps.7d1d.example.opentlc.com/hawkular/metrics\nextensions:\n  properties: {}\n  scriptURLs: []\n  stylesheetURLs: []\nfeatures:\n  clusterResourceOverridesEnabled: false\n  inactivityTimeoutMinutes: 0\nkind: WebConsoleConfiguration\nservingInfo:\n  bindAddress: 0.0.0.0:8443\n  bindNetwork: tcp4\n  certFile: /var/serving-cert/tls.crt\n  clientCA: ''\n  keyFile: /var/serving-cert/tls.key\n  maxRequestsInFlight: 0\n  namedCertificates: null\n  requestTimeoutSeconds: 0\n", expectedUrl)
		extracted := extractWebConsoleUrl(ocp3WebConsole)
		assert.Equal(t, expectedUrl, extracted)
	})
}
