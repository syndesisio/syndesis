/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector;

import java.sql.JDBCType;

public class StoredProcedureColumn {
    
    private int ordinal;
    private String name;
    private ColumnMode mode;
    private JDBCType jdbcType;
    
    public int getOrdinal() {
        return ordinal;
    }
    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public ColumnMode getMode() {
        return mode;
    }
    public void setMode(ColumnMode mode) {
        this.mode = mode;
    }
    public JDBCType getJdbcType() {
        return jdbcType;
    }
    public void setJdbcType(JDBCType jdbcType) {
        this.jdbcType = jdbcType;
    }
}
