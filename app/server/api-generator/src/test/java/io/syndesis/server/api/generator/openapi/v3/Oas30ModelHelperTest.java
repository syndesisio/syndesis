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

import io.apicurio.datamodels.core.models.common.Server;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import org.assertj.core.api.Assertions;
import org.junit.Test;

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
    }

    private static Server getServerWithUrl(String url) {
        return getOpenApiDocWithUrl(url).servers.get(0);
    }

    private static Oas30Document getOpenApiDocWithUrl(String url) {
        Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.addServer(url, "Dummy for testing");
        return openApiDoc;
    }
}
