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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import io.syndesis.connector.sql.common.SqlTest.Setup;
import io.syndesis.connector.sql.common.SqlTest.Teardown;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SqlTest.class)
@Setup({
    "CREATE TABLE NAME0 (id INTEGER PRIMARY KEY, firstName VARCHAR(255), lastName VARCHAR(255))",
    "CREATE TABLE ADDRESS0 (id INTEGER PRIMARY KEY, Address VARCHAR(255), lastName VARCHAR(255))"
})
@Teardown({
    "DROP TABLE NAME0",
    "DROP TABLE ADDRESS0"
})
public class SqlParserTest {

    @Test
    public void parseUpdateWithConstant(final Connection con) throws SQLException {
        try (Connection connection = con) {
            SqlStatementParser parser = new SqlStatementParser(connection,
                "UPDATE NAME0 SET FIRSTNAME=:#first, LASTNAME='Jenssen' WHERE ID=:#id");
            SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(2, info.getInParams().size());
            Assertions.assertEquals("first", info.getInParams().get(0).getName());
            Assertions.assertEquals("FIRSTNAME", info.getInParams().get(0).getColumn());
            Assertions.assertEquals(String.class, info.getInParams().get(0).getTypeValue().getClazz());
            Assertions.assertEquals("id", info.getInParams().get(1).getName());
            Assertions.assertEquals("ID", info.getInParams().get(1).getColumn());
            if (connection.getMetaData().getDatabaseProductName().equalsIgnoreCase(DbEnum.ORACLE.name())) {
                Assertions.assertEquals(BigDecimal.class, info.getInParams().get(1).getTypeValue().getClazz());
            } else {
                Assertions.assertEquals(Integer.class, info.getInParams().get(1).getTypeValue().getClazz());
            }
        }
    }

    @Test
    public void parseDelete(final Connection con) throws SQLException {
        try (Connection connection = con) {
            final SqlStatementParser parser = new SqlStatementParser(connection, "DELETE FROM NAME0 WHERE ID=:#id");
            final SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(1, info.getInParams().size());
            Assertions.assertEquals("id", info.getInParams().get(0).getName());
            Assertions.assertEquals("ID", info.getInParams().get(0).getColumn());
            if (connection.getMetaData().getDatabaseProductName().equalsIgnoreCase(DbEnum.ORACLE.name())) {
                Assertions.assertEquals(BigDecimal.class, info.getInParams().get(0).getTypeValue().getClazz());
            } else {
                Assertions.assertEquals(Integer.class, info.getInParams().get(0).getTypeValue().getClazz());
            }
            Assertions.assertFalse(info.getDefaultedSqlStatement().contains(":"));
        }
    }

    @Test
    public void parseInsertIntoAllColumnsOfTheTable(final Connection con) throws SQLException {
        try (Connection connection = con) {
            final SqlStatementParser parser = new SqlStatementParser(connection,
                "INSERT INTO NAME0 VALUES (:#id, :#firstname, :#lastname)");
            final SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(3, info.getInParams().size());
            Assertions.assertEquals("id", info.getInParams().get(0).getName());
            Assertions.assertEquals(0, info.getInParams().get(0).getColumnPos());
            Assertions.assertEquals("firstname", info.getInParams().get(1).getName());
            Assertions.assertEquals(1, info.getInParams().get(1).getColumnPos());
            Assertions.assertEquals("lastname", info.getInParams().get(2).getName());
            Assertions.assertEquals(2, info.getInParams().get(2).getColumnPos());
            Assertions.assertEquals(String.class, info.getInParams().get(2).getTypeValue().getClazz());
        }
    }

    @Test
    public void parseInsertWithConstant(final Connection con) throws SQLException {
        try (Connection connection = con) {
            SqlStatementParser parser = new SqlStatementParser(connection,
                "INSERT INTO NAME0 VALUES (29, :#firstname, :#lastname)");
            SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(2, info.getInParams().size());
            Assertions.assertEquals(1, info.getInParams().get(0).getColumnPos());
            Assertions.assertEquals("firstname", info.getInParams().get(0).getName());
            Assertions.assertEquals(2, info.getInParams().get(1).getColumnPos());
            Assertions.assertEquals("lastname", info.getInParams().get(1).getName());
            Assertions.assertEquals(String.class, info.getInParams().get(1).getTypeValue().getClazz());
        }
    }

    @Test
    public void parseInsertWithConstantLowerCase(final Connection con) throws SQLException {
        try (Connection connection = con) {
            SqlStatementParser parser = new SqlStatementParser(connection,
                "INSERT INTO NAME0 values (29, :#firstname, :#lastname)");
            SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(2, info.getInParams().size());
            Assertions.assertEquals(1, info.getInParams().get(0).getColumnPos());
            Assertions.assertEquals("firstname", info.getInParams().get(0).getName());
            Assertions.assertEquals(2, info.getInParams().get(1).getColumnPos());
            Assertions.assertEquals("lastname", info.getInParams().get(1).getName());
            Assertions.assertEquals(String.class, info.getInParams().get(1).getTypeValue().getClazz());
        }
    }

    @Test
    public void parseInsertWithSpecifiedColumnNames(final Connection con) throws SQLException {
        try (Connection connection = con) {
            final SqlStatementParser parser = new SqlStatementParser(connection,
                "INSERT INTO NAME0 (FIRSTNAME, LASTNAME) VALUES (:#firstname, :#lastname)");
            final SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(2, info.getInParams().size());
            Assertions.assertEquals("firstname", info.getInParams().get(0).getName());
            Assertions.assertEquals(0, info.getInParams().get(0).getColumnPos());
            Assertions.assertEquals("FIRSTNAME", info.getInParams().get(0).getColumn());
            Assertions.assertEquals("lastname", info.getInParams().get(1).getName());
            Assertions.assertEquals(1, info.getInParams().get(1).getColumnPos());
            Assertions.assertEquals("LASTNAME", info.getInParams().get(1).getColumn());
        }
    }

    @Test
    public void parseSelect(final Connection con) throws SQLException {
        try (Connection connection = con) {
            final SqlStatementParser parser = new SqlStatementParser(connection,
                "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID=:#id");
            final SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(1, info.getInParams().size());
            Assertions.assertEquals("id", info.getInParams().get(0).getName());
            Assertions.assertEquals("ID", info.getInParams().get(0).getColumn());
        }
    }

    @Test
    public void parseSelectMultiValuePredicate(final Connection con) throws SQLException {
        try (Connection connection = con) {
            final SqlStatementParser parser = new SqlStatementParser(connection,
                "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID=:#id AND FIRSTNAME LIKE :#name");
            final SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(2, info.getInParams().size());
            Assertions.assertEquals("id", info.getInParams().get(0).getName());
            Assertions.assertEquals("ID", info.getInParams().get(0).getColumn());
            Assertions.assertEquals("name", info.getInParams().get(1).getName());
            Assertions.assertEquals("FIRSTNAME", info.getInParams().get(1).getColumn());
        }
    }

    @Test
    public void parseSelectWithNotEquals(final Connection con) throws SQLException {
        try (Connection connection = con) {
            final SqlStatementParser parser = new SqlStatementParser(connection,
                "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID!=:#id");
            final SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(1, info.getInParams().size());
            Assertions.assertEquals("id", info.getInParams().get(0).getName());
            Assertions.assertEquals("ID", info.getInParams().get(0).getColumn());
        }
    }

    @Test
    public void parseSelectWithGreaterThan(final Connection con) throws SQLException {
        try (Connection connection = con) {
            final SqlStatementParser parser = new SqlStatementParser(connection,
                "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID>:#id");
            final SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(1, info.getInParams().size());
            Assertions.assertEquals("id", info.getInParams().get(0).getName());
            Assertions.assertEquals("ID", info.getInParams().get(0).getColumn());
        }
    }

    @Test
    public void parseSelectWithGreaterThanEquals(final Connection con) throws SQLException {
        try (Connection connection = con) {
            final SqlStatementParser parser = new SqlStatementParser(connection,
                "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID>=:#id");
            final SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(1, info.getInParams().size());
            Assertions.assertEquals("id", info.getInParams().get(0).getName());
            Assertions.assertEquals("ID", info.getInParams().get(0).getColumn());
        }
    }

    @Test
    public void parseSelectWithLowerThan(final Connection con) throws SQLException {
        try (Connection connection = con) {
            final SqlStatementParser parser = new SqlStatementParser(connection,
                "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID<:#id");
            final SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(1, info.getInParams().size());
            Assertions.assertEquals("id", info.getInParams().get(0).getName());
            Assertions.assertEquals("ID", info.getInParams().get(0).getColumn());
        }
    }

    @Test
    public void parseSelectWithLowerThanEquals(final Connection con) throws SQLException {
        try (Connection connection = con) {
            final SqlStatementParser parser = new SqlStatementParser(connection,
                "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID<:#id");
            final SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(1, info.getInParams().size());
            Assertions.assertEquals("id", info.getInParams().get(0).getName());
            Assertions.assertEquals("ID", info.getInParams().get(0).getColumn());
        }
    }

    @Test
    public void parseSelectWithInBetween(final Connection con) throws SQLException {
        try (Connection connection = con) {
            final SqlStatementParser parser = new SqlStatementParser(connection,
                "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE ID BETWEEN :#from AND :#to");
            final SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(2, info.getInParams().size());
            Assertions.assertEquals("from", info.getInParams().get(0).getName());
            Assertions.assertEquals("ID", info.getInParams().get(0).getColumn());
            Assertions.assertEquals("to", info.getInParams().get(1).getName());
            Assertions.assertEquals("ID", info.getInParams().get(1).getColumn());
        }
    }

    @Test
    public void parseSelectWithIn(final Connection con) throws SQLException {
        try (Connection connection = con) {
            final SqlStatementParser parser = new SqlStatementParser(connection,
                "SELECT * FROM NAME0 WHERE LASTNAME IN (:#name, :#othername)");
            final SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(2, info.getInParams().size());
            Assertions.assertEquals("name", info.getInParams().get(0).getName());
            Assertions.assertEquals("LASTNAME", info.getInParams().get(0).getColumn());
            Assertions.assertEquals("othername", info.getInParams().get(1).getName());
            Assertions.assertEquals("LASTNAME", info.getInParams().get(1).getColumn());
        }
    }

    @Test
    public void parseSelectWithJoin(final Connection con) throws SQLException {
        try (Connection connection = con) {
            final SqlStatementParser parser = new SqlStatementParser(connection,
                "SELECT FIRSTNAME, NAME0.LASTNAME, ADDRESS FROM NAME0, ADDRESS0 WHERE NAME0.LASTNAME=ADDRESS0.LASTNAME AND FIRSTNAME LIKE :#first");
            final SqlStatementMetaData info = parser.parse();
            Assertions.assertTrue(info.getTableNames().contains("NAME0"));
            Assertions.assertTrue(info.getTableNames().contains("ADDRESS0"));
            Assertions.assertEquals(1, info.getInParams().size());
            Assertions.assertEquals("FIRSTNAME", info.getInParams().get(0).getColumn());
            Assertions.assertEquals("first", info.getInParams().get(0).getName());
        }
    }

    @Test
    public void parseSelectWithLike(final Connection con) throws SQLException {
        try (Connection connection = con) {
            final SqlStatementParser parser = new SqlStatementParser(connection,
                "SELECT FIRSTNAME, LASTNAME FROM NAME0 WHERE FIRSTNAME LIKE :#first");
            final SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(1, info.getInParams().size());
            Assertions.assertEquals("first", info.getInParams().get(0).getName());
            Assertions.assertEquals("FIRSTNAME", info.getInParams().get(0).getColumn());
        }
    }

    @Test
    public void parseUpdate(final Connection con) throws SQLException {
        try (Connection connection = con) {
            final SqlStatementParser parser = new SqlStatementParser(connection,
                "UPDATE NAME0 SET FIRSTNAME=:#first WHERE ID=:#id");
            final SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(2, info.getInParams().size());
            Assertions.assertEquals("first", info.getInParams().get(0).getName());
            Assertions.assertEquals("FIRSTNAME", info.getInParams().get(0).getColumn());
            Assertions.assertEquals("id", info.getInParams().get(1).getName());
            Assertions.assertEquals("ID", info.getInParams().get(1).getColumn());
        }
    }

    @Test
    public void parseInsertWithConstantAndColumnNames(final Connection con) throws SQLException {
        try (Connection connection = con) {
            SqlStatementParser parser = new SqlStatementParser(connection,
                "INSERT INTO NAME0 (FIRSTNAME, LASTNAME) VALUES ('Kurt', :#lastname)");
            SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));
            Assertions.assertEquals(1, info.getInParams().size());
            Assertions.assertEquals("lastname", info.getInParams().get(0).getName());
            Assertions.assertEquals(1, info.getInParams().get(0).getColumnPos());
            Assertions.assertEquals("LASTNAME", info.getInParams().get(0).getColumn());
        }
    }

    @Test
    public void parseCastExpressions(final Connection con) throws SQLException {
        try (Connection connection = con) {
            final SqlStatementParser parser = new SqlStatementParser(connection,
                "INSERT INTO NAME0 (FIRSTNAME, LASTNAME) VALUES (CAST(:#firstname AS VARCHAR), CAST(:#lastname AS VARCHAR))");

            final SqlStatementMetaData info = parser.parse();
            Assertions.assertEquals("NAME0", info.getTableNames().get(0));

            final List<SqlParam> inParams = info.getInParams();
            Assertions.assertEquals(2, inParams.size());

            final SqlParam firstParameter = inParams.get(0);
            Assertions.assertEquals("firstname", firstParameter.getName());
            Assertions.assertEquals("FIRSTNAME", firstParameter.getColumn());
            Assertions.assertEquals(String.class, firstParameter.getTypeValue().getClazz());
            Assertions.assertEquals(0, firstParameter.getColumnPos());

            final SqlParam secondParameter = inParams.get(1);
            Assertions.assertEquals("lastname", secondParameter.getName());
            Assertions.assertEquals("LASTNAME", secondParameter.getColumn());
            Assertions.assertEquals(String.class, secondParameter.getTypeValue().getClazz());
            Assertions.assertEquals(1, secondParameter.getColumnPos());
        }
    }
}
