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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.skife.jdbi.v2.Query;

class PropertySqlExpressionBuilder extends SqlExpressionBuilder {
    private final String idx;

    public PropertySqlExpressionBuilder(final String idx) {
        this.idx = idx;
    }

    @Override
    public void build(StringBuilder sql, ArrayList<Consumer<Query<Map<String, Object>>>> binds, AtomicInteger bindCounter) {
        int b1 = bindCounter.incrementAndGet();
        sql.append("idx = :f").append(b1).append(" AND value");
        binds.add(query -> {
            query.bind("f" + b1, idx);
        });
    }

}
