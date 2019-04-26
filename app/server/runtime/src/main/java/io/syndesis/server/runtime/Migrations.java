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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import io.syndesis.common.model.Schema;
import io.syndesis.common.util.thread.Threads;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.jsondb.dao.Migrator;
import io.syndesis.server.jsondb.impl.SqlJsonDB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "dao.autoMigration", havingValue = "true", matchIfMissing = true)
public class Migrations {

    private static final Logger LOG = LoggerFactory.getLogger(Migrations.class);

    private final SqlJsonDB jsondb;

    private final DataManager manager;

    private CompletableFuture<Void> migrationsDone;

    private final Migrator migrator;

    private final StoredSettings storedSettings;

    public Migrations(final SqlJsonDB jsondb, final DataManager manager, final StoredSettings storedSettings, final Migrator migrator) {
        this.jsondb = jsondb;
        this.manager = manager;
        this.storedSettings = storedSettings;
        this.migrator = migrator;
    }

    public int getTargetVersion() {
        return Schema.VERSION;
    }

    public CompletableFuture<Void> migrationsDone() {
        return migrationsDone;
    }

    @PostConstruct
    public Future<Void> run() {
        migrationsDone = CompletableFuture.runAsync(this::performMigrations, Executors.newSingleThreadExecutor(Threads.newThreadFactory("DB migration")));

        return migrationsDone;
    }

    Void performMigrations() {
        final String storedVersion = storedSettings.get("model_schema_version");

        final int versionInDB;
        if (storedVersion == null) {
            LOG.info("Setting up the DB for the first time.");
            jsondb.dropTables();
            jsondb.createTables();
            storedSettings.set("model_schema_version", "0");
            versionInDB = 0;
        } else {
            versionInDB = Integer.parseInt(storedVersion);
        }

        if (getTargetVersion() != versionInDB) {
            LOG.info("DB schema changed.");
            final int from = versionInDB;
            final int to = getTargetVersion();

            // Apply per version migration scripts.
            for (int version = from + 1; version <= to; version++) {
                migrator.migrate(jsondb, version);
                storedSettings.set("model_schema_version", Integer.toString(version));
            }
        } else {
            LOG.info("DB schema has not changed: {}", getTargetVersion());
        }
        manager.resetDeploymentData();

        return null;
    }
}
