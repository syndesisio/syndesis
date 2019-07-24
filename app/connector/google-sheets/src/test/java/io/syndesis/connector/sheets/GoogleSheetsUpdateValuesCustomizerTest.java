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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsConstants;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsValuesApiMethod;
import org.apache.camel.component.google.sheets.stream.GoogleSheetsStreamConstants;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.syndesis.connector.sheets.model.RangeCoordinate;
import io.syndesis.connector.support.util.ConnectorOptions;

public class GoogleSheetsUpdateValuesCustomizerTest extends AbstractGoogleSheetsCustomizerTestSupport {

    private GoogleSheetsUpdateValuesCustomizer customizer;

    @Before
    public void setupCustomizer() {
        customizer = new GoogleSheetsUpdateValuesCustomizer();
    }

    @Test
    public void testBeforeProducerFromOptions() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("spreadsheetId", getSpreadsheetId());
        options.put("range", "A1");
        options.put("valueInputOption", "RAW");

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());
        getComponent().getBeforeProducer().process(inbound);

        Assert.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsValuesApiMethod.class).getName(), ConnectorOptions.extractOption(options, "apiName"));
        Assert.assertEquals("update", ConnectorOptions.extractOption(options, "methodName"));

        Assert.assertEquals(getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assert.assertEquals("A1", inbound.getIn().getHeader(GoogleSheetsStreamConstants.RANGE));
        Assert.assertEquals(RangeCoordinate.DIMENSION_ROWS, inbound.getIn().getHeader(GoogleSheetsStreamConstants.MAJOR_DIMENSION));
        Assert.assertEquals("RAW", inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "valueInputOption"));

        ValueRange valueRange = (ValueRange) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "values");
        Assert.assertEquals(0L, valueRange.getValues().size());
    }

    @Test
    public void testBeforeProducerRowDimension() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("range", "A1:B1");

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());

        String model = "{" +
                            "\"spreadsheetId\": \"" + getSpreadsheetId() + "\"," +
                            "\"A\": \"a1\"," +
                            "\"B\": \"b1\"" +
                        "}";
        inbound.getIn().setBody(model);

        getComponent().getBeforeProducer().process(inbound);

        Assert.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsValuesApiMethod.class).getName(), ConnectorOptions.extractOption(options, "apiName"));
        Assert.assertEquals("update", ConnectorOptions.extractOption(options, "methodName"));

        Assert.assertEquals(getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assert.assertEquals("A1:B1", inbound.getIn().getHeader(GoogleSheetsStreamConstants.RANGE));
        Assert.assertEquals(RangeCoordinate.DIMENSION_ROWS, inbound.getIn().getHeader(GoogleSheetsStreamConstants.MAJOR_DIMENSION));
        Assert.assertEquals("USER_ENTERED", inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "valueInputOption"));

        ValueRange valueRange = (ValueRange) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "values");
        Assert.assertEquals(1L, valueRange.getValues().size());
        Assert.assertEquals("a1", valueRange.getValues().get(0).get(0));
        Assert.assertEquals("b1", valueRange.getValues().get(0).get(1));
    }

    @Test
    public void testBeforeProducerColumnDimension() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("range", "A1:A2");
        options.put("majorDimension", RangeCoordinate.DIMENSION_COLUMNS);

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());

        String model = "{" +
                            "\"spreadsheetId\": \"" + getSpreadsheetId() + "\"," +
                            "\"#1\": \"a1\"," +
                            "\"#2\": \"a2\"" +
                        "}";
        inbound.getIn().setBody(model);

        getComponent().getBeforeProducer().process(inbound);

        Assert.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsValuesApiMethod.class).getName(), ConnectorOptions.extractOption(options, "apiName"));
        Assert.assertEquals("update", ConnectorOptions.extractOption(options, "methodName"));

        Assert.assertEquals(getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assert.assertEquals("A1:A2", inbound.getIn().getHeader(GoogleSheetsStreamConstants.RANGE));
        Assert.assertEquals(RangeCoordinate.DIMENSION_COLUMNS, inbound.getIn().getHeader(GoogleSheetsStreamConstants.MAJOR_DIMENSION));
        Assert.assertEquals("USER_ENTERED", inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "valueInputOption"));

        ValueRange valueRange = (ValueRange) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "values");
        Assert.assertEquals(1L, valueRange.getValues().size());
        Assert.assertEquals("a1", valueRange.getValues().get(0).get(0));
        Assert.assertEquals("a2", valueRange.getValues().get(0).get(1));
    }

    @Test
    public void testBeforeProducerMultipleRows() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("range", "A1:B2");

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());

        List<String> model = Arrays.asList("{" +
                        "\"spreadsheetId\": \"" + getSpreadsheetId() + "\"," +
                        "\"A\": \"a1\"," +
                        "\"B\": \"b1\"" +
                    "}",
                    "{" +
                        "\"spreadsheetId\": \"" + getSpreadsheetId() + "\"," +
                        "\"A\": \"a2\"," +
                        "\"B\": \"b2\"" +
                    "}");
        inbound.getIn().setBody(model);

        getComponent().getBeforeProducer().process(inbound);

        Assert.assertEquals("A1:B2", inbound.getIn().getHeader(GoogleSheetsStreamConstants.RANGE));
        Assert.assertEquals(RangeCoordinate.DIMENSION_ROWS, inbound.getIn().getHeader(GoogleSheetsStreamConstants.MAJOR_DIMENSION));
        Assert.assertEquals("USER_ENTERED", inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "valueInputOption"));

        ValueRange valueRange = (ValueRange) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "values");
        Assert.assertEquals(2L, valueRange.getValues().size());
        Assert.assertEquals(2L, valueRange.getValues().get(0).size());
        Assert.assertEquals("a1", valueRange.getValues().get(0).get(0));
        Assert.assertEquals("b1", valueRange.getValues().get(0).get(1));
        Assert.assertEquals(2L, valueRange.getValues().get(1).size());
        Assert.assertEquals("a2", valueRange.getValues().get(1).get(0));
        Assert.assertEquals("b2", valueRange.getValues().get(1).get(1));
    }

    @Test
    public void testBeforeProducerMultipleColumns() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("range", "A1:B2");
        options.put("majorDimension", RangeCoordinate.DIMENSION_COLUMNS);

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());

        List<String> model = Arrays.asList("{" +
                        "\"spreadsheetId\": \"" + getSpreadsheetId() + "\"," +
                        "\"#1\": \"a1\"," +
                        "\"#2\": \"a2\"" +
                    "}",
                    "{" +
                        "\"spreadsheetId\": \"" + getSpreadsheetId() + "\"," +
                        "\"#1\": \"b1\"," +
                        "\"#2\": \"b2\"" +
                    "}");

        inbound.getIn().setBody(model);

        getComponent().getBeforeProducer().process(inbound);

        Assert.assertEquals("A1:B2", inbound.getIn().getHeader(GoogleSheetsStreamConstants.RANGE));
        Assert.assertEquals(RangeCoordinate.DIMENSION_COLUMNS, inbound.getIn().getHeader(GoogleSheetsStreamConstants.MAJOR_DIMENSION));
        Assert.assertEquals("USER_ENTERED", inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "valueInputOption"));

        ValueRange valueRange = (ValueRange) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "values");
        Assert.assertEquals(2L, valueRange.getValues().size());
        Assert.assertEquals(2L, valueRange.getValues().get(0).size());
        Assert.assertEquals("a1", valueRange.getValues().get(0).get(0));
        Assert.assertEquals("a2", valueRange.getValues().get(0).get(1));
        Assert.assertEquals(2L, valueRange.getValues().get(1).size());
        Assert.assertEquals("b1", valueRange.getValues().get(1).get(0));
        Assert.assertEquals("b2", valueRange.getValues().get(1).get(1));
    }

    @Test
    public void testBeforeProducerAutoFillColumnValues() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("range", "A1:C2");

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());

        List<String> model = Arrays.asList("{" +
                    "\"spreadsheetId\": \"" + getSpreadsheetId() + "\"," +
                    "\"A\": \"a1\"," +
                    "\"C\": \"c1\"" +
                "}",
                "{" +
                    "\"spreadsheetId\": \"" + getSpreadsheetId() + "\"," +
                    "\"A\": \"a2\"," +
                    "\"B\": \"b2\"" +
                "}");

        inbound.getIn().setBody(model);

        getComponent().getBeforeProducer().process(inbound);

        Assert.assertEquals("A1:C2", inbound.getIn().getHeader(GoogleSheetsStreamConstants.RANGE));
        Assert.assertEquals(RangeCoordinate.DIMENSION_ROWS, inbound.getIn().getHeader(GoogleSheetsStreamConstants.MAJOR_DIMENSION));
        Assert.assertEquals("USER_ENTERED", inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "valueInputOption"));

        ValueRange valueRange = (ValueRange) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "values");
        Assert.assertEquals(2L, valueRange.getValues().size());
        Assert.assertEquals(3L, valueRange.getValues().get(0).size());
        Assert.assertEquals("a1", valueRange.getValues().get(0).get(0));
        Assert.assertNull(valueRange.getValues().get(0).get(1));
        Assert.assertEquals("c1", valueRange.getValues().get(0).get(2));
        Assert.assertEquals(3L, valueRange.getValues().get(1).size());
        Assert.assertEquals("a2", valueRange.getValues().get(1).get(0));
        Assert.assertEquals("b2", valueRange.getValues().get(1).get(1));
        Assert.assertNull(valueRange.getValues().get(1).get(2));
    }

    @Test
    public void testBeforeProducerAutoFillRowValues() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("range", "A1:C3");
        options.put("majorDimension", RangeCoordinate.DIMENSION_COLUMNS);

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());

        List<String> model = Arrays.asList("{" +
                    "\"spreadsheetId\": \"" + getSpreadsheetId() + "\"," +
                    "\"#1\": \"a1\"," +
                    "\"#3\": \"c1\"" +
                "}",
                "{" +
                    "\"spreadsheetId\": \"" + getSpreadsheetId() + "\"," +
                    "\"#1\": \"a2\"," +
                    "\"#2\": \"b2\"" +
                "}");

        inbound.getIn().setBody(model);

        getComponent().getBeforeProducer().process(inbound);

        Assert.assertEquals("A1:C3", inbound.getIn().getHeader(GoogleSheetsStreamConstants.RANGE));
        Assert.assertEquals(RangeCoordinate.DIMENSION_COLUMNS, inbound.getIn().getHeader(GoogleSheetsStreamConstants.MAJOR_DIMENSION));
        Assert.assertEquals("USER_ENTERED", inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "valueInputOption"));

        ValueRange valueRange = (ValueRange) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "values");
        Assert.assertEquals(2L, valueRange.getValues().size());
        Assert.assertEquals(3L, valueRange.getValues().get(0).size());
        Assert.assertEquals("a1", valueRange.getValues().get(0).get(0));
        Assert.assertNull(valueRange.getValues().get(0).get(1));
        Assert.assertEquals("c1", valueRange.getValues().get(0).get(2));
        Assert.assertEquals(3L, valueRange.getValues().get(1).size());
        Assert.assertEquals("a2", valueRange.getValues().get(1).get(0));
        Assert.assertEquals("b2", valueRange.getValues().get(1).get(1));
        Assert.assertNull(valueRange.getValues().get(1).get(2));
    }

    @Test
    public void testBeforeProducerWithJsonArray() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("range", "A1:B2");

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());

        String body = "[{" +
                            "\"spreadsheetId\": \"" + getSpreadsheetId() + "\"," +
                            "\"A\": \"a1\"," +
                            "\"B\": \"b1\"" +
                        "}," +
                        "{" +
                            "\"spreadsheetId\": \"" + getSpreadsheetId() + "\"," +
                            "\"A\": \"a2\"," +
                            "\"B\": \"b2\"" +
                        "}]";
        inbound.getIn().setBody(body);

        getComponent().getBeforeProducer().process(inbound);

        Assert.assertEquals("A1:B2", inbound.getIn().getHeader(GoogleSheetsStreamConstants.RANGE));
        Assert.assertEquals(RangeCoordinate.DIMENSION_ROWS, inbound.getIn().getHeader(GoogleSheetsStreamConstants.MAJOR_DIMENSION));
        Assert.assertEquals("USER_ENTERED", inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "valueInputOption"));

        ValueRange valueRange = (ValueRange) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "values");
        Assert.assertEquals(2L, valueRange.getValues().size());
        Assert.assertEquals(2L, valueRange.getValues().get(0).size());
        Assert.assertEquals("a1", valueRange.getValues().get(0).get(0));
        Assert.assertEquals("b1", valueRange.getValues().get(0).get(1));
        Assert.assertEquals(2L, valueRange.getValues().get(1).size());
        Assert.assertEquals("a2", valueRange.getValues().get(1).get(0));
        Assert.assertEquals("b2", valueRange.getValues().get(1).get(1));
    }

    @Test
    public void testBeforeProducerWithJsonObject() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("range", "A1:B2");

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());

        String body = "{\"spreadsheetId\": \"" + getSpreadsheetId() + "\", \"A\": \"a1\", \"B\": \"b1\" }";
        inbound.getIn().setBody(body);

        getComponent().getBeforeProducer().process(inbound);

        Assert.assertEquals("A1:B2", inbound.getIn().getHeader(GoogleSheetsStreamConstants.RANGE));
        Assert.assertEquals(RangeCoordinate.DIMENSION_ROWS, inbound.getIn().getHeader(GoogleSheetsStreamConstants.MAJOR_DIMENSION));
        Assert.assertEquals("USER_ENTERED", inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "valueInputOption"));

        ValueRange valueRange = (ValueRange) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "values");
        Assert.assertEquals(1L, valueRange.getValues().size());
        Assert.assertEquals(2L, valueRange.getValues().get(0).size());
        Assert.assertEquals("a1", valueRange.getValues().get(0).get(0));
        Assert.assertEquals("b1", valueRange.getValues().get(0).get(1));
    }
}
