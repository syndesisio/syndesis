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
package io.syndesis.connector.salesforce;

import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.component.connector.DataType;
import org.apache.camel.component.connector.DefaultConnectorComponent;
import org.apache.camel.component.connector.DefaultConnectorEndpoint;
import org.apache.camel.processor.Pipeline;

import java.util.Map;

public abstract class SalesforceConnector extends DefaultConnectorComponent {

    protected SalesforceConnector(final String componentName, final String componentScheme, final Class<?> componentClass) {
        super(componentName, componentScheme, componentClass);
    }

    @Override
    protected final Endpoint createEndpoint(final String uri, final String remaining, final Map<String, Object> parameters)
        throws Exception {
        final DefaultConnectorEndpoint connectorEndpoint = (DefaultConnectorEndpoint) super.createEndpoint(uri, remaining, parameters);

        final DataType inputDataType = connectorEndpoint.getInputDataType();
        final UnmarshallProcessor unmarshallInputProcessor = new UnmarshallInputProcessor(inputDataType);
        final Processor existingBeforeProducer = connectorEndpoint.getBeforeProducer();
        if (existingBeforeProducer == null) {
            connectorEndpoint.setBeforeProducer(unmarshallInputProcessor);
        } else {
            connectorEndpoint.setBeforeProducer(Pipeline.newInstance(getCamelContext(), unmarshallInputProcessor, existingBeforeProducer));
        }

        final DataType outputDataType = connectorEndpoint.getOutputDataType();
        final UnmarshallProcessor unmarshallOutputProcessor = new UnmarshallOutputProcessor(outputDataType);
        final Processor existingAfterProducer = connectorEndpoint.getAfterProducer();
        if (existingAfterProducer == null) {
            connectorEndpoint.setAfterProducer(unmarshallOutputProcessor);
        } else {
            connectorEndpoint.setAfterProducer(Pipeline.newInstance(getCamelContext(), unmarshallOutputProcessor, existingAfterProducer));
        }

        return connectorEndpoint;
    }

}
