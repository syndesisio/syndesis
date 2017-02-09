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

import org.apache.camel.CamelContext;
import org.apache.camel.component.kafka.KafkaComponent;

import java.util.Map;

public class K8KafkaComponent extends KafkaComponent {

    public K8KafkaComponent() {
        setEndpointClass(K8KafkaEndpoint.class);
    }

    public K8KafkaComponent(CamelContext context) {
        super(context);
        setEndpointClass(K8KafkaEndpoint.class);
    }


    @Override
    protected K8KafkaEndpoint createEndpoint(String uri, String remaining, Map<String, Object> params) throws Exception {
        K8KafkaEndpoint endpoint = new K8KafkaEndpoint(uri, this);
        K8KafkaConfiguration kafkaConfiguration = new K8KafkaConfiguration();
        endpoint.setConfiguration(kafkaConfiguration);


        // configure component options before endpoint properties which can override from params
        endpoint.getConfiguration().setWorkerPool(getWorkerPool());

        setProperties(endpoint.getConfiguration(), params);
        setProperties(endpoint, params);
        String topic = remaining.split("\\?")[0];
        if (topic != null){
            endpoint.getConfiguration().setTopic(topic);
        }

        String brokers = kafkaConfiguration.getServiceName() + ":" + kafkaConfiguration.getPort();
        endpoint.getConfiguration().setBrokers(brokers);
        return endpoint;
    }
}
