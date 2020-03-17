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

package io.syndesis.server.runtime.migrations;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.server.jsondb.CloseableJsonDB;
import io.syndesis.server.jsondb.dao.Migrator;
import io.syndesis.server.jsondb.impl.MemorySqlJsonDB;
import io.syndesis.server.runtime.DefaultMigrator;

import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static io.syndesis.server.runtime.migrations.MigrationsHelper.load;

import static org.assertj.core.api.Assertions.assertThat;

public class UpgradeVersion41Test {

    @Test
    public void shouldPerformSchemaUpgrade() throws IOException {
        try (CloseableJsonDB jsondb = MemorySqlJsonDB.create(Collections.emptyList());
            InputStream data = UpgradeVersion41Test.class.getResourceAsStream("/migrations/41/integration.json")) {

            jsondb.push("/integrations", data);

            final Migrator migrator = new DefaultMigrator(new DefaultResourceLoader());
            migrator.migrate(jsondb, 41);

            final List<Integration> integrations = load(jsondb, "/integrations", Integration.class);

            // integrations
            assertThat(integrations).hasSize(1);

            final Integration integration = integrations.get(0);

            assertThat(integration.getFlows()).hasSize(1);

            final Flow flow = integration.getFlows().get(0);

            final List<Step> steps = flow.getSteps();
            assertThat(steps).hasSize(2);

            Step maybeStep = steps.get(1);
            assertThat(maybeStep.getAction()).isPresent().satisfies(action -> assertThat(action.get().getId()).contains("io.syndesis:fhir-read-connector"));

            final Optional<Connection> maybeConnection = maybeStep.getConnection();
            assertThat(maybeConnection).isPresent().satisfies(connection -> assertThat(connection.get().getConnector()).isPresent()
                .satisfies(connector -> assertThat(connector.get().getConfiguredProperties()).containsEntry("encoding", "XML")));
        }
    }

    static void assertHttpAction(final Action action) {
        action.getId().map(actionId -> assertThat(actionId).isIn(
            "io.syndesis.connector:connector-http:https-periodic-invoke-url",
            "io.syndesis.connector:connector-http:http-periodic-invoke-url",
            "io.syndesis.connector:connector-http:https-invoke-url",
            "io.syndesis.connector:connector-http:http-invoke-url"));

        assertThat(action.getDescriptor()).isInstanceOf(ConnectorDescriptor.class);

        final ConnectorDescriptor descriptor = (ConnectorDescriptor) action.getDescriptor();

        descriptor.getConnectorFactory().map(connectoFactory -> assertThat(connectoFactory).isIn(
            "io.syndesis.connector.http.HttpConnectorFactories$Http",
            "io.syndesis.connector.http.HttpConnectorFactories$Https"));
    }

}
