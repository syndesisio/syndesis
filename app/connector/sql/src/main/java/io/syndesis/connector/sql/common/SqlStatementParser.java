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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlStatementParser {

    /*
     * C - INSERT INTO NAME VALUES (:id, :firstname, :lastname)
     * R - SELECT FIRSTNAME, LASTNAME FROM NAME WHERE ID=:id
     * U - UPDATE NAME SET FIRSTNAME=:firstname WHERE ID=:id
     * D - DELETE FROM NAME WHERE ID=:id
     *
     * DEMO_ADD(INTEGER ${body[A]}
     *
     * validate no "AS"
     * input params
     * output params
     * table name
     */
    private final Connection connection;
    private String schema;
    private DbMetaDataHelper dbHelper;
    private final SqlStatementMetaData statementInfo;
    private List<String> sqlArray = new ArrayList<>();
    private final List<String> sqlArrayUpperCase = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlStatementParser.class);

    public SqlStatementParser(Connection connection, String sql) throws SQLException {
        this(connection, null, sql);
    }

    public SqlStatementParser(Connection connection, String schema, String sql) throws SQLException {
        super();
        statementInfo = new SqlStatementMetaData(sql.trim(), schema);
        this.connection = connection;
        dbHelper = new DbMetaDataHelper(connection);
        this.schema = getSchema(schema);
    }

    public SqlStatementMetaData parseSelectOnly() throws SQLException {

        statementInfo.setTablesInSchema(dbHelper.fetchTables(null, schema, null));
        sqlArray = splitSqlStatement(statementInfo.getSqlStatement());
        for (String word : sqlArray) {
            sqlArrayUpperCase.add(word.toUpperCase(Locale.US));
        }

        if ("SELECT".equals(sqlArrayUpperCase.get(0))) {
            parseSelect();
            if (! statementInfo.getInParams().isEmpty()) {
                throw new SQLException("Your statement is invalid and cannot contain input parameters");
            }
        } else {
            throw new SQLException("Your statement is invalid and should start with SELECT");
        }
        return statementInfo;
    }

    public SqlStatementMetaData parse() throws SQLException {

        statementInfo.setTablesInSchema(dbHelper.fetchTables(null, schema, null));
        sqlArray = splitSqlStatement(statementInfo.getSqlStatement());
        for (String word : sqlArray) {
            sqlArrayUpperCase.add(word.toUpperCase(Locale.US));
        }

        switch (sqlArrayUpperCase.get(0)) {
            case "INSERT":
                parseInsert();
                break;
            case "UPDATE":
                parseUpdate();
                break;
            case "DELETE":
                parseDelete();
                break;
            case "SELECT":
                parseSelect();
                break;
            default:
                throw new SQLException("Your statement is invalid and should start with INSERT, UPDATE, SELECT or DELETE");
        }
        return statementInfo;
    }

    private String getSchema(String userSchema) {
        //if user set, then use that
        if (userSchema != null) {
            return userSchema;
        }
        try {
            //try grabbing from the connection, not all drivers support this
            return connection.getSchema();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(),e);
        } catch (AbstractMethodError e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage());
            }
        }
        try {
            //finally try setting reasonable default
            DbMetaDataHelper dbHelper = new DbMetaDataHelper(connection);
            return dbHelper.getDefaultSchema(connection.getMetaData().getUserName());
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(),e);
        }
        return null;
    }

    private void parseInsert() throws SQLException {
        statementInfo.setStatementType(StatementType.INSERT);
        String tableNameInsert = statementInfo.addTable(sqlArrayUpperCase.get(2));
        if (! statementInfo.getTablesInSchema().contains(tableNameInsert)) {
            throw new SQLException(String.format("Table '%s' does not exist", tableNameInsert));
        }
        if (statementInfo.hasInputParams()) {
            List<SqlParam> inputParams = findInsertParams(tableNameInsert);
            if (inputParams.get(0).getColumn() != null) {
                statementInfo.setInParams(
                        dbHelper.getJDBCInfoByColumnNames(
                                null, schema, tableNameInsert, inputParams));
            } else {
                statementInfo.setInParams(
                        dbHelper.getJDBCInfoByColumnOrder(
                                null, schema, tableNameInsert, inputParams));
            }
        }
        List<SqlParam> autoIncrementParamList = dbHelper.getAutoIncrementColumnList(
                null, schema, tableNameInsert);
        if (! autoIncrementParamList.isEmpty()) {
            statementInfo.setOutParams(autoIncrementParamList);
            //SQL only supports one auto increment column
            statementInfo.setAutoIncrementColumnName(autoIncrementParamList.get(0).getName());
        }
    }

    private void parseUpdate() throws SQLException  {
        statementInfo.setStatementType(StatementType.UPDATE);
        String tableNameUpdate = statementInfo.addTable(sqlArrayUpperCase.get(1));
        if (! statementInfo.getTablesInSchema().contains(tableNameUpdate)) {
            throw new SQLException(String.format("Table '%s' does not exist", tableNameUpdate));
        }
        if (statementInfo.hasInputParams()) {
            List<SqlParam> inputParams = findInputParams(Collections.emptyList());
            statementInfo.setInParams(
                    dbHelper.getJDBCInfoByColumnNames(
                            null, schema, tableNameUpdate, inputParams));
        }
    }

    private void parseDelete() throws SQLException  {
        statementInfo.setStatementType(StatementType.DELETE);
        String tableNameDelete = statementInfo.addTable(sqlArrayUpperCase.get(2));
        if (! statementInfo.getTablesInSchema().contains(tableNameDelete)) {
            throw new SQLException(String.format("Table '%s' does not exist", tableNameDelete));
        }
        if (statementInfo.hasInputParams()) {
            List<SqlParam> inputParams = findInputParams(Collections.emptyList());
            statementInfo.setInParams(
                    dbHelper.getJDBCInfoByColumnNames(
                            null, schema, tableNameDelete, inputParams));
        }
    }

    private void parseSelect() throws SQLException  {
        statementInfo.setStatementType(StatementType.SELECT);
        List<String> tableNamesSelect = findTablesInSelectStatement();
        if (! tableNamesSelect.isEmpty()) {
            for (String tableNameSelect : tableNamesSelect) {
                if (! statementInfo.getTablesInSchema().contains(tableNameSelect)) {
                    throw new SQLException(String.format("Table '%s' does not exist", tableNameSelect));
                }
            }
        }
        if (statementInfo.hasInputParams()) {
            List<SqlParam> inputParams = findInputParams(Collections.emptyList());
            statementInfo.setTableNames(findTablesInSelectStatement());
            statementInfo.setInParams(
                    dbHelper.getJDBCInfoByColumnNames(
                            null, schema, statementInfo.getTableNames().get(0), inputParams));
        }
        statementInfo.setOutParams(dbHelper.getOutputColumnInfo(statementInfo.getDefaultedSqlStatement()));
    }

    List<String> splitSqlStatement(String sql) {
        List<String> sqlArray = new ArrayList<>();
        String[] segments = sql.split("!=|=|<=|>=|<|>|,|\\s|\\(|\\)", -1);
        for (String segment : segments) {
            if (!"".equals(segment)) {
                sqlArray.add(segment);
            }
        }
        return sqlArray;
    }

    /**
     * INSERT INTO table_name (column1, column2, column3, ...)
     * VALUES (value1, value2, value3, ...);
     *
     * INSERT INTO table_name
     * VALUES (value1, value2, value3, ...);
     * @param tableName
     * @return
     */
    List<SqlParam> findInsertParams(String tableName) {
        boolean isColumnName = false;
        List<String> columnNames = new ArrayList<>();
        for (String word: sqlArrayUpperCase) {
            if ("VALUES".equals(word)) {
                isColumnName = false;
            }
            if (isColumnName) {
                columnNames.add(word);
            }
            if (tableName.equals(word)) {
                isColumnName = true; //in the next iteration
            }
        }
        int v = sqlArrayUpperCase.indexOf("VALUES") + 1;
        List<SqlParam> params = Collections.emptyList();
        if (!columnNames.isEmpty()) {
            List<String> values = sqlArray.subList(v, v + columnNames.size() );
            params = findInputParams(values);
            int paramCounter = 0;
            for (int i=0; i<columnNames.size(); i++) {
                if (values.get(i).startsWith(":#")) {
                    params.get(paramCounter++).setColumn(columnNames.get(i).toUpperCase(Locale.US));
                }
            }
        } else {
            List<String> values = sqlArray.subList(v, sqlArray.size());
            params = findInputParams(values);
        }
        return params;
    }

    List<SqlParam> findInputParams(List<String> values) {
        List<SqlParam> params = new ArrayList<>();
        int i=0;
        for (String word: sqlArray) {
            if (word.startsWith(":#")) {
                SqlParam param = new SqlParam(word.substring(2));
                String column = sqlArray.get(i-1);
                if ("LIKE".equalsIgnoreCase(column)) {
                    column = sqlArray.get(i-2);
                }
                if ("BETWEEN".equalsIgnoreCase(column)) {
                    column = sqlArray.get(i-2);
                }
                if ("AND".equalsIgnoreCase(column)) {
                    column = sqlArray.get(i-4);
                }
                if (column.startsWith(":#") || "VALUES".equalsIgnoreCase(column) || values.contains(column)) {
                    param.setColumnPos(values.indexOf(word));
                } else {
                    param.setColumn(column.toUpperCase(Locale.US));
                }
                params.add(param);
            }
            i++;
        }
        return params;
    }

    List<String> findTablesInSelectStatement() throws SQLException {
        boolean isTable = false;
        List<String> tables = new ArrayList<>();
        for (String word: sqlArrayUpperCase) {
            if (! statementInfo.getTablesInSchema().contains(word)) {
                if (isTable && tables.isEmpty()) {
                    throw new SQLException(String.format("Table '%s' does not exist in schema '%s'", word, schema));
                }
                isTable = false;
            }
            if (isTable) {
                tables.add(word);
            }
            if ("FROM".equals(word)) {
                isTable = true; //in the next iteration
            }

        }
        return tables;
    }


}
