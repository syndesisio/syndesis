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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.syndesis.connector.sql.db.Db;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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

    public ResultSet fetchFunctions(final String catalog,
        final String schemaPattern, final String functionPattern) throws SQLException {
        return db.fetchFunctions(meta, catalog, schemaPattern, functionPattern);
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
                final String name = columnMetaData.getName();
                final JDBCType type = columnMetaData.getType();
                SqlParam sqlParam = new SqlParam(name, type);
                sqlParam.setJdbcType(type);
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

            final String name = c.getName();
            param.setColumn(name);

            final JDBCType type = c.getType();
            param.setJdbcType(type);

            paramList.add(param);
        }
        return paramList;
    }

    private List<ColumnMetaData> getColumnMetaData(String catalog,
            String schema, String tableName, String columnName, int expectedSize) throws SQLException {
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
        try (PreparedStatement stmt = createPreparedStatement(sqlSelectStatement);
            ResultSet resultSet = stmt.executeQuery();) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            if (metaData.getColumnCount()>0){
                for (int i=1; i<=metaData.getColumnCount(); i++) {
                    SqlParam param = new SqlParam(metaData.getColumnName(i));
                    final JDBCType type = JDBCTypeHelper.determineJDBCType(metaData, i);
                    param.setJdbcType(type);
                    paramList.add(param);
                }
            }
            return paramList;
        }
    }

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION") // needed for https://github.com/spotbugs/spotbugs/issues/432
    private PreparedStatement createPreparedStatement(String sqlSelectStatement) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sqlSelectStatement);
        ps.setMaxRows(1);
        return ps;
    }

    private static List<ColumnMetaData> convert(ResultSet resultSet) throws SQLException {
        List<ColumnMetaData> list = new ArrayList<>();
        Integer position = 0;
        while (resultSet.next()) {
            // the order in which the columns are read is significant for some databases
            // for certain combinations of Oracle Database / Oracle JDBC driver if we
            // try to fetch COLUMN_DEF column after IS_AUTOINCREMENT we get:
            //     java.sql.SQLException: Stream has already been closed
            // reason for this could like in the fact that the IS_AUTOINCREMENT column
            // is the last column the table metadata ResultSet has, and once we try to
            // read it we moved beyond the previous columns in the ResultSet coupled
            // with the fact that reading could be unbuffered or that on reaching the
            // end of the row data that bit of stream is closed automatically
            // this issue was reported in https://issues.jboss.org/browse/ENTESB-12159
            // against Oracle 12.1
            String name = resultSet.getString("COLUMN_NAME");
            JDBCType type = JDBCTypeHelper.determineJDBCType(resultSet);
            String columnDefString = resultSet.getString("COLUMN_DEF");
            String autoIncString = resultSet.getString("IS_AUTOINCREMENT");

            boolean autoIncrement = false;
            if ("YES".equalsIgnoreCase(autoIncString) ||
                    (columnDefString != null && columnDefString.contains("nextval"))) {
                autoIncrement = true;
            }

            ColumnMetaData columnMetaData = new ColumnMetaData(name, type, position++, autoIncrement);
            list.add(columnMetaData);
        }
        return list;
    }

}
