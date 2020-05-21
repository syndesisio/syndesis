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
package io.syndesis.dv.server.endpoint;

import org.teiid.metadata.Column;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Represents the configuration for the schema table's column info
 */
@JsonSerialize(as = RestSourceColumn.class)
@JsonInclude(Include.NON_NULL)
public class RestSourceColumn {

    /*
     * The column name
     */
    private final String name;

    /*
     * The column datatype
     */
    private final String datatype;

    /**
     * Constructor for use when deserializing
     */
    public RestSourceColumn(Column column) {
        super();
        this.name = column.getName();
        this.datatype = column.getRuntimeType();
    }

    public String getName() {
        return this.name;
    }

    public String getDatatype() {
        return this.datatype;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\nRestSourceColumn: NAME: ");
        sb.append(getName()).append(" Type: ").append(getDatatype());
        return sb.toString();
    }

}
