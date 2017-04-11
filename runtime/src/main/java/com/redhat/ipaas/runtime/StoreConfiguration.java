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
package com.redhat.ipaas.runtime;

import com.redhat.ipaas.core.IPaasServerException;
import com.redhat.ipaas.core.Json;
import com.redhat.ipaas.jsondb.JsonDB;
import com.redhat.ipaas.jsondb.impl.SqlJsonDB;
import org.skife.jdbi.v2.DBI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * Creates and configures a DBI object
 */
@Configuration
public class StoreConfiguration {

    @Value("${dao.schema.version}")
    private String daoSchemaVersion;

    @Bean
    public DBI dbiBean(@Autowired DataSource dataSource) {
        DBI dbi = new DBI(dataSource);
        return dbi;
    }

    @Bean
    @Autowired
    public JsonDB realTimeDB(DBI dbi) {
        return init(new SqlJsonDB(dbi, null));
    }

    private SqlJsonDB init(SqlJsonDB jsondb) {
        try {
            jsondb.createTables();
        } catch (Exception ignore) {
        }

        String versionInDB = getClusterProperty(jsondb, "dao_schema_version");
        if (daoSchemaVersion != null &&
            (versionInDB == null || !daoSchemaVersion.equals(versionInDB))) {

            // really silly migration strategy for now, reset the DB:
            jsondb.dropTables();
            jsondb.createTables();

            setClusterProperty(jsondb, "dao_schema_version", daoSchemaVersion);
        }

        return jsondb;
    }

    public static String getClusterProperty(SqlJsonDB jsondb, String name) {
        String json = jsondb.getAsString("/cluster-properties/"+name);
        if( json==null ) {
            return null;
        }
        try {
            return Json.mapper().readValue(json, String.class);
        } catch (IOException e) {
            throw IPaasServerException.launderThrowable(e);
        }
    }

    public  static void setClusterProperty(SqlJsonDB jsondb, String name, String value) {
        String json;
        try {
            json = Json.mapper().writeValueAsString(value);
        } catch (IOException e) {
            throw IPaasServerException.launderThrowable(e);
        }
        jsondb.set("/cluster-properties/"+name, json);
    }

}
