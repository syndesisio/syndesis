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

import static io.syndesis.server.jsondb.impl.SqlJsonDB.DatabaseKind.H2;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.skife.jdbi.v2.Query;

import io.syndesis.server.jsondb.impl.SqlJsonDB;

class BinarySqlExpressionBuilder extends SqlExpressionBuilder {
    private final SqlJsonDB db;
    private final SqlExpressionBuilder arg1;
    private final String op;
    private final SqlExpressionBuilder arg2;

    public BinarySqlExpressionBuilder(SqlJsonDB db, SqlExpressionBuilder arg1, String op, SqlExpressionBuilder arg2) {
        this.db = db;
        this.arg1 = arg1;
        this.op = op;
        this.arg2 = arg2;
    }

    @Override
    public void build(StringBuilder sql, ArrayList<Consumer<Query<Map<String, Object>>>> binds, AtomicInteger bindCounter) {
        sql.append("SELECT ");
        if (db.getDatabaseKind() == H2) {
            sql.append("trim_suffix(path, split_part('#', idx, 2)||'/')");
        } else {
            sql.append("trim(trailing split_part('#', idx, 2)||'/' from path)");
        }
        sql.append("as match_path FROM jsondb WHERE (");
        arg1.build(sql, binds, bindCounter);
        sql.append(op);
        arg2.build(sql, binds, bindCounter);
        sql.append(')');
    }
}
