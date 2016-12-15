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

import io.fabric8.funktion.agent.support.CamelTester;
import io.fabric8.funktion.agent.support.TestRouteBuilder;
import io.fabric8.funktion.model.Funktion;
import io.fabric8.kubernetes.api.KubernetesHelper;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.fabric8.utils.URLUtils.pathJoin;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 */
public class AgentKT {
    private static final transient Logger LOG = LoggerFactory.getLogger(AgentKT.class);

    protected Agent agent = new Agent();

    @Test
    public void testAgentSubscribe() throws Throwable {
        String elasticSearchPath = "/testagent/funktion/1";
        String namespace = agent.getCurrentNamespace();

        String elasticsearchURL = getHttpServiceURL(namespace, "elasticsearch");
        String elasticSearchResource = pathJoin(elasticsearchURL, elasticSearchPath);
        deleteResource(elasticSearchResource);

        LOG.info("Creating a subscription in namespace: " + namespace);

        String expectedName = getClass().getName();
        SamplePayload payload = new SamplePayload(expectedName, new Date());
        String body = KubernetesHelper.toJson(payload);
        LOG.info("Sending JSON: " + body);

        SubscribeResponse response = null;
        try {
            Funktion funktion = new Funktion();

            funktion.createFlow().trigger("timer://cheese?fixedRate=true&period=5000").trace(true).logResult(true).singleMessageMode(true).
                    setBody(body).addEndpoint("http://elasticsearch:9200" + elasticSearchPath);


            Map<String, String> applicationProperties = new HashMap<>();
            applicationProperties.put("foo", "bar");

            SubscribeRequest request = new SubscribeRequest(namespace, funktion, applicationProperties);

            LOG.info("Creating a flow");
            response = agent.subscribe(request);

            LOG.info("Created a Subscription at " + response.getNamespace() + "/" + response.getName());

            assertThat(response.getNamespace()).describedAs("namespace").isEqualTo(namespace);

            assertWaitForResults(namespace, elasticSearchResource, payload);

        } finally {
            if (response != null) {
                LOG.info("Deleting the Subscription at  " + response.getNamespace() + "/" + response.getName());
                agent.unsubscribe(response.getNamespace(), response.getName());
                LOG.info("Deleted the Subscription at  " + response.getNamespace() + "/" + response.getName());
            }
        }

    }

    private void deleteResource(String resource) {
        LOG.info("trying to delete " + resource);
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.delete(resource);
        } catch (RestClientException e) {
            LOG.info("Ignoring delete exception on resource: " + resource + " as it may not exist: " + e);
        }

        // lets assert it does not exist any more
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(resource, String.class);
            fail("Should not have found resource: " + resource + " with: " + response.getBody());
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            int statusCode = e.getRawStatusCode();
            LOG.info("As expected we could not find resource " + resource + " and got status: " + statusCode + " with: " + responseBody);
            assertThat(statusCode).describedAs("Response code after getting " + resource + " with result: " + responseBody).isGreaterThanOrEqualTo(400);
        }
    }

    private void assertWaitForResults(String namespace, String elasticSearchResource, SamplePayload payload) throws Throwable {
        final String pollUrl = pathJoin(elasticSearchResource, "/_source");
        LOG.info("Querying elasticsearch URL: " + pollUrl);

        CamelTester.assertIsSatisfied(new TestRouteBuilder() {
            @Override
            protected void configureTest() throws Exception {
                // expectations
                results.expectedBodiesReceived(payload);
                results.setResultWaitTime(60 * 1000L);

                from("timer://poller?delay=10000&period=2000").errorHandler(deadLetterChannel(errors)).
                        to(pollUrl).
                        unmarshal().json(JsonLibrary.Jackson, SamplePayload.class).
                        to(results);
            }
        });
    }

    protected String getHttpServiceURL(String namespace, String serviceName) {
        String answer = KubernetesHelper.getServiceURL(agent.getKubernetesClient(), serviceName, namespace, "http", true);
        assertThat(answer).describedAs("Could not find service URL " + namespace + "/" + serviceName).isNotEmpty();
        return answer;

    }

}
