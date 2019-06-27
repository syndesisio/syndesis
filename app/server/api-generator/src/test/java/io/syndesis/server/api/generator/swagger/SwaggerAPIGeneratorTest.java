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
import java.util.List;
import java.util.function.Predicate;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.server.api.generator.APIIntegration;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.ProvidedApiTemplate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SwaggerAPIGeneratorTest {

    @Test
    public void infoShouldHandleNullModels() {
        final SwaggerAPIGenerator generator = new SwaggerAPIGenerator();

        final APISummary summary = generator.info("invalid", APIValidationContext.NONE);

        assertThat(summary).isNotNull();
        assertThat(summary.getErrors()).hasSize(1)
            .allSatisfy(v -> assertThat(v.message()).startsWith("This document cannot be uploaded. Provide an OpenAPI 2.0 document"));
        assertThat(summary.getWarnings()).isEmpty();
    }

    @Test
    public void infoShouldHandleNullPaths() {
        final SwaggerAPIGenerator generator = new SwaggerAPIGenerator();

        final APISummary summary = generator.info("{\"swagger\": \"2.0\"}", APIValidationContext.NONE);

        assertThat(summary).isNotNull();
        assertThat(summary.getErrors()).isEmpty();
        assertThat(summary.getWarnings()).isEmpty();
    }

    @Test
    public void infoShouldHandleNullSpecifications() {
        final SwaggerAPIGenerator generator = new SwaggerAPIGenerator();

        final APISummary summary = generator.info(null, APIValidationContext.NONE);

        assertThat(summary).isNotNull();
        assertThat(summary.getErrors()).hasSize(1).allSatisfy(v -> assertThat(v.message()).startsWith("Unable to resolve OpenAPI document from"));
        assertThat(summary.getWarnings()).isEmpty();
    }

    @Test
    public void testEmptyOperationSummary() throws IOException {
        final ProvidedApiTemplate template = new ProvidedApiTemplate(dummyConnection(), "fromAction", "toAction");
        final String specification = TestHelper.resource("/swagger/empty-summary.json");
        final SwaggerAPIGenerator generator = new SwaggerAPIGenerator();

        final APIIntegration apiIntegration = generator.generateIntegration(specification, template);
        assertThat(apiIntegration).isNotNull();
        assertThat(apiIntegration.getIntegration().getFlows()).hasSize(3);

        final List<Flow> flows = apiIntegration.getIntegration().getFlows();

        assertThat(flows).filteredOn(operationIdEquals("operation-1")).first().hasFieldOrPropertyWithValue("name", "Receiving GET request on /hi");
        assertThat(flows).filteredOn(operationIdEquals("operation-2")).first().hasFieldOrPropertyWithValue("name", "post operation");
        assertThat(flows).filteredOn(operationIdEquals("operation-3")).first().hasFieldOrPropertyWithValue("name", "Receiving PUT request on /hi");
    }

    private static Connection dummyConnection() {
        final Connector connector = new Connector.Builder()
            .addAction(
                new ConnectorAction.Builder().id("fromAction")
                    .descriptor(new ConnectorDescriptor.Builder().build())
                    .build())
            .addAction(
                new ConnectorAction.Builder().id("toAction")
                    .descriptor(new ConnectorDescriptor.Builder().build())
                    .build())
            .build();

        return new Connection.Builder().connector(connector).build();
    }

    private static Predicate<Flow> operationIdEquals(final String operationId) {
        return f -> f.getMetadata(OpenApi.OPERATION_ID).map(id -> id.equals(operationId)).orElse(false);
    }
}
