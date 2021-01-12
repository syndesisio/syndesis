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
package io.syndesis.connector.sql.common;

import java.sql.JDBCType;

public class ColumnMetaData {

    private final boolean isAutoIncrement;

    private final String name;

    private final int position;

    private final JDBCType type;

    public ColumnMetaData(final String name, final JDBCType type, final int position, final boolean isAutoIncrement) {
        this.name = name;
        this.type = type;
        this.position = position;
        this.isAutoIncrement = isAutoIncrement;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public JDBCType getType() {
        return type;
    }

    public boolean isAutoIncrement() {
        return isAutoIncrement;
    }

}
