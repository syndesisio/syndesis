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
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

public class SqlMetaDataTest {

    @ClassRule
    public static SqlConnectionRule db = new SqlConnectionRule();

    @Test
    public void defaultValuesTest() throws SQLException {
        try (Statement stmt = db.connection.createStatement()) {
            final String createTable = "CREATE TABLE ALLTYPES (charType CHAR, varcharType VARCHAR(255), "
                + "numericType NUMERIC, decimalType DECIMAL, smallintType SMALLINT," + "dateType DATE, timeType TIME )";
            stmt.executeUpdate(createTable);
            final String sql = String.format("INSERT INTO ALLTYPES VALUES ('J', 'Jackson',0, 0, 0, '%s', '%s')", SqlParam.SqlSampleValue.DATE_VALUE,
                SqlParam.SqlSampleValue.TIME_VALUE);
            stmt.executeUpdate(sql);
        }

        String select = "SELECT * FROM ALLTYPES where charType=:#myCharValue";
        final SqlStatementParser sqlParser = new SqlStatementParser(db.connection, select);
        final SqlStatementMetaData paramInfo = sqlParser.parse();

        final List<SqlParam> inputParams = new DbMetaDataHelper(db.connection).getJDBCInfoByColumnNames(null, db.schema,
            paramInfo.getTableNames().get(0), paramInfo.getInParams());
        // information for input
        Assert.assertEquals(1, inputParams.size());
        Assert.assertEquals(Character.class, inputParams.get(0).getTypeValue().getClazz());

        // information for output of select statement
        for (final SqlParam sqlParam : inputParams) {
            select = select.replace(":#" + sqlParam.getName(), "'" + sqlParam.getTypeValue().getSampleValue().toString() + "'");
        }
        final List<SqlParam> outputParams = new DbMetaDataHelper(db.connection).getOutputColumnInfo(select);
        Assert.assertEquals(7, outputParams.size());
        Assert.assertEquals(Character.class, outputParams.get(0).getTypeValue().getClazz());
    }

    @Test
    public void parseInsertAllColumns() throws SQLException {
        try (Statement stmt = db.connection.createStatement()) {
            final String createTable = "CREATE TABLE NAME2 (id INTEGER PRIMARY KEY, firstName VARCHAR(255), " + "lastName VARCHAR(255))";
            stmt.executeUpdate(createTable);
            stmt.executeUpdate("INSERT INTO NAME2 VALUES (1, 'Joe', 'Jackson')");
            stmt.executeUpdate("INSERT INTO NAME2 VALUES (2, 'Roger', 'Waters')");
        }

        final String sqlStatement = "INSERT INTO NAME2 VALUES (:#id, :#first, :#last)";
        final SqlStatementParser parser = new SqlStatementParser(db.connection, sqlStatement);
        final SqlStatementMetaData info = parser.parse();

        final List<SqlParam> paramList = new DbMetaDataHelper(db.connection).getJDBCInfoByColumnOrder(null, null, "NAME2",
            info.getInParams());
        Assert.assertEquals("INTEGER", paramList.get(0).getJdbcType().getName());
        Assert.assertEquals("VARCHAR", paramList.get(1).getJdbcType().getName());
        Assert.assertEquals("VARCHAR", paramList.get(2).getJdbcType().getName());

    }

    @Test
    public void parseInsertWithSpecifiedColumnNames() throws SQLException {
        try (Statement stmt = db.connection.createStatement()) {
            final String createTable = "CREATE TABLE NAME3 (id INTEGER PRIMARY KEY, firstName VARCHAR(255), " + "lastName VARCHAR(255))";
            stmt.executeUpdate(createTable);
            stmt.executeUpdate("INSERT INTO NAME3 (ID, FIRSTNAME, LASTNAME) VALUES (1, 'Joe', 'Jackson')");
            stmt.executeUpdate("INSERT INTO NAME3 (ID, FIRSTNAME, LASTNAME) VALUES (2, 'Roger', 'Waters')");
        }

        final String sqlStatement = "INSERT INTO NAME3 (ID, FIRSTNAME, LASTNAME) VALUES (:#id, :#first, :#last)";
        final SqlStatementParser parser = new SqlStatementParser(db.connection, sqlStatement);
        final SqlStatementMetaData info = parser.parse();

        final List<SqlParam> paramList = new DbMetaDataHelper(db.connection).getJDBCInfoByColumnNames(null, null, "NAME3",
            info.getInParams());
        Assert.assertEquals("INTEGER", paramList.get(0).getJdbcType().getName());
        Assert.assertEquals("VARCHAR", paramList.get(1).getJdbcType().getName());
        Assert.assertEquals("VARCHAR", paramList.get(2).getJdbcType().getName());

    }

    @Test
    public void parseInsertAutoIncrPK() throws SQLException {
        try (Statement stmt = db.connection.createStatement()) {
            final String createTable = "CREATE TABLE NAME4 (id2 INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), firstName VARCHAR(255), " + "lastName VARCHAR(255))";
            stmt.executeUpdate(createTable);
            stmt.executeUpdate("INSERT INTO NAME4 (FIRSTNAME, LASTNAME) VALUES ('Joe', 'Jackson')");
            stmt.executeUpdate("INSERT INTO NAME4 (FIRSTNAME, LASTNAME) VALUES ('Roger', 'Waters')");
        }

        final String sqlStatement = "INSERT INTO NAME4 (FIRSTNAME, LASTNAME) VALUES (:#first, :#last)";
        final SqlStatementParser parser = new SqlStatementParser(db.connection, sqlStatement);
        final SqlStatementMetaData info = parser.parse();

        final List<SqlParam> paramList = new DbMetaDataHelper(db.connection).getJDBCInfoByColumnNames(null, null, "NAME4",
            info.getInParams());
        Assert.assertEquals("VARCHAR", paramList.get(0).getJdbcType().getName());
        Assert.assertEquals("VARCHAR", paramList.get(1).getJdbcType().getName());

    }

    @Test
    public void parseSelect() throws SQLException {
        final Statement stmt = db.connection.createStatement();
        final String createTable = "CREATE TABLE NAME (id INTEGER PRIMARY KEY, firstName VARCHAR(255), " + "lastName VARCHAR(255))";
        stmt.executeUpdate(createTable);
        stmt.executeUpdate("INSERT INTO name VALUES (1, 'Joe', 'Jackson')");
        stmt.executeUpdate("INSERT INTO NAME VALUES (2, 'Roger', 'Waters')");

        final String sqlStatement = "SELECT FIRSTNAME, LASTNAME FROM NAME WHERE ID=:#id";
        final SqlStatementParser parser = new SqlStatementParser(db.connection, sqlStatement);
        final SqlStatementMetaData info = parser.parse();

        final List<SqlParam> paramList = new DbMetaDataHelper(db.connection).getOutputColumnInfo(info.getDefaultedSqlStatement());
        Assert.assertEquals("VARCHAR", paramList.get(0).getJdbcType().getName());
        Assert.assertEquals("VARCHAR", paramList.get(1).getJdbcType().getName());
    }

    @AfterClass
    public static void afterClass() throws SQLException {
        try (final Statement stmt = db.connection.createStatement()) {
            stmt.execute("DROP table ALLTYPES");
            stmt.execute("DROP table NAME");
            stmt.execute("DROP table NAME2");
            stmt.execute("DROP table NAME3");
        }
    }

}
