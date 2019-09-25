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
package io.syndesis.connector.box.customizer;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import io.syndesis.connector.box.BoxFile;
import io.syndesis.connector.box.BoxFileDownload;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoxDownloadCustomizer implements ComponentProxyCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(BoxDownloadCustomizer.class);

    private String fileId;
    private String encoding;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        fileId = ConnectorOptions.extractOption(options, "fileId");
        encoding = ConnectorOptions.extractOption(options, "encoding");
        component.setBeforeProducer(this::beforeProducer);
        component.setAfterProducer(this::afterProducer);
    }

    private void beforeProducer(Exchange exchange) {
        Message in = exchange.getIn();
        // retrieve the BoxFileDownload object mapping if exists
        BoxFileDownload boxFileInfo = in.getBody(BoxFileDownload.class);
        if (boxFileInfo != null && boxFileInfo.getFileId() != null) {
            fileId = boxFileInfo.getFileId();
        }
        if (fileId != null) {
            in.setHeader("CamelBox.fileId", fileId);
            in.setBody(new ByteArrayOutputStream());
        } else {
            LOG.error("There is a missing CamelBox.fileId parameter, you should set the File ID parameter in the Box Download step parameter or add a data mapping step to set it.");
        }
    }

    private void afterProducer(Exchange exchange) {
        BoxFile file = new BoxFile();
        Message in = exchange.getIn();
        file.setId(fileId);
        ByteArrayOutputStream output = in.getBody(ByteArrayOutputStream.class);
        try {
            file.setContent(new String(output.toByteArray(), encoding));
        } catch (UnsupportedEncodingException e) {
            LOG.error("Failed to convert file content to String. Invalid file encoding: {}", encoding);
        }
        file.setSize(output.size());
        in.setBody(file);
    }
}
