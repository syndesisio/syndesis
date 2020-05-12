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

import java.util.Map;
import java.util.Objects;

import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;

public class BoxUploadCustomizer implements ComponentProxyCustomizer {

    private String filename;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        filename = ConnectorOptions.extractOption(options, "fileName");
        component.setBeforeProducer(this::beforeProducer);
    }

    private void beforeProducer(Exchange exchange) {
        Message in = exchange.getIn();
        if (filename == null) {
            // dropbox sets the filename as DOWNLOADED_FILE
            // ftp sets the filename as CamelFileName
            filename = retrieveFilenameFromHeader(in, "DOWNLOADED_FILE", "CamelFileName", "CamelAwsS3Key");
        }
        Objects.requireNonNull(filename, "A filename is required. You can set it in the Box connection options.");
        // in case the filename contains path separators.
        filename = filename.substring(filename.lastIndexOf('/') + 1);
        in.setHeader("CamelBox.fileName", filename);
    }

    private static String retrieveFilenameFromHeader(Message in, String ...keys) {
        String value = null;
        for (String key: keys) {
            value = in.getHeader(key, String.class);
            if (value != null) {
                break;
            }
        }
        return value;
    }

}
