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
package io.syndesis.cli.command.migrate;

import java.util.HashMap;
import java.util.Map;

import io.syndesis.cli.command.SyndesisCommand;
import io.syndesis.common.model.Schema;
import io.syndesis.server.jsondb.JsonDB;
import io.syndesis.server.jsondb.dao.Migrator;
import io.syndesis.server.runtime.StoredSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

import com.kakawait.spring.boot.picocli.autoconfigure.ExitStatus;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "migrate")
public class MigrateCommand extends SyndesisCommand {

    private static final Logger LOG = LoggerFactory.getLogger(MigrateCommand.class);

    private static final String SCHEMA_VERSION_KEY = "model_schema_version";

    @Option(names = {"-e", "--encryption-password"}, description = "Database encryption password", required = false,
        paramLabel = "<password>")
    private String encryptionPassword;

    @Option(names = {"-p", "--password"}, description = "Password to authenticate against the database", required = false)
    private String password;

    @Option(names = {"-t", "--target"}, description = "Target schema version number", required = false, paramLabel = "<version>")
    private int targetVersion = Schema.VERSION;

    @Option(names = {"-d", "--url"}, description = {"JDBC url for the database", "Example: jdbc:postgresql://localhost:5432/syndesis"},
        required = false)
    private String url;

    @Option(names = {"-u", "--user"}, description = "Username to authenticate against the database", required = false)
    private String username;

    @Override
    public ExitStatus call() {
        final Map<String, Object> parameters = new HashMap<>();
        putParameter(parameters, "spring.datasource.url", url);
        putParameter(parameters, "spring.datasource.username", username);
        putParameter(parameters, "spring.datasource.password", password);
        putParameter(parameters, "encrypt.key", encryptionPassword);

        try (AbstractApplicationContext context = createContext("migration", parameters)) {
            final StoredSettings storedSettings = context.getBean(StoredSettings.class);

            final String storedVersion = storedSettings.get(SCHEMA_VERSION_KEY);

            final int version = storedVersion == null ? 0 : Integer.parseInt(storedVersion);

            LOG.info("Current database schema version: {}", version);
            if (version < targetVersion) {
                LOG.info("Migrating to version: {}", targetVersion);
            }

            final JsonDB jsondb = context.getBean(JsonDB.class);

            final Migrator migrator = context.getBean(Migrator.class);

            for (int i = version; i <= targetVersion; i++) {
                migrator.migrate(jsondb, i);
            }

            storedSettings.set(SCHEMA_VERSION_KEY, Integer.toString(targetVersion));

            LOG.info("Migration done");

            return ExitStatus.OK;
        }
    }

    void setEncryptionPassword(final String encryptionPassword) {
        this.encryptionPassword = encryptionPassword;
    }

    void setPassword(final String password) {
        this.password = password;
    }

    void setTargetVersion(final int targetVersion) {
        this.targetVersion = targetVersion;
    }

    void setUrl(final String url) {
        this.url = url;
    }

    void setUsername(final String username) {
        this.username = username;
    }
}
