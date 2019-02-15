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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.Data;

@Data
@SuppressWarnings("PMD.StdCyclomaticComplexity")
public class SqlParam {

    private String name;
    private String column;
    private JDBCType jdbcType;
    private int columnPos;
    private TypeValue<?> typeValue;
    private Boolean isConstant;
    private String value;

    public SqlParam() {
        super();
    }

    public SqlParam(String name) {
        super();
        this.name = name;
    }

    public void setJdbcType(JDBCType jdbcType) {
        this.jdbcType = jdbcType;
        this.typeValue = javaType(jdbcType);
    }


    @Data
    public static class TypeValue<T> {

        private Class<T> clazz;
        private T sampleValue;

        public TypeValue(Class<T> clazz, T sampleValue) {
            super();
            this.clazz = clazz;
            this.sampleValue = sampleValue;
        }
    }

    public static class SqlSampleValue {
        public static final List<String> ARRAY_VALUE = Collections.unmodifiableList(Arrays.asList("1","2","3"));
        public static final byte[] BINARY_VALUE = {1,2,3};
        public static final String STRING_VALUE = "abc";
        public static final Character CHAR_VALUE = 'a';
        public static final Date DATE_VALUE = new Date(new java.util.Date().getTime());
        public static final Time TIME_VALUE = new Time(new java.util.Date().getTime());
        public static final Timestamp TIMESTAMP_VALUE = new Timestamp(new java.util.Date().getTime());
        public static final BigDecimal DECIMAL_VALUE = BigDecimal.ZERO;
        public static final Boolean BOOLEAN_VALUE = Boolean.TRUE;
        public static final Double DOUBLE_VALUE = Double.valueOf(0);
        public static final Integer INTEGER_VALUE = 0;
        public static final Long LONG_VALUE = 0L;
        public static final Float FLOAT_VALUE = 0f;
    }

    @SuppressWarnings({"rawtypes", "PMD.CyclomaticComplexity"})
    static TypeValue<?> javaType(final JDBCType jdbcType) {

        switch (jdbcType) {
        case ARRAY:
        case BINARY:
        case BLOB:
        case LONGVARBINARY:
        case VARBINARY:
            return new TypeValue<>(List.class, SqlSampleValue.ARRAY_VALUE);
        case BIT:
        case BOOLEAN:
            return new TypeValue<>(Boolean.class, SqlSampleValue.BOOLEAN_VALUE);
        case CHAR:
            return new TypeValue<>(Character.class, SqlSampleValue.CHAR_VALUE);
        case CLOB:
        case DATALINK:
        case LONGNVARCHAR:
        case LONGVARCHAR:
        case NCHAR:
        case NCLOB:
        case NVARCHAR:
        case ROWID:
        case SQLXML:
        case VARCHAR:
            return new TypeValue<>(String.class, SqlSampleValue.STRING_VALUE);
        case DATE:
            return new TypeValue<>(Date.class, SqlSampleValue.DATE_VALUE);
        case TIME:
            return new TypeValue<>(Time.class, SqlSampleValue.TIME_VALUE);
        case TIMESTAMP:
        case TIMESTAMP_WITH_TIMEZONE:
        case TIME_WITH_TIMEZONE:
            return new TypeValue<>(Timestamp.class, SqlSampleValue.TIMESTAMP_VALUE);
        case DECIMAL:
        case NUMERIC:
            return new TypeValue<>(BigDecimal.class, SqlSampleValue.DECIMAL_VALUE);
        case FLOAT:
        case DOUBLE:
            return new TypeValue<>(Double.class, SqlSampleValue.DOUBLE_VALUE);
        case REAL:
            return new TypeValue<>(Float.class, SqlSampleValue.FLOAT_VALUE);
        case BIGINT:
            return new TypeValue<>(Long.class, SqlSampleValue.LONG_VALUE);
        case SMALLINT:
        case INTEGER:
        case TINYINT:
            return new TypeValue<>(Integer.class, SqlSampleValue.INTEGER_VALUE);
        case NULL:
            return null;
        case DISTINCT:
        case JAVA_OBJECT:
        case OTHER:
        case REF:
        case REF_CURSOR:
        case STRUCT:
        default:
            return new TypeValue<>(String.class, SqlSampleValue.STRING_VALUE);
        }
    }

}
