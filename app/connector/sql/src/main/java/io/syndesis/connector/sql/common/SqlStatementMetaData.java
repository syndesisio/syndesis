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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class SqlStatementMetaData {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlStatementMetaData.class);

    private StatementType statementType;
    private List<SqlParam> inParams = new ArrayList<>();
    private List<SqlParam> outParams = new ArrayList<>();
    private List<String> tableNames = new ArrayList<>();
    private String sqlStatement;
    private String camelSqlStatement;
    private Set<String> tablesInSchema;
    private String schema;
    private String defaultedSqlStatement;
    private String autoIncrementColumnName;
    private boolean batch;

    public SqlStatementMetaData(String sqlStatement, String schema) {
        super();
        this.sqlStatement = sqlStatement;
        this.schema = schema;
    }

    public String addTable(String tableName) throws SQLException {
        String upperCaseTableName = tableName.toUpperCase(Locale.getDefault());
        if (tablesInSchema.contains(upperCaseTableName)) {
            tableNames.add(upperCaseTableName);
        } else {
            throw new SQLException(String.format("Table '%s' does not exist in schema '%s'", upperCaseTableName, schema));
        }
        return tableName;
    }

    public boolean hasInputParams() {
        return sqlStatement.contains(":#");
    }

    public int numberOfInputParams() {
        int fromIndex = 0;
        int numberOfInputParams=0;
        while (fromIndex >= 0) {
            fromIndex = sqlStatement.indexOf(':', fromIndex);
            numberOfInputParams++;
        }
        return numberOfInputParams;
    }

    public String getCamelSqlStatement() {
        if (camelSqlStatement == null) {
            camelSqlStatement = sqlStatement;
            for (SqlParam param : inParams) {
                camelSqlStatement = camelSqlStatement.replace(":" + param.getName(), ":#" + param.getName());
            }
        }
        return camelSqlStatement;
    }

    @SuppressWarnings("rawtypes")
    public String getDefaultedSqlStatement() {
        final List<Class> stringTypes = Arrays.asList(String.class, Character.class);
        if (defaultedSqlStatement == null) {
            defaultedSqlStatement = sqlStatement;
            for (SqlParam param : inParams) {
                if (stringTypes.contains(param.getTypeValue().getClazz())) {
                    defaultedSqlStatement = defaultedSqlStatement.replace(":#" +
                            param.getName(), "'" + param.getTypeValue().getSampleValue().toString() + "'");
                } else {
                    defaultedSqlStatement = defaultedSqlStatement.replace(":#" +
                            param.getName(), param.getTypeValue().getSampleValue().toString());
                }
            }
        }
        return defaultedSqlStatement;
    }

    public boolean isVerifiedBatchUpdateMode() {
        if (batch) {
            if (!hasInputParams()) {
                LOGGER.warn("Batch update mode set but no input params specified - automatically using non batch update mode");
                return false;
            }

            if (statementType == StatementType.SELECT) {
                LOGGER.warn("Batch update mode not supported on SELECT statement - automatically using non batch update mode");
                return false;
            }
        }

        return batch;
    }

}
