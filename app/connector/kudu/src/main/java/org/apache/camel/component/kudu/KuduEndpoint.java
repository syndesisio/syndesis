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

import org.apache.camel.component.kudu.api.KuduTablesManager;
import org.apache.camel.component.kudu.internal.*;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.util.component.AbstractApiEndpoint;
import org.apache.camel.util.component.ApiMethod;
import org.apache.camel.util.component.ApiMethodPropertiesHelper;
import org.apache.kudu.client.KuduClient;

import java.util.Map;

/**
 * Represents a Kudu endpoint.
 */
@UriEndpoint(firstVersion = "1.0-SNAPSHOT",
        scheme = "kudu",
        title = "Kudu",
        syntax="kudu:apiName/methodName",
        consumerClass = KuduConsumer.class,
        label = "database")
public class KuduEndpoint extends AbstractApiEndpoint<KuduApiName, KuduConfiguration> {

    @UriParam(name = "configuration")
    private KuduConfiguration kuduConfiguration;

    private KuduClient client;

    private boolean clientShared;

    private Object apiProxy;

    private KuduClient getClient() {
        return client;
    }

    public KuduEndpoint(String uri, KuduComponent component,
                        KuduApiName apiName, String methodName, KuduConfiguration endpointConfiguration) {
        super(uri, component, apiName, methodName, KuduApiCollection.getCollection().getHelper(apiName), endpointConfiguration);

        this.kuduConfiguration = endpointConfiguration;
    }

    @Override
    public Producer createProducer() throws Exception {
        return new KuduProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        // make sure inBody is not set for consumers
        if (inBody != null) {
            throw new IllegalArgumentException("Option inBody is not supported for consumer endpoint");
        }
        final KuduConsumer consumer = new KuduConsumer(this, processor);
        // also set consumer.* properties
        configureConsumer(consumer);
        return consumer;
    }

    @Override
    protected String getThreadProfileName() {
        return KuduConstants.THREAD_PROFILE_NAME;
    }

    /**
     * Create connection to kudu or reuse the connection held
     * by the component
     *
     */
    private void createKuduConnection() {
        final KuduComponent component = getComponent();
        this.clientShared = kuduConfiguration.equals(getComponent().getConfiguration());
        if (clientShared) {
            // get shared singleton connection from Component
            this.client = component.getClient();
        } else {
            this.client = KuduConnectionHelper.createConnection(kuduConfiguration);
        }
    }

    /**
     * Set the correct apiProxy and method call based on the apiName parameter
     *
     * @param args Args
     */
    private void createApiProxy(Map<String, Object> args) {
        switch (apiName) {
            case TABLES:
                apiProxy = getClient();
                break;
            default:
                throw new IllegalArgumentException("Invalid API name " + apiName);
        }
    }

    @Override
    protected void afterConfigureProperties() {
        // create connection eagerly, a good way to validate kuduConfiguration
        createKuduConnection();
    }

    @Override
    public Object getApiProxy(ApiMethod method, Map<String, Object> args) {
        if (apiProxy == null) {
            // create API proxy lazily
            createApiProxy(args);
        }
        return apiProxy;
    }

    @Override
    public KuduComponent getComponent() {
        return (KuduComponent) super.getComponent();
    }

    @Override
    protected ApiMethodPropertiesHelper<KuduConfiguration> getPropertiesHelper() {
        return KuduPropertiesHelper.getHelper();
    }
}
