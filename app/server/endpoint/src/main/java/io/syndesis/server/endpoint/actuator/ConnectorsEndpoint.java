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

import java.util.List;

import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.dao.manager.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

/**
 * Defines methods for refreshing connectors without restarting the server.
 */
@Configuration
@Endpoint(id = "connectors")
@ConditionalOnProperty(value = "management.endpoints.connectors.enabled", havingValue = "true", matchIfMissing = true)
public class ConnectorsEndpoint {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(ConnectorsEndpoint.class);

    private final DataManager dataManager;

    public ConnectorsEndpoint(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @ReadOperation
    public List<Connector> connectors() {
        return dataManager.fetchAll(Connector.class).getItems();
    }

    @WriteOperation
    public Object connector(Connector connector) {
        try {
            updateConnector(connector);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            LOG.error("Error while updating the connector", ex);
            return ResponseEntity.badRequest().build();
        }
    }

    public void updateConnector(Connector value) {
        dataManager.set(value);
    }
}

