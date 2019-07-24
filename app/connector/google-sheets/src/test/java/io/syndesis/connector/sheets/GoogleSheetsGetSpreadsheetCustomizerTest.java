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
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsApiMethod;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import io.syndesis.connector.sheets.model.GoogleSpreadsheet;
import io.syndesis.connector.support.util.ConnectorOptions;

public class GoogleSheetsGetSpreadsheetCustomizerTest extends AbstractGoogleSheetsCustomizerTestSupport {

    private GoogleSheetsGetSpreadsheetCustomizer customizer;

    @Before
    public void setupCustomizer() {
        customizer = new GoogleSheetsGetSpreadsheetCustomizer();
    }

    @Test
    public void testBeforeConsumer() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("spreadsheetId", getSpreadsheetId());

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());

        Spreadsheet spreadsheet = new Spreadsheet();
        spreadsheet.setSpreadsheetId(getSpreadsheetId());
        SpreadsheetProperties spreadsheetProperties = new SpreadsheetProperties();
        spreadsheetProperties.setTitle("SyndesisTest");
        spreadsheetProperties.setTimeZone("America/New_York");
        spreadsheetProperties.setLocale("en");
        spreadsheet.setProperties(spreadsheetProperties);

        Sheet sheet = new Sheet();
        SheetProperties sheetProperties = new SheetProperties();
        sheetProperties.setTitle("Sheet1");
        sheetProperties.setSheetId(1);
        sheetProperties.setIndex(1);
        sheet.setProperties(sheetProperties);

        spreadsheet.setSheets(Collections.singletonList(sheet));

        inbound.getIn().setBody(spreadsheet);
        getComponent().getBeforeConsumer().process(inbound);

        Assert.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsApiMethod.class).getName(), ConnectorOptions.extractOption(options, "apiName"));
        Assert.assertEquals("get", ConnectorOptions.extractOption(options, "methodName"));

        GoogleSpreadsheet model = (GoogleSpreadsheet) inbound.getIn().getBody();
        Assert.assertEquals(getSpreadsheetId(), model.getSpreadsheetId());
        Assert.assertEquals("SyndesisTest", model.getTitle());
        Assert.assertEquals("America/New_York", model.getTimeZone());
        Assert.assertEquals("en", model.getLocale());

        Assert.assertEquals(1, model.getSheets().size());
        Assert.assertEquals("Sheet1", model.getSheets().get(0).getTitle());
        Assert.assertEquals(Integer.valueOf(1), model.getSheets().get(0).getSheetId());
        Assert.assertEquals(Integer.valueOf(1), model.getSheets().get(0).getIndex());
    }

}
