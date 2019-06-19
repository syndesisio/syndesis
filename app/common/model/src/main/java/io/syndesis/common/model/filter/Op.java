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
package io.syndesis.common.model.filter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
@JsonDeserialize(builder = Op.Builder.class)
@SuppressWarnings("PMD.ShortClassName")
public interface Op extends Serializable {

    Op EQUALS = new Op.Builder().label("equals").operator("==").build();
    Op EQUALS_IGNORE_CASE = new Op.Builder().label("equals (ignores case)").operator("=~").build();
    Op NOT_EQUALS = new Op.Builder().label("not equals").operator("!=").build();

    Op LESS_THAN = new Op.Builder().label("less than").operator("<").build();
    Op LESS_THAN_OR_EQUALS = new Op.Builder().label("less than or equal to").operator("<=").build();
    Op GREATER_THAN = new Op.Builder().label("greater than").operator(">").build();
    Op GREATER_THAN_OR_EQUALS = new Op.Builder().label("greater than or equal to").operator(">=").build();

    Op CONTAINS = new Op.Builder().label("contains").operator("contains").build();
    Op CONTAINS_IGNORE_CASE = new Op.Builder().label("contains (ignore case)").operator("~~").build();
    Op NOT_CONTAINS = new Op.Builder().label("not contains").operator("not contains").build();
    Op MATCHES = new Op.Builder().label("matches").operator("regex").build();
    Op NOT_MATCHES = new Op.Builder().label("not matches").operator("not regex").build();
    Op IN = new Op.Builder().label("in").operator("in").build();
    Op NOT_IN = new Op.Builder().label("not in").operator("not in").build();

    Op[] DEFAULT_OPTS = new Op[] {
        EQUALS, EQUALS_IGNORE_CASE, NOT_EQUALS,
        LESS_THAN, LESS_THAN_OR_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUALS,
        CONTAINS, CONTAINS_IGNORE_CASE, NOT_CONTAINS,
        MATCHES, NOT_MATCHES,
        IN, NOT_IN
    };

    String getLabel();

    String getOperator();

    class Builder extends ImmutableOp.Builder {
    }
}
