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
package io.syndesis.server.api.generator.openapi.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20SecurityScheme;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.syndesis.server.jsondb.impl.JsonRecordSupport;

import org.junit.jupiter.api.Test;

import static io.syndesis.server.api.generator.openapi.TestHelper.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class OasModelHelperTest {

    @Test
    public void minimizingShouldNotLooseHeaderParameters() {
        final String definition = "{\"openapi\": \"3.0.0\", \"paths\": {\"/\": {\"get\": {\"parameters\": [{\"in\": \"header\", \"name\": \"operation-header\"}]},\"parameters\": [{\"in\": \"header\", \"name\": \"path-header\"}]}}}";

        final OasDocument original = (OasDocument) Library.readDocumentFromJSONString(definition);
        final OasPathItem originalPath = original.paths.getPathItem("/");
        assertThat(originalPath.getParameters()).extracting(OasParameter::getName).containsOnly("path-header");
        assertThat(originalPath.get.getParameters()).extracting(OasParameter::getName).containsOnly("operation-header");

        final String minimizedString = SpecificationOptimizer.minimizeForComponent(original);

        final OasDocument minimized = (OasDocument) Library.readDocumentFromJSONString(minimizedString);

        final OasPathItem path = minimized.paths.getPathItem("/");
        assertThat(path.getParameters()).extracting(OasParameter::getName).containsOnly("path-header");

        final OasOperation operation = path.get;
        assertThat(operation.parameters).extracting(OasParameter::getName).containsOnly("operation-header");
    }

    @Test
    public void minimizingShouldNotLooseMultipleKeySecurityRequirements() {
        final String definition = "{\"swagger\":\"2.0\",\"paths\":{\"/api\":{\"get\":{\"security\":[{\"secured1\":[]},{\"secured2\":[]}]}}}}";

        final Oas20Document openApiDoc = (Oas20Document) Library.readDocumentFromJSONString(definition);

        final String minimizedString = SpecificationOptimizer.minimizeForComponent(openApiDoc);

        final Oas20Document minimized = (Oas20Document) Library.readDocumentFromJSONString(minimizedString);

        final Oas20Operation getApi = (Oas20Operation) minimized.paths.getPathItem("/api").get;
        assertThat(getApi.security).hasSize(2);
        assertThat(getApi.security.get(0).getSecurityRequirementNames()).containsExactly("secured1");
        assertThat(getApi.security.get(0).getScopes("secured1")).isEmpty();
        assertThat(getApi.security.get(1).getSecurityRequirementNames()).containsExactly("secured2");
        assertThat(getApi.security.get(1).getScopes("secured2")).isEmpty();
    }

    @Test
    public void minimizingShouldNotLooseSecurityDefinitions() {
        final String definition = "{\"swagger\":\"2.0\",\"securityDefinitions\": {\n" +
            "        \"api-key-header\": {\n" +
            "            \"type\": \"apiKey\",\n" +
            "            \"name\": \"API-KEY\",\n" +
            "            \"in\": \"header\"\n" +
            "        },\n" +
            "        \"api-key-parameter\": {\n" +
            "            \"type\": \"apiKey\",\n" +
            "            \"name\": \"api_key\",\n" +
            "            \"in\": \"query\"\n" +
            "        }\n" +
            "    }}";

        final Oas20Document openApiDoc = (Oas20Document) Library.readDocumentFromJSONString(definition);

        final String minimizedString = SpecificationOptimizer.minimizeForComponent(openApiDoc);

        final Oas20Document minimized = (Oas20Document) Library.readDocumentFromJSONString(minimizedString);

        final Oas20SecurityScheme apiKeyHeader = new Oas20SecurityScheme("api-key-header");
        apiKeyHeader.type = "apiKey";
        apiKeyHeader.in = "header";
        apiKeyHeader.name = "API-KEY";

        final Oas20SecurityScheme apiKeyParameter = new Oas20SecurityScheme("api-key-parameter");
        apiKeyParameter.type = "apiKey";
        apiKeyParameter.in = "query";
        apiKeyParameter.name = "api_key";

        assertThat(minimized.securityDefinitions.getSecuritySchemes()).hasSize(2);
        assertThat(minimized.securityDefinitions.getSecurityScheme(apiKeyHeader._schemeName).type).isEqualTo(apiKeyHeader.type);
        assertThat(minimized.securityDefinitions.getSecurityScheme(apiKeyHeader._schemeName).in).isEqualTo(apiKeyHeader.in);
        assertThat(minimized.securityDefinitions.getSecurityScheme(apiKeyHeader._schemeName).name).isEqualTo(apiKeyHeader.name);
        assertThat(minimized.securityDefinitions.getSecurityScheme(apiKeyParameter._schemeName).type).isEqualTo(apiKeyParameter.type);
        assertThat(minimized.securityDefinitions.getSecurityScheme(apiKeyParameter._schemeName).in).isEqualTo(apiKeyParameter.in);
        assertThat(minimized.securityDefinitions.getSecurityScheme(apiKeyParameter._schemeName).name).isEqualTo(apiKeyParameter.name);
    }

    @Test
    public void minimizingShouldProduceReadableV2Specification() throws IOException {
        final String specification = resource("/openapi/v2/todo.json");

        final Oas20Document openApiDoc = (Oas20Document) Library.readDocumentFromJSONString(specification);
        final String minimizedString = SpecificationOptimizer.minimizeForComponent(openApiDoc);

        final Oas20Document minimized = (Oas20Document) Library.readDocumentFromJSONString(minimizedString);
        assertThat(minimized.paths.getItems()).hasSize(2);
    }

    @Test
    public void minimizingShouldProduceReadableV3Specification() throws IOException {
        final String specification = resource("/openapi/v3/todo.json");

        final Oas30Document openApiDoc = (Oas30Document) Library.readDocumentFromJSONString(specification);
        final String minimizedString = SpecificationOptimizer.minimizeForComponent(openApiDoc);

        final Oas30Document minimized = (Oas30Document) Library.readDocumentFromJSONString(minimizedString);
        assertThat(minimized.paths.getItems()).hasSize(2);
    }

    @Test
    public void shouldSanitizeListOfTags() {
        assertThat(OasModelHelper.sanitizeTags(Arrays.asList("tag", "wag ", " bag", ".]t%a$g#[/")))
            .containsExactly("tag", "wag", "bag");
    }

    @Test
    public void shouldSanitizeTags() {
        assertThat(OasModelHelper.sanitizeTag("tag")).isEqualTo("tag");
        assertThat(OasModelHelper.sanitizeTag(".]t%a$g#[/")).isEqualTo("tag");

        final char[] str = new char[1024];
        final String randomString = IntStream.range(0, str.length)
            .map(x -> (int) (Character.MAX_CODE_POINT * Math.random())).mapToObj(i -> new String(Character.toChars(i)))
            .collect(Collectors.joining(""));
        final String sanitized = OasModelHelper.sanitizeTag(randomString);
        assertThatCode(() -> JsonRecordSupport.validateKey(sanitized)).doesNotThrowAnyException();
    }
}
