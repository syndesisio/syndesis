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
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.server.api.generator.APIIntegration;
import io.syndesis.server.api.generator.ProvidedApiTemplate;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SwaggerAPIGeneratorTest {

    @Test
    public void testEmptyOperationSummary() throws IOException {
        ProvidedApiTemplate template = new ProvidedApiTemplate(dummyConnection(), "fromAction", "toAction");
        String specification = TestHelper.resource("/swagger/empty-summary.json");
        SwaggerAPIGenerator generator = new SwaggerAPIGenerator();

        APIIntegration apiIntegration = generator.generateIntegration(specification, template);
        assertThat(apiIntegration).isNotNull();
        assertThat(apiIntegration.getIntegration().getFlows()).hasSize(3);

        List<Flow> flows = apiIntegration.getIntegration().getFlows();

        assertThat(flows).filteredOn(idEndsWith("-1")).first().hasFieldOrPropertyWithValue("name", "Receiving GET request on /hi");
        assertThat(flows).filteredOn(idEndsWith("-2")).first().hasFieldOrPropertyWithValue("name", "post operation");
        assertThat(flows).filteredOn(idEndsWith("-3")).first().hasFieldOrPropertyWithValue("name", "Receiving PUT request on /hi");
    }

    private static Predicate<Flow> idEndsWith(String end) {
        return f -> f.getId().map(id -> id.endsWith(end)).orElse(false);
    }

    private static Connection dummyConnection() {
        Connector connector = new Connector.Builder()
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
 }
