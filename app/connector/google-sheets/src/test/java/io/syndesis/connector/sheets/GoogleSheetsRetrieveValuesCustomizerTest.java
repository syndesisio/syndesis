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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsConstants;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsValuesApiMethod;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.syndesis.connector.sheets.model.RangeCoordinate;
import io.syndesis.connector.support.util.ConnectorOptions;

public class GoogleSheetsRetrieveValuesCustomizerTest extends AbstractGoogleSheetsCustomizerTestSupport {

    private GoogleSheetsRetrieveValuesCustomizer customizer;

    @Before
    public void setupCustomizer() {
        customizer = new GoogleSheetsRetrieveValuesCustomizer();
    }

    @Test
    public void testBeforeProducerFromOptions() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("spreadsheetId", getSpreadsheetId());
        options.put("range", "A1");
        options.put("majorDimension", RangeCoordinate.DIMENSION_ROWS);

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());
        getComponent().getBeforeProducer().process(inbound);

        Assert.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsValuesApiMethod.class).getName(), ConnectorOptions.extractOption(options, "apiName"));
        Assert.assertEquals("get", ConnectorOptions.extractOption(options, "methodName"));

        Assert.assertEquals(getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "spreadsheetId"));
        Assert.assertEquals("A1", inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "range"));
        Assert.assertEquals(RangeCoordinate.DIMENSION_ROWS, inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "majorDimension"));
    }

    @Test
    public void testAfterProducerRowDimension() throws Exception {
        String range = "A1:A5";
        String sheetName = "Sheet1";
        String majorDimension = RangeCoordinate.DIMENSION_ROWS;

        List<List<Object>> values = Arrays.asList(Collections.singletonList("a1"),
                Collections.singletonList("a2"),
                Collections.singletonList("a3"),
                Collections.singletonList("a4"),
                Collections.singletonList("a5"));

        List<String> expectedValueModel = Arrays.asList("{\"spreadsheetId\":\"%s\", \"A\":\"a1\"}",
                "{\"spreadsheetId\":\"%s\", \"A\":\"a2\"}",
                "{\"spreadsheetId\":\"%s\", \"A\":\"a3\"}",
                "{\"spreadsheetId\":\"%s\", \"A\":\"a4\"}",
                "{\"spreadsheetId\":\"%s\", \"A\":\"a5\"}");


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
        getComponent().getAfterProducer().process(inbound);

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

    @Test
    public void testAfterProducerColumnDimension() throws Exception {
        String range = "A1:A5";
        String sheetName = "Sheet1";
        String majorDimension = RangeCoordinate.DIMENSION_COLUMNS;

        List<List<Object>> values = Collections.singletonList(Arrays.asList("a1", "a2", "a3", "a4", "a5"));
        List<String> expectedValueModel = Collections.singletonList("{\"spreadsheetId\":\"%s\", \"#1\":\"a1\",\"#2\":\"a2\",\"#3\":\"a3\",\"#4\":\"a4\",\"#5\":\"a5\"}");

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
        getComponent().getAfterProducer().process(inbound);

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
