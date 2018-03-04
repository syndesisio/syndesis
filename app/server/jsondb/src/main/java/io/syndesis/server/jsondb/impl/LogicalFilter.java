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
package io.syndesis.server.jsondb.impl;

import java.util.List;

import io.syndesis.server.jsondb.Filter;
import io.syndesis.common.model.ToJson;

/**
 *
 */
public class LogicalFilter implements Filter, ToJson {

    public enum Op {
        AND,
        OR
    }

    private final Op op;
    private final List<Filter> filters;

    public LogicalFilter(Op op, List<Filter> filters) {
        this.op = op;
        if( filters.size() <= 1 ) {
            throw new IllegalArgumentException("Logical filter requires at least two sub filters.");
        }
        this.filters = filters;
    }

    public List<Filter> filters() {
        return filters;
    }
    public Op op() {
        return op;
    }
}
