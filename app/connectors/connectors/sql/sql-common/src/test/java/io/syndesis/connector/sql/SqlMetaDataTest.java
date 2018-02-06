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
package io.syndesis.connector.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.syndesis.connector.sql.SqlParam.SqlSampleValue;

public class SqlMetaDataTest {

    private static Connection connection;
    private static String schema;
    private static SqlCommon sqlCommon;
    private static Map<String, Object> parameters = new HashMap<String, Object>();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        sqlCommon = new SqlCommon();
        Properties properties = new Properties();
        connection = sqlCommon.setupConnection(connection, properties);
        for (final String name : properties.stringPropertyNames()) {
            parameters.put(name.substring(name.indexOf(".") + 1), properties.getProperty(name));
        }
        schema = DatabaseMetaDataHelper.getDefaultSchema(
                connection.getMetaData().getDatabaseProductName(), parameters.get("user").toString());
    }

    @AfterClass
    public static void afterClass() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("DROP table ALLTYPES");
        stmt.execute("DROP table NAME");
        stmt.execute("DROP table NAME2");
        stmt.execute("DROP table NAME3");
        sqlCommon.closeConnection(connection);
    }
    
    @Test
    public void defaultValuesTest() throws SQLException {
        
        Statement stmt = connection.createStatement();
        String createTable = "CREATE TABLE ALLTYPES (charType CHAR, varcharType VARCHAR(255), " + 
                "numericType NUMERIC, decimalType DECIMAL, smallintType SMALLINT," +
                "dateType DATE, timeType TIME )";
        stmt.executeUpdate(createTable);
        String sql = String.format("INSERT INTO ALLTYPES VALUES ('J', 'Jackson',0, 0, 0, '%s', '%s')",
                SqlSampleValue.dateValue, SqlSampleValue.timeValue);
        stmt.executeUpdate(sql);
        String select = "SELECT * FROM ALLTYPES where charType=:#myCharValue";
        SqlStatementParser sqlParser = new SqlStatementParser(connection, schema, select);
        SqlStatementMetaData paramInfo = sqlParser.parse();
        
        final DatabaseMetaData meta = connection.getMetaData();
        List<SqlParam> inputParams = DatabaseMetaDataHelper.getJDBCInfoByColumnNames(meta, 
                new DBInfo(null,schema, paramInfo.getTableNames().get(0)), paramInfo.getInParams());
        //information for input
        Assert.assertEquals(1,inputParams.size());
        Assert.assertEquals(Character.class, inputParams.get(0).getTypeValue().getClazz());

        //information for output of select statement
        for (SqlParam sqlParam : inputParams) {
            select = select.replace(":#" + sqlParam.getName(), "'" + sqlParam.getTypeValue().getSampleValue().toString() + "'");
        }
        List<SqlParam> outputParams = DatabaseMetaDataHelper.getOutputColumnInfo(connection, select);
        Assert.assertEquals(7, outputParams.size());
        Assert.assertEquals(Character.class, outputParams.get(0).getTypeValue().getClazz());
    }
    
    @Test
    public void parseSelect() throws JsonProcessingException, JSONException, SQLException {
    
        Statement stmt = connection.createStatement();
        String createTable = "CREATE TABLE NAME (id INTEGER PRIMARY KEY, firstName VARCHAR(255), " + 
                "lastName VARCHAR(255))"; 
        stmt.executeUpdate(createTable);
        stmt.executeUpdate("INSERT INTO name VALUES (1, 'Joe', 'Jackson')");
        stmt.executeUpdate("INSERT INTO NAME VALUES (2, 'Roger', 'Waters')");

        String sqlStatement = "SELECT FIRSTNAME, LASTNAME FROM NAME WHERE ID=:#id";
        SqlStatementParser parser = new SqlStatementParser(connection, schema, sqlStatement);
        SqlStatementMetaData info = parser.parse();
 
        List<SqlParam> paramList = DatabaseMetaDataHelper.getOutputColumnInfo(connection, info.getDefaultedSqlStatement());
        Assert.assertEquals("VARCHAR", paramList.get(0).getJdbcType().getName());
        Assert.assertEquals("VARCHAR", paramList.get(1).getJdbcType().getName());
    }

    @Test
    public void parseInsertAllColumns() throws SQLException {
        
        Statement stmt = connection.createStatement();
        String createTable = "CREATE TABLE NAME2 (id INTEGER PRIMARY KEY, firstName VARCHAR(255), " + 
                "lastName VARCHAR(255))"; 
        stmt.executeUpdate(createTable);
        stmt.executeUpdate("INSERT INTO NAME2 VALUES (1, 'Joe', 'Jackson')");
        stmt.executeUpdate("INSERT INTO NAME2 VALUES (2, 'Roger', 'Waters')");

        String sqlStatement = "INSERT INTO NAME2 VALUES (:#id, :#first, :#last)";
        SqlStatementParser parser = new SqlStatementParser(connection, schema, sqlStatement);
        SqlStatementMetaData info = parser.parse();

        List<SqlParam> paramList = DatabaseMetaDataHelper.getJDBCInfoByColumnOrder(
                connection.getMetaData(), 
                new DBInfo(null, null, "NAME2"),
                info.getInParams());
        Assert.assertEquals("INTEGER", paramList.get(0).getJdbcType().getName());
        Assert.assertEquals("VARCHAR", paramList.get(1).getJdbcType().getName());
        Assert.assertEquals("VARCHAR", paramList.get(2).getJdbcType().getName());

    }

    @Test
    public void parseInsertWithSpecifiedColumnNames() throws SQLException {
        
        Statement stmt = connection.createStatement();
        String createTable = "CREATE TABLE NAME3 (id INTEGER PRIMARY KEY, firstName VARCHAR(255), " + 
                "lastName VARCHAR(255))"; 
        stmt.executeUpdate(createTable);
        stmt.executeUpdate("INSERT INTO NAME3 (ID, FIRSTNAME, LASTNAME) VALUES (1, 'Joe', 'Jackson')");
        stmt.executeUpdate("INSERT INTO NAME3 (ID, FIRSTNAME, LASTNAME) VALUES (2, 'Roger', 'Waters')");

        String sqlStatement = "INSERT INTO NAME3 (ID, FIRSTNAME, LASTNAME) VALUES (:#id, :#first, :#last)";
        SqlStatementParser parser = new SqlStatementParser(connection, schema, sqlStatement);
        SqlStatementMetaData info = parser.parse();

        List<SqlParam> paramList = DatabaseMetaDataHelper.getJDBCInfoByColumnNames(
                connection.getMetaData(), 
                new DBInfo(null, null, "NAME3"),
                info.getInParams());
        Assert.assertEquals("INTEGER", paramList.get(0).getJdbcType().getName());
        Assert.assertEquals("VARCHAR", paramList.get(1).getJdbcType().getName());
        Assert.assertEquals("VARCHAR", paramList.get(2).getJdbcType().getName());

    }
}
