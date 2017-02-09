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

import org.apache.camel.Endpoint;
import org.apache.camel.component.nats.NatsComponent;
import org.apache.camel.component.nats.NatsEndpoint;

import java.util.Map;

public class K8NatsComponent extends NatsComponent {

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        K8NatsConfiguration config = new K8NatsConfiguration();
        setProperties(config, parameters);


        String topic = remaining.split("\\?")[0];
        if (topic != null){
            config.setTopic(topic);
        }

        String servers = config.getServiceName() + ":" + config.getPort();
        config.setServers(servers);

        NatsEndpoint endpoint = new NatsEndpoint(uri, this, config);
        return endpoint;
    }
}
