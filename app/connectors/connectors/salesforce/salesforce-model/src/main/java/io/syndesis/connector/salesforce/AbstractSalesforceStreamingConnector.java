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

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.connector.DefaultConnectorComponent;
import org.apache.camel.component.salesforce.SalesforceEndpointConfig;
import org.apache.camel.model.language.ConstantExpression;
import org.apache.camel.processor.Enricher;

public abstract class AbstractSalesforceStreamingConnector extends DefaultConnectorComponent {
    private final String topicPrefix;

    private final String topicSufix;

    public AbstractSalesforceStreamingConnector(final String componentName, final String componentSchema, final String className, final String topicPrefix, final String topicSufix) {
        super(componentName, componentSchema, className);
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

        final String salesforceComponent = getComponentName() + "-component";

        final Enricher enricher = new Enricher(
            new ConstantExpression(salesforceComponent + ":getSObject?rawPayload=true&sObjectName=" + sObjectName));
        enricher.setCamelContext(getCamelContext());

        setBeforeConsumer(enricher);

        return super.createEndpointUri(scheme, options);
    }

    public String objectNameFromTopic(final Exchange exchange) {
        final Message in = exchange.getIn();
        final String topic = in.getHeader("CamelSalesforceTopicName", String.class);

        return topic.substring(topicPrefix.length(), topic.length() - topicSufix.length());
    }

    protected String topicNameFor(final Map<String, String> options) {
        final String sObjectName = options.get(SalesforceEndpointConfig.SOBJECT_NAME);

        return topicPrefix + sObjectName + topicSufix;
    }
}
