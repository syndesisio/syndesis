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
import java.util.Map;

import io.syndesis.common.model.action.ActionDescriptor.ActionDescriptorStep;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.server.jsondb.CloseableJsonDB;
import io.syndesis.server.jsondb.dao.Migrator;
import io.syndesis.server.jsondb.impl.MemorySqlJsonDB;
import io.syndesis.server.runtime.DefaultMigrator;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static io.syndesis.server.runtime.migrations.MigrationsHelper.load;

import static org.assertj.core.api.Assertions.assertThat;

public class UpgradeVersion43Test {

    @Test
    public void shouldPerformSchemaUpgrade() throws IOException {
        try (CloseableJsonDB jsondb = MemorySqlJsonDB.create(Collections.emptyList());
            InputStream is = UpgradeVersion43Test.class.getResourceAsStream("/migrations/43/integration.json")) {
            jsondb.push("/integrations", is);

            final Migrator migrator = new DefaultMigrator(new DefaultResourceLoader());
            migrator.migrate(jsondb, 43);

            final List<Integration> integrations = load(jsondb, "/integrations", Integration.class);

            assertThat(integrations).hasSize(1);

            final Integration migrated = integrations.get(0);
            assertThat(migrated.getFlows())
                .hasSize(5)
                .allSatisfy(flow -> assertThat(lastStepOf(flow))
                    .satisfies(step -> assertThat(step.getActionAs(ConnectorAction.class))
                        .hasValueSatisfying(lastAction -> {
                            assertThat(lastAction.getId()).hasValue("io.syndesis:api-provider-end");

                            final ConnectorDescriptor descriptor = lastAction.getDescriptor();
                            assertThat(descriptor.getExceptionHandler()).hasValue("io.syndesis.connector.apiprovider.ApiProviderOnExceptionHandler");

                            final Map<String, ActionDescriptorStep> propertyDefinitionSteps = descriptor.getPropertyDefinitionStepsAsMap();
                            final Map<String, ConfigurationProperty> configurationProperties = propertyDefinitionSteps.get("configuration").getProperties();
                            assertThat(configurationProperties.get("errorResponseCodes").getDefaultValue()).isEqualTo("{\"SERVER_ERROR\":\"500\"}");
                            assertThat(configurationProperties.get("returnBody").getDefaultValue()).isEqualTo("true");
                        })));
        }
    }

    static Step lastStepOf(final Flow flow) {
        final List<Step> steps = flow.getSteps();

        return steps.get(steps.size() - 1);
    }

}
