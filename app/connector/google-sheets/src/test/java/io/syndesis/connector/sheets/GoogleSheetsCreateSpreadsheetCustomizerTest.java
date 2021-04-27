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
import org.apache.camel.component.google.sheets.internal.GoogleSheetsConstants;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsApiMethod;
import org.apache.camel.impl.DefaultExchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import io.syndesis.connector.sheets.model.GoogleSheet;
import io.syndesis.connector.sheets.model.GoogleSpreadsheet;
import io.syndesis.connector.support.util.ConnectorOptions;

public class GoogleSheetsCreateSpreadsheetCustomizerTest extends AbstractGoogleSheetsCustomizerTestSupport {

    private GoogleSheetsCreateSpreadsheetCustomizer customizer;

    @BeforeEach
    public void setupCustomizer() {
        customizer = new GoogleSheetsCreateSpreadsheetCustomizer();
    }

    @Test
    public void testBeforeProducerFromOptions() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("title", "SyndesisTest");
        options.put("timeZone", "America/New_York");
        options.put("locale", "en");

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());
        getComponent().getBeforeProducer().process(inbound);

        Assertions.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsApiMethod.class).getName(), ConnectorOptions.extractOption(options, "apiName"));
        Assertions.assertEquals("create", ConnectorOptions.extractOption(options, "methodName"));

        Spreadsheet spreadsheet = (Spreadsheet) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "content");
        Assertions.assertNull(spreadsheet.getSpreadsheetId());
        Assertions.assertEquals("SyndesisTest", spreadsheet.getProperties().getTitle());
        Assertions.assertEquals("America/New_York", spreadsheet.getProperties().getTimeZone());
        Assertions.assertEquals("en", spreadsheet.getProperties().getLocale());

        Assertions.assertNull(spreadsheet.getSheets());
    }

    @Test
    public void testBeforeProducerFromModel() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        Exchange inbound = new DefaultExchange(createCamelContext());

        GoogleSpreadsheet model = new GoogleSpreadsheet();
        model.setTitle("SyndesisTest");
        model.setTimeZone("America/New_York");
        model.setLocale("en");

        GoogleSheet sheetModel = new GoogleSheet();
        sheetModel.setTitle("Sheet1");
        sheetModel.setSheetId(1);
        sheetModel.setIndex(1);

        model.setSheets(Collections.singletonList(sheetModel));

        inbound.getIn().setBody(model);
        getComponent().getBeforeProducer().process(inbound);

        Spreadsheet spreadsheet = (Spreadsheet) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "content");
        Assertions.assertNull(spreadsheet.getSpreadsheetId());
        Assertions.assertEquals("SyndesisTest", spreadsheet.getProperties().getTitle());
        Assertions.assertEquals("America/New_York", spreadsheet.getProperties().getTimeZone());
        Assertions.assertEquals("en", spreadsheet.getProperties().getLocale());

        Assertions.assertNotNull(spreadsheet.getSheets());
        Assertions.assertEquals(1, spreadsheet.getSheets().size());
        Assertions.assertEquals("Sheet1", spreadsheet.getSheets().get(0).getProperties().getTitle());
    }

    @Test
    public void testAfterProducer() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

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
        getComponent().getAfterProducer().process(inbound);

        GoogleSpreadsheet model = (GoogleSpreadsheet) inbound.getIn().getBody();
        Assertions.assertEquals(getSpreadsheetId(), model.getSpreadsheetId());
        Assertions.assertEquals("SyndesisTest", model.getTitle());
        Assertions.assertEquals("America/New_York", model.getTimeZone());
        Assertions.assertEquals("en", model.getLocale());

        Assertions.assertEquals(1, model.getSheets().size());
        Assertions.assertEquals("Sheet1", model.getSheets().get(0).getTitle());
        Assertions.assertEquals(Integer.valueOf(1), model.getSheets().get(0).getSheetId());
        Assertions.assertEquals(Integer.valueOf(1), model.getSheets().get(0).getIndex());
    }

}
