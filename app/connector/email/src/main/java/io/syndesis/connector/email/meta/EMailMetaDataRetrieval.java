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
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import io.syndesis.connector.email.EMailConstants;
import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;

public class EMailMetaDataRetrieval extends ComponentMetadataRetrieval implements EMailConstants {

    @Override
    protected MetaDataExtension resolveMetaDataExtension(CamelContext context, Class<? extends MetaDataExtension> metaDataExtensionClass, String componentId, String actionId) {
        return new EMailMetaDataExtension(context);
    }

    @Override
    protected SyndesisMetadata adapt(CamelContext context, String componentId, String actionId, Map<String, Object> properties, MetaDataExtension.MetaData metadata) {
        EMailMetadata emailMetadata = (EMailMetadata) metadata.getPayload();

        //
        // The camel-mail component has 6 different connectors and they
        // are named according to the protocol of the mail server,
        // ie. imap(s), pop3(s), smtp(s)
        //
        Protocol protocol = emailMetadata.getProtocol();

        if (actionId.endsWith(EMailFunction.READ.connectorId()) && protocol.isProducer()) {
            //
            // Read action NOT applicable to SMTP connectors
            //
            String msg = "The protocol for the selection connection is set to '" + protocol.id() +
                    "'. This is not appropriate for a consuming email connector. Please amend the connector and try again.";
            throw new IllegalArgumentException(msg);
        } else if (actionId.endsWith(EMailFunction.SEND.connectorId()) && protocol.isReceiver()) {
            //
            // Send action is only applicable to SMTP connectors
            //
            String msg = "The protocol of the selected connection is set to '" + protocol.id() +
                    "'. Only 'smtp' is appropriate for a producing email connector. Please amend the connector and try again.";
            throw new IllegalArgumentException(msg);
        }

        return SyndesisMetadata.EMPTY;
    }
}
