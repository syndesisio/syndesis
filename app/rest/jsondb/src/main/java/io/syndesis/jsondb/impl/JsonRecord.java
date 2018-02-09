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

/**
 * JsonRecord is used to hold that data stored in a database record.
 */
public final class JsonRecord {

    private final String path;
    private final String value;
    private final String ovalue;
    private final int kind;
    private final String index;

    private JsonRecord(String path, String value, String ovalue, int kind, String index) {
        this.path = path;
        this.value = value;
        this.ovalue = ovalue;
        this.kind = kind;
        this.index = index;
    }

    public String getPath() {
        return path;
    }

    public String getValue() {
        return value;
    }

    public String getOValue() {
        return ovalue;
    }

    public int getKind() {
        return kind;
    }

    public String getIndex() {
        return index;
    }

    public static JsonRecord of(String path, String value, String ovalue, int kind, String index) {
        return new JsonRecord(path, value, ovalue, kind, index);
    }

}
