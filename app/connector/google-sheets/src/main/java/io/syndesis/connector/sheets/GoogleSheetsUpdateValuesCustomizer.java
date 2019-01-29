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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.syndesis.common.util.Json;
import io.syndesis.connector.sheets.meta.GoogleSheetsMetaDataHelper;
import io.syndesis.connector.sheets.model.RangeCoordinate;
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
    private String majorDimension;
    private String valueInputOption;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setApiMethod(options);
        component.setBeforeProducer(this::beforeProducer);
    }

    private void setApiMethod(Map<String, Object> options) {
        spreadsheetId = (String) options.get("spreadsheetId");
        range = (String) options.get("range");
        majorDimension = Optional.ofNullable(options.get("majorDimension"))
                                  .map(Object::toString)
                                  .orElse(RangeCoordinate.DIMENSION_ROWS);
        valueInputOption = Optional.ofNullable(options.get("valueInputOption"))
                                   .map(Object::toString)
                                   .orElse("USER_ENTERED");

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

    @SuppressWarnings("unchecked")
    private void beforeProducer(Exchange exchange) throws IOException {
        final Message in = exchange.getIn();

        List<String> jsonBeans = null;
        if (in.getBody() instanceof List) {
            jsonBeans = in.getBody(List.class);
        } else if (in.getBody(String.class) != null) {
            jsonBeans = Collections.singletonList(in.getBody(String.class));
        }

        ValueRange valueRange = new ValueRange();
        List<List<Object>> values = new ArrayList<>();

        if (ObjectHelper.isNotEmpty(jsonBeans)) {
            final ObjectSchema spec = GoogleSheetsMetaDataHelper.createSchema(range, majorDimension);

            for (String json : jsonBeans) {
                Map<String, Object> dataShape = Json.reader().forType(Map.class).readValue(json);

                if (dataShape.containsKey("spreadsheetId")) {
                    spreadsheetId = Optional.ofNullable(dataShape.remove("spreadsheetId"))
                            .map(Object::toString)
                            .orElse(spreadsheetId);
                }

                List<Object> rangeValues = new ArrayList<>();
                spec.getProperties()
                        .entrySet()
                        .stream()
                        .filter(specEntry -> !Arrays.asList("spreadsheetId").contains(specEntry.getKey()))
                        .forEach(specEntry -> rangeValues.add(dataShape.getOrDefault(specEntry.getKey(), null)));

                values.add(rangeValues);
            }
        }

        valueRange.setMajorDimension(majorDimension);
        valueRange.setValues(values);

        in.setHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID, spreadsheetId);
        in.setHeader(GoogleSheetsStreamConstants.RANGE, range);
        in.setHeader(GoogleSheetsStreamConstants.MAJOR_DIMENSION, majorDimension);
        in.setHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "values", valueRange);
        in.setHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "valueInputOption", valueInputOption);
    }
}
