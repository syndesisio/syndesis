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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.apicurio.datamodels.openapi.v3.models.Oas30Components;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30MediaType;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Parameter;
import io.apicurio.datamodels.openapi.v3.models.Oas30PathItem;
import io.apicurio.datamodels.openapi.v3.models.Oas30Response;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import io.apicurio.datamodels.openapi.v3.models.Oas30SchemaDefinition;
import io.apicurio.datamodels.openapi.v3.models.Oas30SecurityScheme;
import io.syndesis.common.model.Violation;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import io.syndesis.server.api.generator.openapi.OpenApiSecurityScheme;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Oas30ValidationRulesTest {

    private static final Oas30ValidationRules RULES = new Oas30ValidationRules(APIValidationContext.NONE);

    @Test
    public void cyclicSchemaReferencesValidationShouldOperateOnParsedModel() {
        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().build();

        final OpenApiModelInfo validated = RULES.validateCyclicReferences(info);
        assertThat(validated).isSameAs(info);
    }

    @Test
    public void shouldNotGenerateErrorWhenOperationsArePresent() {
        final Oas30Document openApiDoc = new Oas30Document();
        final Oas30PathItem pathItem = new Oas30PathItem("/test");
        pathItem.get = new Oas30Operation("get");
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/test", pathItem);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();

        final OpenApiModelInfo validated = RULES.validateOperationsGiven(info);
        assertThat(validated.getErrors()).isEmpty();
        assertThat(validated.getWarnings()).isEmpty();
    }

    @Test
    public void shouldNotGenerateErrorForSupportedAuthType() {
        final Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.components = openApiDoc.createComponents();
        Oas30SecurityScheme basicAuth = openApiDoc.components.createSecurityScheme("basic_auth");
        basicAuth.type = "http";
        openApiDoc.components.addSecurityScheme("basic_auth", basicAuth);

        Oas30SecurityScheme apiKey = openApiDoc.components.createSecurityScheme("api_key");
        apiKey.type = OpenApiSecurityScheme.API_KEY.getName();
        openApiDoc.components.addSecurityScheme("api_key", apiKey);

        Oas30SecurityScheme oauth2 = openApiDoc.components.createSecurityScheme("oauth2");
        oauth2.type = OpenApiSecurityScheme.OAUTH2.getName();
        openApiDoc.components.addSecurityScheme("oauth2", oauth2);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();

        final OpenApiModelInfo validated = RULES.validateConsumedAuthTypes(info);
        assertThat(validated.getErrors()).isEmpty();
        assertThat(validated.getWarnings()).isEmpty();
    }

    @Test
    public void shouldGenerateWarningForUnsupportedAuthType() {
        final Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.components = openApiDoc.createComponents();
        Oas30SecurityScheme secretAuth = openApiDoc.components.createSecurityScheme("secret_auth");
        secretAuth.type = "secret";
        openApiDoc.components.addSecurityScheme("secret_auth", secretAuth);

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
        final Oas30Document openApiDoc = new Oas30Document();
        final Oas30PathItem pathItem = new Oas30PathItem("/api");
        final Oas30Operation operation = new Oas30Operation("get");
        final Oas30Parameter parameter = new Oas30Parameter();
        parameter.$ref = "#/components/schemas/Request";
        operation.parameters = new ArrayList<>();
        operation.parameters.add(parameter);

        Oas30Response response = new Oas30Response("200");
        Oas30Schema responseSchema = new Oas30Schema();
        responseSchema.$ref = "#/components/schemas/Response";
        Oas30MediaType mediaType = response.createMediaType("application/json");
        mediaType.schema = responseSchema;
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
        final Oas30Document openApiDoc = new Oas30Document();
        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();

        final OpenApiModelInfo validated = RULES.validateCyclicReferences(info);
        assertThat(validated).isEqualTo(info);
    }

    @Test
    public void shouldReportIssuesForCyclicSchemaReferences() {
        final Oas30Document openApiDoc = new Oas30Document();
        final Oas30PathItem pathItem = new Oas30PathItem("/api");
        final Oas30Operation operation = new Oas30Operation("post");
        final Oas30Parameter parameter = new Oas30Parameter();
        parameter.$ref = "#/components/schemas/Request";
        operation.parameters = new ArrayList<>();
        operation.parameters.add(parameter);

        pathItem.post = operation;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/api", pathItem);

        Oas30Components components = openApiDoc.createComponents();
        openApiDoc.components = components;
        Oas30SchemaDefinition schemaDefinition = components.createSchemaDefinition("Request");
        schemaDefinition.$ref = "#/components/schemas/Request";
        components.addSchemaDefinition("Request", schemaDefinition);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();

        final OpenApiModelInfo validated = RULES.validateCyclicReferences(info);
        assertThat(validated.getErrors()).containsOnly(new Violation.Builder().error("cyclic-schema").message("Cyclic references are not supported").build());
    }

    @Test
    public void shouldValidateOperationsArePresent() {
        final Oas30Document openApiDoc = new Oas30Document();
        final Oas30PathItem pathItem = new Oas30PathItem("/test");
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
        final Oas30Document openApiDoc = new Oas30Document();
        final Oas30PathItem pathItem = new Oas30PathItem("/path");
        Oas30Operation get = new Oas30Operation("get");
        get.operationId = "o1";
        pathItem.get = get;
        Oas30Operation post = new Oas30Operation("post");
        post.operationId = "o2";
        pathItem.post = post;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/path", pathItem);

        final Oas30PathItem otherPathItem = new Oas30PathItem("/other");
        Oas30Operation patch = new Oas30Operation("patch");
        patch.operationId = "o2";
        otherPathItem.patch = patch;
        Oas30Operation put = new Oas30Operation("put");
        put.operationId = "o3";
        otherPathItem.put = put;
        openApiDoc.paths.addPathItem("/other", otherPathItem);

        final Oas30PathItem morePathItem = new Oas30PathItem("/more");
        Oas30Operation options = new Oas30Operation("options");
        options.operationId = "o4";
        morePathItem.options = options;
        Oas30Operation delete = new Oas30Operation("delete");
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
        final Oas30Document openApiDoc = new Oas30Document();
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
        final Oas30Document openApiDoc = new Oas30Document();
        final Oas30PathItem pathItem = new Oas30PathItem("/path");
        Oas30Operation get = new Oas30Operation("get");
        get.operationId = "o1";
        pathItem.get = get;
        Oas30Operation post = new Oas30Operation("post");
        post.operationId = "o2";
        final Oas30Parameter headerParameter = new Oas30Parameter();
        headerParameter.in = "header";
        post.parameters = Collections.singletonList(headerParameter);
        post.requestBody = post.createRequestBody();
        Oas30MediaType requestBody = post.requestBody.createMediaType("application/json");
        post.requestBody.content = Collections.singletonMap("application/json", requestBody);

        pathItem.post = post;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/path", pathItem);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();
        final OpenApiModelInfo validated = RULES.validateRequestResponseBodySchemas(info);

        final List<Violation> warnings = validated.getWarnings();
        assertThat(warnings).hasSize(1);
        final Violation reportedWarning = warnings.get(0);
        assertThat(reportedWarning.error()).isEqualTo("missing-request-schema");
        assertThat(reportedWarning.property()).isEmpty();
        assertThat(reportedWarning.message()).isEqualTo("Operation POST /path does not provide a schema for the request " +
            "body on media type application/json");
    }

    @Test
    public void shouldValidateMissingResponseBodySchema() {
        final Oas30Document openApiDoc = new Oas30Document();
        final Oas30PathItem pathItem = new Oas30PathItem("/path");
        Oas30Operation get = new Oas30Operation("get");
        get.operationId = "o1";
        get.responses = get.createResponses();
        get.responses.addResponse("404", get.responses.createResponse("404"));
        get.responses.addResponse("200", get.responses.createResponse("200"));
        pathItem.get = get;
        Oas30Operation post = new Oas30Operation("post");
        post.operationId = "o2";
        final Oas30Parameter headerParameter = new Oas30Parameter();
        headerParameter.in = "header";
        post.parameters = Collections.singletonList(headerParameter);
        post.requestBody = post.createRequestBody();
        Oas30Schema bodySchema = new Oas30Schema();
        bodySchema.addProperty("foo", new Oas30Schema());
        Oas30MediaType requestBody = post.requestBody.createMediaType("application/json");
        requestBody.schema = bodySchema;
        post.requestBody.content = Collections.singletonMap("application/json", requestBody);

        pathItem.post = post;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/path", pathItem);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();
        final OpenApiModelInfo validated = RULES.validateRequestResponseBodySchemas(info);

        final List<Violation> warnings = validated.getWarnings();
        assertThat(warnings).hasSize(1);
        final Violation reportedWarning = warnings.get(0);
        assertThat(reportedWarning.error()).isEqualTo("missing-response-schema");
        assertThat(reportedWarning.property()).isEmpty();
        assertThat(reportedWarning.message()).isEqualTo("Operation GET /path does not provide a response schema for code 200");
    }

    @Test
    public void shouldNotReportIssuesForValidResponseBodySchema() {
        final Oas30Document openApiDoc = new Oas30Document();
        final Oas30PathItem pathItem = new Oas30PathItem("/path");
        Oas30Operation get = new Oas30Operation("get");
        get.operationId = "o1";
        get.responses = get.createResponses();
        get.responses.addResponse("404", get.responses.createResponse("404"));
        Oas30Response response = (Oas30Response) get.responses.createResponse("200");
        Oas30Schema responseSchema = new Oas30Schema();
        responseSchema.addProperty("foo", new Oas30Schema());
        Oas30MediaType responseBody = response.createMediaType("application/json");
        responseBody.schema = responseSchema;
        response.content = Collections.singletonMap("application/json", responseBody);
        get.responses.addResponse("200", response);
        pathItem.get = get;
        Oas30Operation post = new Oas30Operation("post");
        post.operationId = "o2";
        final Oas30Parameter headerParameter = new Oas30Parameter();
        headerParameter.in = "header";
        post.parameters = Collections.singletonList(headerParameter);
        post.requestBody = post.createRequestBody();
        Oas30Schema bodySchema = new Oas30Schema();
        bodySchema.addProperty("foo", new Oas30Schema());
        Oas30MediaType requestBody = post.requestBody.createMediaType("application/json");
        requestBody.schema = bodySchema;
        post.requestBody.content = Collections.singletonMap("application/json", requestBody);

        pathItem.post = post;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/path", pathItem);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();
        final OpenApiModelInfo validated = RULES.validateRequestResponseBodySchemas(info);

        assertThat(validated.getErrors()).isEmpty();
        assertThat(validated.getWarnings()).isEmpty();
    }

    @Test
    public void shouldNotGenerateWarningForSameServerBasePath() {
        final Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.addServer("https://development.syndesis.io/v1", "Development server");
        openApiDoc.addServer("https://staging.syndesis.io/v1", "Staging server");
        openApiDoc.addServer("https://api.syndesis.io/v1", "Production server");

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();

        final OpenApiModelInfo validated = Oas30ValidationRules.validateServerBasePaths(info);
        assertThat(validated.getErrors()).isEmpty();
        assertThat(validated.getWarnings()).isEmpty();
    }

    @Test
    public void shouldGenerateWarningForDifferingServerBasePaths() {
        final Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.addServer("https://syndesis.io/development", "Development server");
        openApiDoc.addServer("https://syndesis.io/staging", "Staging server");
        openApiDoc.addServer("https://syndesis.io/api", "Production server");

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();

        final OpenApiModelInfo validated = Oas30ValidationRules.validateServerBasePaths(info);
        assertThat(validated.getErrors()).isEmpty();
        assertThat(validated.getWarnings()).containsOnly(new Violation.Builder().error("differing-base-paths")
            .message("Specified servers do not share the same base path. REST endpoint will use '/development' as base path.").build());
    }

    @Test
    public void shouldValidateUnsupportedLinksFeature() {
        final Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.components = openApiDoc.createComponents();
        openApiDoc.components.links = Collections.singletonMap("foo", openApiDoc.components.createLinkDefinition("foo"));

        final Oas30PathItem pathItem = new Oas30PathItem("/path");
        Oas30Operation get = new Oas30Operation("get");
        get.operationId = "o1";
        get.responses = get.createResponses();
        Oas30Response response = (Oas30Response) get.responses.createResponse("200");
        response.links = Collections.singletonMap("foo", openApiDoc.components.createLinkDefinition("foo"));
        get.responses.addResponse("200", response);

        pathItem.get = get;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/path", pathItem);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();

        final OpenApiModelInfo validated = Oas30ValidationRules.validateUnsupportedLinksFeature(info);
        assertThat(validated.getErrors()).isEmpty();
        final List<Violation> warnings = validated.getWarnings();
        assertThat(warnings).hasSize(2);
        Violation reportedWarning = warnings.get(0);
        assertThat(reportedWarning.error()).isEqualTo("unsupported-links-feature");
        assertThat(reportedWarning.property()).isNull();
        assertThat(reportedWarning.message()).isEqualTo("Links component is not supported yet. " +
            "This part of the OpenAPI specification will be ignored.");

        reportedWarning = warnings.get(1);
        assertThat(reportedWarning.error()).isEqualTo("unsupported-links-feature");
        assertThat(reportedWarning.property()).isEmpty();
        assertThat(reportedWarning.message()).isEqualTo("Operation GET /path uses unsupported links feature. " +
            "All links will be ignored.");
    }

    @Test
    public void shouldValidateUnsupportedCallbacksFeature() {
        final Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.components = openApiDoc.createComponents();
        openApiDoc.components.callbacks = Collections.singletonMap("foo", openApiDoc.components.createCallbackDefinition("foo"));

        final Oas30PathItem pathItem = new Oas30PathItem("/path");
        Oas30Operation post = new Oas30Operation("post");
        post.operationId = "o2";
        post.callbacks = Collections.singletonMap("foo", post.createCallback("foo"));

        pathItem.post = post;
        openApiDoc.paths = openApiDoc.createPaths();
        openApiDoc.paths.addPathItem("/path", pathItem);

        final OpenApiModelInfo info = new OpenApiModelInfo.Builder().model(openApiDoc).build();

        final OpenApiModelInfo validated = Oas30ValidationRules.validateUnsupportedCallbacksFeature(info);
        assertThat(validated.getErrors()).isEmpty();
        final List<Violation> warnings = validated.getWarnings();
        assertThat(warnings).hasSize(2);
        Violation reportedWarning = warnings.get(0);
        assertThat(reportedWarning.error()).isEqualTo("unsupported-callbacks-feature");
        assertThat(reportedWarning.property()).isNull();
        assertThat(reportedWarning.message()).isEqualTo("Callbacks component is not supported yet. " +
            "This part of the OpenAPI specification will be ignored.");

        reportedWarning = warnings.get(1);
        assertThat(reportedWarning.error()).isEqualTo("unsupported-callbacks-feature");
        assertThat(reportedWarning.property()).isEmpty();
        assertThat(reportedWarning.message()).isEqualTo("Operation POST /path uses unsupported callbacks feature. " +
            "All callbacks will be ignored.");
    }
}
