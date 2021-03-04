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

import java.sql.JDBCType;
import java.util.Arrays;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlMetadataRetrievalTest {

    @ParameterizedTest(name = "{index}: {0} -> {1}")
    @MethodSource("expectedSchemasForColumnTypes")
    public void shouldGenerateExpectedSchema(final JDBCType jdbcType, final JsonSchema jsonSchema) {
        assertThat(SqlMetadataRetrieval.schemaFor(jdbcType)).isEqualTo(jsonSchema);
    }

    public static Iterable<Object[]> expectedSchemasForColumnTypes() {
        final JsonSchemaFactory factory = new JsonSchemaFactory();

        final ArraySchema binary = factory.arraySchema();
        binary.setItemsSchema(factory.integerSchema());

        final StringSchema date = factory.stringSchema();
        date.setFormat(JsonValueFormat.DATE_TIME);
        return Arrays.asList(new Object[][] {
            {JDBCType.ARRAY, factory.arraySchema()},
            {JDBCType.BINARY, binary},
            {JDBCType.BLOB, binary},
            {JDBCType.LONGVARBINARY, binary},
            {JDBCType.VARBINARY, binary},
            {JDBCType.BIT, factory.booleanSchema()},
            {JDBCType.BOOLEAN, factory.booleanSchema()},
            {JDBCType.CHAR, factory.stringSchema()},
            {JDBCType.CLOB, factory.stringSchema()},
            {JDBCType.DATALINK, factory.stringSchema()},
            {JDBCType.LONGNVARCHAR, factory.stringSchema()},
            {JDBCType.LONGVARCHAR, factory.stringSchema()},
            {JDBCType.NCHAR, factory.stringSchema()},
            {JDBCType.NCLOB, factory.stringSchema()},
            {JDBCType.NVARCHAR, factory.stringSchema()},
            {JDBCType.ROWID, factory.stringSchema()},
            {JDBCType.SQLXML, factory.stringSchema()},
            {JDBCType.VARCHAR, factory.stringSchema()},
            {JDBCType.DATE, date},
            {JDBCType.TIME, date},
            {JDBCType.TIMESTAMP, date},
            {JDBCType.TIMESTAMP_WITH_TIMEZONE, date},
            {JDBCType.TIME_WITH_TIMEZONE, date},
            {JDBCType.DECIMAL, factory.numberSchema()},
            {JDBCType.DOUBLE, factory.numberSchema()},
            {JDBCType.FLOAT, factory.numberSchema()},
            {JDBCType.NUMERIC, factory.numberSchema()},
            {JDBCType.REAL, factory.numberSchema()},
            {JDBCType.INTEGER, factory.integerSchema()},
            {JDBCType.BIGINT, factory.integerSchema()},
            {JDBCType.SMALLINT, factory.integerSchema()},
            {JDBCType.TINYINT, factory.integerSchema()},
            {JDBCType.NULL, factory.nullSchema()},
            {JDBCType.DISTINCT, factory.anySchema()},
            {JDBCType.JAVA_OBJECT, factory.anySchema()},
            {JDBCType.OTHER, factory.anySchema()},
            {JDBCType.REF, factory.anySchema()},
            {JDBCType.REF_CURSOR, factory.anySchema()},
            {JDBCType.STRUCT, factory.anySchema()}
        });
    }
}
