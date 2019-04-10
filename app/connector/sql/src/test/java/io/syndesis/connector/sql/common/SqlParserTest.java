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

import java.math.BigDecimal;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

public class SqlParserTest {

    @ClassRule
    public static SqlConnectionRule db = new SqlConnectionRule();

    @Test
    public void parseUpdateWithConstant() throws SQLException {
        SqlStatementParser parser = new SqlStatementParser(db.connection,
                "UPDATE NAME0 SET FIRSTNAME=:#first, LASTNAME='Jenssen' WHERE ID=:#id");
        SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(2, info.getInParams().size());
        Assert.assertEquals("first", info.getInParams().get(0).getName());
        Assert.assertEquals("FIRSTNAME", info.getInParams().get(0).getColumn());
        Assert.assertEquals(String.class, info.getInParams().get(0).getTypeValue().getClazz());
        Assert.assertEquals("id", info.getInParams().get(1).getName());
        Assert.assertEquals("ID", info.getInParams().get(1).getColumn());
        if (db.connection.getMetaData().getDatabaseProductName().equalsIgnoreCase(DbEnum.ORACLE.name())) {
            Assert.assertEquals(BigDecimal.class, info.getInParams().get(1).getTypeValue().getClazz());
        } else {
            Assert.assertEquals(Integer.class, info.getInParams().get(1).getTypeValue().getClazz());
        }
    }

    @Test
    public void parseDelete() throws SQLException {
        final SqlStatementParser parser = new SqlStatementParser(db.connection, "DELETE FROM NAME0 WHERE ID=:#id");
        final SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(1, info.getInParams().size());
        Assert.assertEquals("id", info.getInParams().get(0).getName());
        Assert.assertEquals("ID", info.getInParams().get(0).getColumn());
        if (db.connection.getMetaData().getDatabaseProductName().equalsIgnoreCase(DbEnum.ORACLE.name())) {
            Assert.assertEquals(BigDecimal.class, info.getInParams().get(0).getTypeValue().getClazz());
        } else {
            Assert.assertEquals(Integer.class, info.getInParams().get(0).getTypeValue().getClazz());
        }
        Assert.assertFalse(info.getDefaultedSqlStatement().contains(":"));
    }

    @Test
    public void parseInsertIntoAllColumnsOfTheTable() throws SQLException {
        final SqlStatementParser parser = new SqlStatementParser(db.connection,
            "INSERT INTO NAME0 VALUES (:#id, :#firstname, :#lastname)");
        final SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(3, info.getInParams().size());
        Assert.assertEquals("id", info.getInParams().get(0).getName());
        Assert.assertEquals(0, info.getInParams().get(0).getColumnPos());
        Assert.assertEquals("firstname", info.getInParams().get(1).getName());
        Assert.assertEquals(1, info.getInParams().get(1).getColumnPos());
        Assert.assertEquals("lastname", info.getInParams().get(2).getName());
        Assert.assertEquals(2, info.getInParams().get(2).getColumnPos());
        Assert.assertEquals(String.class, info.getInParams().get(2).getTypeValue().getClazz());
    }

    @Test
    public void parseInsertWithConstant() throws SQLException {
        SqlStatementParser parser = new SqlStatementParser(db.connection,
                "INSERT INTO NAME0 VALUES (29, :#firstname, :#lastname)");
        SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(2, info.getInParams().size());
        Assert.assertEquals(1, info.getInParams().get(0).getColumnPos());
        Assert.assertEquals("firstname", info.getInParams().get(0).getName());
        Assert.assertEquals(2, info.getInParams().get(1).getColumnPos());
        Assert.assertEquals("lastname", info.getInParams().get(1).getName());
        Assert.assertEquals(String.class, info.getInParams().get(1).getTypeValue().getClazz());
    }

    @Test
    public void parseInsertWithConstantLowerCase() throws SQLException {
        SqlStatementParser parser = new SqlStatementParser(db.connection,
                "INSERT INTO NAME0 values (29, :#firstname, :#lastname)");
        SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(2, info.getInParams().size());
        Assert.assertEquals(1, info.getInParams().get(0).getColumnPos());
        Assert.assertEquals("firstname", info.getInParams().get(0).getName());
        Assert.assertEquals(2, info.getInParams().get(1).getColumnPos());
        Assert.assertEquals("lastname", info.getInParams().get(1).getName());
        Assert.assertEquals(String.class, info.getInParams().get(1).getTypeValue().getClazz());
    }

    @Test
    public void parseInsertWithSpecifiedColumnNames() throws SQLException {
        final SqlStatementParser parser = new SqlStatementParser(db.connection,
            "INSERT INTO NAME0 (FIRSTNAME, LASTNAME) VALUES (:#firstname, :#lastname)");
        final SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(2, info.getInParams().size());
        Assert.assertEquals("firstname", info.getInParams().get(0).getName());
        Assert.assertEquals(0, info.getInParams().get(0).getColumnPos());
        Assert.assertEquals("FIRSTNAME", info.getInParams().get(0).getColumn());
        Assert.assertEquals("lastname", info.getInParams().get(1).getName());
        Assert.assertEquals(1, info.getInParams().get(1).getColumnPos());
        Assert.assertEquals("LASTNAME", info.getInParams().get(1).getColumn());
    }

    @Test
    public void parseSelect() throws SQLException {
        final SqlStatementParser parser = new SqlStatementParser(db.connection,
            "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID=:#id");
        final SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(1, info.getInParams().size());
        Assert.assertEquals("id", info.getInParams().get(0).getName());
        Assert.assertEquals("ID", info.getInParams().get(0).getColumn());
    }

    @Test
    public void parseSelectMultiValuePredicate() throws SQLException {
        final SqlStatementParser parser = new SqlStatementParser(db.connection,
            "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID=:#id AND FIRSTNAME LIKE :#name");
        final SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(2, info.getInParams().size());
        Assert.assertEquals("id", info.getInParams().get(0).getName());
        Assert.assertEquals("ID", info.getInParams().get(0).getColumn());
        Assert.assertEquals("name", info.getInParams().get(1).getName());
        Assert.assertEquals("FIRSTNAME", info.getInParams().get(1).getColumn());
    }

    @Test
    public void parseSelectWithNotEquals() throws SQLException {
        final SqlStatementParser parser = new SqlStatementParser(db.connection,
                "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID!=:#id");
        final SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(1, info.getInParams().size());
        Assert.assertEquals("id", info.getInParams().get(0).getName());
        Assert.assertEquals("ID", info.getInParams().get(0).getColumn());
    }

    @Test
    public void parseSelectWithGreaterThan() throws SQLException {
        final SqlStatementParser parser = new SqlStatementParser(db.connection,
                "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID>:#id");
        final SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(1, info.getInParams().size());
        Assert.assertEquals("id", info.getInParams().get(0).getName());
        Assert.assertEquals("ID", info.getInParams().get(0).getColumn());
    }

    @Test
    public void parseSelectWithGreaterThanEquals() throws SQLException {
        final SqlStatementParser parser = new SqlStatementParser(db.connection,
                "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID>=:#id");
        final SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(1, info.getInParams().size());
        Assert.assertEquals("id", info.getInParams().get(0).getName());
        Assert.assertEquals("ID", info.getInParams().get(0).getColumn());
    }

    @Test
    public void parseSelectWithLowerThan() throws SQLException {
        final SqlStatementParser parser = new SqlStatementParser(db.connection,
                "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID<:#id");
        final SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(1, info.getInParams().size());
        Assert.assertEquals("id", info.getInParams().get(0).getName());
        Assert.assertEquals("ID", info.getInParams().get(0).getColumn());
    }

    @Test
    public void parseSelectWithLowerThanEquals() throws SQLException {
        final SqlStatementParser parser = new SqlStatementParser(db.connection,
                "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID<:#id");
        final SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(1, info.getInParams().size());
        Assert.assertEquals("id", info.getInParams().get(0).getName());
        Assert.assertEquals("ID", info.getInParams().get(0).getColumn());
    }

    @Test
    public void parseSelectWithInBetween() throws SQLException {
        final SqlStatementParser parser = new SqlStatementParser(db.connection,
                "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID BETWEEN :#from AND :#to");
        final SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(2, info.getInParams().size());
        Assert.assertEquals("from", info.getInParams().get(0).getName());
        Assert.assertEquals("ID", info.getInParams().get(0).getColumn());
        Assert.assertEquals("to", info.getInParams().get(1).getName());
        Assert.assertEquals("ID", info.getInParams().get(1).getColumn());
    }

    @Test
    public void parseSelectWithJoin() throws SQLException {
        final SqlStatementParser parser = new SqlStatementParser(db.connection,
            "SELECT FIRSTNAME, NAME0.LASTNAME, ADDRESS FROM NAME0, ADDRESS0 WHERE NAME0.LASTNAME=ADDRESS0.LASTNAME AND FIRSTNAME LIKE :#first");
        final SqlStatementMetaData info = parser.parse();
        Assert.assertTrue(info.getTableNames().contains("NAME0"));
        Assert.assertTrue(info.getTableNames().contains("ADDRESS0"));
        Assert.assertEquals(1, info.getInParams().size());
        Assert.assertEquals("FIRSTNAME", info.getInParams().get(0).getColumn());
        Assert.assertEquals("first", info.getInParams().get(0).getName());
    }

    @Test
    public void parseSelectWithLike() throws SQLException {
        final SqlStatementParser parser = new SqlStatementParser(db.connection,
            "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE FIRSTNAME LIKE :#first");
        final SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(1, info.getInParams().size());
        Assert.assertEquals("first", info.getInParams().get(0).getName());
        Assert.assertEquals("FIRSTNAME", info.getInParams().get(0).getColumn());
    }

    @Test
    public void parseUpdate() throws SQLException {
        final SqlStatementParser parser = new SqlStatementParser(db.connection,
            "UPDATE NAME0 SET FIRSTNAME=:#first WHERE ID=:#id");
        final SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(2, info.getInParams().size());
        Assert.assertEquals("first", info.getInParams().get(0).getName());
        Assert.assertEquals("FIRSTNAME", info.getInParams().get(0).getColumn());
        Assert.assertEquals("id", info.getInParams().get(1).getName());
        Assert.assertEquals("ID", info.getInParams().get(1).getColumn());
    }

    @Test
    public void parseInsertWithConstantAndColumnNames() throws SQLException {
        SqlStatementParser parser = new SqlStatementParser(db.connection,
                "INSERT INTO NAME0 (FIRSTNAME, LASTNAME) VALUES ('Kurt', :#lastname)");
        SqlStatementMetaData info = parser.parse();
        Assert.assertEquals("NAME0", info.getTableNames().get(0));
        Assert.assertEquals(1, info.getInParams().size());
        Assert.assertEquals("lastname", info.getInParams().get(0).getName());
        Assert.assertEquals(1, info.getInParams().get(0).getColumnPos());
        Assert.assertEquals("LASTNAME", info.getInParams().get(0).getColumn());
    }

    @Test(expected=SQLException.class)
    public void parseSelectFromNoneExistingTable() throws SQLException {
        final SqlStatementParser parser = new SqlStatementParser(db.connection,
            "SELECT FIRSTNAME, LASTNAME FROM NAME-NOTEXIST WHERE FIRSTNAME LIKE :#first");
        parser.parse();
    }
}
