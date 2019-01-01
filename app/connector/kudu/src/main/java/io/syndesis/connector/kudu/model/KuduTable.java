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

package io.syndesis.connector.kudu.model;

import org.apache.kudu.Type;

import java.util.List;

public class KuduTable {
    private String name;
    private Schema schema;
    private CreateTableOptions builder;


    /**
     * @return name of the table
     */
    public String getName() {
        return name;
    }

    /**
     * Specifies the name of the table
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format(name);
    }

    public CreateTableOptions getBuilder() {
        return builder;
    }

    public void setBuilder(CreateTableOptions builder) {
        this.builder = builder;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }


    public static class Schema {
        private ColumnSchema[] columns;

        public ColumnSchema[] getColumns() {
            return columns;
        }

        public void setColumns(ColumnSchema[] columns) {
            this.columns = columns;
        }
    }

    public static class CreateTableOptions {
        private List<String> rangeKeys;

        public List<String> getRangeKeys() {
            return rangeKeys;
        }

        public void setRangeKeys(List<String> rangeKeys) {
            this.rangeKeys = rangeKeys;
        }
    }

    public static class ColumnSchema {
        private String name;
        private boolean key;

        public boolean isKey() {
            return key;
        }

        public void setKey(boolean key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
