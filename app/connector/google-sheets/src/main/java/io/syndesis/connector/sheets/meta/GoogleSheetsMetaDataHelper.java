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

    public static ObjectSchema createSchema(String range, String majorDimension, boolean split) {
        ObjectSchema spec = new ObjectSchema();
        spec.set$schema(JSON_SCHEMA_ORG_SCHEMA);
        spec.setTitle("VALUE_RANGE");

        spec.putProperty("spreadsheetId", new JsonSchemaFactory().stringSchema());

        RangeCoordinate coordinate = RangeCoordinate.fromRange(range);
        if (ObjectHelper.equal(RangeCoordinate.DIMENSION_ROWS, majorDimension)) {
            createSchemaFromRowDimension(spec, coordinate, split);
        } else if (ObjectHelper.equal(RangeCoordinate.DIMENSION_COLUMNS, majorDimension)) {
            createSchemaFromColumnDimension(spec, coordinate, split);
        }

        return spec;
    }

    /**
     * Create dynamic json schema from row dimension. If split only a single object "ROW" holding 1-n column values is
     * created. Otherwise each row results in a separate object with 1-n column values as property.
     *
     * @param spec
     * @param coordinate
     * @param split
     */
    private static void createSchemaFromRowDimension(ObjectSchema spec, RangeCoordinate coordinate, boolean split) {
        if (split) {
            ObjectSchema rowSpec = new ObjectSchema();
            rowSpec.set$schema(JSON_SCHEMA_ORG_SCHEMA);
            rowSpec.setTitle("ROW");
            for (int i = coordinate.getColumnStartIndex(); i < coordinate.getColumnEndIndex(); i++) {
                rowSpec.putProperty(CellCoordinate.getColumnName(i), new JsonSchemaFactory().stringSchema());
            }
            spec.putProperty("#", rowSpec);
        } else {
            for (int rowIndex = coordinate.getRowStartIndex() + 1; rowIndex <= coordinate.getRowEndIndex(); rowIndex++) {
                ObjectSchema rowSpec = new ObjectSchema();
                rowSpec.set$schema(JSON_SCHEMA_ORG_SCHEMA);
                rowSpec.setTitle("ROW_" + rowIndex);
                for (int i = coordinate.getColumnStartIndex(); i < coordinate.getColumnEndIndex(); i++) {
                    rowSpec.putProperty(CellCoordinate.getColumnName(i), new JsonSchemaFactory().stringSchema());
                }

                spec.putProperty("#" + rowIndex, rowSpec);
            }
        }
    }

    /**
     * Create dynamic json schema from column dimension. If split only a single object "COLUMN" holding 1-n row values is
     * created. Otherwise each column results in a separate object with 1-n row values as property.
     *
     * @param spec
     * @param coordinate
     * @param split
     */
    private static void createSchemaFromColumnDimension(ObjectSchema spec, RangeCoordinate coordinate, boolean split) {
        if (split) {
            ObjectSchema columnSpec = new ObjectSchema();
            columnSpec.set$schema(JSON_SCHEMA_ORG_SCHEMA);
            columnSpec.setTitle("COLUMN");
            for (int i = coordinate.getRowStartIndex() + 1; i <= coordinate.getRowEndIndex(); i++) {
                columnSpec.putProperty("#" + i, new JsonSchemaFactory().stringSchema());
            }
            spec.putProperty("$", columnSpec);
        } else {
            for (int columnIndex = coordinate.getColumnStartIndex(); columnIndex < coordinate.getColumnEndIndex(); columnIndex++) {
                ObjectSchema columnSpec = new ObjectSchema();
                columnSpec.set$schema(JSON_SCHEMA_ORG_SCHEMA);
                columnSpec.setTitle(CellCoordinate.getColumnName(columnIndex));
                for (int i = coordinate.getRowStartIndex() + 1; i <= coordinate.getRowEndIndex(); i++) {
                    columnSpec.putProperty("#" + i, new JsonSchemaFactory().stringSchema());
                }

                spec.putProperty(columnSpec.getTitle(), columnSpec);
            }
        }
    }
}
