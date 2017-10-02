/**
 * Copyright (C) 2016 Red Hat, Inc.
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

import java.net.URISyntaxException;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.connector.DefaultConnectorComponent;
import org.apache.camel.component.salesforce.SalesforceEndpointConfig;
import org.apache.camel.model.language.ConstantExpression;
import org.apache.camel.processor.Enricher;
import org.apache.camel.util.IntrospectionSupport;

public abstract class AbstractSalesforceStreamingConnector extends DefaultConnectorComponent {
    private final String topicPrefix;

    private final String topicSufix;

    public AbstractSalesforceStreamingConnector(final String componentName, final String className,
        final String topicPrefix, final String topicSufix) {
        super(componentName, className);
        this.topicPrefix = topicPrefix;
        this.topicSufix = topicSufix;
    }

    @Override
    public String createEndpointUri(final String scheme, final Map<String, String> options) throws URISyntaxException {
        final String sObjectName = options.get(SalesforceEndpointConfig.SOBJECT_NAME);

        final String query = "SELECT Id FROM " + sObjectName;
        options.put("topicName", topicNameFor(options));
        options.put(SalesforceEndpointConfig.SOBJECT_QUERY, query);
        options.remove(SalesforceEndpointConfig.SOBJECT_NAME);

        final Enricher enricher = new Enricher(new ConstantExpression("direct:salesforce-streaming-fetch"));
        enricher.setCamelContext(getCamelContext());

        setBeforeConsumer(enricher);

        return super.createEndpointUri(scheme, options);
    }

    public String objectNameFromTopic(final Exchange exchange) {
        final Message in = exchange.getIn();
        final String topic = in.getHeader("CamelSalesforceTopicName", String.class);

        return topic.substring(topicPrefix.length(), topic.length() - topicSufix.length());
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        final CamelContext camelContext = getCamelContext();
        final Component salesforce = camelContext.getComponent("salesforce", true, false);
        IntrospectionSupport.setProperties(salesforce, getOptions());

        camelContext.addService(salesforce, true, true);

        if (camelContext.getRouteStatus("salesforce-streaming-fetch") == null) {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("direct:salesforce-streaming-fetch").id("salesforce-streaming-fetch")
                        .setHeader(SalesforceEndpointConfig.SOBJECT_ID, simple("${body.id}"))
                        .setHeader(SalesforceEndpointConfig.SOBJECT_NAME,
                            method(AbstractSalesforceStreamingConnector.this, "objectNameFromTopic"))
                        .to("salesforce:getSObject?rawPayload=true");
                }
            });
        }
    }

    protected String topicNameFor(final Map<String, String> options) {
        final String sObjectName = options.get(SalesforceEndpointConfig.SOBJECT_NAME);

        return topicPrefix + sObjectName + topicSufix;
    }
}
