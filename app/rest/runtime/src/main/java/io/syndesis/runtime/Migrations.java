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

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.syndesis.core.SyndesisServerException;
import io.syndesis.core.util.Resources;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.jsondb.JsonDB;
import io.syndesis.jsondb.dao.Migrator;
import io.syndesis.jsondb.impl.SqlJsonDB;
import io.syndesis.model.Schema;

/**
 *
 */
@Service
public class Migrations implements Migrator {
    private static final Logger LOG = LoggerFactory.getLogger(Migrations.class);

    private final SqlJsonDB jsondb;
    private final DataManager manager;
    private final StoredSettings storedSettings;

    Migrations(SqlJsonDB jsondb, DataManager manager, StoredSettings storedSettings) {
        this.jsondb = jsondb;
        this.manager = manager;
        this.storedSettings = storedSettings;
    }

    @PostConstruct
    public void run() {
        String versionInDB = storedSettings.get("model_schema_version");
        if ( versionInDB == null ) {
            LOG.info("Setting up the DB for the first time.");
            jsondb.dropTables();
            jsondb.createTables();
            storedSettings.set("model_schema_version", "0");
            versionInDB = "0";
        }

        if( !getTargetVersion().equals(versionInDB) ) {
            LOG.info("DB schema changed.");
            int from = Integer.parseInt(versionInDB);
            int to = Integer.parseInt(getTargetVersion());

            // Apply per version migration scripts.
            for (int i = from; i < to; i++) {
                int version = i + 1;
                migrate(jsondb, version);
                storedSettings.set("model_schema_version", Integer.toString(version));
            }
        } else {
            LOG.info("DB schema has not changed: {}", getTargetVersion());
        }
        manager.resetDeploymentData();
    }

    public String getTargetVersion() {
        return Schema.VERSION;
    }

    @Override
    public void migrate(JsonDB jsondb, int toVersion) {
        try {
            String file = migrationsScriptPrefix() + toVersion + ".js";
            String migrationScript = Resources.getResourceAsText(file);
            if( migrationScript == null ) {
                return;
            }
            LOG.info("Migrating to schema: {}", toVersion);
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            engine.put("internal",  map(
                "jsondb", jsondb
            ));

            engine.eval(Resources.getResourceAsText("migrations/common.js"));
            engine.eval(migrationScript);
        } catch (IOException|ScriptException e) {
            throw new SyndesisServerException(e);
        }
    }

    protected String migrationsScriptPrefix() {
        return "/migrations/up-";
    }

}
