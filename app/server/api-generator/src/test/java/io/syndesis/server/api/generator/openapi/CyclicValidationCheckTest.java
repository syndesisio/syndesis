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
package io.syndesis.server.api.generator.openapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20PathItem;
import io.apicurio.datamodels.openapi.v2.models.Oas20Response;
import io.apicurio.datamodels.openapi.v2.models.Oas20Schema;
import io.apicurio.datamodels.openapi.v2.models.Oas20SchemaDefinition;
import io.syndesis.common.util.Resources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CyclicValidationCheckTest {

    @Test
    public void shouldFindCyclicReferenceInOpenHabSwagger() throws IOException {
        final Oas20Document openApiDoc = (Oas20Document) Library.readDocumentFromJSONString(Resources.getResourceAsText("openapi/v2/openhab.json"));

        assertThat(CyclicValidationCheck.hasCyclicReferences(getSchemaDefinitions(openApiDoc))).isTrue();
    }

    @Test
    public void shouldFindCyclicSelfReferencesInParameters() {
        final Oas20Document openApiDoc = new Oas20Document();
        final Oas20PathItem pathItem = new Oas20PathItem("/api");
        final Oas20Operation operation = new Oas20Operation("post");
        final Oas20Parameter parameter = new Oas20Parameter();
        parameter.$ref = "#/definitions/A";
        operation.parameters = new ArrayList<>();
        operation.parameters.add(parameter);
        pathItem.post = operation;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/api", pathItem);

        Oas20SchemaDefinition schemaDefinition = new Oas20SchemaDefinition("A");
        schemaDefinition.type = "object";
        Oas20Schema propertySchema = new Oas20Schema();
        propertySchema.$ref = "#/definitions/A";
        schemaDefinition.properties = new HashMap<>();
        schemaDefinition.properties.put("cyclic", propertySchema);
        openApiDoc.definitions = openApiDoc.createDefinitions();
        openApiDoc.definitions.addDefinition("A", schemaDefinition);

        assertThat(CyclicValidationCheck.hasCyclicReferences(getSchemaDefinitions(openApiDoc))).isTrue();
    }

    @Test
    public void shouldFindCyclicSelfReferencesInResponses() {
        final Oas20Document openApiDoc = new Oas20Document();
        final Oas20PathItem pathItem = new Oas20PathItem("/api");
        final Oas20Operation operation = new Oas20Operation("post");

        Oas20Response response = new Oas20Response("200");
        Oas20Schema responseSchema = new Oas20Schema();
        responseSchema.$ref = "#/definitions/A";
        response.schema = responseSchema;
        operation.responses = operation.createResponses();
        operation.responses.addResponse("200", response);

        pathItem.post = operation;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/api", pathItem);

        Oas20SchemaDefinition schemaDefinition = new Oas20SchemaDefinition("A");
        schemaDefinition.type = "object";
        Oas20Schema propertySchema = new Oas20Schema();
        propertySchema.$ref = "#/definitions/A";
        schemaDefinition.properties = new HashMap<>();
        schemaDefinition.properties.put("cyclic", propertySchema);
        openApiDoc.definitions = openApiDoc.createDefinitions();
        openApiDoc.definitions.addDefinition("A", schemaDefinition);

        assertThat(CyclicValidationCheck.hasCyclicReferences(getSchemaDefinitions(openApiDoc))).isTrue();
    }

    @Test
    public void shouldFindMultipleStepCyclicReferencesInParameters() {
        final Oas20Document openApiDoc = new Oas20Document();
        final Oas20PathItem pathItem = new Oas20PathItem("/api");
        final Oas20Operation operation = new Oas20Operation("post");
        final Oas20Parameter parameter = new Oas20Parameter();
        parameter.$ref = "#/definitions/A";
        operation.parameters = new ArrayList<>();
        operation.parameters.add(parameter);
        pathItem.post = operation;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/api", pathItem);
        openApiDoc.definitions = openApiDoc.createDefinitions();

        for (int c = 'A'; c < 'Z'; c++) {
            final String current = String.valueOf((char) c);
            final String next = String.valueOf((char) (c + 1));

            Oas20SchemaDefinition schemaDefinition = new Oas20SchemaDefinition(current);
            schemaDefinition.type = "object";
            Oas20Schema propertySchema = new Oas20Schema();
            propertySchema.$ref = "#/definitions/" + next;
            schemaDefinition.properties = new HashMap<>();
            schemaDefinition.properties.put(next, propertySchema);
            openApiDoc.definitions.addDefinition(current, schemaDefinition);
        }

        Oas20SchemaDefinition schemaDefinition = new Oas20SchemaDefinition("Z");
        schemaDefinition.type = "object";
        Oas20Schema propertySchema = new Oas20Schema();
        propertySchema.$ref = "#/definitions/A";
        schemaDefinition.properties = new HashMap<>();
        schemaDefinition.properties.put("a", propertySchema);
        openApiDoc.definitions.addDefinition("Z", schemaDefinition);

        assertThat(CyclicValidationCheck.hasCyclicReferences(getSchemaDefinitions(openApiDoc))).isTrue();
    }

    @Test
    public void shouldFindMultipleStepCyclicReferencesInResponses() {
        final Oas20Document openApiDoc = new Oas20Document();
        final Oas20PathItem pathItem = new Oas20PathItem("/api");
        final Oas20Operation operation = new Oas20Operation("post");

        Oas20Response response = new Oas20Response("200");
        Oas20Schema responseSchema = new Oas20Schema();
        responseSchema.$ref = "#/definitions/A";
        response.schema = responseSchema;
        operation.responses = operation.createResponses();
        operation.responses.addResponse("200", response);

        pathItem.post = operation;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/api", pathItem);
        openApiDoc.definitions = openApiDoc.createDefinitions();

        for (int c = 'A'; c < 'Z'; c++) {
            final String current = String.valueOf((char) c);
            final String next = String.valueOf((char) (c + 1));

            Oas20SchemaDefinition schemaDefinition = new Oas20SchemaDefinition(current);
            schemaDefinition.type = "object";
            Oas20Schema propertySchema = new Oas20Schema();
            propertySchema.$ref = "#/definitions/" + next;
            schemaDefinition.properties = new HashMap<>();
            schemaDefinition.properties.put(next, propertySchema);
            openApiDoc.definitions.addDefinition(current, schemaDefinition);
        }

        Oas20SchemaDefinition schemaDefinition = new Oas20SchemaDefinition("Z");
        schemaDefinition.type = "object";
        Oas20Schema propertySchema = new Oas20Schema();
        propertySchema.$ref = "#/definitions/A";
        schemaDefinition.properties = new HashMap<>();
        schemaDefinition.properties.put("a", propertySchema);
        openApiDoc.definitions.addDefinition("Z", schemaDefinition);

        assertThat(CyclicValidationCheck.hasCyclicReferences(getSchemaDefinitions(openApiDoc))).isTrue();
    }

    @Test
    public void shouldFindNoCyclicReferencesWhenThereAreNone() {
        final Oas20Document openApiDoc = new Oas20Document();
        final Oas20PathItem pathItem = new Oas20PathItem("/api");
        final Oas20Operation operation = new Oas20Operation("post");
        final Oas20Parameter parameter = new Oas20Parameter();
        parameter.$ref = "#/definitions/Request";
        operation.parameters = new ArrayList<>();
        operation.parameters.add(parameter);

        Oas20Response response = new Oas20Response("200");
        Oas20Schema responseSchema = new Oas20Schema();
        responseSchema.$ref = "#/definitions/Response";
        response.schema = responseSchema;
        operation.responses = operation.createResponses();
        operation.responses.addResponse("200", response);

        pathItem.post = operation;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/api", pathItem);

        Oas20SchemaDefinition requestSchemaDefinition = new Oas20SchemaDefinition("Request");
        requestSchemaDefinition.type = "object";
        Oas20Schema requestPropertySchema = new Oas20Schema();
        requestPropertySchema.type = "integer";
        requestPropertySchema.format = "int32";
        requestSchemaDefinition.properties = new HashMap<>();
        requestSchemaDefinition.properties.put("reqval", requestPropertySchema);
        openApiDoc.definitions = openApiDoc.createDefinitions();
        openApiDoc.definitions.addDefinition("Request", requestSchemaDefinition);

        Oas20SchemaDefinition responseSchemaDefinition = new Oas20SchemaDefinition("Response");
        responseSchemaDefinition.type = "object";
        Oas20Schema responsePropertySchema = new Oas20Schema();
        responsePropertySchema.type = "integer";
        responsePropertySchema.format = "int32";
        responseSchemaDefinition.properties = new HashMap<>();
        responseSchemaDefinition.properties.put("resval", responsePropertySchema);
        openApiDoc.definitions.addDefinition("Request", responseSchemaDefinition);

        assertThat(CyclicValidationCheck.hasCyclicReferences(getSchemaDefinitions(openApiDoc))).isFalse();
    }

    @Test
    public void shouldFindThreeStepCyclicReferencesInParameters() {
        final Oas20Document openApiDoc = new Oas20Document();
        final Oas20PathItem pathItem = new Oas20PathItem("/api");
        final Oas20Operation operation = new Oas20Operation("post");
        final Oas20Parameter parameter = new Oas20Parameter();
        parameter.$ref = "#/definitions/A";
        operation.parameters = new ArrayList<>();
        operation.parameters.add(parameter);
        pathItem.post = operation;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/api", pathItem);

        Oas20SchemaDefinition schemaDefinitionA = new Oas20SchemaDefinition("A");
        schemaDefinitionA.type = "object";
        Oas20Schema propertySchemaA = new Oas20Schema();
        propertySchemaA.$ref = "#/definitions/B";
        schemaDefinitionA.properties = new HashMap<>();
        schemaDefinitionA.properties.put("b", propertySchemaA);
        openApiDoc.definitions = openApiDoc.createDefinitions();
        openApiDoc.definitions.addDefinition("A", schemaDefinitionA);

        Oas20SchemaDefinition schemaDefinitionB = new Oas20SchemaDefinition("B");
        schemaDefinitionB.type = "object";
        Oas20Schema propertySchemaB = new Oas20Schema();
        propertySchemaB.$ref = "#/definitions/C";
        schemaDefinitionB.properties = new HashMap<>();
        schemaDefinitionB.properties.put("c", propertySchemaB);
        openApiDoc.definitions.addDefinition("B", schemaDefinitionB);

        Oas20SchemaDefinition schemaDefinitionC = new Oas20SchemaDefinition("C");
        schemaDefinitionC.type = "object";
        Oas20Schema propertySchemaC = new Oas20Schema();
        propertySchemaC.$ref = "#/definitions/A";
        schemaDefinitionC.properties = new HashMap<>();
        schemaDefinitionC.properties.put("a", propertySchemaC);
        openApiDoc.definitions.addDefinition("C", schemaDefinitionC);

        assertThat(CyclicValidationCheck.hasCyclicReferences(getSchemaDefinitions(openApiDoc))).isTrue();
    }

    @Test
    public void shouldFindTwoStepCyclicReferencesInParameters() {
        final Oas20Document openApiDoc = new Oas20Document();
        final Oas20PathItem pathItem = new Oas20PathItem("/api");
        final Oas20Operation operation = new Oas20Operation("post");
        final Oas20Parameter parameter = new Oas20Parameter();
        parameter.$ref = "#/definitions/A";
        operation.parameters = new ArrayList<>();
        operation.parameters.add(parameter);
        pathItem.post = operation;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/api", pathItem);

        Oas20SchemaDefinition schemaDefinitionA = new Oas20SchemaDefinition("A");
        schemaDefinitionA.type = "object";
        Oas20Schema propertySchemaA = new Oas20Schema();
        propertySchemaA.$ref = "#/definitions/B";
        schemaDefinitionA.properties = new HashMap<>();
        schemaDefinitionA.properties.put("b", propertySchemaA);
        openApiDoc.definitions = openApiDoc.createDefinitions();
        openApiDoc.definitions.addDefinition("A", schemaDefinitionA);

        Oas20SchemaDefinition schemaDefinitionB = new Oas20SchemaDefinition("B");
        schemaDefinitionB.type = "object";
        Oas20Schema propertySchemaB = new Oas20Schema();
        propertySchemaB.$ref = "#/definitions/A";
        schemaDefinitionB.properties = new HashMap<>();
        schemaDefinitionB.properties.put("a", propertySchemaB);
        openApiDoc.definitions.addDefinition("B", schemaDefinitionB);

        assertThat(CyclicValidationCheck.hasCyclicReferences(getSchemaDefinitions(openApiDoc))).isTrue();
    }

    @Test
    public void shouldFindTwoStepCyclicReferencesInResponses() {
        final Oas20Document openApiDoc = new Oas20Document();
        final Oas20PathItem pathItem = new Oas20PathItem("/api");
        final Oas20Operation operation = new Oas20Operation("post");

        Oas20Response response = new Oas20Response("200");
        Oas20Schema responseSchema = new Oas20Schema();
        responseSchema.$ref = "#/definitions/A";
        response.schema = responseSchema;
        operation.responses = operation.createResponses();
        operation.responses.addResponse("200", response);

        pathItem.post = operation;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/api", pathItem);

        Oas20SchemaDefinition schemaDefinitionA = new Oas20SchemaDefinition("A");
        schemaDefinitionA.type = "object";
        Oas20Schema propertySchemaA = new Oas20Schema();
        propertySchemaA.$ref = "#/definitions/B";
        schemaDefinitionA.properties = new HashMap<>();
        schemaDefinitionA.properties.put("b", propertySchemaA);
        openApiDoc.definitions = openApiDoc.createDefinitions();
        openApiDoc.definitions.addDefinition("A", schemaDefinitionA);

        Oas20SchemaDefinition schemaDefinitionB = new Oas20SchemaDefinition("B");
        schemaDefinitionB.type = "object";
        Oas20Schema propertySchemaB = new Oas20Schema();
        propertySchemaB.$ref = "#/definitions/A";
        schemaDefinitionB.properties = new HashMap<>();
        schemaDefinitionB.properties.put("a", propertySchemaB);
        openApiDoc.definitions.addDefinition("B", schemaDefinitionB);

        assertThat(CyclicValidationCheck.hasCyclicReferences(getSchemaDefinitions(openApiDoc))).isTrue();
    }

    @Test
    public void shouldTolerateTrivialValue() {
        assertThat(CyclicValidationCheck.hasCyclicReferences(Collections.emptyMap())).isFalse();
    }

    private static Map<String, ? extends OasSchema> getSchemaDefinitions(Oas20Document openApiDoc) {
        return openApiDoc.definitions.getDefinitions().stream().collect(Collectors.toMap(Oas20SchemaDefinition::getName, def -> def));
    }
}
