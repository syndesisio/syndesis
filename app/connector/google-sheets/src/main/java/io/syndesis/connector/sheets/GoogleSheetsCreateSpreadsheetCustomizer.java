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

import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsApiMethod;
import org.apache.camel.util.ObjectHelper;

import java.util.Map;

public class GoogleSheetsCreateSpreadsheetCustomizer implements ComponentProxyCustomizer {

    private String title;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setApiMethod(options);
        component.setBeforeProducer(this::beforeProducer);
    }

    private void setApiMethod(Map<String, Object> options) {
        title = (String) options.get("title");

        options.put("apiName",
                GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsApiMethod.class).getName());
        options.put("methodName", "create");
    }

    private void beforeProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        final String body = exchange.getIn().getBody(String.class);

        if (ObjectHelper.isNotEmpty(body)) {
            title = body;
        }

        Spreadsheet spreadsheet = new Spreadsheet();
        SpreadsheetProperties spreadsheetProperties = new SpreadsheetProperties();

        spreadsheetProperties.setTitle(title);

        spreadsheet.setProperties(spreadsheetProperties);

        in.setHeader("CamelGoogleSheets.content", spreadsheet);
    }
}
