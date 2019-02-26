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
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import io.syndesis.connector.sheets.model.GoogleSheet;
import io.syndesis.connector.sheets.model.GoogleSpreadsheet;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsApiMethod;
import org.apache.camel.util.ObjectHelper;

public class GoogleSheetsGetSpreadsheetCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setApiMethod(options);
        component.setBeforeConsumer(this::beforeConsumer);
    }

    private void setApiMethod(Map<String, Object> options) {
        options.put("apiName",
                GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsApiMethod.class).getName());
        options.put("methodName", "get");
    }

    private void beforeConsumer(Exchange exchange) {
        final Message in = exchange.getIn();
        final Spreadsheet spreadsheet = exchange.getIn().getBody(Spreadsheet.class);

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
