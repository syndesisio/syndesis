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

package io.fabric8.funktion.camel.components.k8kafka;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.component.kafka.KafkaConsumer;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.apache.camel.component.kafka.KafkaProducer;
import org.apache.camel.impl.SynchronousDelegateProducer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;

/**
 * The k8kafka component allows messages to be sent to (or consumed from) Apache Kafka brokers.
 */
@UriEndpoint(scheme = "k8kafka", title = "k8Kafka", syntax = "kafka:<topic>", consumerClass = KafkaConsumer.class, label = "messaging")
public class K8KafkaEndpoint extends KafkaEndpoint  {

    @UriParam
    private K8KafkaConfiguration kafkaConfiguration; //only used by APT generator

    public K8KafkaEndpoint() {
    }

    public K8KafkaEndpoint(String endpointUri, K8KafkaComponent component) {
        super(endpointUri, component);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        KafkaConsumer consumer = new KafkaConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }

    @Override
    public Producer createProducer() throws Exception {
        KafkaProducer producer = createProducer(this);
        if (isSynchronous()) {
            return new SynchronousDelegateProducer(producer);
        } else {
            return producer;
        }
    }


}
