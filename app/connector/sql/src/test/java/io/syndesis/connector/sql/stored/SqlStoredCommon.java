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
package io.syndesis.connector.sql.stored;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.syndesis.connector.sql.SqlSupport;
import io.syndesis.connector.sql.common.DbEnum;
import io.syndesis.connector.sql.common.stored.StoredProcedureMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.fail;


public class SqlStoredCommon {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlStoredCommon.class);

    public static void setupStoredProcedure(Connection connection, Properties properties) throws Exception {

        try {
            String dbProductName = connection.getMetaData().getDatabaseProductName();
            Map<String,Object> parameters = new HashMap<>();
            for (final String name: properties.stringPropertyNames()) {
                parameters.put(name.substring(name.indexOf('.') + 1), properties.getProperty(name));
            }
            Map<String,StoredProcedureMetadata> storedProcedures = SqlSupport.getStoredProcedures(parameters);

            if (!storedProcedures.keySet().contains("DEMO_ADD")
                    && DbEnum.APACHE_DERBY.equals(DbEnum.fromName(dbProductName))) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(SampleStoredProcedures.DERBY_DEMO_ADD_SQL);
                    LOGGER.info("Created procedure {}", SampleStoredProcedures.DERBY_DEMO_ADD_SQL);
                } catch (SQLException e) {
                    LOGGER.warn("", e);
                    fail("Exception during Stored Procedure Creation.", e);
                }
            }
            if (!storedProcedures.keySet().contains("DEMO_OUT")
                    && DbEnum.APACHE_DERBY.equals(DbEnum.fromName(dbProductName))) {
                try (Statement stmt = connection.createStatement()) {
                    //Create procedure
                    stmt.execute(SampleStoredProcedures.DERBY_DEMO_OUT_SQL);
                } catch (Exception e) {
                    fail("Exception during Stored Procedure Creation.", e);
                }
            }
        } catch (SQLException ex) {
            LOGGER.warn("", ex);
            fail("Exception during database startup.", ex);
        }
    }

}
