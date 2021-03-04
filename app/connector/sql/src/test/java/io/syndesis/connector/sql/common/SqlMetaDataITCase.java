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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import io.syndesis.connector.sql.common.DatabaseContainers.Database;
import io.syndesis.connector.sql.db.Db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.JdbcDatabaseContainer;

@ExtendWith(DatabaseContainers.class)
@Database({"postgres:11.11", "mariadb:10.5.8"})
public class SqlMetaDataITCase {

    @TestTemplate
    public void defaultValuesTest(final JdbcDatabaseContainer<?> database) throws SQLException {
        try (Connection con = database.createConnection("");
            Statement stmt = con.createStatement()) {
            final String createTable = "CREATE TABLE TEST (charType CHAR, varcharType VARCHAR(255), "
                + "numericType NUMERIC, decimalType DECIMAL, smallintType SMALLINT," + "dateType DATE, timeType TIME )";
            stmt.executeUpdate(createTable);
            final String sql = String.format("INSERT INTO TEST VALUES ('J', 'Jackson',0, 0, 0, '%s', '%s')", SqlParam.SqlSampleValue.DATE_VALUE,
                SqlParam.SqlSampleValue.TIME_VALUE);
            stmt.executeUpdate(sql);

            String select = "SELECT * FROM TEST where charType=:#myCharValue";
            final SqlStatementParser sqlParser = new SqlStatementParser(con, select);
            final SqlStatementMetaData paramInfo = sqlParser.parse();
            final List<SqlParam> inputParams = paramInfo.getInParams();
            // information for input
            Assertions.assertEquals(1, inputParams.size());
            Assertions.assertEquals(Character.class, inputParams.get(0).getTypeValue().getClazz());

            // information for output of select statement
            final List<SqlParam> outputParams = paramInfo.getOutParams();
            Assertions.assertEquals(7, outputParams.size());
            Assertions.assertEquals(Character.class, outputParams.get(0).getTypeValue().getClazz());
        }
    }

    @TestTemplate
    public void parseInsertAllColumns(final JdbcDatabaseContainer<?> database) throws SQLException {
        try (Connection con = database.createConnection("");
            Statement stmt = con.createStatement()) {
            final String createTable = "CREATE TABLE TEST (id INTEGER PRIMARY KEY, firstName VARCHAR(255), " + "lastName VARCHAR(255))";
            stmt.executeUpdate(createTable);
            stmt.executeUpdate("INSERT INTO TEST VALUES (1, 'Joe', 'Jackson')");
            stmt.executeUpdate("INSERT INTO TEST VALUES (2, 'Roger', 'Waters')");

            final String sqlStatement = "INSERT INTO TEST VALUES (:#id, :#first, :#last)";
            final SqlStatementParser parser = new SqlStatementParser(con, sqlStatement);
            final SqlStatementMetaData info = parser.parse();

            final List<SqlParam> paramList = new DbMetaDataHelper(con).getJDBCInfoByColumnOrder(null, null, "TEST",
                info.getInParams());
            Assertions.assertEquals("INTEGER", paramList.get(0).getJdbcType().getName());
            Assertions.assertEquals("VARCHAR", paramList.get(1).getJdbcType().getName());
            Assertions.assertEquals("VARCHAR", paramList.get(2).getJdbcType().getName());
        }
    }

    @TestTemplate
    public void parseInsertWithSpecifiedColumnNames(final JdbcDatabaseContainer<?> database) throws SQLException {
        try (Connection con = database.createConnection("");
            Statement stmt = con.createStatement()) {
            final String createTable = "CREATE TABLE TEST (id INTEGER PRIMARY KEY, firstName VARCHAR(255), " + "lastName VARCHAR(255))";
            stmt.executeUpdate(createTable);
            stmt.executeUpdate("INSERT INTO TEST (ID, FIRSTNAME, LASTNAME) VALUES (1, 'Joe', 'Jackson')");
            stmt.executeUpdate("INSERT INTO TEST (ID, FIRSTNAME, LASTNAME) VALUES (2, 'Roger', 'Waters')");

            final String sqlStatement = "INSERT INTO TEST (ID, FIRSTNAME, LASTNAME) VALUES (:#id, :#first, :#last)";
            final SqlStatementParser parser = new SqlStatementParser(con, sqlStatement);
            final SqlStatementMetaData info = parser.parse();

            final List<SqlParam> paramList = new DbMetaDataHelper(con).getJDBCInfoByColumnOrder(null, null, "TEST",
                info.getInParams());
            Assertions.assertEquals("INTEGER", paramList.get(0).getJdbcType().getName());
            Assertions.assertEquals("VARCHAR", paramList.get(1).getJdbcType().getName());
            Assertions.assertEquals("VARCHAR", paramList.get(2).getJdbcType().getName());
        }
    }

    @TestTemplate
    public void parseInsertAutoIncrPK(final JdbcDatabaseContainer<?> database) throws SQLException {
        try (Connection con = database.createConnection("");
            Statement stmt = con.createStatement()) {
            Db testDb = new DbAdapter(con).getDb();
            final String createTable = "CREATE TABLE TEST (id2 " + testDb.getAutoIncrementGrammar() + ", firstName VARCHAR(255), " + "lastName VARCHAR(255))";
            stmt.executeUpdate(createTable);
            stmt.executeUpdate("INSERT INTO TEST (FIRSTNAME, LASTNAME) VALUES ('Joe', 'Jackson')");
            stmt.executeUpdate("INSERT INTO TEST (FIRSTNAME, LASTNAME) VALUES ('Roger', 'Waters')");

            final String sqlStatement = "INSERT INTO TEST (FIRSTNAME, LASTNAME) VALUES (:#first, :#last)";
            final SqlStatementParser parser = new SqlStatementParser(con, sqlStatement);
            final SqlStatementMetaData info = parser.parse();

            final List<SqlParam> paramList = new DbMetaDataHelper(con).getJDBCInfoByColumnNames(null, null, "TEST",
                info.getInParams());
            Assertions.assertEquals("VARCHAR", paramList.get(0).getJdbcType().getName());
            Assertions.assertEquals("VARCHAR", paramList.get(1).getJdbcType().getName());
        }
    }

    @TestTemplate
    public void parseSelect(final JdbcDatabaseContainer<?> database) throws SQLException {
        try (Connection con = database.createConnection("");
            Statement stmt = con.createStatement()) {
            final String createTable = "CREATE TABLE TEST (id INTEGER PRIMARY KEY, firstName VARCHAR(255), " + "lastName VARCHAR(255))";
            stmt.executeUpdate(createTable);
            stmt.executeUpdate("INSERT INTO TEST VALUES (1, 'Joe', 'Jackson')");
            stmt.executeUpdate("INSERT INTO TEST VALUES (2, 'Roger', 'Waters')");

            final String sqlStatement = "SELECT FIRSTNAME, LASTNAME FROM TEST WHERE ID=:#id";
            final SqlStatementParser parser = new SqlStatementParser(con, sqlStatement);
            final SqlStatementMetaData info = parser.parse();

            final List<SqlParam> paramList = new DbMetaDataHelper(con).getJDBCInfoByColumnOrder(null, null, "TEST",
                info.getInParams());
            Assertions.assertEquals("INTEGER", paramList.get(0).getJdbcType().getName());
        }
    }

    @AfterEach
    void dropTableTest(final JdbcDatabaseContainer<?> database) {
        try (Connection con = database.createConnection("");
            Statement stmt = con.createStatement()) {
            stmt.execute("DROP TABLE TEST");
        } catch (final SQLException ignore) {
            // ignored
        }
    }
}
