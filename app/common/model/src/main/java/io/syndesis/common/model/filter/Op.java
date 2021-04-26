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

import java.io.IOException;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonDeserialize(using = Op.Deserializer.class)
@SuppressWarnings("PMD.ShortClassName")
@Schema(implementation = Op.Schema.class)
public enum Op {

    CONTAINS("contains", "contains"),
    CONTAINS_IGNORE_CASE("contains (ignore case)", "~~"),
    EQUALS("equals", "=="),
    EQUALS_IGNORE_CASE("equals (ignores case)", "=~"),
    GREATER_THAN("greater than", ">"),
    GREATER_THAN_OR_EQUALS("greater than or equal to", ">="),
    IN("in", "in"),
    LESS_THAN("less than", "<"),
    LESS_THAN_OR_EQUALS("less than or equal to", "<="),
    MATCHES("matches", "regex"),
    NOT_CONTAINS("not contains", "not contains"),
    NOT_EQUALS("not equals", "!="),
    NOT_IN("not in", "not in"),
    NOT_MATCHES("not matches", "not regex");

    @JsonProperty(index = 0)
    private final String label;

    @JsonProperty(index = 1)
    private final String operator;

    @SuppressWarnings("UnusedVariable")
    @io.swagger.v3.oas.annotations.media.Schema(name = "Op")
    static final class Schema {
        @JsonProperty(index = 0)
        private String label;

        @JsonProperty(index = 1)
        private String operator;

        private Schema() {
            // used solely to describe the API for OpenAPI document generation
        }
    }

    public static final class Deserializer extends JsonDeserializer<Op> {

        @Override
        public Op deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final JsonNode node = ctxt.readTree(p);
            final JsonNode operator = node.get("operator");
            if (operator == null) {
                return null;
            }

            final String opOperator = operator.textValue();

            for (final Op op : Op.values()) {
                if (op.operator.equals(opOperator)) {
                    return op;
                }
            }

            return null;
        }
    }

    Op(final String label, final String operator) {
        this.label = label;
        this.operator = operator;
    }

    public String getLabel() {
        return label;
    }

    public String getOperator() {
        return operator;
    }
}
