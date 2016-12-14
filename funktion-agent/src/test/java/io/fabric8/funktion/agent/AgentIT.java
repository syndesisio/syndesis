/*
 * Copyright 2016 Red Hat, Inc.
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
package io.fabric8.funktion.agent;

import io.fabric8.funktion.model.Funktion;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 */
public class AgentIT {
    private static final transient Logger LOG = LoggerFactory.getLogger(AgentIT.class);

    protected Agent agent = new Agent();

    @Test
    public void testCreateSubscription() throws Exception {
        String namespace = agent.getCurrentNamespace();
        System.out.println("Creating a subscription in namespace: " + namespace);

        SubscribeResponse response =  null;

        try {
            Funktion funktion = new Funktion();
            // TODO enable after release!
            funktion.createFlow().trigger("timer://cheese?fixedRate=true&period=5000").trace(true).logResult(true).addEndpoint("http://elasticsearch/funktionAgentTest/1");
            //funktion.createRule().trigger("timer://cheese?period=5000").logResult(true).addEndpointStep("http://elasticsearch/funktionAgentTest/1").singleMessageMode(true);


            Map<String, String> applicationProperties = new HashMap<>();
            SubscribeRequest request = new SubscribeRequest(namespace, funktion, applicationProperties);

            LOG.info("Creating a flow");
            response = agent.subscribe(request);

            LOG.info("Created a Subscription at " + response.getNamespace() + "/" + response.getName());

            assertThat(response.getNamespace()).describedAs("namespace").isEqualTo(namespace);


            // TODO lets wait for the data to arrive!

            Thread.sleep(10000000);

        } finally {
            if (response != null) {
                LOG.info("Deleting the Subscription at  " + response.getNamespace() + "/" + response.getName());
                agent.unsubscribe(response.getNamespace(), response.getName());
                LOG.info("Deleted the Subscription at  " + response.getNamespace() + "/" + response.getName());
            }
        }

    }

}
