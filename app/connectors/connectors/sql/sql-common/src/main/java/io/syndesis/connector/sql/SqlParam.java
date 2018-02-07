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
package io.syndesis.connector.sql;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
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
    public class TypeValue<T> {

        private Class<T> clazz;
        private T sampleValue;
        
        public TypeValue(Class<T> clazz, T sampleValue) {
            super();
            this.clazz = clazz;
            this.sampleValue = sampleValue;
        }
    }
    
    public static class SqlSampleValue {

        static List<String> arrayValue = Arrays.asList("1","2","3");
        static byte[] binaryValue = {1,2,3};
        static String stringValue = "abc";
        static Character charValue = 'a';
        static Date dateValue = new Date(new java.util.Date().getTime());
        static Time timeValue = new Time(new java.util.Date().getTime());
        static Timestamp timestampValue = new Timestamp(new java.util.Date().getTime());
        static BigDecimal decimalValue = BigDecimal.ZERO;
        static Boolean booleanValue = Boolean.TRUE;
        static Double doubleValue = Double.valueOf(0);
        static Integer integerValue = 0;
        static Long longValue = 0l;
        static Float floatValue = 0f;
    }
    
    @SuppressWarnings({"rawtypes", "PMD.CyclomaticComplexity"})
    static TypeValue<?> javaType(final JDBCType jdbcType) {
        
        SqlParam sqlParam = new SqlParam();
        switch (jdbcType) {
        case ARRAY:
        case BINARY:
        case BLOB:
        case LONGVARBINARY:
        case VARBINARY:
            return sqlParam.new TypeValue<List>(List.class, SqlSampleValue.arrayValue);
        case BIT:
        case BOOLEAN:
            return sqlParam.new TypeValue<Boolean>(Boolean.class, SqlSampleValue.booleanValue);
        case CHAR:
            return sqlParam.new TypeValue<Character>(Character.class, SqlSampleValue.charValue);
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
            return sqlParam.new TypeValue<String>(String.class, SqlSampleValue.stringValue);
        case DATE:
            return sqlParam.new TypeValue<Date>(Date.class, SqlSampleValue.dateValue);
        case TIME:
            return sqlParam.new TypeValue<Time>(Time.class, SqlSampleValue.timeValue);
        case TIMESTAMP:
        case TIMESTAMP_WITH_TIMEZONE:
        case TIME_WITH_TIMEZONE:
            return sqlParam.new TypeValue<Timestamp>(Timestamp.class, SqlSampleValue.timestampValue);
        case DECIMAL:
        case NUMERIC:
            return sqlParam.new TypeValue<BigDecimal>(BigDecimal.class, SqlSampleValue.decimalValue);
        case FLOAT:
        case DOUBLE:
            return sqlParam.new TypeValue<Double>(Double.class, SqlSampleValue.doubleValue);
        case REAL:
            return sqlParam.new TypeValue<Float>(Float.class, SqlSampleValue.floatValue);
        case BIGINT:
            return sqlParam.new TypeValue<Long>(Long.class, SqlSampleValue.longValue);
        case SMALLINT:
        case INTEGER:
        case TINYINT:
            return sqlParam.new TypeValue<Integer>(Integer.class, SqlSampleValue.integerValue);
        case NULL:
            return null;
        case DISTINCT:
        case JAVA_OBJECT:
        case OTHER:
        case REF:
        case REF_CURSOR:
        case STRUCT:
        default:
            return sqlParam.new TypeValue<String>(String.class, SqlSampleValue.stringValue);
        }
    }

}
