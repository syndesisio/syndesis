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
package io.syndesis.server.endpoint.actuator;

import io.syndesis.common.model.connection.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter;
import org.springframework.boot.actuate.endpoint.mvc.HypermediaDisabled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Binds an additional post method to the connectors endpoint,
 * using spring-boot 1.5.x actuator binding (should be changed in spring-boot 2.x).
 */
@Configuration
@ConditionalOnProperty(value = "endpoints.connectors.enabled", havingValue = "true", matchIfMissing = true)
public class ConnectorsMvcEndpoint extends EndpointMvcAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectorsMvcEndpoint.class);

    private ConnectorsEndpoint delegate;

    public ConnectorsMvcEndpoint(ConnectorsEndpoint delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @RequestMapping(
        method = {RequestMethod.POST},
        consumes = {"application/vnd.spring-boot.actuator.v1+json", "application/json"},
        produces = {"application/vnd.spring-boot.actuator.v1+json", "application/json"}
    )
    @HypermediaDisabled
    public Object set(@RequestBody Connector connector) {
        if (!this.delegate.isEnabled()) {
            return this.getDisabledResponse();
        } else {
            try {
                this.delegate.updateConnector(connector);
                return ResponseEntity.ok().build();
            } catch (Exception ex) {
                LOG.error("Error while updating the connector", ex);
                return ResponseEntity.badRequest().build();
            }
        }
    }
}

