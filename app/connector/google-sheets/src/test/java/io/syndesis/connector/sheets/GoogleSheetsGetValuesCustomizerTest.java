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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.services.sheets.v4.model.ValueRange;
import io.syndesis.connector.sheets.model.RangeCoordinate;
import org.apache.camel.Exchange;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsValuesApiMethod;
import org.apache.camel.component.google.sheets.stream.GoogleSheetsStreamConstants;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

@RunWith(Parameterized.class)
public class GoogleSheetsGetValuesCustomizerTest extends AbstractGoogleSheetsCustomizerTestSupport {

    private GoogleSheetsGetValuesCustomizer customizer;

    private final String range;
    private final String sheetName;
    private final String majorDimension;
    private final List<List<Object>> values;
    private final String expectedValueModel;

    public GoogleSheetsGetValuesCustomizerTest(String range, String sheetName, String majorDimension, List<List<Object>> values, String expectedValueModel) {
        this.range = range;
        this.sheetName = sheetName;
        this.majorDimension = majorDimension;
        this.values = values;
        this.expectedValueModel = expectedValueModel;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "A1:A5", "Sheet1", RangeCoordinate.DIMENSION_ROWS, Arrays.asList(Collections.singletonList("a1"),
                                                            Collections.singletonList("a2"),
                                                            Collections.singletonList("a3"),
                                                            Collections.singletonList("a4"),
                                                            Collections.singletonList("a5")),
                        "{\"#1\": {\"A\":\"a1\"},\"#2\": {\"A\":\"a2\"}, \"#3\": {\"A\":\"a3\"}, \"#4\": {\"A\":\"a4\"}, \"#5\": {\"A\":\"a5\"},\"spreadsheetId\":\"%s\"}"},
                { "A1:A5", "Sheet1", RangeCoordinate.DIMENSION_COLUMNS, Collections.singletonList(Arrays.asList("a1", "a2", "a3", "a4", "a5")),
                        "{\"A\": {\"#1\":\"a1\",\"#2\":\"a2\",\"#3\":\"a3\",\"#4\":\"a4\",\"#5\":\"a5\"},\"spreadsheetId\":\"%s\"}"},
                { "A1:B2", "Sheet1", RangeCoordinate.DIMENSION_ROWS, Arrays.asList(Arrays.asList("a1", "b1"), Arrays.asList("a2", "b2")),
                        "{\"#1\": {\"A\":\"a1\",\"B\":\"b1\"},\"#2\": {\"A\":\"a2\",\"B\":\"b2\"},\"spreadsheetId\":\"%s\"}"},
                { "A1:B2", "Sheet1", RangeCoordinate.DIMENSION_COLUMNS, Arrays.asList(Arrays.asList("a1", "a2"), Arrays.asList("b1", "b2")),
                        "{\"A\": {\"#1\":\"a1\",\"#2\":\"a2\"},\"B\": {\"#1\":\"b1\",\"#2\":\"b2\"},\"spreadsheetId\":\"%s\"}"}
        });
    }

    @Before
    public void setupCustomizer() {
        customizer = new GoogleSheetsGetValuesCustomizer();
    }

    @Test
    public void testBeforeConsumer() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("spreadsheetId", getSpreadsheetId());
        options.put("range", range);
        options.put("splitResults", false);

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());

        inbound.getIn().setHeader(GoogleSheetsStreamConstants.RANGE_INDEX, 1);
        inbound.getIn().setHeader(GoogleSheetsStreamConstants.VALUE_INDEX, 1);

        ValueRange valueRange = new ValueRange();
        valueRange.setRange(sheetName + "!" + range);
        valueRange.setMajorDimension(majorDimension);
        valueRange.setValues(values);

        inbound.getIn().setBody(valueRange);
        getComponent().getBeforeConsumer().process(inbound);

        Assert.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsValuesApiMethod.class).getName(), options.get("apiName"));
        Assert.assertEquals("get", options.get("methodName"));

        String model = (String) inbound.getIn().getBody();
        JSONAssert.assertEquals(String.format(expectedValueModel, getSpreadsheetId()), model, JSONCompareMode.STRICT);
    }
}