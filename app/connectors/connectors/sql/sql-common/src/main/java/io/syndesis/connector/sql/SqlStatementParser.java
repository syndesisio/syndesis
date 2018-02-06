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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private final String schema;
    private final SqlStatementMetaData statementInfo;
    private List<String> sqlArray = new ArrayList<>();

    public SqlStatementParser(Connection connection, String schema, String sql) {
        super();
        statementInfo = new SqlStatementMetaData(sql.trim());
        this.connection = connection;
        this.schema = schema;
    }

    public SqlStatementMetaData parseSelectOnly() throws SQLException {

        DatabaseMetaData meta = connection.getMetaData();
        statementInfo.setTablesInSchema(DatabaseMetaDataHelper.fetchTables(meta, null, schema, null));
        sqlArray = splitSqlStatement(statementInfo.getSqlStatement());

        if ("SELECT".equals(sqlArray.get(0).toUpperCase(Locale.US))) {
            parseSelect(meta);
            if (! statementInfo.getInParams().isEmpty()) {
                throw new SQLException("Your statement is invalid and cannot contain input parameters");
            }
        } else {
            throw new SQLException("Your statement is invalid and should start with SELECT");
        }
        return statementInfo;
    }

    public SqlStatementMetaData parse() throws SQLException {

        DatabaseMetaData meta = connection.getMetaData();
        statementInfo.setTablesInSchema(DatabaseMetaDataHelper.fetchTables(meta, null, schema, null));
        sqlArray = splitSqlStatement(statementInfo.getSqlStatement());

        switch (sqlArray.get(0).toUpperCase(Locale.US)) {
            case "INSERT":
                parseInsert(meta);
                break;
            case "UPDATE":
                parseUpdate(meta);
                break;
            case "DELETE":
                parseDelete(meta);
                break;
            case "SELECT":
                parseSelect(meta);
                break;
            default:
                throw new SQLException("Your statement is invalid and should start with INSERT, UPDATE, SELECT or DELETE");
        }
        return statementInfo;
    }

    private void parseInsert(DatabaseMetaData meta) throws SQLException {
        statementInfo.setStatementType(StatementType.INSERT);
        String tableNameInsert = statementInfo.addTable(sqlArray.get(2));
        if (statementInfo.hasInputParams()) {
            List<SqlParam> inputParams = findInsertParams(tableNameInsert);
            if (inputParams.get(0).getColumn() != null) {
                statementInfo.setInParams(
                        DatabaseMetaDataHelper.getJDBCInfoByColumnNames(
                                meta, null, schema, tableNameInsert, inputParams));
            } else {
                statementInfo.setInParams(
                        DatabaseMetaDataHelper.getJDBCInfoByColumnOrder(
                                meta, null, schema, tableNameInsert, inputParams));
            }
        }
    }

    private void parseUpdate(DatabaseMetaData meta) throws SQLException  {
        statementInfo.setStatementType(StatementType.UPDATE);
        String tableNameUpdate = statementInfo.addTable(sqlArray.get(1));
        if (statementInfo.hasInputParams()) {
            List<SqlParam> inputParams = findInputParams();
            statementInfo.setInParams(
                    DatabaseMetaDataHelper.getJDBCInfoByColumnNames(
                            meta, null, schema, tableNameUpdate, inputParams));
        }
    }

    private void parseDelete(DatabaseMetaData meta) throws SQLException  {
        statementInfo.setStatementType(StatementType.DELETE);
        String tableNameDelete = statementInfo.addTable(sqlArray.get(2));
        if (statementInfo.hasInputParams()) {
            List<SqlParam> inputParams = findInputParams();
            statementInfo.setInParams(
                    DatabaseMetaDataHelper.getJDBCInfoByColumnNames(
                            meta, null, schema, tableNameDelete, inputParams));
        }
    }

    private void parseSelect(DatabaseMetaData meta) throws SQLException  {
        statementInfo.setStatementType(StatementType.SELECT);
        if (statementInfo.hasInputParams()) {
            List<SqlParam> inputParams = findInputParams();
            statementInfo.setTableNames(findTablesInSelectStatement()); //TODO support multiple tables
            statementInfo.setInParams(
                    DatabaseMetaDataHelper.getJDBCInfoByColumnNames(
                            meta, null, schema, statementInfo.getTableNames().get(0), inputParams));
        }
        statementInfo.setOutParams(DatabaseMetaDataHelper.getOutputColumnInfo(connection, statementInfo.getDefaultedSqlStatement()));
    }

    /* default */ List<String> splitSqlStatement(String sql) {
        List<String> sqlArray = new ArrayList<>();
        String[] segments = sql.split("=|\\,|\\s|\\(|\\)");
        for (String segment : segments) {
            if (!"".equals(segment)) {
                sqlArray.add(segment);
            }
        }
        return sqlArray;
    }

    /* default */ List<SqlParam> findInsertParams(String tableName) {
        boolean isColumnName = false;
        List<String> columnNames = new ArrayList<>();
        for (String word: sqlArray) {
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
        List<SqlParam> params = findInputParams();
        if (columnNames.size() == params.size()) {
            for (int i=0; i<params.size(); i++) {
                params.get(i).setColumn(columnNames.get(i).toUpperCase(Locale.US));
            }
        }
        return params;
    }
    
    /* default */ List<SqlParam> findInputParams() {
        List<SqlParam> params = new ArrayList<>();
        int i=0;
        int columnPos=0;
        for (String word: sqlArray) {
            if (word.startsWith(":#")) {
                SqlParam param = new SqlParam(word.substring(2));
                String column = sqlArray.get(i-1);
                if ("LIKE".equals(column)) {
                    column = sqlArray.get(i-2);
                }
                if (column.startsWith(":#") || "VALUES".equals(column)) {
                    param.setColumnPos(columnPos++);
                } else {
                    param.setColumn(column.toUpperCase(Locale.US));
                }
                params.add(param);
            }
            i++;
        }
        return params;
    }
    
    /* default */ List<SqlParam> findOutputColumnsInSelectStatement() {
        boolean isParam = true;
        List<SqlParam> params = new ArrayList<>();
        for (String word: sqlArray) {
            if (isParam && !"SELECT".equals(word) && !"DISTINCT".equals(word)) {
                SqlParam param = new SqlParam(word);
                param.setColumn(word);
            }
            if ("FROM".equals(word)) {
                isParam = false;
                break;
            }
        }
        return params;
    }

    /* default */ List<String> findTablesInSelectStatement() {
        boolean isTable = false;
        List<String> tables = new ArrayList<>();
        for (String word: sqlArray) {
            if (! statementInfo.getTablesInSchema().contains(word)) {
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
