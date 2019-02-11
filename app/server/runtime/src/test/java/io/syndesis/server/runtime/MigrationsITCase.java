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
package io.syndesis.server.runtime;

import java.util.concurrent.ExecutionException;

import io.syndesis.common.util.Json;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.jsondb.impl.SqlJsonDB;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import com.fasterxml.jackson.core.JsonProcessingException;

import static io.syndesis.common.util.Json.map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest()
@ActiveProfiles("test")
@ContextConfiguration(classes = {Application.class, DataSourceConfiguration.class, DataStoreConfiguration.class,})
public class MigrationsITCase {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private SqlJsonDB jsondb;

    @Autowired
    private DataManager manager;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private StoredSettings storedSettings;

    @Test
    public void shouldPerformMigrations() throws JsonProcessingException, InterruptedException, ExecutionException {
        resetDB();
        Migrations migrations = createMigrations("classpath:test-migrations", 0);
        migrations.run().get();

        jsondb.set("/test", json(map("u10001", map("name", "Hiram Chirino"))));

        migrations = createMigrations("classpath:test-migrations", 3);
        migrations.run().get();

        final String json = jsondb.getAsString("/test");
        assertThat(json).isEqualTo("{\"u10001\":{\"name\":\"Hiram Chirino Migrated\"}}");

    }

    private Migrations createMigrations(final String prefix, final int target) {
        final DefaultMigrator migrator = new DefaultMigrator(resourceLoader) {
            @Override
            protected String defaultMigrationScriptsPath() {
                return prefix;
            }
        };

        return new Migrations(jsondb, manager, storedSettings, migrator) {
            @Override
            public int getTargetVersion() {
                return target;
            }
        };

    }

    private void resetDB() {
        storedSettings.dropTables();
        storedSettings.createTables();
        jsondb.dropTables();
        jsondb.createTables();
    }

    private static String json(final Object value) throws JsonProcessingException {
        return Json.writer().writeValueAsString(value);
    }
}
