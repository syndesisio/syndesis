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

import java.io.IOException;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.syndesis.common.model.Violation;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Oas30SchemaValidatorTest {

    @Test
    public void shouldNotGenerateErrorForValidSpec() throws IOException {
        final Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.info = openApiDoc.createInfo();
        openApiDoc.info.version = "1.0.0";
        openApiDoc.info.title = "Simple API";
        openApiDoc.paths = openApiDoc.createPaths();
        final OpenApiModelInfo.Builder infoBuilder = new OpenApiModelInfo.Builder().model(openApiDoc);

        new Oas30SchemaValidator().validateJSonSchema(JsonUtils.reader().readTree(Library.writeDocumentToJSONString(openApiDoc)), infoBuilder);
        OpenApiModelInfo validated = infoBuilder.build();
        assertThat(validated.getWarnings()).isEmpty();
        assertThat(validated.getErrors()).isEmpty();
    }

    @Test
    public void shouldGenerateErrorForIncompleteSpec() throws IOException {
        final Oas30Document openApiDoc = new Oas30Document();
        final OpenApiModelInfo.Builder infoBuilder = new OpenApiModelInfo.Builder().model(openApiDoc);

        new Oas30SchemaValidator().validateJSonSchema(JsonUtils.reader().readTree(Library.writeDocumentToJSONString(openApiDoc)), infoBuilder);
        OpenApiModelInfo validated = infoBuilder.build();
        assertThat(validated.getWarnings()).isEmpty();
        assertThat(validated.getErrors()).containsOnly(new Violation.Builder().error("validation").message("object has missing required properties ([\"info\",\"paths\"])").property("").build());
    }
}
