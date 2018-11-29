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
package io.syndesis.connector.odata;

import java.io.InputStream;

import org.apache.camel.Message;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.serialization.ClientODataDeserializer;
import org.apache.olingo.client.api.serialization.ODataBinder;
import org.apache.olingo.commons.api.format.ContentType;

/**
 * Abstract base class for OData connectors that take an input entity.
 * @author dhirajsb
 */
public abstract class AbstractODataEntityConnector extends AbstractODataConnector {

    public AbstractODataEntityConnector(String componentName, String componentScheme, String className) {
        super(componentName, componentScheme, className);

        setBeforeProducer(exchange -> {
            // convert json into ClientEntity
            Message in = exchange.getIn();
            ignoreResponseHeaders(in);
            final ODataBinder binder = odataClient.getBinder();
            final ClientODataDeserializer deserializer = odataClient.getDeserializer(ContentType.APPLICATION_JSON);
            ClientEntity oDataEntity = binder.getODataEntity(deserializer.toEntity(in.getBody(InputStream.class)));
            in.setBody(oDataEntity);
        });
    }

}
