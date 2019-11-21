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
package io.syndesis.dv.metadata.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = QSColumn.class)
@JsonInclude(Include.NON_NULL)
public class QSColumn {

    private String type;

    private String name;

    private String label;

    protected QSColumn() {
    }

    public QSColumn(String dataType, String columnName, String columnLabel) {
        this.type = dataType;
        this.name = columnName;
        this.label = columnLabel;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }
}
