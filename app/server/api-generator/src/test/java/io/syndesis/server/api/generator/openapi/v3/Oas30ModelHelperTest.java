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

import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class Oas30ModelHelperTest {

    @Test
    public void shouldGetBasePathFromServerDefinition() {
        Assertions.assertThat(Oas30ModelHelper.getBasePath(getOpenApiDocWithBasePath(null))).isEqualTo("/");
        Assertions.assertThat(Oas30ModelHelper.getBasePath(getOpenApiDocWithBasePath(""))).isEqualTo("/");
        Assertions.assertThat(Oas30ModelHelper.getBasePath(getOpenApiDocWithBasePath("http://syndesis.io"))).isEqualTo("/");
        Assertions.assertThat(Oas30ModelHelper.getBasePath(getOpenApiDocWithBasePath("http://syndesis.io/"))).isEqualTo("/");
        Assertions.assertThat(Oas30ModelHelper.getBasePath(getOpenApiDocWithBasePath("http://syndesis.io/v1"))).isEqualTo("/v1");
        Assertions.assertThat(Oas30ModelHelper.getBasePath(getOpenApiDocWithBasePath("/v1"))).isEqualTo("/v1");
        Assertions.assertThat(Oas30ModelHelper.getBasePath(getOpenApiDocWithBasePath("http is awesome!"))).isEqualTo("/");
    }

    private static Oas30Document getOpenApiDocWithBasePath(String basePath) {
        Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.addServer(basePath, "Dummy for testing");
        return openApiDoc;
    }
}
