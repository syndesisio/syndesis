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

import static io.syndesis.server.jsondb.impl.JsonRecordSupport.FALSE_VALUE_PREFIX;
import static io.syndesis.server.jsondb.impl.JsonRecordSupport.NULL_VALUE_PREFIX;
import static io.syndesis.server.jsondb.impl.JsonRecordSupport.STRING_VALUE_PREFIX;
import static io.syndesis.server.jsondb.impl.JsonRecordSupport.TRUE_VALUE_PREFIX;
import static io.syndesis.server.jsondb.impl.JsonRecordSupport.toLexSortableString;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.skife.jdbi.v2.Query;

class LiteralSqlExpressionBuilder extends SqlExpressionBuilder {
    private final Object value;

    public LiteralSqlExpressionBuilder(Object value) {
        this.value = value;
    }

    @Override
    public void build(StringBuilder sql, ArrayList<Consumer<Query<Map<String, Object>>>> binds, AtomicInteger bindCounter) {
        int b1 = bindCounter.incrementAndGet();
        sql.append(":f").append(b1);
        binds.add(query -> {
            if( value == null  ) {
                query.bind("f" + b1, String.valueOf(NULL_VALUE_PREFIX));
            } else if( Boolean.FALSE.equals(value) ) {
                query.bind("f" + b1, String.valueOf(FALSE_VALUE_PREFIX));
            } else if( Boolean.TRUE.equals(value) ) {
                query.bind("f" + b1, String.valueOf(TRUE_VALUE_PREFIX));
            } else if( value.getClass() == String.class ) {
                query.bind("f" + b1, STRING_VALUE_PREFIX+value.toString());
            } else if( value instanceof Number ) {
                query.bind("f" + b1, toLexSortableString(value.toString()));
            } else {
                query.bind("f" + b1, STRING_VALUE_PREFIX+value.toString());
            }
        });
    }
}
