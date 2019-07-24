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

import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.UpdateSpreadsheetPropertiesRequest;
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
import org.apache.camel.component.google.sheets.stream.GoogleSheetsStreamConstants;
import org.apache.camel.util.ObjectHelper;

public class GoogleSheetsUpdateSpreadsheetCustomizer implements ComponentProxyCustomizer {

    private String spreadsheetId;
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
        spreadsheetId = ConnectorOptions.extractOption(options, "spreadsheetId");
        title = ConnectorOptions.extractOption(options, "title");
        timeZone = ConnectorOptions.extractOption(options, "timeZone");
        locale = ConnectorOptions.extractOption(options, "locale");

        options.put("apiName",
                GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsApiMethod.class).getName());
        options.put("methodName", "batchUpdate");
    }

    private void beforeProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        final GoogleSpreadsheet model = exchange.getIn().getBody(GoogleSpreadsheet.class);

        if (model != null) {
            if (ObjectHelper.isNotEmpty(model.getSpreadsheetId())) {
                spreadsheetId = model.getSpreadsheetId();
            }
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

        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
        batchUpdateRequest.setIncludeSpreadsheetInResponse(true);
        batchUpdateRequest.setRequests(new ArrayList<>());

        List<String> fields = new ArrayList<>();
        SpreadsheetProperties spreadsheetProperties = getSpreadsheetProperties(fields);
        if (ObjectHelper.isNotEmpty(fields)) {
            UpdateSpreadsheetPropertiesRequest updatePropertiesRequest = new UpdateSpreadsheetPropertiesRequest();
            updatePropertiesRequest.setProperties(spreadsheetProperties);
            updatePropertiesRequest.setFields(String.join(",", fields));
            batchUpdateRequest.getRequests().add(new Request().setUpdateSpreadsheetProperties(updatePropertiesRequest));
        }

        if (model != null && ObjectHelper.isNotEmpty(model.getSheets())) {
            for (GoogleSheet sheet : model.getSheets()) {
                UpdateSheetPropertiesRequest updateSheetPropertiesRequest = new UpdateSheetPropertiesRequest();
                SheetProperties properties = new SheetProperties();
                properties.setSheetId(sheet.getSheetId());
                properties.setIndex(sheet.getIndex());
                properties.setTitle(sheet.getTitle());
                updateSheetPropertiesRequest.setProperties(properties);
                updateSheetPropertiesRequest.setFields("title");
                batchUpdateRequest.getRequests().add(new Request().setUpdateSheetProperties(updateSheetPropertiesRequest));
            }
        }

        in.setHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID, spreadsheetId);
        in.setHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest", batchUpdateRequest);
    }

    private void afterProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        final BatchUpdateSpreadsheetResponse batchUpdateResponse = in.getBody(BatchUpdateSpreadsheetResponse.class);

        GoogleSpreadsheet model = new GoogleSpreadsheet();
        model.setSpreadsheetId(batchUpdateResponse.getSpreadsheetId());

        if (ObjectHelper.isNotEmpty(batchUpdateResponse.getUpdatedSpreadsheet())) {
            SpreadsheetProperties spreadsheetProperties = batchUpdateResponse.getUpdatedSpreadsheet().getProperties();
            if (ObjectHelper.isNotEmpty(spreadsheetProperties)) {
                model.setTitle(spreadsheetProperties.getTitle());
                model.setUrl(batchUpdateResponse.getUpdatedSpreadsheet().getSpreadsheetUrl());
                model.setTimeZone(spreadsheetProperties.getTimeZone());
                model.setLocale(spreadsheetProperties.getLocale());
            }

            List<GoogleSheet> sheets = new ArrayList<>();
            if (ObjectHelper.isNotEmpty(batchUpdateResponse.getUpdatedSpreadsheet().getSheets())) {
                batchUpdateResponse.getUpdatedSpreadsheet().getSheets().stream()
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

    private SpreadsheetProperties getSpreadsheetProperties(List<String> fields) {
        SpreadsheetProperties spreadsheetProperties = new SpreadsheetProperties();

        if (ObjectHelper.isNotEmpty(title)) {
            spreadsheetProperties.setTitle(title);
            fields.add("title");
        }

        if (ObjectHelper.isNotEmpty(timeZone)) {
            spreadsheetProperties.setTimeZone(timeZone);
            fields.add("timeZone");
        }

        if (ObjectHelper.isNotEmpty(locale)) {
            spreadsheetProperties.setLocale(locale);
            fields.add("locale");
        }

        return spreadsheetProperties;
    }
}
