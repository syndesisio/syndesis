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
package io.syndesis.connector.apiprovider;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import io.syndesis.common.util.Properties;
import io.syndesis.connector.support.util.ConnectorOptions;


public class ApiProviderOnExceptionHandler implements Processor, Properties {

    private static final String HTTP_RESPONSE_CODE_PROPERTY        = "httpResponseCode";
    private static final String HTTP_ERROR_RESPONSE_CODES_PROPERTY = "errorResponseCodes";
    private static final String ERROR_RESPONSE_BODY                = "returnBody";

    Map<String, String> errorResponseCodeMappings;
    Boolean isReturnBody;
    Integer httpResponseStatus;

    @Override
    public void process(Exchange exchange) {
        ErrorStatusInfo statusInfo =
                ErrorMapper.mapError(exchange.getException(), errorResponseCodeMappings, httpResponseStatus);
        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, statusInfo.getResponseCode());
        if (isReturnBody) {
            exchange.getIn().setBody(statusInfo.toJson());
        } else {
            exchange.getIn().setBody("");
        }
        exchange.setProperty(Exchange.ERRORHANDLER_HANDLED, Boolean.TRUE);
    }

    @Override
    public void setProperties(Map<String, String> configuredProperties) {
        errorResponseCodeMappings = ErrorMapper.jsonToMap(
                ConnectorOptions.extractOptionAndMap(configuredProperties, HTTP_ERROR_RESPONSE_CODES_PROPERTY, String::valueOf, ""));
        isReturnBody =
                ConnectorOptions.extractOptionAndMap(configuredProperties, ERROR_RESPONSE_BODY, Boolean::valueOf, false);
        httpResponseStatus =
                ConnectorOptions.extractOptionAndMap(configuredProperties, HTTP_RESPONSE_CODE_PROPERTY, Integer::valueOf, 200);

    }
}
