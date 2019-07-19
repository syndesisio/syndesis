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
import org.apache.camel.component.google.sheets.internal.GoogleSheetsConstants;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsValuesApiMethod;
import org.apache.camel.component.google.sheets.stream.GoogleSheetsStreamConstants;
import org.apache.camel.impl.DefaultExchange;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.syndesis.connector.sheets.model.RangeCoordinate;
import io.syndesis.connector.support.util.ConnectorOptions;

@RunWith(Parameterized.class)
public class GoogleSheetsNamedColumnsTest extends AbstractGoogleSheetsCustomizerTestSupport {

    private final String range;
    private final String columnNames;
    private final List<List<Object>> values;
    private final List<String> model;

    public GoogleSheetsNamedColumnsTest(String range, List<List<Object>> values, List<String> model, String columnNames) {
        this.range = range;
        this.values = values;
        this.model = model;
        this.columnNames = columnNames;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "A1:A3", Arrays.asList(Collections.singletonList("a1"), Collections.singletonList("a2"), Collections.singletonList("a3")),
                        Arrays.asList("{\"spreadsheetId\":\"%s\", \"col_a\":\"a1\"}", "{\"spreadsheetId\":\"%s\", \"col_a\":\"a2\"}", "{\"spreadsheetId\":\"%s\", \"col_a\":\"a3\"}"), "col_a"},
                { "A1:B2", Arrays.asList(Arrays.asList("a1", "b1"), Arrays.asList("a2", "b2")),
                        Arrays.asList("{\"spreadsheetId\":\"%s\", \"col_a\":\"a1\", \"col_b\":\"b1\"}", "{\"spreadsheetId\":\"%s\", \"col_a\":\"a2\",\"col_b\":\"b2\"}"), "col_a,col_b"},
                { "A1:D1", Collections.singletonList(Arrays.asList("a1", "b1", "c1", "d1")),
                        Collections.singletonList("{\"spreadsheetId\":\"%s\", \"col_a\":\"a1\", \"col_b\":\"b1\", \"col_c\":\"c1\", \"col_d\":\"d1\"}"), "col_a,col_b,col_c,col_d"},
                { "A1:D1", Collections.singletonList(Arrays.asList("a1", "b1", "c1", "d1")),
                        Collections.singletonList("{\"spreadsheetId\":\"%s\", \"col_a\":\"a1\", \"col_b\":\"b1\", \"C\":\"c1\", \"D\":\"d1\"}"), "col_a,col_b"},
                { "E1:G1", Collections.singletonList(Arrays.asList("e1", "f1", "g1")),
                        Collections.singletonList("{\"spreadsheetId\":\"%s\", \"col_e\":\"e1\", \"col_f\":\"f1\", \"G\":\"g1\"}"), "col_e,col_f"}
        });
    }

    @Test
    public void testGetValuesCustomizer() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("spreadsheetId", getSpreadsheetId());
        options.put("range", range);
        options.put("columnNames", columnNames);
        options.put("splitResults", false);

        GoogleSheetsGetValuesCustomizer customizer = new GoogleSheetsGetValuesCustomizer();
        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());

        ValueRange valueRange = new ValueRange();
        valueRange.setRange(range);
        valueRange.setMajorDimension(RangeCoordinate.DIMENSION_ROWS);
        valueRange.setValues(values);

        inbound.getIn().setBody(valueRange);
        getComponent().getBeforeConsumer().process(inbound);

        Assert.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsValuesApiMethod.class).getName(), ConnectorOptions.extractOption(options, "apiName"));
        Assert.assertEquals("get", ConnectorOptions.extractOption(options, "methodName"));

        @SuppressWarnings("unchecked")
        List<String> body = inbound.getIn().getBody(List.class);
        Assert.assertEquals(model.size(), body.size());
        Iterator<String> modelIterator = body.iterator();
        for (String expected : model) {
            JSONAssert.assertEquals(String.format(expected, getSpreadsheetId()), modelIterator.next(), JSONCompareMode.STRICT);
        }
    }

    @Test
    public void testUpdateValuesCustomizer() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("columnNames", columnNames);
        options.put("range", range);

        GoogleSheetsUpdateValuesCustomizer customizer = new GoogleSheetsUpdateValuesCustomizer();
        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());
        inbound.getIn().setBody(model);

        getComponent().getBeforeProducer().process(inbound);

        Assertions.assertThat(inbound.getIn().getHeader(GoogleSheetsStreamConstants.RANGE)).isEqualTo(range);
        Assertions.assertThat(inbound.getIn().getHeader(GoogleSheetsStreamConstants.MAJOR_DIMENSION)).isEqualTo(RangeCoordinate.DIMENSION_ROWS);

        ValueRange valueRange = (ValueRange) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "values");
        Assertions.assertThat(valueRange.getValues()).hasSize(values.size());

        for (List<Object> rowValues : values) {
            Assertions.assertThat(valueRange.getValues()).contains(rowValues);
        }
    }

}
