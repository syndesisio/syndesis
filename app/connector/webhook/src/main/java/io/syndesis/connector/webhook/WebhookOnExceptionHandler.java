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
package io.syndesis.connector.webhook;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syndesis.common.util.Properties;
import io.syndesis.connector.support.processor.ErrorMapper;
import io.syndesis.connector.support.processor.ErrorStatusInfo;
import io.syndesis.connector.support.util.ConnectorOptions;


public class WebhookOnExceptionHandler implements Processor, Properties {

    private static final String HTTP_RESPONSE_CODE_PROPERTY        = "httpResponseCode";
    private static final String HTTP_ERROR_RESPONSE_CODES_PROPERTY = "errorResponseCodes";
    private static final String ERROR_RESPONSE_BODY                = "returnBody";
    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookOnExceptionHandler.class);

    Map<String, Integer> errorResponseCodeMappings;
    Boolean isReturnBody;
    Integer httpResponseStatus;

    @Override
    public void process(Exchange exchange) {
        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        LOGGER.debug("Webhook exception handler processing exception on exchange", cause);
        ErrorStatusInfo statusInfo =
                ErrorMapper.mapError(cause, errorResponseCodeMappings, httpResponseStatus);
        exchange.getMessage().removeHeaders("*");
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, statusInfo.getHttpResponseCode());
        if (isReturnBody) {
            exchange.getMessage().setBody(statusInfo.toJson());
        } else {
            exchange.getMessage().setBody("");
        }
        LOGGER.info(String.format("Error response: %s - %s", statusInfo.getError(), statusInfo.getMessage()));
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
