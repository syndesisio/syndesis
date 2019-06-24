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
import io.syndesis.server.dao.manager.DataManager;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Defines methods for refreshing connectors without restarting the server.
 */
@Configuration
@ConditionalOnProperty(value = "endpoints.connectors.enabled", havingValue = "true", matchIfMissing = true)
public class ConnectorsEndpoint extends AbstractEndpoint<List<Connector>> {

    private DataManager dataManager;

    public ConnectorsEndpoint(DataManager dataManager) {
        super("connectors", false, true);
        this.dataManager = dataManager;
    }

    @Override
    public List<Connector> invoke() {
        return dataManager.fetchAll(Connector.class).getItems();
    }

    public void updateConnector(Connector value) {
        dataManager.set(value);
    }
}

