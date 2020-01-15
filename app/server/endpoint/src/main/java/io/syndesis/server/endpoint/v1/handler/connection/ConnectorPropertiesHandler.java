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

import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collections;

import com.netflix.hystrix.HystrixExecutable;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.syndesis.common.model.connection.DynamicConnectionPropertiesMetadata;
import io.syndesis.server.verifier.MetadataConfigurationProperties;

@Tag(name = "properties")
public class ConnectorPropertiesHandler {
    private final MetadataConfigurationProperties config;

    public ConnectorPropertiesHandler(final MetadataConfigurationProperties config) {
        this.config = config;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public DynamicConnectionPropertiesMetadata dynamicConnectionProperties(@PathParam("id") final String connectorId) {
        final HystrixExecutable<DynamicConnectionPropertiesMetadata> meta = createMetadataConnectionPropertiesCommand(connectorId);
        return meta.execute();
    }

    protected HystrixExecutable<DynamicConnectionPropertiesMetadata> createMetadataConnectionPropertiesCommand(final String connectorId) {
        return new MetadataConnectionPropertiesCommand(config, connectorId, Collections.emptyMap());
    }

}
