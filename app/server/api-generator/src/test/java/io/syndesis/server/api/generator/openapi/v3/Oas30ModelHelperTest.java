/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.syndesis.server.api.generator.openapi.v3;

import java.util.Arrays;

import io.apicurio.datamodels.core.models.common.Server;
import io.apicurio.datamodels.core.models.common.ServerVariable;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30ServerVariable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class Oas30ModelHelperTest {

    @Test
    public void shouldGetBasePathFromServerDefinition() {
        Assertions.assertThat(Oas30ModelHelper.getBasePath(getOpenApiDocWithUrl(null))).isEqualTo("/");
        Assertions.assertThat(Oas30ModelHelper.getBasePath(getOpenApiDocWithUrl(""))).isEqualTo("/");
        Assertions.assertThat(Oas30ModelHelper.getBasePath(getOpenApiDocWithUrl("http://syndesis.io"))).isEqualTo("/");
        Assertions.assertThat(Oas30ModelHelper.getBasePath(getOpenApiDocWithUrl("http://syndesis.io/"))).isEqualTo("/");
        Assertions.assertThat(Oas30ModelHelper.getBasePath(getOpenApiDocWithUrl("http://syndesis.io/v1"))).isEqualTo("/v1");
        Assertions.assertThat(Oas30ModelHelper.getBasePath(getOpenApiDocWithUrl("https://syndesis.io/v1"))).isEqualTo("/v1");
        Assertions.assertThat(Oas30ModelHelper.getBasePath(getOpenApiDocWithUrl("/v1"))).isEqualTo("/v1");
        Assertions.assertThat(Oas30ModelHelper.getBasePath(getOpenApiDocWithUrl("http is awesome!"))).isEqualTo("/");
        Assertions.assertThat(Oas30ModelHelper.getBasePath(getOpenApiDocWithUrl("{scheme}://syndesis.io/{basePath}",
            variable("scheme", "http", "https"),
            variable("basePath", "v1", "v2")))).isEqualTo("/v1");
    }

    @Test
    public void shouldGetURLSchemeFromServerDefinition() {
        Assertions.assertThat(Oas30ModelHelper.getScheme(getServerWithUrl(null))).isEqualTo("http");
        Assertions.assertThat(Oas30ModelHelper.getScheme(getServerWithUrl(""))).isEqualTo("http");
        Assertions.assertThat(Oas30ModelHelper.getScheme(getServerWithUrl("http://syndesis.io"))).isEqualTo("http");
        Assertions.assertThat(Oas30ModelHelper.getScheme(getServerWithUrl("http://syndesis.io/v1"))).isEqualTo("http");
        Assertions.assertThat(Oas30ModelHelper.getScheme(getServerWithUrl("https://syndesis.io/v1"))).isEqualTo("https");
        Assertions.assertThat(Oas30ModelHelper.getScheme(getServerWithUrl("/v1"))).isEqualTo("http");
        Assertions.assertThat(Oas30ModelHelper.getScheme(getServerWithUrl("http is awesome!"))).isEqualTo("http");
        Assertions.assertThat(Oas30ModelHelper.getScheme(getServerWithUrl("Something completely different"))).isEqualTo("http");
        Assertions.assertThat(Oas30ModelHelper.getScheme(getServerWithUrl("{scheme}://syndesis.io/v1", variable("scheme", "https")))).isEqualTo("https");
        Assertions.assertThat(Oas30ModelHelper.getScheme(getServerWithUrl("{scheme}://syndesis.io/v1", variable("scheme", "https", "http")))).isEqualTo("https");
    }

    @Test
    public void shouldGetHostFromServerDefinition() {
        Assertions.assertThat(Oas30ModelHelper.getHost(getOpenApiDocWithUrl(null))).isNull();
        Assertions.assertThat(Oas30ModelHelper.getHost(getOpenApiDocWithUrl(""))).isNull();
        Assertions.assertThat(Oas30ModelHelper.getHost(getOpenApiDocWithUrl("http://syndesis.io"))).isEqualTo("syndesis.io");
        Assertions.assertThat(Oas30ModelHelper.getHost(getOpenApiDocWithUrl("http://syndesis.io/v1"))).isEqualTo("syndesis.io");
        Assertions.assertThat(Oas30ModelHelper.getHost(getOpenApiDocWithUrl("https://syndesis.io/v1"))).isEqualTo("syndesis.io");
        Assertions.assertThat(Oas30ModelHelper.getHost(getOpenApiDocWithUrl("/v1"))).isNull();
        Assertions.assertThat(Oas30ModelHelper.getHost(getOpenApiDocWithUrl("http is awesome!"))).isNull();
        Assertions.assertThat(Oas30ModelHelper.getHost(getOpenApiDocWithUrl("Something completely different"))).isNull();
        Assertions.assertThat(Oas30ModelHelper.getHost(getOpenApiDocWithUrl("{scheme}://syndesis.io/v1", variable("scheme", "https")))).isEqualTo("syndesis.io");
        Assertions.assertThat(Oas30ModelHelper.getHost(getOpenApiDocWithUrl("{scheme}://{host}/{basePath}",
            variable("scheme", "http", "https"),
            variable("host", "syndesis.io"),
            variable("basePath", "v1", "v2")))).isEqualTo("syndesis.io");
    }

    private static Server getServerWithUrl(String url, ServerVariable... variables) {
        return getOpenApiDocWithUrl(url, variables).servers.get(0);
    }

    private static Oas30Document getOpenApiDocWithUrl(String url, ServerVariable... variables) {
        Oas30Document openApiDoc = new Oas30Document();
        Server server = openApiDoc.addServer(url, "Dummy for testing");

        for (ServerVariable variable : variables) {
            server.addServerVariable(variable._name, variable);
        }

        return openApiDoc;
    }

    private static ServerVariable variable(String name, String... values) {
        Oas30ServerVariable variable = new Oas30ServerVariable(name);

        if (values.length > 0) {
            variable.default_ = values[0];
        }

        if (values.length > 1) {
            variable.enum_ = Arrays.asList(values);
        }

        return variable;
    }
}
