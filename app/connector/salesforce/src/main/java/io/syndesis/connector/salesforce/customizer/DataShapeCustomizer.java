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
package io.syndesis.connector.salesforce.customizer;

import java.io.IOException;
import java.util.Map;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.util.Json;
import io.syndesis.connector.salesforce.SalesforceConstants;
import io.syndesis.connector.salesforce.SalesforceUtil;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.processor.Pipeline;
import org.springframework.cglib.core.internal.Function;

public class DataShapeCustomizer implements ComponentProxyCustomizer, CamelContextAware {
    private CamelContext camelContext;

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        try {
            consumeOption(camelContext, options, SalesforceConstants.CONNECTOR_ACTION_ID, String.class, actionId -> {
                final Connector connector = SalesforceUtil.mandatoryLookupConnector(this.camelContext, SalesforceConstants.CONNECTOR_ID);
                final ConnectorAction action = SalesforceUtil.mandatoryLookupAction(this.camelContext, connector, actionId);

                action.getInputDataShape()
                    .map(dataShape -> new UnmarshallProcessor(dataShape, Exchange::getIn))
                    .map(processor -> Pipeline.newInstance(this.camelContext, processor, component.getBeforeProducer()))
                    .ifPresent(component::setBeforeProducer);

                action.getOutputDataShape()
                    .map(dataShape -> new UnmarshallProcessor(dataShape, Exchange::getOut))
                    .map(processor -> Pipeline.newInstance(this.camelContext, processor, component.getAfterProducer()))
                    .ifPresent(component::setAfterProducer);
            });
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public final class UnmarshallProcessor implements Processor {
        private final Class<?> type;
        private final Function<Exchange, Message> messageFunction;

        public UnmarshallProcessor(final DataShape dataShape, Function<Exchange, Message> messageFunction) {
            this.messageFunction = messageFunction;

            if (dataShape.getKind() == DataShapeKinds.JAVA) {
                type = camelContext.getClassResolver().resolveClass(dataShape.getType());
                if (type == null) {
                    throw new IllegalArgumentException("The specified class for shape `" + dataShape + "` cannot be found");
                }
            } else {
                type = null;
            }
        }

        @Override
        public final void process(final Exchange exchange) throws Exception {
            if (exchange.isFailed()) {
                return;
            }

            if (type == null) {
                return;
            }

            final Message message = messageFunction.apply(exchange);
            final String bodyAsString = message.getBody(String.class);

            if (bodyAsString == null) {
                return;
            }

            try {
                final Object output = Json.reader().forType(type).readValue(bodyAsString);
                message.setBody(output);
            } catch (final IOException e) {
                exchange.setException(e);
            }
        }
    }
}
