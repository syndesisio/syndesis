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
package io.syndesis.connector.sql.common;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.syndesis.connector.sql.SqlConnectorMetaDataExtension;
import io.syndesis.connector.sql.common.DatabaseContainers.Database;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.JdbcDatabaseContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Database({"mysql:8.0.25"})
public class MysqlMetadataITCase {

    private static final CamelContext CAMEL = new DefaultCamelContext();

    @BeforeEach
    public void createTestObjects(final JdbcDatabaseContainer<?> mysqldb) throws SQLException {
        try (Connection connection = mysqldb.createConnection("")) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(
                    "CREATE TABLE testOne ("
                        + "id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',"
                        + "name varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '姓名',"
                        + "PRIMARY KEY (id) USING BTREE"
                        + ") ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic");
            }
        }
    }

    @AfterEach
    public void dropTestObjects(final JdbcDatabaseContainer<?> mysqldb) throws SQLException {
        try (Connection connection = mysqldb.createConnection("")) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE testOne");
            }
        }
    }

    @ExtendWith(DatabaseContainers.class)
    @TestTemplate
    public void shouldFetchTableMetadata(final JdbcDatabaseContainer<?> mysqldb) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("url", mysqldb.getJdbcUrl());
        parameters.put("user", mysqldb.getUsername());
        parameters.put("password", mysqldb.getPassword());
        parameters.put("query", "SELECT * from testOne WHERE ID=:#id");

        final SqlConnectorMetaDataExtension metaData = new SqlConnectorMetaDataExtension(CAMEL);

        final Optional<MetaData> meta = metaData.meta(parameters);

        assertThat(meta).get().extracting(MetaData::getPayload).isInstanceOfSatisfying(SqlStatementMetaData.class, payload -> {
            assertThat(payload.getTableNames()).containsOnly("testOne");
            assertThat(payload.getInParams()).hasSize(1).element(0).satisfies(param -> {
                assertThat(param.getColumn()).isEqualTo("ID");
                assertThat(param.getName()).isEqualTo("id");
                assertThat(param.getColumnPos()).isEqualTo(0);
                assertThat(param.getJdbcType()).isEqualTo(JDBCType.BIGINT);
            });
            assertThat(payload.getOutParams()).hasSize(2)
                .satisfies(params -> {
                    assertThat(params).element(0).satisfies(param -> {
                        assertThat(param.getName()).isEqualTo("id");
                        assertThat(param.getJdbcType()).isEqualTo(JDBCType.BIGINT);
                    });
                    assertThat(params).element(1).satisfies(param -> {
                        assertThat(param.getName()).isEqualTo("name");
                        assertThat(param.getJdbcType()).isEqualTo(JDBCType.VARCHAR);
                    });
                });
        });
    }

    @AfterAll
    public static void stopCamelContext() throws Exception {
        CAMEL.stop();
    }
}
