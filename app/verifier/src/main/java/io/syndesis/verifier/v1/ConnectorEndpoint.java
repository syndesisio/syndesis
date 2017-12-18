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
package io.syndesis.verifier.v1;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import io.syndesis.verifier.api.MetadataAdapter;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Path("/connectors")
public class ConnectorEndpoint {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CamelContext camelContext;

    @Path("/{connectorId}/actions")
    public ActionDefinitionEndpoint actions(@PathParam("connectorId") final String connectorId) throws Exception {
        MetadataAdapter<?> adapter;

        try {
            adapter = applicationContext.getBean(connectorId + "-adapter", MetadataAdapter.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalStateException("Unable to find adapter for:" + connectorId, e);
        }

        return new ActionDefinitionEndpoint(camelContext, connectorId, adapter);
    }

}
