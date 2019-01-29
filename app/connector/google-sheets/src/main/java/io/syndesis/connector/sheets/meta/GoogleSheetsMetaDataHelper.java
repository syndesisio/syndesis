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

package io.syndesis.connector.sheets.meta;

import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import io.syndesis.connector.sheets.model.CellCoordinate;
import io.syndesis.connector.sheets.model.RangeCoordinate;
import org.apache.camel.util.ObjectHelper;

/**
 * @author Christoph Deppisch
 */
public final class GoogleSheetsMetaDataHelper {

    private static final String JSON_SCHEMA_ORG_SCHEMA = "http://json-schema.org/schema#";

    /**
     * Prevent instantiation for utility class.
     */
    private GoogleSheetsMetaDataHelper() {
        super();
    }

    public static ObjectSchema createSchema(String range, String majorDimension) {
        ObjectSchema spec = new ObjectSchema();
        spec.set$schema(JSON_SCHEMA_ORG_SCHEMA);
        spec.setTitle("VALUE_RANGE");

        spec.putProperty("spreadsheetId", new JsonSchemaFactory().stringSchema());

        RangeCoordinate coordinate = RangeCoordinate.fromRange(range);
        if (ObjectHelper.equal(RangeCoordinate.DIMENSION_ROWS, majorDimension)) {
            createSchemaFromRowDimension(spec, coordinate);
        } else if (ObjectHelper.equal(RangeCoordinate.DIMENSION_COLUMNS, majorDimension)) {
            createSchemaFromColumnDimension(spec, coordinate);
        }

        return spec;
    }

    /**
     * Create dynamic json schema from row dimension. If split only a single object "ROW" holding 1-n column values is
     * created. Otherwise each row results in a separate object with 1-n column values as property.
     *
     * @param spec
     * @param coordinate
     */
    private static void createSchemaFromRowDimension(ObjectSchema spec, RangeCoordinate coordinate) {
        for (int i = coordinate.getColumnStartIndex(); i < coordinate.getColumnEndIndex(); i++) {
            spec.putProperty(CellCoordinate.getColumnName(i), new JsonSchemaFactory().stringSchema());
        }
    }

    /**
     * Create dynamic json schema from column dimension. If split only a single object "COLUMN" holding 1-n row values is
     * created. Otherwise each column results in a separate object with 1-n row values as property.
     *
     * @param spec
     * @param coordinate
     */
    private static void createSchemaFromColumnDimension(ObjectSchema spec, RangeCoordinate coordinate) {
        for (int i = coordinate.getRowStartIndex() + 1; i <= coordinate.getRowEndIndex(); i++) {
            spec.putProperty("#" + i, new JsonSchemaFactory().stringSchema());
        }
    }
}
