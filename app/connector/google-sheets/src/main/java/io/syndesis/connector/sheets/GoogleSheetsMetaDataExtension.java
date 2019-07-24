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

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import io.syndesis.connector.sheets.meta.GoogleValueRangeMetaData;
import io.syndesis.connector.sheets.model.RangeCoordinate;
import io.syndesis.connector.support.util.ConnectorOptions;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.DefaultMetaData;
import org.apache.camel.util.ObjectHelper;

public class GoogleSheetsMetaDataExtension extends AbstractMetaDataExtension {
    private static final MetaData EMPTY_METADATA = new DefaultMetaData(null, null, null);

    public GoogleSheetsMetaDataExtension(CamelContext camelContext) {
        super(camelContext);
    }

    @Override
    public Optional<MetaData> meta(final Map<String, Object> properties) {
        final String spreadsheetId = ConnectorOptions.extractOption(properties, "spreadsheetId", "");
        final String range = ConnectorOptions.extractOption(properties, "range", "");
        final String headerRow = ConnectorOptions.extractOption(properties, "headerRow");

        final String[] columnNames = ConnectorOptions.extractOptionAndMap(properties, "columnNames",
            names -> names.split(","), new String[]{});

        Arrays.parallelSetAll(columnNames, (i) -> columnNames[i].trim());

        if (ObjectHelper.isNotEmpty(range)) {
            final GoogleValueRangeMetaData valueRangeMetaData = new GoogleValueRangeMetaData();
            valueRangeMetaData.setSpreadsheetId(spreadsheetId);
            valueRangeMetaData.setRange(range);
            valueRangeMetaData.setHeaderRow(headerRow);
            valueRangeMetaData.setColumnNames(columnNames);

            final String majorDimension = ConnectorOptions.extractOption(properties, "majorDimension", RangeCoordinate.DIMENSION_ROWS);

            valueRangeMetaData.setMajorDimension(majorDimension);

            final boolean split = ConnectorOptions.extractOptionAndMap(properties, "splitResults",
                    Boolean::valueOf, false);

            valueRangeMetaData.setSplit(split);

            return Optional.of(new DefaultMetaData(null, null, valueRangeMetaData));
        }

        return Optional.of(EMPTY_METADATA);
    }
}
