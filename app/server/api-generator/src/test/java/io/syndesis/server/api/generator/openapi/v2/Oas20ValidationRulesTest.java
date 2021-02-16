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
package io.syndesis.server.api.generator.openapi.v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20PathItem;
import io.apicurio.datamodels.openapi.v2.models.Oas20Response;
import io.apicurio.datamodels.openapi.v2.models.Oas20Schema;
import io.apicurio.datamodels.openapi.v2.models.Oas20SchemaDefinition;
import io.apicurio.datamodels.openapi.v2.models.Oas20SecurityScheme;
import io.syndesis.common.model.Violation;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import io.syndesis.server.api.generator.openapi.OpenApiSecurityScheme;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Oas20ValidationRulesTest {

    private static final Oas20ValidationRules RULES = new Oas20ValidationRules(APIValidationContext.NONE);

    @Test
    public void cyclicSchemaReferencesValidationShouldOperateOnParsedModel() {
        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().build();

        final OpenApiModelInfo validated = RULES.validateCyclicReferences(info);
        assertThat(validated).isSameAs(info);
    }

    @Test
    public void shouldNotGenerateErrorWhenOperationsArePresent() {
        final Oas20Document openApiDoc = new Oas20Document();
        final Oas20PathItem pathItem = new Oas20PathItem("/test");
        pathItem.get = new Oas20Operation("get");
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/test", pathItem);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();

        final OpenApiModelInfo validated = RULES.validateOperationsGiven(info);
        assertThat(validated.getErrors()).isEmpty();
        assertThat(validated.getWarnings()).isEmpty();
    }

    @Test
    public void shouldNotGenerateErrorForSupportedAuthType() {
        final Oas20Document openApiDoc = new Oas20Document();
        openApiDoc.securityDefinitions = openApiDoc.createSecurityDefinitions();
        Oas20SecurityScheme basicAuth = openApiDoc.securityDefinitions.createSecurityScheme("basic_auth");
        basicAuth.type = OpenApiSecurityScheme.BASIC.getName();
        openApiDoc.securityDefinitions.addSecurityScheme("basic_auth", basicAuth);

        Oas20SecurityScheme apiKey = openApiDoc.securityDefinitions.createSecurityScheme("api_key");
        apiKey.type = OpenApiSecurityScheme.API_KEY.getName();
        openApiDoc.securityDefinitions.addSecurityScheme("api_key", apiKey);

        Oas20SecurityScheme oauth2 = openApiDoc.securityDefinitions.createSecurityScheme("oauth2");
        oauth2.type = OpenApiSecurityScheme.OAUTH2.getName();
        openApiDoc.securityDefinitions.addSecurityScheme("oauth2", oauth2);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();

        final OpenApiModelInfo validated = RULES.validateConsumedAuthTypes(info);
        assertThat(validated.getErrors()).isEmpty();
        assertThat(validated.getWarnings()).isEmpty();
    }

    @Test
    public void shouldGenerateWarningForUnsupportedAuthType() {
        final Oas20Document openApiDoc = new Oas20Document();
        openApiDoc.securityDefinitions = openApiDoc.createSecurityDefinitions();
        Oas20SecurityScheme secretAuth = openApiDoc.securityDefinitions.createSecurityScheme("secret_auth");
        secretAuth.type = "secret";
        openApiDoc.securityDefinitions.addSecurityScheme("secret_auth", secretAuth);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();

        OpenApiModelInfo validated = RULES.validateProvidedAuthTypes(info);
        assertThat(validated.getErrors()).isEmpty();
        assertThat(validated.getWarnings()).containsOnly(new Violation.Builder().error("unsupported-auth").message("Authentication type secret is currently not supported").property("").build());

        validated = RULES.validateConsumedAuthTypes(info);
        assertThat(validated.getErrors()).isEmpty();
        assertThat(validated.getWarnings()).containsOnly(new Violation.Builder().error("unsupported-auth").message("Authentication type secret is currently not supported").property("").build());
    }

    @Test
    public void shouldNotReportIssuesForNonCyclicSchemaReferences() {
        final Oas20Document openApiDoc = new Oas20Document();
        final Oas20PathItem pathItem = new Oas20PathItem("/api");
        final Oas20Operation operation = new Oas20Operation("get");
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

        pathItem.get = operation;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/api", pathItem);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();

        final OpenApiModelInfo validated = RULES.validateCyclicReferences(info);
        assertThat(validated).isEqualTo(info);
    }

    @Test
    public void shouldNotReportIssuesForTrivialOpenAPISpec() {
        final Oas20Document openApiDoc = new Oas20Document();
        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();

        final OpenApiModelInfo validated = RULES.validateCyclicReferences(info);
        assertThat(validated).isEqualTo(info);
    }

    @Test
    public void shouldReportIssuesForCyclicSchemaReferences() {
        final Oas20Document openApiDoc = new Oas20Document();
        final Oas20PathItem pathItem = new Oas20PathItem("/api");
        final Oas20Operation operation = new Oas20Operation("post");
        final Oas20Parameter parameter = new Oas20Parameter();
        parameter.$ref = "#/definitions/Request";
        operation.parameters = new ArrayList<>();
        operation.parameters.add(parameter);

        pathItem.post = operation;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/api", pathItem);

        Oas20SchemaDefinition schemaDefinition = new Oas20SchemaDefinition("Request");
        schemaDefinition.$ref = "#/definitions/Request";
        openApiDoc.definitions = openApiDoc.createDefinitions();
        openApiDoc.definitions.addDefinition("Request", schemaDefinition);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();

        final OpenApiModelInfo validated = RULES.validateCyclicReferences(info);
        assertThat(validated.getErrors()).containsOnly(new Violation.Builder().error("cyclic-schema").message("Cyclic references are not supported").build());
    }

    @Test
    public void shouldValidateOperationsArePresent() {
        final Oas20Document openApiDoc = new Oas20Document();
        final Oas20PathItem pathItem = new Oas20PathItem("/test");
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/test", pathItem);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();

        final OpenApiModelInfo validated = RULES.validateOperationsGiven(info);
        final List<Violation> errors = validated.getErrors();
        assertThat(errors).containsExactly(new Violation.Builder()
            .property("")
            .error("missing-operations")
            .message("No operations defined")
            .build());
    }

    @Test
    public void shouldValidateOperationUniqueness() {
        final Oas20Document openApiDoc = new Oas20Document();
        final Oas20PathItem pathItem = new Oas20PathItem("/path");
        Oas20Operation get = new Oas20Operation("get");
        get.operationId = "o1";
        pathItem.get = get;
        Oas20Operation post = new Oas20Operation("post");
        post.operationId = "o2";
        pathItem.post = post;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/path", pathItem);

        final Oas20PathItem otherPathItem = new Oas20PathItem("/other");
        Oas20Operation patch = new Oas20Operation("patch");
        patch.operationId = "o2";
        otherPathItem.patch = patch;
        Oas20Operation put = new Oas20Operation("put");
        put.operationId = "o3";
        otherPathItem.put = put;
        openApiDoc.paths.addPathItem("/other", otherPathItem);

        final Oas20PathItem morePathItem = new Oas20PathItem("/more");
        Oas20Operation options = new Oas20Operation("options");
        options.operationId = "o4";
        morePathItem.options = options;
        Oas20Operation delete = new Oas20Operation("delete");
        delete.operationId = "o3";
        morePathItem.delete = delete;
        openApiDoc.paths.addPathItem("/more", morePathItem);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();
        final OpenApiModelInfo validated = RULES.validateUniqueOperationIds(info);

        final List<Violation> warnings = validated.getWarnings();
        assertThat(warnings).hasSize(1);
        final Violation nonUniqueWarning = warnings.get(0);
        assertThat(nonUniqueWarning.error()).isEqualTo("non-unique-operation-ids");
        assertThat(nonUniqueWarning.property()).isNull();
        assertThat(nonUniqueWarning.message()).isEqualTo("Found operations with non unique operationIds: o2, o3");
    }

    @Test
    public void shouldValidatePathsArePresent() {
        final Oas20Document openApiDoc = new Oas20Document();
        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();

        final OpenApiModelInfo validated = RULES.validateOperationsGiven(info);
        final List<Violation> errors = validated.getErrors();
        assertThat(errors).containsExactly(new Violation.Builder()
            .property("paths")
            .error("missing-paths")
            .message("No paths defined")
            .build());
    }

    @Test
    public void shouldValidateMissingRequestBodySchema() {
        final Oas20Document openApiDoc = new Oas20Document();
        final Oas20PathItem pathItem = new Oas20PathItem("/path");
        Oas20Operation get = new Oas20Operation("get");
        get.operationId = "o1";
        pathItem.get = get;
        Oas20Operation post = new Oas20Operation("post");
        post.operationId = "o2";
        final Oas20Parameter headerParameter = new Oas20Parameter();
        headerParameter.in = "header";
        final Oas20Parameter bodyParameter = new Oas20Parameter();
        bodyParameter.in = "body";
        post.parameters = Arrays.asList(headerParameter, bodyParameter);

        pathItem.post = post;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/path", pathItem);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();
        final OpenApiModelInfo validated = RULES.validateRequestResponseBodySchemas(info);

        final List<Violation> warnings = validated.getWarnings();
        assertThat(warnings).hasSize(1);
        final Violation nonUniqueWarning = warnings.get(0);
        assertThat(nonUniqueWarning.error()).isEqualTo("missing-request-schema");
        assertThat(nonUniqueWarning.property()).isEmpty();
        assertThat(nonUniqueWarning.message()).isEqualTo("Operation POST /path does not provide a schema for the body parameter");
    }

    @Test
    public void shouldValidateMissingResponseBodySchema() {
        final Oas20Document openApiDoc = new Oas20Document();
        final Oas20PathItem pathItem = new Oas20PathItem("/path");
        Oas20Operation get = new Oas20Operation("get");
        get.operationId = "o1";
        get.responses = get.createResponses();
        get.responses.addResponse("404", get.responses.createResponse("404"));
        get.responses.addResponse("200", get.responses.createResponse("200"));
        pathItem.get = get;
        Oas20Operation post = new Oas20Operation("post");
        post.operationId = "o2";
        final Oas20Parameter headerParameter = new Oas20Parameter();
        headerParameter.in = "header";
        final Oas20Parameter bodyParameter = new Oas20Parameter();
        bodyParameter.in = "body";
        Oas20Schema bodySchema = new Oas20Schema();
        bodySchema.addProperty("foo", new Oas20Schema());
        bodyParameter.schema = bodySchema;
        post.parameters = Arrays.asList(headerParameter, bodyParameter);

        pathItem.post = post;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/path", pathItem);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();
        final OpenApiModelInfo validated = RULES.validateRequestResponseBodySchemas(info);

        final List<Violation> warnings = validated.getWarnings();
        assertThat(warnings).hasSize(1);
        final Violation nonUniqueWarning = warnings.get(0);
        assertThat(nonUniqueWarning.error()).isEqualTo("missing-response-schema");
        assertThat(nonUniqueWarning.property()).isEmpty();
        assertThat(nonUniqueWarning.message()).isEqualTo("Operation GET /path does not provide a response schema for code 200");
    }

    @Test
    public void shouldNotReportIssuesForValidResponseBodySchema() {
        final Oas20Document openApiDoc = new Oas20Document();
        final Oas20PathItem pathItem = new Oas20PathItem("/path");
        Oas20Operation get = new Oas20Operation("get");
        get.operationId = "o1";
        get.responses = get.createResponses();
        get.responses.addResponse("404", get.responses.createResponse("404"));
        Oas20Response response = (Oas20Response) get.responses.createResponse("200");
        Oas20Schema responseSchema = new Oas20Schema();
        responseSchema.addProperty("foo", new Oas20Schema());
        response.schema = responseSchema;
        get.responses.addResponse("200", response);
        pathItem.get = get;
        Oas20Operation post = new Oas20Operation("post");
        post.operationId = "o2";
        final Oas20Parameter headerParameter = new Oas20Parameter();
        headerParameter.in = "header";
        final Oas20Parameter bodyParameter = new Oas20Parameter();
        bodyParameter.in = "body";
        Oas20Schema bodySchema = new Oas20Schema();
        bodySchema.addProperty("foo", new Oas20Schema());
        bodyParameter.schema = bodySchema;
        post.parameters = Arrays.asList(headerParameter, bodyParameter);

        pathItem.post = post;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/path", pathItem);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();
        final OpenApiModelInfo validated = RULES.validateRequestResponseBodySchemas(info);

        assertThat(validated.getErrors()).isEmpty();
        assertThat(validated.getWarnings()).isEmpty();
    }
}
