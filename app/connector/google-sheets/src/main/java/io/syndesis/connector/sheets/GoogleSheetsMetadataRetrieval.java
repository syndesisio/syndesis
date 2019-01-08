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
package io.syndesis.connector.sheets;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Json;
import io.syndesis.connector.sheets.meta.GoogleValueRangeMetaData;
import io.syndesis.connector.sheets.model.CellCoordinate;
import io.syndesis.connector.sheets.model.RangeCoordinate;
import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import org.apache.camel.util.ObjectHelper;

public final class GoogleSheetsMetadataRetrieval extends ComponentMetadataRetrieval {

    private static final String JSON_SCHEMA_ORG_SCHEMA = "http://json-schema.org/schema#";

    private static final String SHEETS_GET_VALUES_ACTION = "io.syndesis:sheets-get-values-connector";
    private static final String SHEETS_UPDATE_VALUES_ACTION = "io.syndesis:sheets-update-values-connector";
    private static final String SHEETS_APPEND_VALUES_ACTION = "io.syndesis:sheets-append-values-connector";

    @Override
    protected SyndesisMetadata adapt(CamelContext context, String componentId, String actionId, Map<String, Object> properties, MetaData metadata) {
        @SuppressWarnings("unchecked")
        final GoogleValueRangeMetaData valueRangeMetaData = (GoogleValueRangeMetaData) metadata.getPayload();

        if (valueRangeMetaData != null) {
            // build the input and output schemas
            final ObjectSchema spec = new ObjectSchema();
            spec.set$schema(JSON_SCHEMA_ORG_SCHEMA);
            spec.setTitle("VALUE_RANGE");

            spec.putProperty("spreadsheetId", new JsonSchemaFactory().stringSchema());

            String majorDimension = Optional.ofNullable(valueRangeMetaData.getMajorDimension())
                                            .orElse("ROWS");

            RangeCoordinate coordinate = RangeCoordinate.fromRange(valueRangeMetaData.getRange());
            if (ObjectHelper.equal("ROWS", majorDimension)) {
                createSchemaFromRowDimension(spec, coordinate, valueRangeMetaData.isSplit());
            } else if (ObjectHelper.equal("COLUMNS", majorDimension)) {
                createSchemaFromColumnDimension(spec, coordinate, valueRangeMetaData.isSplit());
            }

            try {
                DataShape.Builder inDataShapeBuilder = new DataShape.Builder().type("VALUE_RANGE_PARAM_IN");
                if (ObjectHelper.isEqualToAny(actionId, SHEETS_UPDATE_VALUES_ACTION, SHEETS_APPEND_VALUES_ACTION)) {
                    inDataShapeBuilder.kind(DataShapeKinds.JSON_SCHEMA)
                            .name("ValueRange Parameter")
                            .description(String.format("Parameters of range [%s]", valueRangeMetaData.getRange()))
                            .specification(Json.writer().writeValueAsString(spec));
                } else {
                    inDataShapeBuilder.kind(DataShapeKinds.NONE);
                }

                DataShape.Builder outDataShapeBuilder = new DataShape.Builder().type("VALUE_RANGE_PARAM_OUT");
                if (ObjectHelper.equal(actionId, SHEETS_GET_VALUES_ACTION)) {
                    outDataShapeBuilder.kind(DataShapeKinds.JSON_SCHEMA)
                            .name("ValueRange Result")
                            .description(String.format("Results of range [%s]", valueRangeMetaData.getRange()))
                            .specification(Json.writer().writeValueAsString(spec));
                } else {
                    outDataShapeBuilder.kind(DataShapeKinds.NONE);
                }

                return SyndesisMetadata.of(inDataShapeBuilder.build(), outDataShapeBuilder.build());
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        } else {
            return SyndesisMetadata.EMPTY;
        }
    }

    /**
     * Create dynamic json schema from row dimension. If split only a single object "ROW" holding 1-n column values is
     * created. Otherwise each row results in a separate object with 1-n column values as property.
     *
     * @param spec
     * @param coordinate
     * @param split
     */
    private void createSchemaFromRowDimension(ObjectSchema spec, RangeCoordinate coordinate, boolean split) {
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
    private void createSchemaFromColumnDimension(ObjectSchema spec, RangeCoordinate coordinate, boolean split) {
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

    @Override
    protected MetaDataExtension resolveMetaDataExtension(CamelContext context, Class<? extends MetaDataExtension> metaDataExtensionClass, String componentId, String actionId) {
        return new GoogleSheetsMetaDataExtension(context);
    }
}
