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
package io.syndesis.server.endpoint.v1.handler.connection;

import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.syndesis.server.verifier.MetadataConfigurationProperties;

@Api(value = "properties")
public class ConnectorPropertiesHandler {
    private final String metaPropertiesUrl;

    public ConnectorPropertiesHandler(final MetadataConfigurationProperties config) {
        metaPropertiesUrl = String.format("http://%s/api/v1/connectors/%%s/properties/meta", config.getService());
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response enrichWithDynamicProperties(@PathParam("id") final String connectorId, final Map<String, Object> props) {
        // TODO replace properly with circuit breaker
        String metadataUrl = String.format(metaPropertiesUrl, connectorId);
        Client client = createClient();
        WebTarget target = client.target(metadataUrl);

        return target.request().post(Entity.entity(props, MediaType.APPLICATION_JSON_TYPE));
    }

    Client createClient() {
        return ClientBuilder.newClient();
    }
}
