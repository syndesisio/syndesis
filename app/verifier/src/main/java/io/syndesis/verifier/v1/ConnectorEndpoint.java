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

import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import io.syndesis.verifier.v1.metadata.MetadataAdapter;

import org.springframework.stereotype.Component;

@Component
@Path("/connectors")
public class ConnectorEndpoint {

    private final Map<String, MetadataAdapter<?>> adapters;

    public ConnectorEndpoint(final Map<String, MetadataAdapter<?>> adapters) {
        this.adapters = adapters;
    }

    @Path("/{connectorId}/actions")
    public ActionDefinitionEndpoint actions(@PathParam("connectorId") final String connectorId) throws Exception {
        final MetadataAdapter<?> adapter = MetadataEndpoint.adapterFor(adapters, connectorId);

        return new ActionDefinitionEndpoint(connectorId, adapter);
    }

}
