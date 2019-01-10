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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

import io.syndesis.connector.support.verifier.api.MetadataRetrieval;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
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
@Path("/connectors")
public class ConnectorEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(VerifierEndpoint.class);
    private static final String RESOURCE_PATH = "META-INF/syndesis/connector/meta/";

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CamelContext camelContext;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{connectorId}/actions/{actionId}")
    public SyndesisMetadata actions(@PathParam("connectorId") final String connectorId, @PathParam("actionId") final String actionId,
        final Map<String, Object> properties) {

        MetadataRetrieval adapter = null;

        try {
            adapter = applicationContext.getBean(connectorId + "-adapter", MetadataRetrieval.class);
        } catch (NoSuchBeanDefinitionException | NoSuchBeanException ignored) {
            LOGGER.debug("No bean of type: {} with id: '{}-adapter' found in application context, switch to factory finder",
                MetadataRetrieval.class.getName(), connectorId);

            try {
                // Then fallback to camel's factory finder
                final FactoryFinder finder = camelContext.getFactoryFinder(RESOURCE_PATH);
                final Class<?> type = finder.findClass(connectorId);

                adapter = (MetadataRetrieval) camelContext.getInjector().newInstance(type);
            } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") final Exception e) {
                LOGGER.warn("No factory finder of type: {} for id: {}", MetadataRetrieval.class.getName(), connectorId, e);
            }
        }

        if (adapter == null) {
            throw new IllegalStateException("Unable to find adapter for: " + connectorId);
        }

        try {
            return adapter.fetch(camelContext, connectorId, actionId, properties);
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") final Exception e) {
            LOGGER.error("Unable to fetch and process metadata for connector: {}, action: {}", connectorId, actionId);
            LOGGER.debug("Unable to fetch and process metadata for connector: {}, action: {}, properties: {}", connectorId, actionId,
                properties, e);

            throw adapter.handle(e);
        }
    }
}
