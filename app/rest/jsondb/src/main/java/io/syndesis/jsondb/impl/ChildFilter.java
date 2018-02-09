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
package io.syndesis.jsondb.impl;

import io.syndesis.jsondb.Filter;
import io.syndesis.model.ToJson;

/**
 *
 */
public class ChildFilter extends Filter implements ToJson {

    private final String field;
    private final Op op;
    private final Object value;

    public ChildFilter(String field, Op op, Object value) {
        this.field = field;
        this.op = op;
        this.value = value;
    }

    public String field() {
        return field;
    }

    public Op op() {
        return op;
    }

    public Object value() {
        return value;
    }

}
