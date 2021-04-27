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
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsConstants;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsApiMethod;
import org.apache.camel.component.google.sheets.stream.GoogleSheetsStreamConstants;
import org.apache.camel.impl.DefaultExchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.UpdateSpreadsheetPropertiesRequest;
import io.syndesis.connector.sheets.model.GoogleSheet;
import io.syndesis.connector.sheets.model.GoogleSpreadsheet;
import io.syndesis.connector.support.util.ConnectorOptions;

public class GoogleSheetsUpdateSpreadsheetCustomizerTest extends AbstractGoogleSheetsCustomizerTestSupport {

    public static final String SYNDESIS_TEST = "SyndesisTest";
    public static final String AMERICA_NEW_YORK = "America/New_York";
    public static final String EN = "en";
    public static final String TITLE_TIME_ZONE_LOCALE = "title,timeZone,locale";
    public static final String SHEET_1 = "Sheet1";
    public static final String SHEET_2 = "Sheet2";
    public static final String TITLE = "title";
    private GoogleSheetsUpdateSpreadsheetCustomizer customizer;

    @BeforeEach
    public void setupCustomizer() {
        customizer = new GoogleSheetsUpdateSpreadsheetCustomizer();
    }

    @Test
    public void testBeforeProducerFromOptions() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("spreadsheetId", getSpreadsheetId());
        options.put(TITLE, SYNDESIS_TEST);
        options.put("timeZone", AMERICA_NEW_YORK);
        options.put("locale", EN);

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());
        getComponent().getBeforeProducer().process(inbound);

        Assertions.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsApiMethod.class).getName(), ConnectorOptions.extractOption(options, "apiName"));
        Assertions.assertEquals("batchUpdate", ConnectorOptions.extractOption(options, "methodName"));

        Assertions.assertNotNull(inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assertions.assertEquals(getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
        Assertions.assertEquals(1, batchUpdateRequest.getRequests().size());

        UpdateSpreadsheetPropertiesRequest updateSpreadsheetPropertiesRequest = batchUpdateRequest.getRequests().get(0).getUpdateSpreadsheetProperties();
        Assertions.assertEquals(TITLE_TIME_ZONE_LOCALE, updateSpreadsheetPropertiesRequest.getFields());
        Assertions.assertEquals(SYNDESIS_TEST, updateSpreadsheetPropertiesRequest.getProperties().getTitle());
        Assertions.assertEquals(AMERICA_NEW_YORK, updateSpreadsheetPropertiesRequest.getProperties().getTimeZone());
        Assertions.assertEquals(EN, updateSpreadsheetPropertiesRequest.getProperties().getLocale());
    }

    @Test
    public void testBeforeProducerFromModel() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        Exchange inbound = new DefaultExchange(createCamelContext());

        GoogleSpreadsheet model = new GoogleSpreadsheet();
        model.setSpreadsheetId(getSpreadsheetId());
        model.setTitle(SYNDESIS_TEST);
        model.setTimeZone(AMERICA_NEW_YORK);
        model.setLocale(EN);

        GoogleSheet sheet1 = new GoogleSheet();
        sheet1.setTitle(SHEET_1);
        sheet1.setSheetId(1);
        sheet1.setIndex(1);

        GoogleSheet sheet2 = new GoogleSheet();
        sheet2.setTitle(SHEET_2);
        sheet2.setSheetId(2);

        model.setSheets(Arrays.asList(sheet1, sheet2));

        inbound.getIn().setBody(model);
        getComponent().getBeforeProducer().process(inbound);

        Assertions.assertNotNull(inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assertions.assertEquals(model.getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
        Assertions.assertEquals(3, batchUpdateRequest.getRequests().size());

        UpdateSpreadsheetPropertiesRequest updateSpreadsheetPropertiesRequest = batchUpdateRequest.getRequests().get(0).getUpdateSpreadsheetProperties();
        Assertions.assertEquals(TITLE_TIME_ZONE_LOCALE, updateSpreadsheetPropertiesRequest.getFields());
        Assertions.assertEquals(SYNDESIS_TEST, updateSpreadsheetPropertiesRequest.getProperties().getTitle());
        Assertions.assertEquals(AMERICA_NEW_YORK, updateSpreadsheetPropertiesRequest.getProperties().getTimeZone());
        Assertions.assertEquals(EN, updateSpreadsheetPropertiesRequest.getProperties().getLocale());

        UpdateSheetPropertiesRequest updateSheetPropertiesRequest = batchUpdateRequest.getRequests().get(1).getUpdateSheetProperties();
        Assertions.assertEquals(TITLE, updateSheetPropertiesRequest.getFields());
        Assertions.assertEquals(Integer.valueOf(1), updateSheetPropertiesRequest.getProperties().getIndex());
        Assertions.assertEquals(Integer.valueOf(1), updateSheetPropertiesRequest.getProperties().getSheetId());
        Assertions.assertEquals(SHEET_1, updateSheetPropertiesRequest.getProperties().getTitle());

        updateSheetPropertiesRequest = batchUpdateRequest.getRequests().get(2).getUpdateSheetProperties();
        Assertions.assertEquals(TITLE, updateSheetPropertiesRequest.getFields());
        Assertions.assertNull(updateSheetPropertiesRequest.getProperties().getIndex());
        Assertions.assertEquals(Integer.valueOf(2), updateSheetPropertiesRequest.getProperties().getSheetId());
        Assertions.assertEquals(SHEET_2, updateSheetPropertiesRequest.getProperties().getTitle());
    }

    @Test
    public void testAfterProducer() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        Exchange inbound = new DefaultExchange(createCamelContext());

        BatchUpdateSpreadsheetResponse batchUpdateResponse = new BatchUpdateSpreadsheetResponse();
        batchUpdateResponse.setSpreadsheetId(getSpreadsheetId());

        Spreadsheet spreadsheet = new Spreadsheet();
        spreadsheet.setSpreadsheetId(getSpreadsheetId());
        SpreadsheetProperties spreadsheetProperties = new SpreadsheetProperties();
        spreadsheetProperties.setTitle(SYNDESIS_TEST);
        spreadsheetProperties.setTimeZone(AMERICA_NEW_YORK);
        spreadsheetProperties.setLocale(EN);
        spreadsheet.setProperties(spreadsheetProperties);

        Sheet sheet = new Sheet();
        SheetProperties sheetProperties = new SheetProperties();
        sheetProperties.setTitle(SHEET_1);
        sheetProperties.setSheetId(1);
        sheetProperties.setIndex(1);
        sheet.setProperties(sheetProperties);

        spreadsheet.setSheets(Collections.singletonList(sheet));

        batchUpdateResponse.setUpdatedSpreadsheet(spreadsheet);

        inbound.getIn().setBody(batchUpdateResponse);
        getComponent().getAfterProducer().process(inbound);

        GoogleSpreadsheet model = (GoogleSpreadsheet) inbound.getIn().getBody();
        Assertions.assertEquals(getSpreadsheetId(), model.getSpreadsheetId());
        Assertions.assertEquals(SYNDESIS_TEST, model.getTitle());
        Assertions.assertEquals(AMERICA_NEW_YORK, model.getTimeZone());
        Assertions.assertEquals(EN, model.getLocale());

        Assertions.assertEquals(1, model.getSheets().size());
        Assertions.assertEquals(SHEET_1, model.getSheets().get(0).getTitle());
        Assertions.assertEquals(Integer.valueOf(1), model.getSheets().get(0).getSheetId());
        Assertions.assertEquals(Integer.valueOf(1), model.getSheets().get(0).getIndex());
    }
}
