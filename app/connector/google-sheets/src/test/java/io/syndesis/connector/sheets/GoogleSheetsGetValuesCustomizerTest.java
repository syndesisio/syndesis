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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsValuesApiMethod;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.syndesis.connector.sheets.model.RangeCoordinate;
import io.syndesis.connector.support.util.ConnectorOptions;

@RunWith(Parameterized.class)
public class GoogleSheetsGetValuesCustomizerTest extends AbstractGoogleSheetsCustomizerTestSupport {

    private GoogleSheetsGetValuesCustomizer customizer;

    private final String range;
    private final String sheetName;
    private final String majorDimension;
    private final List<List<Object>> values;
    private final List<String> expectedValueModel;

    public GoogleSheetsGetValuesCustomizerTest(String range, String sheetName, String majorDimension, List<List<Object>> values, List<String> expectedValueModel) {
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
                        Arrays.asList("{\"spreadsheetId\":\"%s\", \"A\":\"a1\"}",
                                      "{\"spreadsheetId\":\"%s\", \"A\":\"a2\"}",
                                      "{\"spreadsheetId\":\"%s\", \"A\":\"a3\"}",
                                      "{\"spreadsheetId\":\"%s\", \"A\":\"a4\"}",
                                      "{\"spreadsheetId\":\"%s\", \"A\":\"a5\"}")},
                { "A1:A5", "Sheet1", RangeCoordinate.DIMENSION_COLUMNS, Collections.singletonList(Arrays.asList("a1", "a2", "a3", "a4", "a5")),
                        Collections.singletonList("{\"spreadsheetId\":\"%s\", \"#1\":\"a1\",\"#2\":\"a2\",\"#3\":\"a3\",\"#4\":\"a4\",\"#5\":\"a5\"}")},
                { "A1:B2", "Sheet1", RangeCoordinate.DIMENSION_ROWS, Arrays.asList(Arrays.asList("a1", "b1"), Arrays.asList("a2", "b2")),
                        Arrays.asList("{\"spreadsheetId\":\"%s\", \"A\":\"a1\",\"B\":\"b1\"}",
                                      "{\"spreadsheetId\":\"%s\", \"A\":\"a2\",\"B\":\"b2\"}")},
                { "A1:B2", "Sheet1", RangeCoordinate.DIMENSION_COLUMNS, Arrays.asList(Arrays.asList("a1", "a2"), Arrays.asList("b1", "b2")),
                        Arrays.asList("{\"spreadsheetId\":\"%s\", \"#1\":\"a1\",\"#2\":\"a2\"}",
                                      "{\"spreadsheetId\":\"%s\", \"#1\":\"b1\",\"#2\":\"b2\"}")}
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

        ValueRange valueRange = new ValueRange();
        valueRange.setRange(sheetName + "!" + range);
        valueRange.setMajorDimension(majorDimension);
        valueRange.setValues(values);

        inbound.getIn().setBody(valueRange);
        getComponent().getBeforeConsumer().process(inbound);

        Assert.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsValuesApiMethod.class).getName(), ConnectorOptions.extractOption(options, "apiName"));
        Assert.assertEquals("get", ConnectorOptions.extractOption(options, "methodName"));

        @SuppressWarnings("unchecked")
        List<String> model = inbound.getIn().getBody(List.class);
        Assert.assertEquals(expectedValueModel.size(), model.size());
        Iterator<String> modelIterator = model.iterator();
        for (String expected : expectedValueModel) {
            JSONAssert.assertEquals(String.format(expected, getSpreadsheetId()), modelIterator.next(), JSONCompareMode.STRICT);
        }
    }
}
