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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.syndesis.common.util.Json;
import io.syndesis.connector.sheets.model.GoogleValueRange;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsValuesApiMethod;
import org.apache.camel.component.google.sheets.stream.GoogleSheetsStreamConstants;
import org.apache.camel.util.ObjectHelper;

public class GoogleSheetsGetValuesCustomizer implements ComponentProxyCustomizer {

    private String spreadsheetId;
    private boolean splitResults;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setApiMethod(options);
        component.setBeforeConsumer(this::beforeConsumer);
    }

    private void setApiMethod(Map<String, Object> options) {
        spreadsheetId = (String) options.get("spreadsheetId");
        splitResults = Boolean.valueOf(Optional.ofNullable(options.get("splitResults"))
                                                .map(Object::toString)
                                                .orElse("false"));

        options.put("apiName",
                GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsValuesApiMethod.class).getName());
        options.put("methodName", "get");
    }

    private void beforeConsumer(Exchange exchange) throws JsonProcessingException {
        final Message in = exchange.getIn();

        final GoogleValueRange model = new GoogleValueRange();
        if (splitResults) {
            final List<?> values = in.getBody(List.class);
            if (values != null) {
                model.setSpreadsheetId(spreadsheetId);

                if (ObjectHelper.isNotEmpty(in.getHeader(GoogleSheetsStreamConstants.RANGE))) {
                    model.setRange(in.getHeader(GoogleSheetsStreamConstants.RANGE).toString());
                }

                model.setValues(Json.writer().writeValueAsString(values));
            }
        } else {
            final ValueRange valueRange = in.getBody(ValueRange.class);
            if (valueRange != null) {
                model.setSpreadsheetId(spreadsheetId);
                model.setRange(valueRange.getRange());
                model.setValues(Json.writer().writeValueAsString(valueRange.getValues()));
            }
        }

        model.setRangeIndex(Optional.ofNullable(in.getHeader(GoogleSheetsStreamConstants.RANGE_INDEX))
                .map(Object::toString)
                .map(Integer::valueOf)
                .orElse(0));
        model.setValueIndex(Optional.ofNullable(in.getHeader(GoogleSheetsStreamConstants.VALUE_INDEX))
                .map(Object::toString)
                .map(Integer::valueOf)
                .orElse(0));

        in.setBody(model);
    }

}
