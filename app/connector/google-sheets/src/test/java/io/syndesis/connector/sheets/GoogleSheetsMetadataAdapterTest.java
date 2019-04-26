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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectWriter;
import io.syndesis.common.util.Json;
import io.syndesis.connector.sheets.model.RangeCoordinate;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.junit.Assert.assertTrue;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@RunWith(Parameterized.class)
public class GoogleSheetsMetadataAdapterTest {

    private final String range;
    private final String actionId;
    private final String majorDimension;
    private final String expectedJson;
    private final boolean split;
    private final String columnNames;

    public GoogleSheetsMetadataAdapterTest(String range, String actionId, String majorDimension, String expectedJson, boolean split, String columnNames) {
        this.range = range;
        this.actionId = actionId;
        this.majorDimension = majorDimension;
        this.expectedJson = expectedJson;
        this.split = split;
        this.columnNames = columnNames;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "A1:D1", "io.syndesis:sheets-get-values-connector", "", "/meta/get_rows_metadata.json", false, "A,B,C,D" },
                { "A1:D1", "io.syndesis:sheets-get-values-connector", RangeCoordinate.DIMENSION_ROWS, "/meta/get_rows_split_metadata.json", true, "A,B,C,D" },
                { "A1:D1", "io.syndesis:sheets-get-values-connector", RangeCoordinate.DIMENSION_ROWS, "/meta/get_rows_metadata.json", false, "A,B,C,D" },
                { "A1:C5", "io.syndesis:sheets-get-values-connector", RangeCoordinate.DIMENSION_COLUMNS, "/meta/get_columns_split_metadata.json", true, "A,B,C" },
                { "A1:A5", "io.syndesis:sheets-get-values-connector", RangeCoordinate.DIMENSION_COLUMNS, "/meta/get_columns_metadata.json", false, "A" },
                { "A5:C5", "io.syndesis:sheets-update-values-connector", RangeCoordinate.DIMENSION_ROWS, "/meta/update_rows_metadata.json", false, "A,B,C" },
                { "A5:C5", "io.syndesis:sheets-update-values-connector", RangeCoordinate.DIMENSION_COLUMNS, "/meta/update_columns_metadata.json", false, "A,B,C" },
                { "A1:G3", "io.syndesis:sheets-append-values-connector", RangeCoordinate.DIMENSION_ROWS, "/meta/append_rows_metadata.json", false, "A,B,C,D,E,F,G" },
                { "A1:G3", "io.syndesis:sheets-append-values-connector", RangeCoordinate.DIMENSION_COLUMNS, "/meta/append_columns_metadata.json", false, "A,B,C,D,E,F,G" },
                { "A1:D1", "io.syndesis:sheets-get-values-connector", RangeCoordinate.DIMENSION_ROWS, "/meta/column_names_metadata.json", false, "Col_A,Col_B,Col_C,Col_D" },
                { "A1:D1", "io.syndesis:sheets-get-values-connector", RangeCoordinate.DIMENSION_ROWS, "/meta/column_names_split_metadata.json", true, "Col_A,Col_B,Col_C,Col_D" }
        });
    }

    @Test
    public void adaptForMetadataTest() throws IOException, JSONException {
        CamelContext camelContext = new DefaultCamelContext();
        GoogleSheetsMetaDataExtension ext = new GoogleSheetsMetaDataExtension(camelContext);
        Map<String,Object> parameters = new HashMap<>();

        if (ObjectHelper.isNotEmpty(majorDimension)) {
            parameters.put("majorDimension", majorDimension);
        }

        if (split) {
            parameters.put("splitResults", true);
        }

        if (ObjectHelper.isNotEmpty(columnNames)) {
            parameters.put("columnNames", columnNames);
        }

        parameters.put("range", range);
        Optional<MetaData> metadata = ext.meta(parameters);
        assertTrue(metadata.isPresent());

        GoogleSheetsMetadataRetrieval adapter = new GoogleSheetsMetadataRetrieval();
        SyndesisMetadata syndesisMetaData = adapter.adapt(camelContext, "sheets", actionId, parameters, metadata.get());
        String expectedMetadata = IOUtils.toString(this.getClass().getResource(expectedJson), StandardCharsets.UTF_8).trim();
        ObjectWriter writer = Json.writer();
        String actualMetadata = writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValueAsString(syndesisMetaData);
        assertEquals(expectedMetadata, actualMetadata, JSONCompareMode.STRICT);
    }
}
