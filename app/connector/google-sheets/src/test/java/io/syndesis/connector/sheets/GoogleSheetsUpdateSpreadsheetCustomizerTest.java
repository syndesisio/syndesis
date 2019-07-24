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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

/**
 * @author Christoph Deppisch
 */
public class GoogleSheetsUpdateSpreadsheetCustomizerTest extends AbstractGoogleSheetsCustomizerTestSupport {

    private GoogleSheetsUpdateSpreadsheetCustomizer customizer;

    @Before
    public void setupCustomizer() {
        customizer = new GoogleSheetsUpdateSpreadsheetCustomizer();
    }

    @Test
    public void testBeforeProducerFromOptions() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("spreadsheetId", getSpreadsheetId());
        options.put("title", "SyndesisTest");
        options.put("timeZone", "America/New_York");
        options.put("locale", "en");

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());
        getComponent().getBeforeProducer().process(inbound);

        Assert.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsApiMethod.class).getName(), ConnectorOptions.extractOption(options, "apiName"));
        Assert.assertEquals("batchUpdate", ConnectorOptions.extractOption(options, "methodName"));

        Assert.assertNotNull(inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assert.assertEquals(getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
        Assert.assertEquals(1, batchUpdateRequest.getRequests().size());

        UpdateSpreadsheetPropertiesRequest updateSpreadsheetPropertiesRequest = batchUpdateRequest.getRequests().get(0).getUpdateSpreadsheetProperties();
        Assert.assertEquals("title,timeZone,locale", updateSpreadsheetPropertiesRequest.getFields());
        Assert.assertEquals("SyndesisTest", updateSpreadsheetPropertiesRequest.getProperties().getTitle());
        Assert.assertEquals("America/New_York", updateSpreadsheetPropertiesRequest.getProperties().getTimeZone());
        Assert.assertEquals("en", updateSpreadsheetPropertiesRequest.getProperties().getLocale());
    }

    @Test
    public void testBeforeProducerFromModel() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        Exchange inbound = new DefaultExchange(createCamelContext());

        GoogleSpreadsheet model = new GoogleSpreadsheet();
        model.setSpreadsheetId(getSpreadsheetId());
        model.setTitle("SyndesisTest");
        model.setTimeZone("America/New_York");
        model.setLocale("en");

        GoogleSheet sheet1 = new GoogleSheet();
        sheet1.setTitle("Sheet1");
        sheet1.setSheetId(1);
        sheet1.setIndex(1);

        GoogleSheet sheet2 = new GoogleSheet();
        sheet2.setTitle("Sheet2");
        sheet2.setSheetId(2);

        model.setSheets(Arrays.asList(sheet1, sheet2));

        inbound.getIn().setBody(model);
        getComponent().getBeforeProducer().process(inbound);

        Assert.assertNotNull(inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assert.assertEquals(model.getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
        Assert.assertEquals(3, batchUpdateRequest.getRequests().size());

        UpdateSpreadsheetPropertiesRequest updateSpreadsheetPropertiesRequest = batchUpdateRequest.getRequests().get(0).getUpdateSpreadsheetProperties();
        Assert.assertEquals("title,timeZone,locale", updateSpreadsheetPropertiesRequest.getFields());
        Assert.assertEquals("SyndesisTest", updateSpreadsheetPropertiesRequest.getProperties().getTitle());
        Assert.assertEquals("America/New_York", updateSpreadsheetPropertiesRequest.getProperties().getTimeZone());
        Assert.assertEquals("en", updateSpreadsheetPropertiesRequest.getProperties().getLocale());

        UpdateSheetPropertiesRequest updateSheetPropertiesRequest = batchUpdateRequest.getRequests().get(1).getUpdateSheetProperties();
        Assert.assertEquals("title", updateSheetPropertiesRequest.getFields());
        Assert.assertEquals(Integer.valueOf(1), updateSheetPropertiesRequest.getProperties().getIndex());
        Assert.assertEquals(Integer.valueOf(1), updateSheetPropertiesRequest.getProperties().getSheetId());
        Assert.assertEquals("Sheet1", updateSheetPropertiesRequest.getProperties().getTitle());

        updateSheetPropertiesRequest = batchUpdateRequest.getRequests().get(2).getUpdateSheetProperties();
        Assert.assertEquals("title", updateSheetPropertiesRequest.getFields());
        Assert.assertNull(updateSheetPropertiesRequest.getProperties().getIndex());
        Assert.assertEquals(Integer.valueOf(2), updateSheetPropertiesRequest.getProperties().getSheetId());
        Assert.assertEquals("Sheet2", updateSheetPropertiesRequest.getProperties().getTitle());
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

        batchUpdateResponse.setUpdatedSpreadsheet(spreadsheet);

        inbound.getIn().setBody(batchUpdateResponse);
        getComponent().getAfterProducer().process(inbound);

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
