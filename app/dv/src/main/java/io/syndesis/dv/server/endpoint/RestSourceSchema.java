/*
 * Copyright (C) 2013 Red Hat, Inc.
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
package io.syndesis.dv.server.endpoint;

import java.util.ArrayList;
import java.util.List;

import org.teiid.metadata.Schema;
import org.teiid.metadata.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Represents the configuration for a source schema
 */
@JsonSerialize(as = RestSourceSchema.class)
@JsonInclude(Include.NON_NULL)
public class RestSourceSchema {

    /*
     * The schema name
     */
    private String name;

    /*
     * The source table objects for this schema
     */
    private RestSourceTable[] tables;

    public RestSourceSchema(Schema schema) {
        super();
        this.name = schema.getName();
        List<RestSourceTable> tables = new ArrayList<RestSourceTable>();
        for( String key : schema.getTables().keySet()) {
            Table nextTable = schema.getTables().get(key);
            tables.add(new RestSourceTable(nextTable));
        }
        this.tables = tables.toArray(new RestSourceTable[0]);
    }

    public String getName() {
        return this.name;
    }

    public RestSourceTable[] getTables() {
        return this.tables;
    }

}
