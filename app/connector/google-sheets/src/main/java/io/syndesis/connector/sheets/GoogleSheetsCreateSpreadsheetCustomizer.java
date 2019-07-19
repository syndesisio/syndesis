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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import io.syndesis.connector.sheets.model.GoogleSheet;
import io.syndesis.connector.sheets.model.GoogleSpreadsheet;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsConstants;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsApiMethod;
import org.apache.camel.util.ObjectHelper;

public class GoogleSheetsCreateSpreadsheetCustomizer implements ComponentProxyCustomizer {

    private String title;
    private String timeZone;
    private String locale;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setApiMethod(options);
        component.setBeforeProducer(this::beforeProducer);
        component.setAfterProducer(this::afterProducer);
    }

    private void setApiMethod(Map<String, Object> options) {
        title = ConnectorOptions.extractOption(options, "title");
        timeZone = ConnectorOptions.extractOption(options, "timeZone");
        locale = ConnectorOptions.extractOption(options, "locale");

        options.put("apiName",
                GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsApiMethod.class).getName());
        options.put("methodName", "create");
    }

    private void beforeProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        final GoogleSpreadsheet model = exchange.getIn().getBody(GoogleSpreadsheet.class);

        if (model != null) {
            if (ObjectHelper.isNotEmpty(model.getTitle())) {
                title = model.getTitle();
            }
            if (ObjectHelper.isNotEmpty(model.getTimeZone())) {
                timeZone = model.getTimeZone();
            }
            if (ObjectHelper.isNotEmpty(model.getLocale())) {
                locale = model.getLocale();
            }
        }

        Spreadsheet spreadsheet = new Spreadsheet();
        SpreadsheetProperties spreadsheetProperties = new SpreadsheetProperties();

        spreadsheetProperties.setTitle(title);
        spreadsheetProperties.setTimeZone(timeZone);
        spreadsheetProperties.setLocale(locale);

        spreadsheet.setProperties(spreadsheetProperties);

        if (model != null && ObjectHelper.isNotEmpty(model.getSheets())) {
            List<Sheet> sheets = new ArrayList<>();
            for (GoogleSheet sheetModel : model.getSheets()) {
                Sheet sheet = new Sheet();
                SheetProperties sheetProperties = new SheetProperties();
                sheetProperties.setSheetId(sheetModel.getSheetId());
                sheetProperties.setIndex(sheetModel.getIndex());
                sheetProperties.setTitle(sheetModel.getTitle());
                sheet.setProperties(sheetProperties);
                sheets.add(sheet);
            }
            spreadsheet.setSheets(sheets);
        }

        in.setHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "content", spreadsheet);
    }

    private void afterProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        final Spreadsheet spreadsheet = in.getBody(Spreadsheet.class);

        GoogleSpreadsheet model = new GoogleSpreadsheet();

        if (ObjectHelper.isNotEmpty(spreadsheet)) {
            model.setSpreadsheetId(spreadsheet.getSpreadsheetId());

            SpreadsheetProperties spreadsheetProperties = spreadsheet.getProperties();
            if (ObjectHelper.isNotEmpty(spreadsheetProperties)) {
                model.setTitle(spreadsheetProperties.getTitle());
                model.setUrl(spreadsheet.getSpreadsheetUrl());
                model.setTimeZone(spreadsheetProperties.getTimeZone());
                model.setLocale(spreadsheetProperties.getLocale());
            }

            List<GoogleSheet> sheets = new ArrayList<>();
            if (ObjectHelper.isNotEmpty(spreadsheet.getSheets())) {
                spreadsheet.getSheets().stream()
                        .map(Sheet::getProperties)
                        .forEach(props -> {
                            GoogleSheet sheet = new GoogleSheet();
                            sheet.setSheetId(props.getSheetId());
                            sheet.setIndex(props.getIndex());
                            sheet.setTitle(props.getTitle());
                            sheets.add(sheet);
                        });

            }
            model.setSheets(sheets);
        }

        in.setBody(model);
    }
}
