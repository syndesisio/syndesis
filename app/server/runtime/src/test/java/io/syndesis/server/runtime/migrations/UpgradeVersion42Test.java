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

import static io.syndesis.server.runtime.migrations.MigrationsHelper.load;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.action.ConnectorDescriptor.StandardizedError;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.server.jsondb.CloseableJsonDB;
import io.syndesis.server.jsondb.dao.Migrator;
import io.syndesis.server.jsondb.impl.MemorySqlJsonDB;
import io.syndesis.server.runtime.DefaultMigrator;

public class UpgradeVersion42Test {

    @Test
    public void shouldPerformSchemaUpgrade() throws IOException {
        try (CloseableJsonDB jsondb = MemorySqlJsonDB.create(Collections.emptyList());
            InputStream is = UpgradeVersion42Test.class.getResourceAsStream("/migrations/42/integration.json")) {
            jsondb.push("/integrations", is);

            final Migrator migrator = new DefaultMigrator(new DefaultResourceLoader());
            migrator.migrate(jsondb, 42);

            final List<Integration> integrations = load(jsondb, "/integrations", Integration.class);

            // integrations
            assertThat(integrations).hasSize(1);

            final Integration integration = integrations.get(0);
            for (Flow flow : integration.getFlows()) {
                for (Step step : flow.getSteps()) {
                    if (step.getAction().isPresent() && step.getAction().get().getDescription() !=null) {
                        if (step.getAction().get().getDescriptor() instanceof ConnectorDescriptor) {
                            List<StandardizedError> standardizedErrors = ((ConnectorDescriptor)step.getAction().get().getDescriptor()).getStandardizedErrors();
                            for (StandardizedError error : standardizedErrors) {
                                assertThat(error.name().startsWith("SQL_")).isFalse();
                            }
                        }
                    }
                    if (step.getConfiguredProperties().containsKey("errorResponseCodes")) {
                        String json = step.getConfiguredProperties().get("errorResponseCodes");
                        assertThat(json.contains("SQL_")).isFalse();
                        assertThat(json.contains("org.springframework.dao.DuplicateKeyException")).isFalse();
                    }
                }
            }
        }
    } 

}
