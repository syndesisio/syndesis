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
package io.syndesis.connector.meta.v1;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.syndesis.connector.support.verifier.api.Verifier;
import io.syndesis.connector.support.verifier.api.VerifierResponse;
import org.apache.camel.CamelContext;
import org.apache.camel.NoSuchBeanException;
import org.apache.camel.spi.FactoryFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


@Component
@Path("/verifier")
public class VerifierEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(VerifierEndpoint.class);
    private static final String RESOURCE_PATH = "META-INF/syndesis/connector/verifier/";

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CamelContext camelContext;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public List<VerifierResponse> verify(@PathParam("id") String connectorId, Map<String, Object> parameters) {
        List<VerifierResponse> answer;
        Verifier verifier;

        try {
            // First find try to lookup the verifier from the application context
            verifier = applicationContext.getBean(connectorId, Verifier.class);
        } catch (NoSuchBeanDefinitionException|NoSuchBeanException ignored) {
            LOGGER.debug("No bean of type: {} with id: {} found in application context, switch to factory finder", Verifier.class.getName(), connectorId);

            verifier = null;

            try {
                // Then fallback to camel's factory finder
                final FactoryFinder finder = camelContext.getFactoryFinder(RESOURCE_PATH);
                final Class<?> type = finder.findClass(connectorId);

                verifier = (Verifier) camelContext.getInjector().newInstance(type);
            } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
                LOGGER.warn("No factory finder of type: {} for id: {}", Verifier.class.getName(), connectorId, e);
            }
        }


        if (verifier != null) {
            answer = verifier.verify(camelContext, connectorId, parameters);
            answer = filterExceptions(answer);
        } else {
            answer = Collections.singletonList(createUnsupportedResponse(connectorId));
        }


        return answer;
    }

    private List<VerifierResponse> filterExceptions(List<VerifierResponse> responses) {
        for (VerifierResponse response : responses) {
            List<VerifierResponse.Error> errors = response.getErrors();
            if (errors != null) {
                for (VerifierResponse.Error error : errors) {
                    Map<String,Object> attributes = error.getAttributes();
                    if (attributes != null) {
                        Set<String> toRemove = new HashSet<>();
                        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                            if (entry.getValue() instanceof Exception) {
                                toRemove.add(entry.getKey());
                            }
                        }
                        for (String key : toRemove) {
                            attributes.remove(key);
                        }
                    }
                }
            }
        }
        return responses;
    }

    private VerifierResponse createUnsupportedResponse(String connectorId) {
        return new VerifierResponse.Builder(Verifier.Status.UNSUPPORTED, Verifier.Scope.PARAMETERS)
            .error("unknown-connector", String.format("No connector for ID %s registered", connectorId))
            .build();
    }
}
