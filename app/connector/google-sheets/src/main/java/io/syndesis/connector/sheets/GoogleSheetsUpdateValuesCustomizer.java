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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.api.services.sheets.v4.model.ValueRange;
import io.syndesis.common.util.Json;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsConstants;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsValuesApiMethod;
import org.apache.camel.component.google.sheets.stream.GoogleSheetsStreamConstants;
import org.apache.camel.util.ObjectHelper;

public class GoogleSheetsUpdateValuesCustomizer implements ComponentProxyCustomizer {

    private String spreadsheetId;
    private String range;
    private String valueInputOption;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setApiMethod(options);
        component.setBeforeProducer(this::beforeProducer);
    }

    private void setApiMethod(Map<String, Object> options) {
        spreadsheetId = (String) options.get("spreadsheetId");
        range = (String) options.get("range");
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
        final String model = exchange.getIn().getBody(String.class);

        ValueRange valueRange = new ValueRange();
        List<List<Object>> values = new ArrayList<>();

        if (ObjectHelper.isNotEmpty(model)) {
            Map<String, Object> dataShape = Json.reader().forType(Map.class).readValue(model);

            if (dataShape.containsKey("spreadsheetId")) {
                spreadsheetId = Optional.ofNullable(dataShape.remove("spreadsheetId"))
                                .map(Object::toString)
                                .orElse(spreadsheetId);
            }

            for(Map.Entry<String, Object> rangeEntry : dataShape.entrySet()) {
                if (rangeEntry.getValue() instanceof Map) {
                    List<Object> rangeValues = new ArrayList<>();
                    for (Object value : ((Map) rangeEntry.getValue()).values()) {
                        rangeValues.add(value);
                    }
                    values.add(rangeValues);
                }
            }
        }

        valueRange.setValues(values);

        in.setHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID, spreadsheetId);
        in.setHeader(GoogleSheetsStreamConstants.RANGE, range);
        in.setHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "values", valueRange);
        in.setHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "valueInputOption", Optional.ofNullable(valueInputOption)
                                                                      .orElse("USER_ENTERED"));
    }
}
