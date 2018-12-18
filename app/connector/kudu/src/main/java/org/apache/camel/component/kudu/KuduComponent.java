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

package org.apache.camel.component.kudu;

import org.apache.camel.component.kudu.internal.KuduConnectionHelper;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.util.component.AbstractApiComponent;

import org.apache.camel.component.kudu.internal.KuduApiCollection;
import org.apache.camel.component.kudu.internal.KuduApiName;
import org.apache.kudu.client.KuduClient;

/**
 * Represents the component that manages {@link KuduEndpoint}.
 */
public class KuduComponent extends AbstractApiComponent<KuduApiName, KuduConfiguration, KuduApiCollection> {


    @Metadata(label = "advanced")
    KuduClient client;

    /**
     * To use a shared kudu client
     *
     * @return the shared connection
     */
    public KuduClient getClient() {
        return client;
    }

    public KuduComponent() {
        super(KuduEndpoint.class, KuduApiName.class, KuduApiCollection.getCollection());
    }

    public KuduComponent(CamelContext context) {
        super(context, KuduEndpoint.class, KuduApiName.class, KuduApiCollection.getCollection());
    }

    @Override
    protected KuduApiName getApiName(String apiNameStr) throws IllegalArgumentException {
        return KuduApiName.fromValue(apiNameStr);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String methodName, KuduApiName apiName,
                                      KuduConfiguration endpointConfiguration) {
        endpointConfiguration.setApiName(apiName);
        endpointConfiguration.setMethodName(methodName);
        KuduEndpoint endpoint = new KuduEndpoint(uri, this, apiName, methodName, endpointConfiguration);
        return endpoint;
    }

    /**
     * To use the shared configuration
     */
    @Override
    public void setConfiguration(KuduConfiguration configuration) {
        super.setConfiguration(configuration);
    }

    /**
     * To use the shared configuration
     */
    @Override
    public KuduConfiguration getConfiguration() {
        return super.getConfiguration();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if ( client == null) {
            if (configuration != null) {
                client = KuduConnectionHelper.createConnection(configuration);
            } else {
                throw new IllegalArgumentException("Unable to connect, kudu component configuration is missing");
            }
        }
    }

    @Override
    protected void doStop() throws Exception {
        if (client != null) {
            client = null;
        }
    }
}
