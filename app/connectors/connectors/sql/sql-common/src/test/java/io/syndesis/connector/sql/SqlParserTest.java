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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class SqlParserTest {

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
        Statement stmt = connection.createStatement();
        String createTable = "CREATE TABLE NAME0 (id INTEGER PRIMARY KEY, firstName VARCHAR(255), " + 
                             "lastName VARCHAR(255))"; 
        stmt.executeUpdate(createTable);
    }

    @AfterClass
    public static void afterClass() throws SQLException {
        sqlCommon.closeConnection(connection);
    }
    
    @Test
    public void parseSelect() throws SQLException {
        SqlStatementParser parser = new SqlStatementParser(connection, schema, "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID=:#id");
        SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(1, info.getInParams().size());
        Assert.assertEquals("ID", info.getInParams().get(0).getName());
        Assert.assertEquals("ID", info.getInParams().get(0).getColumn());
    }

    @Test
    public void parseUpdate() throws SQLException {
        SqlStatementParser parser = new SqlStatementParser(connection, schema, "UPDATE NAME0 SET FIRSTNAME=:#first WHERE ID=:#id");
        SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(2, info.getInParams().size());
        Assert.assertEquals("FIRST", info.getInParams().get(0).getName());
        Assert.assertEquals("FIRSTNAME", info.getInParams().get(0).getColumn());
        Assert.assertEquals("ID", info.getInParams().get(1).getName());
        Assert.assertEquals("ID", info.getInParams().get(1).getColumn());
    }

    @Test
    public void parseDelete() throws SQLException {
        SqlStatementParser parser = new SqlStatementParser(connection, schema, "DELETE FROM NAME0 WHERE ID=:#id");
        SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(1, info.getInParams().size());
        Assert.assertEquals("ID", info.getInParams().get(0).getName());
        Assert.assertEquals("ID", info.getInParams().get(0).getColumn());
        Assert.assertEquals(Integer.class, info.getInParams().get(0).getTypeValue().getClazz());
        Assert.assertFalse(info.getDefaultedSqlStatement().contains(":"));
    }

    @Test
    public void parseInsertIntoAllColumnsOfTheTable() throws SQLException {
        SqlStatementParser parser = new SqlStatementParser(connection, schema, "INSERT INTO NAME0 VALUES (:#id, :#firstname, :#lastname)");
        SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(3, info.getInParams().size());
        Assert.assertEquals("ID", info.getInParams().get(0).getName());
        Assert.assertEquals(0, info.getInParams().get(0).getColumnPos());
        Assert.assertEquals("FIRSTNAME", info.getInParams().get(1).getName());
        Assert.assertEquals(1, info.getInParams().get(1).getColumnPos());
        Assert.assertEquals("LASTNAME", info.getInParams().get(2).getName());
        Assert.assertEquals(2, info.getInParams().get(2).getColumnPos());
        Assert.assertEquals(String.class, info.getInParams().get(2).getTypeValue().getClazz());
    }

    @Test
    public void parseInsertWithSpecifiedColumnNames() throws SQLException {
        SqlStatementParser parser = new SqlStatementParser(connection, schema, "INSERT INTO NAME0 (FIRSTNAME, LASTNAME) VALUES (:#firstname, :#lastname)");
        SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(2, info.getInParams().size());
        Assert.assertEquals("FIRSTNAME", info.getInParams().get(0).getName());
        Assert.assertEquals(0, info.getInParams().get(0).getColumnPos());
        Assert.assertEquals("FIRSTNAME", info.getInParams().get(0).getColumn());
        Assert.assertEquals("LASTNAME", info.getInParams().get(1).getName());
        Assert.assertEquals(1, info.getInParams().get(1).getColumnPos());
        Assert.assertEquals("LASTNAME", info.getInParams().get(1).getColumn());
    }
}
