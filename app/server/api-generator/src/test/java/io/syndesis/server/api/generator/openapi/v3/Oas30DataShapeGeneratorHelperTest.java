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

import java.util.List;

import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30MediaType;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Parameter;
import io.apicurio.datamodels.openapi.v3.models.Oas30ParameterDefinition;
import io.apicurio.datamodels.openapi.v3.models.Oas30PathItem;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class Oas30DataShapeGeneratorHelperTest {

    @Test
    public void shouldGetEmptyOperationParameters() {
        Oas30Document openApiDoc = new Oas30Document();

        openApiDoc.paths = openApiDoc.createPaths();
        Oas30PathItem pathItem = (Oas30PathItem) openApiDoc.paths.createPathItem("/test/{id}");
        openApiDoc.paths.addPathItem("test", pathItem);

        Oas30Operation operation = (Oas30Operation) pathItem.createOperation("get");
        pathItem.get = operation;

        List<Oas30Parameter> operationParameters = Oas30DataShapeGeneratorHelper.getOperationParameters(openApiDoc, operation);
        Assertions.assertThat(operationParameters).isEmpty();
    }

    @Test
    public void shouldGetOperationParameters() {
        Oas30Document openApiDoc = new Oas30Document();

        openApiDoc.paths = openApiDoc.createPaths();
        Oas30PathItem pathItem = (Oas30PathItem) openApiDoc.paths.createPathItem("/test/{id}");
        openApiDoc.paths.addPathItem("test", pathItem);

        Oas30Operation operation = (Oas30Operation) pathItem.createOperation("get");
        pathItem.get = operation;

        Oas30Parameter idParameter = (Oas30Parameter) operation.createParameter();
        idParameter.in = "path";
        idParameter.name = "id";
        operation.addParameter(idParameter);

        List<Oas30Parameter> operationParameters = Oas30DataShapeGeneratorHelper.getOperationParameters(openApiDoc, operation);
        Assertions.assertThat(operationParameters).hasSize(1);
        Assertions.assertThat(operationParameters).contains(idParameter);
    }

    @Test
    public void shouldGetOperationParametersAndReferences() {
        Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.components = openApiDoc.createComponents();
        Oas30ParameterDefinition globalParameter = openApiDoc.components.createParameterDefinition("version");
        openApiDoc.components.addParameterDefinition("Version", globalParameter);
        openApiDoc.components.addParameterDefinition("Unused", openApiDoc.components.createParameterDefinition("unused"));

        openApiDoc.paths = openApiDoc.createPaths();
        Oas30PathItem pathItem = (Oas30PathItem) openApiDoc.paths.createPathItem("/test/{id}");
        openApiDoc.paths.addPathItem("test", pathItem);

        Oas30Operation operation = (Oas30Operation) pathItem.createOperation("get");
        pathItem.get = operation;

        Oas30Parameter versionParameter = (Oas30Parameter) operation.createParameter();
        versionParameter.$ref = "#components/parameters/Version";
        versionParameter.in = "header";
        operation.addParameter(versionParameter);

        Oas30Parameter idParameter = (Oas30Parameter) operation.createParameter();
        idParameter.in = "path";
        idParameter.name = "id";
        operation.addParameter(idParameter);

        List<Oas30Parameter> operationParameters = Oas30DataShapeGeneratorHelper.getOperationParameters(openApiDoc, operation);
        Assertions.assertThat(operationParameters).hasSize(2);
        Assertions.assertThat(operationParameters).contains(globalParameter, idParameter);
    }

    @Test
    public void shouldGetOperationParametersJoinedWithPathItemParametersAndReferences() {
        Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.components = openApiDoc.createComponents();
        Oas30ParameterDefinition globalParameter = openApiDoc.components.createParameterDefinition("version");
        openApiDoc.components.addParameterDefinition("Version", globalParameter);
        openApiDoc.components.addParameterDefinition("Unused", openApiDoc.components.createParameterDefinition("unused"));

        openApiDoc.paths = openApiDoc.createPaths();
        Oas30PathItem pathItem = (Oas30PathItem) openApiDoc.paths.createPathItem("/test/{id}");
        openApiDoc.paths.addPathItem("test", pathItem);

        Oas30Parameter pathParameter = (Oas30Parameter) pathItem.createParameter();
        pathParameter.$ref = "#components/parameters/Version";
        pathItem.addParameter(pathParameter);

        Oas30Operation operation = (Oas30Operation) pathItem.createOperation("get");
        pathItem.get = operation;

        Oas30Parameter operationParameter = (Oas30Parameter) operation.createParameter();
        operationParameter.in = "path";
        operationParameter.name = "id";
        operation.addParameter(operationParameter);

        List<Oas30Parameter> operationParameters = Oas30DataShapeGeneratorHelper.getOperationParameters(openApiDoc, operation);
        Assertions.assertThat(operationParameters).hasSize(2);
        Assertions.assertThat(operationParameters).contains(globalParameter, operationParameter);
    }

    @Test
    public void shouldNotHaveDuplicateOperationParameters() {
        Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.components = openApiDoc.createComponents();
        Oas30ParameterDefinition globalParameter = openApiDoc.components.createParameterDefinition("version");
        openApiDoc.components.addParameterDefinition("Version", globalParameter);

        openApiDoc.paths = openApiDoc.createPaths();
        Oas30PathItem pathItem = (Oas30PathItem) openApiDoc.paths.createPathItem("/test/{id}");
        openApiDoc.paths.addPathItem("test", pathItem);

        Oas30Parameter pathParameter = (Oas30Parameter) pathItem.createParameter();
        pathParameter.$ref = "#components/parameters/Version";
        pathItem.addParameter(pathParameter);

        Oas30Operation operation = (Oas30Operation) pathItem.createOperation("get");
        pathItem.get = operation;

        Oas30Parameter operationParameter = (Oas30Parameter) operation.createParameter();
        operationParameter.$ref = "#components/parameters/Version";
        operation.addParameter(operationParameter);

        List<Oas30Parameter> operationParameters = Oas30DataShapeGeneratorHelper.getOperationParameters(openApiDoc, operation);
        Assertions.assertThat(operationParameters).hasSize(1);
        Assertions.assertThat(operationParameters).contains(globalParameter);
    }

    @Test
    public void shouldGetOperationParameterFromFormData() {
        Oas30Document openApiDoc = new Oas30Document();

        openApiDoc.paths = openApiDoc.createPaths();
        Oas30PathItem pathItem = (Oas30PathItem) openApiDoc.paths.createPathItem("/test/{id}");
        openApiDoc.paths.addPathItem("test", pathItem);

        Oas30Operation operation = (Oas30Operation) pathItem.createOperation("get");
        operation.requestBody = operation.createRequestBody();
        Oas30MediaType formData = operation.requestBody.createMediaType("formData");
        formData.schema = formData.createSchema();
        formData.schema.addProperty("name", formData.createSchema());
        formData.schema.addProperty("age", formData.createSchema());
        operation.requestBody.content.put(Oas30FormDataHelper.MediaType.FORM_URLENCODED.mediaType(), formData);
        pathItem.get = operation;

        List<Oas30Parameter> operationParameters = Oas30DataShapeGeneratorHelper.getOperationParameters(openApiDoc, operation);
        Assertions.assertThat(operationParameters).hasSize(2);
        Assertions.assertThat(operationParameters).allMatch(p -> "formData".equals(p.in));
        Assertions.assertThat(operationParameters).anyMatch(p -> "name".equals(p.name));
        Assertions.assertThat(operationParameters).anyMatch(p -> "age".equals(p.name));
    }
}
