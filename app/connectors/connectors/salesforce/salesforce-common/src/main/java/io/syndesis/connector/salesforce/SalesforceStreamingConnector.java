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

import org.apache.camel.component.connector.DefaultConnectorComponent;
import org.apache.camel.component.salesforce.SalesforceEndpointConfig;
import org.apache.camel.model.language.ConstantExpression;
import org.apache.camel.processor.Enricher;

import java.net.URISyntaxException;
import java.util.Map;

public abstract class SalesforceStreamingConnector extends DefaultConnectorComponent {
    private final static String TOPIC_PREFIX = "syndesis_";

    public SalesforceStreamingConnector(final String componentName, final String componentSchema, final Class<?> componentClass) {
        super(componentName, componentSchema, componentClass);
    }

    @Override
    public final String createEndpointUri(final String scheme, final Map<String, String> options) throws URISyntaxException {
        final String sObjectName = options.get(SalesforceEndpointConfig.SOBJECT_NAME);

        final String query = "SELECT Id FROM " + sObjectName;
        final String topicName = topicNameFor(options);
        options.put("topicName", topicName);
        options.put(SalesforceEndpointConfig.SOBJECT_QUERY, query);
        options.remove(SalesforceEndpointConfig.SOBJECT_NAME);

        final String salesforceComponent = getComponentName() + "-component";

        if (!topicName.endsWith("_delete")) {
            final Enricher enricher = new Enricher(
                new ConstantExpression(salesforceComponent + ":getSObject?rawPayload=true&sObjectName=" + sObjectName));
            enricher.setCamelContext(getCamelContext());

            setBeforeConsumer(enricher);
        }

        return super.createEndpointUri(scheme, options);
    }

    private static String topicNameFor(final Map<String, String> options) {
        final String sObjectName = options.get(SalesforceEndpointConfig.SOBJECT_NAME);

        final String topicSuffix;
        if (Boolean.valueOf(options.get("notifyForOperationCreate"))) {
            topicSuffix = "_create";
        } else if (Boolean.valueOf(options.get("notifyForOperationUpdate"))) {
            topicSuffix = "_update";
        } else if (Boolean.valueOf(options.get("notifyForOperationDelete"))) {
            topicSuffix = "_delete";
        } else if (Boolean.valueOf(options.get("notifyForOperationUndelete"))) {
            topicSuffix = "_undelete";
        } else {
            topicSuffix = "_all";
        }

        return TOPIC_PREFIX + sObjectName + topicSuffix;
    }
}
