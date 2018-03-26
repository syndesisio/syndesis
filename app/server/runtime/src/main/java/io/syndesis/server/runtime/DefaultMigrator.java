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

import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import io.syndesis.common.util.IOStreams;
import io.syndesis.common.util.Resources;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.server.jsondb.WithGlobalTransaction;
import io.syndesis.server.jsondb.JsonDB;
import io.syndesis.server.jsondb.dao.Migrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import static io.syndesis.common.util.Json.map;

@Service
public class DefaultMigrator implements Migrator {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMigrator.class);

    private final ResourceLoader resourceLoader;

    public DefaultMigrator(final ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void migrate(final JsonDB jsondb, final int toVersion) {
        if (jsondb instanceof WithGlobalTransaction) {
            ((WithGlobalTransaction) jsondb).withGlobalTransaction(checkpointed -> performMigration(checkpointed, toVersion));
        } else {
            performMigration(jsondb, toVersion);
        }
    }

    private void performMigration(final JsonDB utilized, final int toVersion) {
        final String path = migrationsScriptPrefix() + toVersion + ".js";
        try {
            final Resource resource = resourceLoader.getResource(path);
            if (!resource.exists()) {
                return;
            }

            final String migrationScript = IOStreams.readText(resource.getInputStream());

            LOG.info("Migrating to schema: {}", toVersion);
            final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            engine.put("internal", map("jsondb", utilized));

            engine.eval(Resources.getResourceAsText("migrations/common.js"));
            engine.eval(migrationScript);
        } catch (IOException | ScriptException e) {
            throw new SyndesisServerException(
                "Unable to perform database migration to version " + toVersion + ", using migration script at: " + path, e);
        }
    }

    protected String migrationsScriptPrefix() {
        return "classpath:/migrations/up-";
    }

}
