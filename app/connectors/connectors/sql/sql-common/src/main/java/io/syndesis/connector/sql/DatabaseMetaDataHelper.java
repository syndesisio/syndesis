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
            defaultSchema = dbUser;
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

    public static ResultSet fetchProcedureColumns(final DatabaseMetaData meta, final String catalog,
        final String schema, final String procedureName) throws SQLException {
        if (meta.getDatabaseProductName().equalsIgnoreCase(DatabaseProduct.POSTGRESQL.name())) {
            return meta.getFunctionColumns(catalog, schema, procedureName, null);
        }

        return meta.getProcedureColumns(catalog, schema, procedureName, null);
    }

    public static ResultSet fetchProcedures(final DatabaseMetaData meta, final String catalog,
        final String schemaPattern, final String procedurePattern) throws SQLException {
        if (meta.getDatabaseProductName().equalsIgnoreCase(DatabaseProduct.POSTGRESQL.name())) {
            return meta.getFunctions(catalog, schemaPattern, procedurePattern);
        }

        return meta.getProcedures(catalog, schemaPattern, procedurePattern);
    }
    
    
    /* default */ static Set<String> fetchTables(final DatabaseMetaData meta, final String catalog,
        final String schemaPattern, final String tableNamePattern) throws SQLException {
        Set<String> tablesInSchema = new HashSet<>();
        ResultSet rs = meta.getTables(catalog, schemaPattern, tableNamePattern, new String[] { "TABLE" });
        while (rs.next()) {
            tablesInSchema.add(rs.getString(3).toUpperCase(Locale.US));
        }
        return tablesInSchema;
    }

    /* default */ static ResultSet fetchTableColumns(final DatabaseMetaData meta, final String catalog,
            final String schema, final String tableName, final String columnName) throws SQLException {

        return meta.getColumns(catalog, schema, tableName, columnName);
    }

    /* default */ static List<SqlParam> getJDBCInfoByColumnNames(final DatabaseMetaData meta, String catalog, 
            String schema, String tableName, final List<SqlParam> params) throws SQLException {
        List<SqlParam> paramList = new ArrayList<>();
        for (int i=0; i<params.size(); i++) {
            SqlParam param = params.get(i);
            String columnName = param.getColumn();
            ResultSet column = meta.getColumns(catalog, schema, tableName, columnName); // NOPMD, TODO: fishy
            if (column.getFetchSize() == 0) {
                //Postgresql does lowercase instead, so let's try that if we don't have a match
                column = meta.getColumns(catalog, schema, tableName.toLowerCase(Locale.US), columnName.toLowerCase(Locale.US));
            }
            column.next(); // NOPMD, TODO: fishy
            param.setJdbcType(JDBCType.valueOf(column.getInt("DATA_TYPE")));
            paramList.add(param);
        }
        return paramList;
    }

    /* default */ static List<SqlParam> getJDBCInfoByColumnOrder(final DatabaseMetaData meta, String catalog, 
            String schema, String tableName, final List<SqlParam> params) throws SQLException {
        List<SqlParam> paramList = new ArrayList<>();
        ResultSet columnSet = meta.getColumns(catalog, "SA", tableName, null); // NOPMD, TODO: fishy
        for (int i=0; i<params.size(); i++) {
            columnSet.next(); // NOPMD, TODO: fishy
            SqlParam param = params.get(i);
            param.setColumn(columnSet.getString("COLUMN_NAME"));
            param.setJdbcType(JDBCType.valueOf(columnSet.getInt("DATA_TYPE")));
            paramList.add(param);
        }
        return paramList;
    }

    /* default */ static List<SqlParam> getOutputColumnInfo(final Connection connection, 
            final String sqlSelectStatement) throws SQLException {
        List<SqlParam> paramList = new ArrayList<>();
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery(sqlSelectStatement);
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
