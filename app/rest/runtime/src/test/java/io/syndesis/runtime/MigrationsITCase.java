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
package io.syndesis.runtime;

import static io.syndesis.core.Json.map;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.syndesis.core.Json;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.jsondb.impl.SqlJsonDB;

@SpringBootTest()
@ActiveProfiles("test")
@ContextConfiguration(
    classes = {
        Application.class,
        DataSourceConfiguration.class,
        DataStoreConfiguration.class,
    }
)
public class MigrationsITCase {

    private static final Logger LOG = LoggerFactory.getLogger(MigrationsITCase.class);

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    protected DBI dbi;
    @Autowired
    protected SqlJsonDB jsondb;
    @Autowired
    protected DataManager manager;
    @Autowired
    protected StoredSettings storedSettings;

    private Migrations createMigrations(String prefix, String target) {
        return new Migrations(jsondb, manager, storedSettings) {
            @Override
            public String getTargetVersion() {
                return target;
            }

            @Override
            protected String migrationsScriptPrefix() {
                return prefix;
            }
        };

    }

    @Test
    public void test() throws JsonProcessingException {
        resetDB();
        Migrations m = createMigrations("test-migrations/up-", "0");
        m.run();

        jsondb.set("/test", json(map(
            "u10001", map(
                "name", "Hiram Chirino"
            )
        )));

        m = createMigrations("test-migrations/up-", "3");
        m.run();

        String json = jsondb.getAsString("/test");
        assertThat(json).isEqualTo("{\"u10001\":{\"name\":\"Hiram Chirino Migrated\"}}");

    }

    private String json(Object value) throws JsonProcessingException {
        return Json.writer().writeValueAsString(value);
    }

    private void resetDB() {
        storedSettings.dropTables();
        storedSettings.createTables();
        jsondb.dropTables();
        jsondb.createTables();
    }
}
