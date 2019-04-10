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
package io.syndesis.server.api.generator.swagger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.openapi.OpenApiHelper;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class UnifiedXmlDataShapeGeneratorRequestShapeTest {

    @Parameter(0)
    public HttpMethod operation;

    @Parameter(1)
    public String path;

    @Parameter(2)
    public String schemaset;

    private final UnifiedXmlDataShapeGenerator generator = new UnifiedXmlDataShapeGenerator();

    private final ObjectNode json;

    private final Swagger swagger;

    public UnifiedXmlDataShapeGeneratorRequestShapeTest() throws IOException {
        final String specification;
        try (InputStream in = UnifiedXmlDataShapeGenerator.class.getResourceAsStream("/swagger/petstore.swagger.json")) {
            specification = IOUtils.toString(in, StandardCharsets.UTF_8);
        }

        json = (ObjectNode) Json.reader().readTree(specification);
        swagger = OpenApiHelper.parse(specification);
    }

    @Test
    public void shouldGenerateAtlasmapSchemaSetForUpdatePetRequest() throws IOException {
        final Operation swaggerOperation = swagger.getPaths().get(path).getOperationMap().get(operation);

        final DataShape shape = generator.createShapeFromRequest(json, swagger, swaggerOperation);

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(shape.getKind()).isEqualTo(DataShapeKinds.XML_SCHEMA);
        softly.assertThat(shape.getName()).isEqualTo("Request");
        softly.assertThat(shape.getDescription()).isEqualTo("API request payload");
        softly.assertThat(shape.getExemplar()).isNotPresent();
        softly.assertAll();

        final String expectedSpecification;
        try (InputStream in = UnifiedXmlDataShapeGenerator.class.getResourceAsStream("/swagger/" + schemaset)) {
            expectedSpecification = IOUtils.toString(in, StandardCharsets.UTF_8);
        }

        final String specification = shape.getSpecification();

        assertThat(specification).isXmlEqualTo(expectedSpecification);
    }

    @Parameters
    public static Iterable<Object[]> data() {
        return Arrays.<Object[]>asList(//
            new Object[] {//
                HttpMethod.PUT, //
                "/pet", //
                "petstore.update-pet.schemaset.xml"//
            }, //
            new Object[] {//
                HttpMethod.POST, //
                "/pet/{petId}", //
                "petstore.update-pet-with-form.schemaset.xml"//
            });
    }
}
