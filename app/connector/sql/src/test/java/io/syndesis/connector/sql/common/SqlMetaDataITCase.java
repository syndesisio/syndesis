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

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import io.syndesis.connector.sql.db.Db;

@RunWith(Parameterized.class)
public class SqlMetaDataITCase {

    @Rule
    public JdbcDatabaseContainer<?> database;
    
    public SqlMetaDataITCase(final JdbcDatabaseContainer<?> database) {
        this.database = database;
    }
 
    @SuppressWarnings("resource")
    @Parameters
    public static Collection<Object> databaseContainerImages() {
        return Arrays.asList(
                //new DerbyContainer(),  //
                new PostgreSQLContainer<>(),
                new MariaDBContainer<>()
                );
    }

    @Test
    public void defaultValuesTest() throws SQLException {
        try (Statement stmt = database.createConnection("").createStatement()) {
            final String createTable = "CREATE TABLE TEST (charType CHAR, varcharType VARCHAR(255), "
                + "numericType NUMERIC, decimalType DECIMAL, smallintType SMALLINT," + "dateType DATE, timeType TIME )";
            stmt.executeUpdate(createTable);
            final String sql = String.format("INSERT INTO TEST VALUES ('J', 'Jackson',0, 0, 0, '%s', '%s')", SqlParam.SqlSampleValue.DATE_VALUE,
                SqlParam.SqlSampleValue.TIME_VALUE);
            stmt.executeUpdate(sql);
        }

        String select = "SELECT * FROM TEST where charType=:#myCharValue";
        final SqlStatementParser sqlParser = new SqlStatementParser(database.createConnection(""), select);
        final SqlStatementMetaData paramInfo = sqlParser.parse();
        final List<SqlParam> inputParams = paramInfo.getInParams();
        // information for input
        Assert.assertEquals(1, inputParams.size());
        Assert.assertEquals(Character.class, inputParams.get(0).getTypeValue().getClazz());

        // information for output of select statement
        final List<SqlParam> outputParams = paramInfo.getOutParams();
        Assert.assertEquals(7, outputParams.size());
        Assert.assertEquals(Character.class, outputParams.get(0).getTypeValue().getClazz());
    }

    @Test
    public void parseInsertAllColumns() throws SQLException {
        try (Statement stmt = database.createConnection("").createStatement()) {
            final String createTable = "CREATE TABLE TEST (id INTEGER PRIMARY KEY, firstName VARCHAR(255), " + "lastName VARCHAR(255))";
            stmt.executeUpdate(createTable);
            stmt.executeUpdate("INSERT INTO TEST VALUES (1, 'Joe', 'Jackson')");
            stmt.executeUpdate("INSERT INTO TEST VALUES (2, 'Roger', 'Waters')");
        }

        final String sqlStatement = "INSERT INTO TEST VALUES (:#id, :#first, :#last)";
        final SqlStatementParser parser = new SqlStatementParser(database.createConnection(""), sqlStatement);
        final SqlStatementMetaData info = parser.parse();

        final List<SqlParam> paramList = new DbMetaDataHelper(database.createConnection("")).getJDBCInfoByColumnOrder(null, null, "TEST",
                info.getInParams());
            Assert.assertEquals("INTEGER", paramList.get(0).getJdbcType().getName());
            Assert.assertEquals("VARCHAR", paramList.get(1).getJdbcType().getName());
            Assert.assertEquals("VARCHAR", paramList.get(2).getJdbcType().getName());

    }

    @Test
    public void parseInsertWithSpecifiedColumnNames() throws SQLException {
        try (Statement stmt = database.createConnection("").createStatement()) {
            final String createTable = "CREATE TABLE TEST (id INTEGER PRIMARY KEY, firstName VARCHAR(255), " + "lastName VARCHAR(255))";
            stmt.executeUpdate(createTable);
            stmt.executeUpdate("INSERT INTO TEST (ID, FIRSTNAME, LASTNAME) VALUES (1, 'Joe', 'Jackson')");
            stmt.executeUpdate("INSERT INTO TEST (ID, FIRSTNAME, LASTNAME) VALUES (2, 'Roger', 'Waters')");
        }

        final String sqlStatement = "INSERT INTO TEST (ID, FIRSTNAME, LASTNAME) VALUES (:#id, :#first, :#last)";
        final SqlStatementParser parser = new SqlStatementParser(database.createConnection(""), sqlStatement);
        final SqlStatementMetaData info = parser.parse();

        final List<SqlParam> paramList = new DbMetaDataHelper(database.createConnection("")).getJDBCInfoByColumnOrder(null, null, "TEST",
                info.getInParams());
        Assert.assertEquals("INTEGER", paramList.get(0).getJdbcType().getName());
        Assert.assertEquals("VARCHAR", paramList.get(1).getJdbcType().getName());
        Assert.assertEquals("VARCHAR", paramList.get(2).getJdbcType().getName());

    }

    @Test
    public void parseInsertAutoIncrPK() throws SQLException {
        Db testDb = new DbAdapter(database.createConnection("")).getDb();
        try (Statement stmt = database.createConnection("").createStatement()) {
            final String createTable = "CREATE TABLE TEST (id2 " + testDb.getAutoIncrementGrammar() + ", firstName VARCHAR(255), " + "lastName VARCHAR(255))";
            stmt.executeUpdate(createTable);
            stmt.executeUpdate("INSERT INTO TEST (FIRSTNAME, LASTNAME) VALUES ('Joe', 'Jackson')");
            stmt.executeUpdate("INSERT INTO TEST (FIRSTNAME, LASTNAME) VALUES ('Roger', 'Waters')");
        }

        final String sqlStatement = "INSERT INTO TEST (FIRSTNAME, LASTNAME) VALUES (:#first, :#last)";
        final SqlStatementParser parser = new SqlStatementParser(database.createConnection(""), sqlStatement);
        final SqlStatementMetaData info = parser.parse();

        final List<SqlParam> paramList = new DbMetaDataHelper(database.createConnection("")).getJDBCInfoByColumnNames(null, null, "TEST",
                info.getInParams());
        Assert.assertEquals("VARCHAR", paramList.get(0).getJdbcType().getName());
        Assert.assertEquals("VARCHAR", paramList.get(1).getJdbcType().getName());

    }

    @Test
    public void parseSelect() throws SQLException {
        final Statement stmt = database.createConnection("").createStatement();
        final String createTable = "CREATE TABLE TEST (id INTEGER PRIMARY KEY, firstName VARCHAR(255), " + "lastName VARCHAR(255))";
        stmt.executeUpdate(createTable);
        stmt.executeUpdate("INSERT INTO TEST VALUES (1, 'Joe', 'Jackson')");
        stmt.executeUpdate("INSERT INTO TEST VALUES (2, 'Roger', 'Waters')");

        final String sqlStatement = "SELECT FIRSTNAME, LASTNAME FROM TEST WHERE ID=:#id";
        final SqlStatementParser parser = new SqlStatementParser(database.createConnection(""), sqlStatement);
        final SqlStatementMetaData info = parser.parse();

        final List<SqlParam> paramList = new DbMetaDataHelper(database.createConnection("")).getJDBCInfoByColumnOrder(null, null, "TEST",
                info.getInParams());
        Assert.assertEquals("INTEGER", paramList.get(0).getJdbcType().getName());
    }

    @After
    public void afterClass() throws SQLException {
        try (final Statement stmt = database.createConnection("").createStatement()) {
            stmt.execute("DROP table TEST");
        }
    }

}
