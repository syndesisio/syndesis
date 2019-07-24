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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.StringJoiner;
import org.apache.camel.component.google.sheets.GoogleSheetsClientFactory;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.syndesis.connector.sheets.GoogleSheetsConnectorHelper;
import io.syndesis.connector.sheets.model.CellCoordinate;
import io.syndesis.connector.sheets.model.RangeCoordinate;
import io.syndesis.connector.support.util.ConnectorOptions;

/**
 * @author Christoph Deppisch
 */
public final class GoogleSheetsMetaDataHelper {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(GoogleSheetsMetaDataHelper.class);
    private static final String JSON_SCHEMA_ORG_SCHEMA = "http://json-schema.org/schema#";

    /**
     * Prevent instantiation for utility class.
     */
    private GoogleSheetsMetaDataHelper() {
        super();
    }

    public static JsonSchema createSchema(String range, String majorDimension, String ... columnNames) {
        return createSchema(range, majorDimension, false, columnNames);
    }

    public static JsonSchema createSchema(String range, String majorDimension, boolean split, String ... columnNames) {
        ObjectSchema spec = new ObjectSchema();

        spec.setTitle("VALUE_RANGE");
        spec.putProperty("spreadsheetId", new JsonSchemaFactory().stringSchema());

        RangeCoordinate coordinate = RangeCoordinate.fromRange(range);
        if (ObjectHelper.equal(RangeCoordinate.DIMENSION_ROWS, majorDimension)) {
            createSchemaFromRowDimension(spec, coordinate, columnNames);
        } else if (ObjectHelper.equal(RangeCoordinate.DIMENSION_COLUMNS, majorDimension)) {
            createSchemaFromColumnDimension(spec, coordinate);
        }

        if (split) {
            spec.set$schema(JSON_SCHEMA_ORG_SCHEMA);
            return spec;
        } else {
            ArraySchema arraySpec = new ArraySchema();
            arraySpec.set$schema(JSON_SCHEMA_ORG_SCHEMA);
            arraySpec.setItemsSchema(spec);
            return arraySpec;
        }
    }

    public static String fetchHeaderRow(String spreadsheetId, String range, String headerRow, Map<String, Object> properties) {
        RangeCoordinate rangeCoordinate = RangeCoordinate.fromRange(range);
        StringBuilder rangeBuilder = new StringBuilder();

        if (range.contains("!")) {
            rangeBuilder.append(range, 0, range.indexOf('!') + 1);
        }

        rangeBuilder.append(CellCoordinate.getColumnName(rangeCoordinate.getColumnStartIndex()))
                    .append(headerRow)
                    .append(':')
                    .append(CellCoordinate.getColumnName(rangeCoordinate.getColumnEndIndex()))
                    .append(headerRow);

        final String rootUrl = ConnectorOptions.extractOption(properties, "rootUrl", Sheets.DEFAULT_ROOT_URL);

        final boolean validateCertificates = ConnectorOptions.extractOptionAndMap(
            properties, "validateCertificates", Boolean::valueOf, false);

        final String serverCertificate = ConnectorOptions.extractOption(properties, "serverCertificate", "");

        try {
            final GoogleSheetsClientFactory clientFactory = GoogleSheetsConnectorHelper.createClientFactory(rootUrl, serverCertificate, validateCertificates);
            Sheets client = GoogleSheetsConnectorHelper.makeClient(clientFactory, properties);

            ValueRange valueRange = client.spreadsheets().values().get(spreadsheetId, rangeBuilder.toString()).execute();
            if (ObjectHelper.isNotEmpty(valueRange.getValues())) {
                StringJoiner joiner = new StringJoiner(",");
                valueRange.getValues().get(0).stream().map(Object::toString).forEach(joiner::add);
                return joiner.toString();
            }
        } catch (IOException | GeneralSecurityException e) {
            LOG.warn(String.format("Failed to fetch header row %s from spreadsheet %s", rangeBuilder.toString(), spreadsheetId), e);
        }

        return rangeCoordinate.getColumnNames();
    }

    /**
     * Create dynamic json schema from row dimension. If split only a single object "ROW" holding 1-n column values is
     * created. Otherwise each row results in a separate object with 1-n column values as property.
     *
     * @param spec
     * @param coordinate
     * @param columnNames
     */
    private static void createSchemaFromRowDimension(ObjectSchema spec, RangeCoordinate coordinate, String ... columnNames) {
        for (int i = coordinate.getColumnStartIndex(); i < coordinate.getColumnEndIndex(); i++) {
            spec.putProperty(CellCoordinate.getColumnName(i, coordinate.getColumnStartIndex(), columnNames), new JsonSchemaFactory().stringSchema());
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
