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
import java.util.List;

import io.syndesis.connector.sql.common.DatabaseContainers.Database;
import io.syndesis.connector.sql.common.IfImagePresentCondition.IfImagePresent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.JdbcDatabaseContainer;

import static org.assertj.core.api.Assertions.assertThat;

@IfImagePresent("oracle/database:18.4.0-xe")
@Database("oracle/database:18.4.0-xe")
public class OracleSqlMetaDataITCase {

    @BeforeEach
    public void createTestTable(final JdbcDatabaseContainer<?> oracle) throws SQLException {
        try (Connection connection = oracle.createConnection("")) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE table1 (column1 VARCHAR(50), column2 VARCHAR(50))");
            }
        }
    }

    @AfterEach
    public void dropTestTable(final JdbcDatabaseContainer<?> oracle) throws SQLException {
        try (Connection connection = oracle.createConnection("")) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE table1");
            }
        }
    }

    @ExtendWith(DatabaseContainers.class)
    @TestTemplate
    public void shouldGenerateMetadataForLowercaseTableNames(final JdbcDatabaseContainer<?> oracle) throws SQLException {
        try (Connection connection = oracle.createConnection("")) {
            final SqlStatementParser parser = new SqlStatementParser(connection, "select column1 from table1 where column2 = :#value_");
            final SqlStatementMetaData info = parser.parse();
            final List<SqlParam> inParams = info.getInParams();

            final DbMetaDataHelper helper = new DbMetaDataHelper(connection);
            final List<SqlParam> paramList = helper.getJDBCInfoByColumnOrder(null, null, "table1", inParams);

            assertThat(paramList).hasSize(1);

            final SqlParam sqlParam = paramList.get(0);
            assertThat(sqlParam.getJdbcType()).isEqualTo(JDBCType.VARCHAR);
        }
    }
}
