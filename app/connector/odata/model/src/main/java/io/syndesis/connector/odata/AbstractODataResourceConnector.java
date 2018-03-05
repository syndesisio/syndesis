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

import org.apache.camel.Message;
import org.apache.camel.component.olingo4.internal.Olingo4Constants;

/**
 * Abstract base class for connectors that take {@link ODataResource} input.
 * @author dhirajsb
 */
public class AbstractODataResourceConnector extends AbstractODataConnector {

    public AbstractODataResourceConnector(String componentName, String componentScheme, String className) {
        super(componentName, componentScheme, className);

        // replace DTO with headers
        setBeforeProducer(exchange -> {
            final Message in = exchange.getIn();
            ignoreResponseHeaders(in);
            final ODataResource resource = in.getBody(ODataResource.class);
            in.setHeader(Olingo4Constants.PROPERTY_PREFIX + "keyPredicate", resource.getKeyPredicate());
            in.setBody(null);
        });
    }

}
