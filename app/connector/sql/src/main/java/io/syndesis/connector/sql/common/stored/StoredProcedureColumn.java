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
package io.syndesis.connector.sql.common.stored;

import java.sql.JDBCType;

public class StoredProcedureColumn {

    private JDBCType jdbcType;
    private ColumnMode mode;
    private String name;
    private int ordinal;

    public JDBCType getJdbcType() {
        return jdbcType;
    }

    public ColumnMode getMode() {
        return mode;
    }

    public String getName() {
        return name;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setJdbcType(final JDBCType jdbcType) {
        this.jdbcType = jdbcType;
    }

    public void setMode(final ColumnMode mode) {
        this.mode = mode;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setOrdinal(final int ordinal) {
        this.ordinal = ordinal;
    }

    public String toProcedureParameterString() {
        if (mode == ColumnMode.IN) {
            return jdbcType + " ${body[" + name + "]}";
        } else if (mode == ColumnMode.OUT) {
            return mode.name() + " " + jdbcType + " " + name;
        } else {
            return null;
        }
    }
}
