/*
 * Copyright 2017 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package io.fabric8.funktion.camel.components.k8nats;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.component.nats.NatsConfiguration;
import org.apache.camel.component.nats.NatsConsumer;
import org.apache.camel.component.nats.NatsEndpoint;
import org.apache.camel.component.nats.NatsProducer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;

/**
 * The nats component allows you produce and consume messages from <a href="http://nats.io/">NATS</a>.
 */
@UriEndpoint(scheme = "k8nats", title = "Nats", syntax = "k8nats:topic", label = "messaging", consumerClass = NatsConsumer.class)
public class K8NatsEndpoint extends NatsEndpoint {


    @UriParam
    private K8NatsConfiguration natsConfiguration; //only used by APT generator

    public K8NatsEndpoint(String uri, K8NatsComponent component, NatsConfiguration config) {
        super(uri, component,config);
    }    
    
    @Override
    public Producer createProducer() throws Exception {
        return new NatsProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        return new NatsConsumer(this, processor);
    }


    @Override
    public boolean isSingleton() {
        return true;
    }

}
