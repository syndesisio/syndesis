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

import java.io.File;
import java.util.Map;

import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Message;
import org.apache.camel.component.file.GenericFile;

public class BoxUploadCustomizer implements ComponentProxyCustomizer {

    private String filename;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        filename = ConnectorOptions.extractOption(options, "fileName");
        component.setBeforeProducer(this::beforeProducer);
    }

    @SuppressWarnings("unchecked")
    private void beforeProducer(Exchange exchange) throws InvalidPayloadException {
        Message in = exchange.getIn();
        if (filename == null) {
            GenericFile<File> genericFile = in.getMandatoryBody(GenericFile.class);
            filename = genericFile.getFileName();
        }
        in.setHeader("CamelBox.fileName", filename);
    }

}
