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
package io.syndesis.jsondb;

import java.util.Arrays;

import io.syndesis.jsondb.impl.ChildFilter;
import io.syndesis.jsondb.impl.LogicalFilter;

/**
 *
 */
@SuppressWarnings("PMD")
public abstract class Filter {

    public enum Op {
        EQ,
        NEQ,
        LT,
        GT,
        LTE,
        GTE,
    }

    public static Filter and(Filter ...filters) {
        return new LogicalFilter(LogicalFilter.Op.AND, Arrays.asList(filters));
    }

    public static Filter or(Filter ...filters) {
        return new LogicalFilter(LogicalFilter.Op.OR, Arrays.asList(filters));
    }

    public static Filter child(String field, Op op, Object value) {
        return new ChildFilter(field, op, value);
    }

}
