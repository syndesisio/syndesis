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

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.api.services.sheets.v4.model.ValueRange;
import io.syndesis.common.util.Json;
import io.syndesis.connector.sheets.model.GoogleValueRange;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsValuesApiMethod;
import org.apache.camel.util.ObjectHelper;

public class GoogleSheetsUpdateValuesCustomizer implements ComponentProxyCustomizer {

    private String spreadsheetId;
    private String range;
    private String values;
    private String valueInputOption;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setApiMethod(options);
        component.setBeforeProducer(this::beforeProducer);
    }

    private void setApiMethod(Map<String, Object> options) {
        spreadsheetId = (String) options.get("spreadsheetId");
        range = (String) options.get("range");
        values = Optional.ofNullable(options.get("values"))
                         .map(Object::toString)
                         .orElse("[[]]");
        valueInputOption = (String) options.get("valueInputOption");

        options.put("apiName",
                GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsValuesApiMethod.class).getName());
        options.put("methodName", getApiMethodName());
    }

    /**
     * Gets the api method name. Subclasses may override method names here.
     * @return
     */
    protected String getApiMethodName() {
        return "update";
    }

    private void beforeProducer(Exchange exchange) throws IOException {
        final Message in = exchange.getIn();
        final GoogleValueRange model = exchange.getIn().getBody(GoogleValueRange.class);

        if (model != null) {
            if (ObjectHelper.isNotEmpty(model.getValues())) {
                values = model.getValues();
            }
            if (ObjectHelper.isNotEmpty(model.getRange())) {
                range = model.getRange();
            }
            if (ObjectHelper.isNotEmpty(model.getSpreadsheetId())) {
                spreadsheetId = model.getSpreadsheetId();
            }
        }

        if (values.charAt(0) != '[') {
            values = "[[" + Arrays.stream(values.split(","))
                    .map(value -> "\"" + value + "\"")
                    .collect(Collectors.joining(",")) + "]]";
        }

        ValueRange valueRange = new ValueRange();
        valueRange.setValues(Arrays.stream((Object[][]) Json.reader().forType(Object[][].class).readValue(values))
                                    .map(Arrays::asList)
                                    .collect(Collectors.toList()));

        in.setHeader("CamelGoogleSheets.spreadsheetId", spreadsheetId);
        in.setHeader("CamelGoogleSheets.range", range);
        in.setHeader("CamelGoogleSheets.values", valueRange);
        in.setHeader("CamelGoogleSheets.valueInputOption", Optional.ofNullable(valueInputOption)
                                                                      .orElse("USER_ENTERED"));
    }
}
