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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.common.SqlParam;
import io.syndesis.connector.sql.common.SqlStatementParser;
import io.syndesis.connector.sql.common.SqlTest.ConnectionInfo;
import io.syndesis.connector.sql.common.SqlTest.Setup;
import io.syndesis.connector.sql.common.SqlTest.Teardown;
import io.syndesis.connector.sql.util.SqlConnectorTestSupport;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Setup("CREATE TABLE ALLTYPES (charType CHAR, varcharType VARCHAR(255), numericType NUMERIC, decimalType DECIMAL, smallType SMALLINT, dateType DATE, timeType TIME )")
@Teardown("DROP TABLE ALLTYPES")
public class SqlConnectorInputParamTest extends SqlConnectorTestSupport {
    public SqlConnectorInputParamTest(ConnectionInfo info) {
        super(info);
    }

    private static final String STATEMENT = "INSERT INTO ALLTYPES " +
        "(charType, varcharType, numericType, decimalType, smallType) VALUES " +
        "(:#CHARVALUE, :#VARCHARVALUE, :#NUMERICVALUE, :#DECIMALVALUE, :#SMALLINTVALUE)";

    @Override
    protected List<Step> createSteps() {
        return Arrays.asList(
            newSimpleEndpointStep(
                "direct",
                builder -> builder.putConfiguredProperty("name", "start")),
            newSqlEndpointStep(
                "sql-connector",
                builder -> builder.putConfiguredProperty("query", STATEMENT)),
            newSimpleEndpointStep(
                "log",
                builder -> builder.putConfiguredProperty("loggerName", "test")));
    }

    @Test
    public void sqlConnectorTest(Connection con) throws Exception {
        try (Connection connection = con) {
            SqlStatementParser parser = new SqlStatementParser(connection, null, STATEMENT);
            parser.parse();

            Map<String, Object> values = new HashMap<>();
            values.put("CHARVALUE", SqlParam.SqlSampleValue.CHAR_VALUE);
            values.put("VARCHARVALUE", SqlParam.SqlSampleValue.STRING_VALUE);
            values.put("NUMERICVALUE", SqlParam.SqlSampleValue.DECIMAL_VALUE);
            values.put("DECIMALVALUE", SqlParam.SqlSampleValue.DECIMAL_VALUE);
            values.put("SMALLINTVALUE", SqlParam.SqlSampleValue.INTEGER_VALUE);

            String result = template().requestBody("direct:start", JSONBeanUtil.toJSONBean(values), String.class);

            Assertions.assertThat(result).isNotNull();

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SELECT * FROM ALLTYPES");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    resultSet.next();
                    Assertions.assertThat(resultSet.getString(1)).isEqualTo(SqlParam.SqlSampleValue.CHAR_VALUE.toString());
                    Assertions.assertThat(resultSet.getString(2)).isEqualTo(SqlParam.SqlSampleValue.STRING_VALUE);
                    Assertions.assertThat(resultSet.getString(3)).isEqualTo(SqlParam.SqlSampleValue.DECIMAL_VALUE.toString());
                    Assertions.assertThat(resultSet.getString(4)).isEqualTo(SqlParam.SqlSampleValue.DECIMAL_VALUE.toString());
                    Assertions.assertThat(resultSet.getString(5)).isEqualTo(SqlParam.SqlSampleValue.INTEGER_VALUE.toString());
                }
            }
        }
    }
}
