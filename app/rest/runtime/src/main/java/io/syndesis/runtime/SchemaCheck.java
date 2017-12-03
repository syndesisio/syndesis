/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.runtime;

import io.syndesis.core.Json;
import io.syndesis.core.SyndesisServerException;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.jsondb.impl.SqlJsonDB;
import io.syndesis.model.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 *
 */
@Service
public class SchemaCheck {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaCheck.class);

    private final SqlJsonDB jsondb;
    private final DataManager manager;

    SchemaCheck(SqlJsonDB jsondb, DataManager manager) {
        this.jsondb = jsondb;
        this.manager = manager;
    }

    @PostConstruct
    public void schemaCheck() {
        String versionInDB = getClusterProperty(jsondb, "dao_schema_version");
        if ( versionInDB == null || !Schema.VERSION.equals(versionInDB) ) {

            LOG.info("DB schema changed.  Resetting the DB.");
            // really silly migration strategy for now, reset the DB:
            jsondb.dropTables();
            jsondb.createTables();
            manager.resetDeploymentData();

            setClusterProperty(jsondb, "dao_schema_version", Schema.VERSION);
        } else {
            LOG.info("DB schema has not changed.");
        }
    }


    public static String getClusterProperty(SqlJsonDB jsondb, String name) {
        String json = jsondb.getAsString("/cluster-properties/" + name);
        if (json == null) {
            return null;
        }
        try {
            return Json.mapper().readValue(json, String.class);
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    public static void setClusterProperty(SqlJsonDB jsondb, String name, String value) {
        String json;
        try {
            json = Json.mapper().writeValueAsString(value);
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
        jsondb.set("/cluster-properties/" + name, json);
    }

}
