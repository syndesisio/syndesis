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
package io.syndesis.connector.ftp;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.syndesis.common.util.ErrorCategory;
import io.syndesis.common.util.SyndesisConnectorException;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

public class FtpConnectorCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {

        component.setBeforeProducer(FtpConnectorCustomizer::doBeforeProducer);

        component.setBeforeConsumer(FtpConnectorCustomizer::doBeforeConsumer);
        component.setAfterConsumer(FtpConnectorCustomizer::doAfterConsumer);
    }

    // Before Uploading or Updating a named file (pattern: to)
    private static void doBeforeProducer(Exchange exchange) throws JsonMappingException, JsonProcessingException {
        final Message in = exchange.getIn();
        final String body = in.getBody(String.class);
        if (body != null && JsonUtils.isJson(body)) {
            final FtpPayload payLoad = new ObjectMapper().readValue(body, FtpPayload.class);
            if (payLoad.getFileName()==null || payLoad.getFileName().equals("error")) {
                throw new SyndesisConnectorException(ErrorCategory.DATA_ACCESS_ERROR, "FileName '"
                       + payLoad.getFileName()  + "' could not be parsed correctly");
            } else {
                in.setHeader(Exchange.FILE_NAME, payLoad.getFileName());
                in.setBody(payLoad.getFileContent());
            }
        }
    }

    // Before Downloading a named file (pattern: pollEnrich)
    private static void doBeforeConsumer(Exchange exchange) throws JsonMappingException, JsonProcessingException {
        final Message in = exchange.getIn();
        final String body = in.getBody(String.class);
        if (body != null && JsonUtils.isJson(body)) {
            FtpPayload payLoad = new ObjectMapper().readValue(in.getBody(String.class), FtpPayload.class);
            if (payLoad.getFileName()==null || payLoad.getFileName().equals("error")) {
                throw new SyndesisConnectorException(ErrorCategory.DATA_ACCESS_ERROR, "FileName"
                        + " could not be parsed correctly");
            } else {
                exchange.getIn().setHeader(Exchange.FILE_NAME, payLoad.getFileName());
            }
        }
    }

    // After Downloading a named file (pattern: pollEnrich)
    private static void doAfterConsumer(Exchange exchange) throws JsonMappingException, JsonProcessingException {
        if (exchange.getException()!=null) {
            throw SyndesisConnectorException.wrap(
                    ErrorCategory.CONNECTOR_ERROR, exchange.getException());
        }
        final Message in = exchange.getIn();
        final String body = in.getBody(String.class);
        final String fileName = in.getHeader(Exchange.FILE_NAME, String.class);
        if (body!=null) {
            final FtpPayload payLoad = new FtpPayload(body);
            payLoad.setFileName(fileName);
            final String jsonPayload = new ObjectMapper().writeValueAsString(payLoad);
            in.setBody(jsonPayload);
        } else {
            final String detailedMsg = "File '" + fileName + "' was not found on the FTP server";
            throw new SyndesisConnectorException(ErrorCategory.ENTITY_NOT_FOUND_ERROR, detailedMsg);
        }
    }

}
