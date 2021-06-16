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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitorAdapter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SqlStatementParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlStatementParser.class);

    private final DbMetaDataHelper helper;

    private final String schema;

    private final String sql;

    private static class JdbcNamedParameterVisitor extends ExpressionVisitorAdapter {

        List<String> columnNames = new ArrayList<>();

        String lastColumnName;

        List<String> parameterNames = new ArrayList<>();

        @Override
        public void visit(final Column column) {
            final String columnName = column.getColumnName();

            // apparently, some databases (or drivers) require column names to
            // be uppercase, otherwise no JDBC metadata is returned
            lastColumnName = columnName.toUpperCase(Locale.US);
        }

        @Override
        public void visit(final JdbcNamedParameter parameter) {
            final String name = parameter.getName();

            parameterNames.add(name.replaceFirst("#", ""));
            // we might not know the column name from the statement, e.g.
            // `INSERT INTO VALUES (a, b, c)` doesn't mention any columns
            // in general we expect if at least one column name is given
            // that all column names are given for all parameters
            if (lastColumnName != null) {
                columnNames.add(lastColumnName);
            }
        }
    }

    private static class ParamsVisitor extends StatementVisitorAdapter {

        List<SqlParam> inParams = new ArrayList<>();

        String tableName;

        @Override
        public void visit(final Delete delete) {
            final Table table = delete.getTable();
            tableName = table.getName();

            final Expression where = delete.getWhere();
            collectExpressionParameters(where);
        }

        @Override
        public void visit(final Insert insert) {
            final Table table = insert.getTable();
            tableName = table.getName();

            final List<Column> columns = insert.getColumns();

            final ItemsList items = insert.getItemsList();
            items.accept(new ItemsListVisitorAdapter() {
                @Override
                public void visit(final ExpressionList expressionList) {
                    int offset = 0;
                    for (final Expression expression : expressionList.getExpressions()) {
                        collectExpressionParameters(expression, offset++, columns);
                    }
                }
            });
        }

        @Override
        public void visit(final Select select) {
            final SelectBody body = select.getSelectBody();

            final TablesNamesFinder tableNames = new TablesNamesFinder();
            final List<String> tableList = tableNames.getTableList(select);
            if (!tableList.isEmpty()) {
                // TODO assumes there will be only one table in a SELECT statement
                tableName = tableList.get(0);
            }

            body.accept(new SelectVisitorAdapter() {
                @Override
                public void visit(final PlainSelect plainSelect) {
                    final Expression where = plainSelect.getWhere();

                    collectExpressionParameters(where);
                }
            });
        }

        @Override
        public void visit(final Update update) {
            final Table table = update.getTable();
            tableName = table.getName();

            int offset = 0;
            final List<Expression> expressions = update.getExpressions();
            for (final Expression expression : expressions) {
                collectExpressionParameters(expression, offset++, update.getColumns());
            }

            final Expression where = update.getWhere();
            collectExpressionParameters(where, offset);
        }

        private void collectExpressionParameters(final Expression expression) {
            collectExpressionParameters(expression, 0);
        }

        private void collectExpressionParameters(final Expression expression, final int offset) {
            collectExpressionParameters(expression, offset, Collections.emptyList());
        }

        private void collectExpressionParameters(final Expression expression, final int offset, final List<Column> columns) {
            // statements without expressions, like SELECT without a WHERE
            if (expression == null) {
                return;
            }

            // we're not interested in literal values, no easy way to check for
            // that other than by the class name
            final String className = expression.getClass().getSimpleName();
            if (className.endsWith("Value")) {
                return;
            }

            final JdbcNamedParameterVisitor params = new JdbcNamedParameterVisitor();
            expression.accept(params);

            for (int i = 0; i < params.parameterNames.size(); i++) {
                final String parameterName = params.parameterNames.get(i);
                final SqlParam param = new SqlParam(parameterName);

                if (!params.columnNames.isEmpty()) {
                    // if there are params.columnNames we collected the same number
                    // of columns as params.parameterNames, look at
                    // JdbcNamedParameterVisitor
                    final String columnName = params.columnNames.get(i);
                    param.setColumn(columnName);
                } else if (columns != null && !columns.isEmpty()) {
                    // yes, the columns given by the parser can be null
                    // these are any named columns that are not associated
                    // with parameter expressions
                    final Column column = columns.get(i + offset);
                    // apparently, some databases (or drivers) require column
                    // names to be uppercase, otherwise no JDBC metadata is
                    // returned
                    final String columnName = column.getColumnName().toUpperCase(Locale.US);
                    param.setColumn(columnName);
                }
                param.setColumnPos(i + offset);

                inParams.add(param);
            }
        }
    }

    public SqlStatementParser(final Connection connection, final String sql) throws SQLException {
        this(connection, null, sql);
    }

    public SqlStatementParser(final Connection connection, final String schema, final String sql) throws SQLException {
        this.sql = sql;
        helper = new DbMetaDataHelper(connection);

        this.schema = determineSchemaGiven(schema, helper);
    }

    public SqlStatementMetaData parse() throws SQLException {
        final Statement statement;
        try {
            statement = CCJSqlParserUtil.parse(sql);
        } catch (final JSQLParserException e) {
            // if we failed we try to parse with the legacy parser
            // if that fails as well we throw the original exception
            final SqlStatementLegacyParser legacyParser = new SqlStatementLegacyParser(helper.connection, schema, sql);
            try {
                return legacyParser.parse();
            } catch (SQLException fromLegacyParser) {
                LOGGER.warn("Parsing with SQL and legacy parser failed, will rethrow the first exception only", fromLegacyParser);
            }

            throw new SQLException(e);
        }

        final SqlStatementMetaData metadata = new SqlStatementMetaData(sql, schema);

        final StatementType statementType = determineStatementTypeFrom(statement);
        metadata.setStatementType(statementType);

        final List<String> tables = collectTableNamesFrom(statement);
        assertTablesExist(tables);
        metadata.setTableNames(tables);

        metadata.setInParams(collectInParams(statement));

        metadata.setOutParams(collectOutParams(metadata));

        setAutoIncrementMetadata(metadata);

        return metadata;
    }

    void assertTablesExist(final List<String> speculativeTables) throws SQLException {
        final Set<String> tablesInDatabase = uppercase(helper.fetchTables(null, schema, null));

        final Set<String> nonExistantTables = new HashSet<>();
        for (final String table : speculativeTables) {
            if (!tablesInDatabase.contains(table.toUpperCase(Locale.US))) {
                nonExistantTables.add(table);
            }
        }

        if (!nonExistantTables.isEmpty()) {
            throw new SQLException(String.format("Table(s) '%s' cannot be found in schema '%s'", String.join("', '", nonExistantTables), schema));
        }
    }

    List<SqlParam> collectInParams(final Statement statement) throws SQLException {
        final ParamsVisitor visitor = new ParamsVisitor();

        statement.accept(visitor);

        final List<SqlParam> inParams = visitor.inParams;

        if (inParams.isEmpty()) {
            return inParams;
        }

        if (inParams.get(0).getColumn() == null) {
            // we're assuming that if the first parameter knows the column name
            // the rest of the parameters will as well
            helper.getJDBCInfoByColumnOrder(null, schema, visitor.tableName, inParams);
        } else {
            // we might not have collected column names, asserted by the first
            // parameter here, in that case we'll go by column numbers
            helper.getJDBCInfoByColumnNames(null, schema, visitor.tableName, inParams);
        }

        return inParams;
    }

    List<SqlParam> collectOutParams(final SqlStatementMetaData metadata) throws SQLException {
        if (metadata.getStatementType() != StatementType.SELECT) {
            // we fetch output column metadata only for SELECT statements
            return Collections.emptyList();
        }

        final String defaultedSqlStatement = metadata.getDefaultedSqlStatement();
        return helper.getOutputColumnInfo(defaultedSqlStatement);
    }

    void setAutoIncrementMetadata(final SqlStatementMetaData metadata) throws SQLException {
        if (metadata.getStatementType() != StatementType.INSERT) {
            // we fetch auto increment metadata only for INSERT statements
            return;
        }

        final List<String> tableNames = metadata.getTableNames();
        if (tableNames.isEmpty()) {
            return;
        }

        // TODO assumes there will be only one table in a INSERT statement
        final String tableName = tableNames.get(0);
        final List<SqlParam> autoIncrementParameters = helper.getAutoIncrementColumnList(null, schema, tableName);
        if (autoIncrementParameters.isEmpty()) {
            return;
        }

        final SqlParam autoIncrementParameter = autoIncrementParameters.get(0);
        // TODO hmm, but it's called name, i.e. not columnName?
        final String columnName = autoIncrementParameter.getName();
        // TODO ^ and here we set column
        metadata.setAutoIncrementColumnName(columnName);

        // in addition INSERT statement can have one output column metadata
        final List<SqlParam> existingOutParams = metadata.getOutParams();
        final List<SqlParam> outParams = new ArrayList<>();
        outParams.addAll(existingOutParams);
        outParams.add(autoIncrementParameter);
        metadata.setOutParams(outParams);
    }

    static List<String> collectTableNamesFrom(final Statement statement) {
        final TablesNamesFinder tables = new TablesNamesFinder();

        return tables.getTableList(statement);
    }

    static String determineSchemaGiven(final String givenSchema, final DbMetaDataHelper helper) {
        // if user set, then use that
        if (givenSchema != null) {
            return givenSchema;
        }
        try {
            // try grabbing from the connection, not all drivers support this
            return helper.connection.getSchema();
        } catch (final SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (final AbstractMethodError e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage());
            }
        }
        try {
            final DatabaseMetaData metadata = helper.connection.getMetaData();
            final String username = metadata.getUserName();

            // finally try setting reasonable default
            return helper.getDefaultSchema(username);
        } catch (final SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    static StatementType determineStatementTypeFrom(final Statement statement) {
        // INSERT, SELECT, UPDATE, DELETE;
        if (statement instanceof Insert) {
            return StatementType.INSERT;
        }

        if (statement instanceof Select) {
            return StatementType.SELECT;
        }

        if (statement instanceof Update) {
            return StatementType.UPDATE;
        }

        if (statement instanceof Delete) {
            return StatementType.DELETE;
        }

        throw new IllegalArgumentException("Unsupported statement type: " + statement.getClass().getSimpleName());
    }

    static Set<String> uppercase(final Set<String> tables) {
        final Set<String> uppercased = new HashSet<>(tables.size());

        for (final String table : tables) {
            uppercased.add(table.toUpperCase(Locale.US));
        }

        return uppercased;
    }

}
