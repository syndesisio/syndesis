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
package io.syndesis.server.jsondb.impl.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.skife.jdbi.v2.Query;

import io.syndesis.server.jsondb.Filter;
import io.syndesis.server.jsondb.JsonDBException;
import io.syndesis.server.jsondb.impl.ChildFilter;
import io.syndesis.server.jsondb.impl.LogicalFilter;
import io.syndesis.server.jsondb.impl.SqlJsonDB;

public abstract class SqlExpressionBuilder {

    protected abstract void build(StringBuilder sql, ArrayList<Consumer<Query<Map<String, Object>>>> binds, AtomicInteger bindCounter);

    public void build(StringBuilder sql, ArrayList<Consumer<Query<Map<String, Object>>>> binds) {
        build(sql, binds, new AtomicInteger());
    }

    public static SqlExpressionBuilder create(SqlJsonDB db, Filter filter, String path) {
        if( filter instanceof ChildFilter ) {
            ChildFilter childFilter = (ChildFilter) filter;
            return create(db, childFilter, path);
        }
        if( filter instanceof LogicalFilter) {
            LogicalFilter logicalFilter = (LogicalFilter) filter;
            return create(db, logicalFilter, path);
        }
        throw new JsonDBException("Unsupported filter: "+filter);
    }

    public static SqlExpressionBuilder create(SqlJsonDB db, ChildFilter filter, String path) {
        String idx = path+"#"+filter.field();
        if( !db.getIndexPaths().contains(idx) ) {
            throw new JsonDBException("You can only filter on fields that are indexed.");
        }
        PropertySqlExpressionBuilder left = new PropertySqlExpressionBuilder(idx);
        LiteralSqlExpressionBuilder right = new LiteralSqlExpressionBuilder(filter.value());
        return new BinarySqlExpressionBuilder(db, left, toSqlOp(filter.op()), right);
    }

    public static SqlExpressionBuilder create(SqlJsonDB db, LogicalFilter filter, String path) {
        List<SqlExpressionBuilder> children = filter.filters().stream()
            .map(x -> create(db, x, path))
            .collect(Collectors.toList());

        switch( filter.op() ) {
            case OR:
                return new LogicalSqlExpressionBuilder(" UNION ",  children);
            case AND:
                return new LogicalSqlExpressionBuilder(" INTERSECT ",  children);
            default:
                throw new JsonDBException("Unsupported op type.");
        }
    }

    private static String toSqlOp(ChildFilter.Op op) {
        switch(op) {
            case EQ: return " = ";
            case NEQ: return " <> ";
            case LT: return " < ";
            case GT: return " > ";
            case LTE: return " <= ";
            case GTE: return " >= ";
            default:
                throw new JsonDBException("Invalid filter comparison operation.");
        }
    }

}
