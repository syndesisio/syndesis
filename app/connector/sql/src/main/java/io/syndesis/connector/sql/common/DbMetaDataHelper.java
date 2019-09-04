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
import java.sql.DatabaseMetaData;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.syndesis.connector.sql.db.Db;

public final class DbMetaDataHelper {

    Db db;
    DatabaseMetaData meta;
    Connection connection;

    public DbMetaDataHelper(final Connection connection) throws SQLException {
        this.db = new DbAdapter(connection).getDb();
        this.connection = connection;
        this.meta = connection.getMetaData();
    }

    public String getDefaultSchema(final String dbUser) {
        return db.getDefaultSchema(dbUser);
    }

    public String adapt(final String pattern) {
        return db.adaptPattern(pattern);
    }

    public ResultSet fetchProcedureColumns(final String catalog,
        final String schema, final String procedureName) throws SQLException {
        return db.fetchProcedureColumns(meta, catalog, schema, procedureName);
    }

    public ResultSet fetchProcedures(final String catalog,
        final String schemaPattern, final String procedurePattern) throws SQLException {
        return db.fetchProcedures(meta, catalog, schemaPattern, procedurePattern);
    }

    Set<String> fetchTables(final String catalog,
        final String schemaPattern, final String tableNamePattern) throws SQLException {
        Set<String> tablesInSchema = new HashSet<>();
        try (ResultSet rs = meta.getTables(
                catalog,
                adapt(schemaPattern),
                adapt(tableNamePattern),
                new String[] { "TABLE", "VIEW" });) {
            while (rs.next()) {
                tablesInSchema.add(rs.getString(3).toUpperCase(Locale.US));
            }
        }
        return tablesInSchema;
    }

    List<SqlParam> getJDBCInfoByColumnNames(String catalog,
            String schema, String tableName, final List<SqlParam> params) throws SQLException {
        List<SqlParam> paramList = new ArrayList<>();
        for (int i=0; i<params.size(); i++) {
            SqlParam param = params.get(i);
            String columnName = param.getColumn();
            List<ColumnMetaData> cList = getColumnMetaData(catalog, schema, tableName, columnName, 1);
            param.setJdbcType(cList.get(0).getType());
            paramList.add(param);
        }
        return paramList;
    }

    List<SqlParam> getAutoIncrementColumnList(String catalog,
            String schema, String tableName) throws SQLException {
        List<SqlParam> outParams = new ArrayList<>();
        List<ColumnMetaData> cList = getColumnMetaData(catalog, schema, tableName, null, -1);
        for (ColumnMetaData columnMetaData : cList) {
            if (columnMetaData.isAutoIncrement()) {
                SqlParam sqlParam = new SqlParam();
                sqlParam.setName(columnMetaData.getName());
                sqlParam.setJdbcType(columnMetaData.getType());
                outParams.add(sqlParam);
                break; //SQL only allows one per table, so we're done.
            }
        }
        return outParams;
    }

    List<SqlParam> getJDBCInfoByColumnOrder(String catalog,
            String schema, String tableName, final List<SqlParam> params) throws SQLException {
        List<SqlParam> paramList = new ArrayList<>();
        List<ColumnMetaData> cList = getColumnMetaData(catalog, schema, tableName, null, params.size());
        for (SqlParam param : params) {
            ColumnMetaData c = cList.get(param.getColumnPos());
            param.setColumn(c.getName());
            param.setJdbcType(c.getType());
            paramList.add(param);
        }
        return paramList;
    }

    @SuppressWarnings("PMD.RemoteInterfaceNamingConvention")
    private List<ColumnMetaData> getColumnMetaData(String catalog, //NOPMD
            String schema, String tableName, String columnName, int expectedSize) throws SQLException { //NOPMD
        try (ResultSet columns = meta.getColumns(
                catalog,
                adapt(schema),
                adapt(tableName),
                adapt(columnName));) {
            List<ColumnMetaData> columnList = convert(columns);
            if (columnList.isEmpty()) {
                //Postgresql does lowercase instead, so let's try that if we don't have a match
                String table = tableName.toLowerCase(Locale.US);
                String column = columnName == null ? null : columnName.toLowerCase(Locale.US);
                try (ResultSet columnMeta = meta.getColumns(catalog, schema, table, column)) {
                    columnList = convert(columnMeta);
                }
            }
            if (expectedSize >= 0 && columnList.size() < expectedSize) {
                String msg = String.format("Invalid SQL, the number of columns (%s) should match the number of number of input parameters (%s)",
                        columnList.size(), expectedSize);
                throw new SQLException(msg);
            }
            return columnList;
        }

    }

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION")
    /* default */ List<SqlParam> getOutputColumnInfo(
            final String sqlSelectStatement) throws SQLException {
        List<SqlParam> paramList = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery(sqlSelectStatement);) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            if (metaData.getColumnCount()>0){
                for (int i=1; i<=metaData.getColumnCount(); i++) {
                    SqlParam param = new SqlParam(metaData.getColumnName(i));
                    param.setJdbcType(JDBCType.valueOf(metaData.getColumnType(i)));
                    paramList.add(param);
                }
            }
            return paramList;
        }
    }

    private static List<ColumnMetaData> convert(ResultSet resultSet) throws SQLException {
        List<ColumnMetaData> list = new ArrayList<>();
        Integer position = 0;
        while (resultSet.next()) {

            ColumnMetaData columnMetaData = new ColumnMetaData();

            columnMetaData.setName(resultSet.getString("COLUMN_NAME"));
            columnMetaData.setType(JDBCType.valueOf(resultSet.getInt("DATA_TYPE")));
            String autoIncString = resultSet.getString("IS_AUTOINCREMENT");
            String columnDefString = resultSet.getString("COLUMN_DEF");

            if ("YES".equalsIgnoreCase(autoIncString) ||
                    (columnDefString != null && columnDefString.contains("nextval"))) {
                columnMetaData.setAutoIncrement(true);
            }
            columnMetaData.setPosition(position++);
            list.add(columnMetaData);
        }
        return list;
    }

}
