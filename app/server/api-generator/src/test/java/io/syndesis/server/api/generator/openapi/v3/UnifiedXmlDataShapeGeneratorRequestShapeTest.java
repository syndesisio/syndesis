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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class UnifiedXmlDataShapeGeneratorRequestShapeTest {

    @Parameter(0)
    public String operation;

    @Parameter(1)
    public String path;

    @Parameter(2)
    public String schemaset;

    private final UnifiedXmlDataShapeGenerator generator = new UnifiedXmlDataShapeGenerator();

    private final ObjectNode json;

    private final Oas30Document openApiDoc;

    public UnifiedXmlDataShapeGeneratorRequestShapeTest() throws IOException {
        final String specification;
        try (InputStream in = UnifiedXmlDataShapeGenerator.class.getResourceAsStream("/openapi/v3/petstore.json")) {
            specification = IOUtils.toString(in, StandardCharsets.UTF_8);
        }

        json = (ObjectNode) JsonUtils.reader().readTree(specification);
        openApiDoc = (Oas30Document) Library.readDocumentFromJSONString(specification);
    }

    @Test
    public void shouldGenerateAtlasmapSchemaSetForUpdatePetRequest() throws IOException {
        final Oas30Operation openApiOperation = OasModelHelper.getOperationMap(openApiDoc.paths.getPathItem(path), Oas30Operation.class).get(operation);

        final DataShape shape = generator.createShapeFromRequest(json, openApiDoc, openApiOperation);

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(shape.getKind()).isEqualTo(DataShapeKinds.XML_SCHEMA);
        softly.assertThat(shape.getName()).isEqualTo("Request");
        softly.assertThat(shape.getDescription()).isEqualTo("API request payload");
        softly.assertThat(shape.getExemplar()).isNotPresent();
        softly.assertAll();

        final String expectedSpecification;
        try (InputStream in = UnifiedXmlDataShapeGenerator.class.getResourceAsStream("/openapi/v3/" + schemaset)) {
            expectedSpecification = IOUtils.toString(in, StandardCharsets.UTF_8);
        }

        final String specification = shape.getSpecification();

        assertThat(specification).isXmlEqualTo(expectedSpecification);
    }

    @Parameters
    public static Iterable<Object[]> data() {
        return Arrays.<Object[]>asList(//
            new Object[] {//
                "put", //
                "/pet", //
                "petstore.update-pet.schemaset.xml"//
            }, //
            new Object[] {//
                "post", //
                "/pet/{petId}", //
                "petstore.update-pet-with-form.schemaset.xml"//
            });
    }
}
