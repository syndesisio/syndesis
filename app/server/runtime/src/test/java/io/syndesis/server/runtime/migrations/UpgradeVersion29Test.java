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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.server.jsondb.JsonDB;
import io.syndesis.server.jsondb.dao.Migrator;
import io.syndesis.server.jsondb.impl.MemorySqlJsonDB;
import io.syndesis.server.runtime.DefaultMigrator;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;

import static io.syndesis.server.runtime.migrations.MigrationsHelper.load;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test db schema migration to version 29.
 */
public class UpgradeVersion29Test {
    private static final int SCHEMA_VERSION = 29;

    @Test
    public void testSchemaUpgrade() throws IOException {
        final JsonDB jsondb = MemorySqlJsonDB.create(Collections.emptyList());
        jsondb.push("/connectors", new ClassPathResource("migrations/29/connector.json").getInputStream());
        jsondb.push("/connections", new ClassPathResource("migrations/29/connection.json").getInputStream());
        jsondb.push("/integrations", new ClassPathResource("migrations/29/integration.json").getInputStream());

        final Migrator migrator = new DefaultMigrator(new DefaultResourceLoader());
        migrator.migrate(jsondb, SCHEMA_VERSION);

        final List<Connector> connectors = load(jsondb, "/connectors", Connector.class);
        final List<Connection> connections = load(jsondb, "/connections", Connection.class);
        final List<Integration> integrations = load(jsondb, "/integrations", Integration.class);

        // connectors
        assertThat(connectors).hasSize(1);
        assertThat(connectors.get(0)).satisfies(UpgradeVersion29Test::validateConnector);

        // connections
        assertThat(connections).hasSize(1);
        assertThat(connections.get(0)).satisfies(UpgradeVersion29Test::validateConnection);

        // integrations
        assertThat(integrations).hasSize(1);
        assertThat(integrations.get(0).getSteps()).isNullOrEmpty();
        assertThat(integrations.get(0).getFlows()).anySatisfy(f -> {
            assertThat(f.getSteps()).filteredOn(s -> {
                return s.getActionAs(ConnectorAction.class).isPresent();
            }).anySatisfy(s -> {
                final ConnectorAction a = s.getActionAs(ConnectorAction.class).get();
                assertThat(a.getDescriptor().getComponentScheme()).get().isEqualTo("rest-swagger");
            });
        });
        assertThat(integrations.get(0).getFlows()).allSatisfy(f -> {
            assertThat(f.getSteps()).filteredOn(s -> {
                return s.getActionAs(ConnectorAction.class).isPresent();
            }).allSatisfy(s -> {
                final ConnectorAction a = s.getActionAs(ConnectorAction.class).get();
                assertThat(a.getDescriptor().getCamelConnectorGAV()).isNull();
                assertThat(a.getDescriptor().getCamelConnectorPrefix()).isNull();

                a.getDescriptor().getComponentScheme().filter(cs -> Objects.equals(cs, "rest-swagger")).ifPresent(cs -> {
                    assertThat(s.getConnection()).get().satisfies(UpgradeVersion29Test::validateConnection);
                });
            });
        });
    }

    private static void validateConnection(final Connection connection) {
        assertThat(connection.getConnector()).get().satisfies(UpgradeVersion29Test::validateConnector);
    }

    private static void validateConnector(final Connector connector) {
        assertThat(connector.getDependencies()).containsOnly(Dependency.maven("io.syndesis.connector:connector-rest-swagger"));
        assertThat(connector.getConnectorFactory()).hasValue("io.syndesis.connector.rest.swagger.ConnectorFactory");
        assertThat(connector.getConnectorCustomizers())
            .containsOnly(
                "io.syndesis.connector.rest.swagger.SpecificationResourceCustomizer",
                "io.syndesis.connector.rest.swagger.AuthenticationCustomizer",
                "io.syndesis.connector.rest.swagger.RequestCustomizer",
                "io.syndesis.connector.rest.swagger.ResponseCustomizer");
        assertThat(connector.getActions(ConnectorAction.class)).allSatisfy(a -> {
            assertThat(a.getDescriptor().getComponentScheme()).hasValue("rest-swagger");
            assertThat(a.getDescriptor().getCamelConnectorGAV()).isNull();
            assertThat(a.getDescriptor().getCamelConnectorPrefix()).isNull();
        });
    }

}
