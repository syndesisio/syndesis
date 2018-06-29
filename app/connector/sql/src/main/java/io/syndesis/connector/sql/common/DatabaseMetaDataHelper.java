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

public final class DatabaseMetaDataHelper {

    private DatabaseMetaDataHelper() {
        // utility class
    }

    public static String getDefaultSchema(final String databaseProductName, String dbUser) {

        String defaultSchema = null;
        // Oracle uses the username as schema
        if (databaseProductName.equalsIgnoreCase(DatabaseProduct.ORACLE.name())) {
            defaultSchema = dbUser.toUpperCase(Locale.US);
        } else if (databaseProductName.equalsIgnoreCase(DatabaseProduct.POSTGRESQL.name())) {
            defaultSchema = "public";
        } else if (databaseProductName.equalsIgnoreCase(DatabaseProduct.APACHE_DERBY.nameWithSpaces())) {
            if (dbUser != null) {
                defaultSchema = dbUser.toUpperCase(Locale.US);
            } else {
                defaultSchema = "NULL";
            }
        }
        return defaultSchema;
    }

    public static String adapt(String databaseProductName, String pattern) {
        if (pattern == null && databaseProductName.equalsIgnoreCase(DatabaseProduct.MYSQL.name())) {
            return "%";
        } else {
            return pattern;
        }
    }

    public static ResultSet fetchProcedureColumns(final DatabaseMetaData meta, final String catalog,
        final String schema, final String procedureName) throws SQLException {
        if (meta.getDatabaseProductName().equalsIgnoreCase(DatabaseProduct.POSTGRESQL.name())) {
            return meta.getFunctionColumns(catalog, schema, procedureName, null);
        }

        return meta.getProcedureColumns(
                catalog,
                adapt(meta.getDatabaseProductName(),schema),
                adapt(meta.getDatabaseProductName(),procedureName),
                adapt(meta.getDatabaseProductName(),null));
    }

    public static ResultSet fetchProcedures(final DatabaseMetaData meta, final String catalog,
        final String schemaPattern, final String procedurePattern) throws SQLException {
        if (meta.getDatabaseProductName().equalsIgnoreCase(DatabaseProduct.POSTGRESQL.name())) {
            return meta.getFunctions(catalog, schemaPattern, procedurePattern);
        }

        return meta.getProcedures(
                catalog,
                adapt(meta.getDatabaseProductName(),schemaPattern),
                adapt(meta.getDatabaseProductName(),procedurePattern));
    }


    static Set<String> fetchTables(final DatabaseMetaData meta, final String catalog,
        final String schemaPattern, final String tableNamePattern) throws SQLException {
        Set<String> tablesInSchema = new HashSet<>();
        try (ResultSet rs = meta.getTables(
                catalog,
                adapt(meta.getDatabaseProductName(),schemaPattern),
                adapt(meta.getDatabaseProductName(), tableNamePattern),
                new String[] { "TABLE" });) {
            while (rs.next()) {
                tablesInSchema.add(rs.getString(3).toUpperCase(Locale.US));
            }
        }
        return tablesInSchema;
    }

    static List<SqlParam> getJDBCInfoByColumnNames(final DatabaseMetaData meta, String catalog,
            String schema, String tableName, final List<SqlParam> params) throws SQLException {
        List<SqlParam> paramList = new ArrayList<>();
        for (int i=0; i<params.size(); i++) {
            SqlParam param = params.get(i);
            String columnName = param.getColumn();
            List<ColumnMetaData> cList = getColumnMetaData(meta, catalog, schema, tableName, columnName, 1);
            param.setJdbcType(cList.get(0).getType());
            paramList.add(param);
        }
        return paramList;
    }

    static List<SqlParam> getJDBCInfoByColumnOrder(final DatabaseMetaData meta, String catalog,
            String schema, String tableName, final List<SqlParam> params) throws SQLException {
        List<SqlParam> paramList = new ArrayList<>();
        List<ColumnMetaData> cList = getColumnMetaData(meta, catalog, schema, tableName, null, params.size());
        for (SqlParam param : params) {
            ColumnMetaData c = cList.get(param.getColumnPos());
            param.setColumn(c.getName());
            param.setJdbcType(c.getType());
            paramList.add(param);
        }
        return paramList;
    }

    @SuppressWarnings("PMD.RemoteInterfaceNamingConvention")
    private static List<ColumnMetaData> getColumnMetaData(final DatabaseMetaData meta, String catalog, //NOPMD
            String schema, String tableName, String columnName, int expectedSize) throws SQLException { //NOPMD
        try (ResultSet columns = meta.getColumns(
                catalog,
                adapt(meta.getDatabaseProductName(),schema),
                adapt(meta.getDatabaseProductName(),tableName),
                adapt(meta.getDatabaseProductName(),columnName));) {
            List<ColumnMetaData> columnList = convert(columns);
            if (columnList.isEmpty()) {
                //Postgresql does lowercase instead, so let's try that if we don't have a match
                String table = tableName.toLowerCase(Locale.US);
                String column = columnName == null ? null : columnName.toLowerCase(Locale.US);
                try (ResultSet columnMeta = meta.getColumns(catalog, schema, table, column)) {
                    columnList = convert(columnMeta);
                }
            }
            if (columnList.size() < expectedSize) {
                String msg = String.format("Invalid SQL, the number of columns (%s) should match the number of number of input parameters (%s)",
                        columnList.size(), expectedSize);
                throw new SQLException(msg);
            }
            return columnList;
        }

    }

    @SuppressWarnings("OBL_UNSATISFIED_OBLIGATION")
    /* default */ static List<SqlParam> getOutputColumnInfo(final Connection connection,
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
            columnMetaData.setPosition(position++);
            list.add(columnMetaData);
        }
        return list;
    }

}
