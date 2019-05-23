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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Json;
import io.syndesis.connector.sheets.meta.GoogleSheetsMetaDataHelper;
import io.syndesis.connector.sheets.meta.GoogleValueRangeMetaData;
import io.syndesis.connector.sheets.model.RangeCoordinate;
import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import org.apache.camel.util.ObjectHelper;

public final class GoogleSheetsMetadataRetrieval extends ComponentMetadataRetrieval {

    private static final String SHEETS_GET_VALUES_ACTION = "io.syndesis:sheets-get-values-connector";
    private static final String SHEETS_RETRIEVE_VALUES_ACTION = "io.syndesis:sheets-retrieve-values-connector";
    private static final String SHEETS_UPDATE_VALUES_ACTION = "io.syndesis:sheets-update-values-connector";
    private static final String SHEETS_APPEND_VALUES_ACTION = "io.syndesis:sheets-append-values-connector";

    @Override
    protected SyndesisMetadata adapt(CamelContext context, String componentId, String actionId, Map<String, Object> properties, MetaData metadata) {
        @SuppressWarnings("unchecked")
        final GoogleValueRangeMetaData valueRangeMetaData = (GoogleValueRangeMetaData) metadata.getPayload();

        if (valueRangeMetaData != null) {
            if (valueRangeMetaData.getColumnNames().length == 0) {
                String columnNames;
                Map<String, List<PropertyPair>> propertySuggestions = new HashMap<>();
                if (ObjectHelper.isNotEmpty(valueRangeMetaData.getSpreadsheetId()) &&
                        ObjectHelper.isNotEmpty(valueRangeMetaData.getHeaderRow())) {
                    columnNames = GoogleSheetsMetaDataHelper.fetchHeaderRow(valueRangeMetaData.getSpreadsheetId(), valueRangeMetaData.getRange(), valueRangeMetaData.getHeaderRow(), properties);
                } else {
                    RangeCoordinate coordinate = RangeCoordinate.fromRange(valueRangeMetaData.getRange());
                    columnNames = coordinate.getColumnNames();
                }

                propertySuggestions.put("columnNames", Collections.singletonList(new PropertyPair(columnNames)));
                return SyndesisMetadata.of(propertySuggestions);
            }

            final JsonSchema spec = GoogleSheetsMetaDataHelper.createSchema(valueRangeMetaData.getRange(),
                    Optional.ofNullable(valueRangeMetaData.getMajorDimension()).orElse(RangeCoordinate.DIMENSION_ROWS),
                    valueRangeMetaData.isSplit(), valueRangeMetaData.getColumnNames());

            try {
                DataShape.Builder inputShapeBuilder = new DataShape.Builder().type("VALUE_RANGE_PARAM_IN");
                if (ObjectHelper.isEqualToAny(actionId, SHEETS_UPDATE_VALUES_ACTION, SHEETS_APPEND_VALUES_ACTION)) {
                    inputShapeBuilder.kind(DataShapeKinds.JSON_SCHEMA)
                            .name("ValueRange Parameter")
                            .description(String.format("Parameters of range [%s]", valueRangeMetaData.getRange()))
                            .specification(Json.writer().writeValueAsString(spec));

                    applyMetadata(inputShapeBuilder, spec);
                } else {
                    inputShapeBuilder.kind(DataShapeKinds.NONE);
                }

                DataShape.Builder outputShapeBuilder = new DataShape.Builder().type("VALUE_RANGE_PARAM_OUT");
                if (ObjectHelper.isEqualToAny(actionId, SHEETS_GET_VALUES_ACTION, SHEETS_RETRIEVE_VALUES_ACTION)) {
                    outputShapeBuilder.kind(DataShapeKinds.JSON_SCHEMA)
                            .name("ValueRange Result")
                            .description(String.format("Results of range [%s]", valueRangeMetaData.getRange()))
                            .specification(Json.writer().writeValueAsString(spec));

                    applyMetadata(outputShapeBuilder, spec);
                } else {
                    outputShapeBuilder.kind(DataShapeKinds.NONE);
                }

                return SyndesisMetadata.of(inputShapeBuilder.build(), outputShapeBuilder.build());
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        } else {
            return SyndesisMetadata.EMPTY;
        }
    }

    private void applyMetadata(DataShape.Builder builder, JsonSchema spec) {
        if (spec.isObjectSchema()) {
            builder.putMetadata("variant", "element");
        }

        if (spec.isArraySchema()) {
            builder.putMetadata("variant", "collection");
        }
    }

    @Override
    protected MetaDataExtension resolveMetaDataExtension(CamelContext context, Class<? extends MetaDataExtension> metaDataExtensionClass, String componentId, String actionId) {
        return new GoogleSheetsMetaDataExtension(context);
    }
}
