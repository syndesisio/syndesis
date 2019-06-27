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

import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.server.jsondb.CloseableJsonDB;
import io.syndesis.server.jsondb.dao.Migrator;
import io.syndesis.server.jsondb.impl.MemorySqlJsonDB;
import io.syndesis.server.runtime.DefaultMigrator;

import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static io.syndesis.server.runtime.migrations.MigrationsHelper.load;

import static org.assertj.core.api.Assertions.assertThat;

public class UpgradeVersion30Test {

    @Test
    public void shouldPerformSchemaUpgrade() throws IOException {
        try (CloseableJsonDB jsondb = MemorySqlJsonDB.create(Collections.emptyList());
            InputStream data = UpgradeVersion30Test.class.getResourceAsStream("/migrations/30/integration.json")) {

            jsondb.push("/integrations", data);

            final Migrator migrator = new DefaultMigrator(new DefaultResourceLoader());
            migrator.migrate(jsondb, 30);

            final List<Integration> integrations = load(jsondb, "/integrations", Integration.class);

            // integrations
            assertThat(integrations).hasSize(1).allSatisfy(integration -> {
                assertThat(integration.getFlows()).allSatisfy(flow -> {
                    assertThat(flow.getId()).hasValueSatisfying(id -> assertThat(id).doesNotContain(":"));
                    assertThat(flow.getMetadata(OpenApi.OPERATION_ID)).isPresent();
                });
            });
        }
    }

}
