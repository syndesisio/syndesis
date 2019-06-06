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
package io.syndesis.connector.email.meta;

import java.util.Map;
import java.util.Optional;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.DefaultMetaData;
import io.syndesis.connector.email.EMailConstants;
import io.syndesis.connector.support.util.ConnectorOptions;

public class EMailMetaDataExtension extends AbstractMetaDataExtension implements EMailConstants {

    EMailMetaDataExtension(CamelContext context) {
        super(context);
    }

    @Override
    public Optional<MetaData> meta(Map<String, Object> parameters) {
        //
        // Method called twice.
        // 1) After selection of the type of integration request
        // 2) After the user has inputed settings and the wizard step is completing
        //
        EMailMetadata emailMetadata = buildMetadata(parameters);
        return Optional.of(new DefaultMetaData(null, null, emailMetadata));
    }

    private EMailMetadata buildMetadata(Map<String, Object> parameters) {
        Protocol protocol =  ConnectorOptions.extractOptionAndMap(parameters, PROTOCOL,  Protocol::getValueOf);
        if (protocol == null) {
            throw new IllegalStateException("Email connector protocol cannot be identified");
        }

        EMailMetadata emailMetadata = new EMailMetadata();
        emailMetadata.setProtocol(protocol);
        return emailMetadata;
    }
}
