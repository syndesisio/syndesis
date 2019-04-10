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
package io.syndesis.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 */
public enum DataShapeKinds {
    ANY("any"),
    JAVA("java"),
    JSON_SCHEMA("json-schema"),
    JSON_INSTANCE("json-instance"),
    XML_SCHEMA("xml-schema"),
    XML_SCHEMA_INSPECTED("xml-schema-inspected"),
    XML_INSTANCE("xml-instance"),
    NONE("none");

    private final String string;

    DataShapeKinds(String string) {
        this.string = string;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.string;
    }

    @JsonCreator
    public static DataShapeKinds fromString(String string) {
        for (DataShapeKinds k : values()) {
            if (k.string.equals(string)) {
                return k;
            }
        }
        throw new IllegalArgumentException(String.format("There's no DataShapeKinds with string '%s'", string));
    }
}
