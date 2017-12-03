/**
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.salesforce;

import org.apache.camel.Message;
import org.apache.camel.component.connector.DefaultConnectorComponent;

/**
 * Camel salesforce-delete-sobject-with-id connector
 */
public class SalesforceDeleteSObjectWithIdComponent extends DefaultConnectorComponent {

    public SalesforceDeleteSObjectWithIdComponent() {
        this(null);

    }

    public SalesforceDeleteSObjectWithIdComponent(String componentSchema) {
        super("salesforce-delete-sobject-with-id", componentSchema, SalesforceDeleteSObjectWithIdComponent.class.getName());

        // replace DTO with id for Salesforce component
        setBeforeProducer(exchange -> {
            final Message in = exchange.getIn();
            final SalesforceIdentifier id = in.getBody(SalesforceIdentifier.class);
            in.setBody(id.getId());
        });
    }

}
