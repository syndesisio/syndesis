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

import io.syndesis.connector.sheets.model.GoogleValueRange;
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

@RunWith(Parameterized.class)
public class GoogleSheetsGetValuesSplitResultsCustomizerTest extends AbstractGoogleSheetsCustomizerTestSupport {

    private GoogleSheetsGetValuesCustomizer customizer;

    private final String range;
    private final String sheetName;
    private final List<Object> values;
    private final String expectedValueModel;

    public GoogleSheetsGetValuesSplitResultsCustomizerTest(String range, String sheetName, List<Object> values, String expectedValueModel) {
        this.range = range;
        this.sheetName = sheetName;
        this.values = values;
        this.expectedValueModel = expectedValueModel;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "A1", "Sheet1", Collections.singletonList("a1"), "[\"a1\"]"},
                { "A1:A5", "Sheet1", Arrays.asList("a1", "a2", "a3", "a4", "a5"), "[\"a1\",\"a2\",\"a3\",\"a4\",\"a5\"]"},
                { "A1:B2", "Sheet1", Arrays.asList("a1", "b1"), "[\"a1\",\"b1\"]"},
                { "A1:B2", "Sheet1", Arrays.asList("a1", "a2"), "[\"a1\",\"a2\"]"}
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
        options.put("splitResults", true);

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());

        inbound.getIn().setHeader(GoogleSheetsStreamConstants.RANGE, sheetName + "!" + range);
        inbound.getIn().setHeader(GoogleSheetsStreamConstants.RANGE_INDEX, 1);
        inbound.getIn().setHeader(GoogleSheetsStreamConstants.VALUE_INDEX, 1);

        inbound.getIn().setBody(values);
        getComponent().getBeforeConsumer().process(inbound);

        Assert.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsValuesApiMethod.class).getName(), options.get("apiName"));
        Assert.assertEquals("get", options.get("methodName"));

        GoogleValueRange model = (GoogleValueRange) inbound.getIn().getBody();
        Assert.assertEquals(getSpreadsheetId(), model.getSpreadsheetId());
        Assert.assertEquals(sheetName + "!" + range, model.getRange());
        Assert.assertEquals(expectedValueModel, model.getValues());
    }
}