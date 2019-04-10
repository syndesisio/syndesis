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

import java.util.Map;

import io.syndesis.integration.component.proxy.ComponentDefinition;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyFactory;
import org.apache.camel.Endpoint;
import org.apache.camel.component.salesforce.SalesforceEndpointConfig;
import org.apache.camel.model.language.ConstantExpression;
import org.apache.camel.processor.Enricher;

public class SalesforceStreamingConnectorFactory implements ComponentProxyFactory {
    @Override
    public ComponentProxyComponent newInstance(String componentId, String componentScheme) {
        return new SalesforceStreamingConnector(componentId, componentScheme);
    }

    private static class SalesforceStreamingConnector extends ComponentProxyComponent {
        SalesforceStreamingConnector(String componentId, String componentScheme) {
            super(componentId, componentScheme);
        }

        @Override
        @SuppressWarnings("PMD.SignatureDeclareThrowsException")
        protected Endpoint createDelegateEndpoint(ComponentDefinition definition, String scheme, Map<String, String> options) throws Exception {
            final String sObjectName = options.get(SalesforceEndpointConfig.SOBJECT_NAME);
            final String query = "SELECT Id FROM " + sObjectName;
            final String topicName = SalesforceUtil.topicNameFor(options);

            options.put("topicName", topicName);
            options.put(SalesforceEndpointConfig.SOBJECT_QUERY, query);
            options.remove(SalesforceEndpointConfig.SOBJECT_NAME);

            if (!topicName.endsWith("_d")) {
                setBeforeConsumer(
                    new Enricher(
                        new ConstantExpression(scheme + ":getSObject?rawPayload=true&sObjectName=" + sObjectName)
                    )
                );
            }

            return super.createDelegateEndpoint(definition, scheme, options);
        }
    }
}
