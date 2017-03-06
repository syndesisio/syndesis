/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ipaas.rest.v1.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.ipaas.rest.v1.model.ListResult;
import com.redhat.ipaas.rest.v1.model.connection.Connection;
import com.redhat.ipaas.rest.v1.model.connection.Connector;
import com.redhat.ipaas.rest.v1.util.Json;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataManagerTest {

    @Rule
    public InfinispanCache infinispan = new InfinispanCache();

    private DataManager dataManager = null;

    private ObjectMapper objectMapper = Json.mapper();

    @Before
    public void setup() {
        //Create Data Manager
        dataManager = new DataManager(infinispan.getCaches(), objectMapper, new ArrayList<>(), "com/redhat/ipaas/rest/v1/deployment.json");
        dataManager.init();
    }

    @Test
    public void getConnectors() {
        ListResult<Connector> connectors = dataManager.fetchAll(Connector.KIND);
        for (Connector connector : connectors.getItems()) {
            System.out.print(connector.getId().get() + ",");
        }
        assertTrue(connectors.getTotalCount() > 1);
        assertTrue(connectors.getItems().size() > 1);
        assertEquals(connectors.getTotalCount(), connectors.getItems().size());
    }

    @Test
    public void getConnections() {
        ListResult<Connection> connections = dataManager.fetchAll(Connection.KIND);
        for (Connection connection : connections.getItems()) {
            System.out.print(connection.getId().get() + ",");
        }
        assertTrue(connections.getTotalCount() > 1);
        assertTrue(connections.getItems().size() > 1);
        assertEquals(connections.getTotalCount(), connections.getItems().size());
    }

    @Test
    public void getConnectorsWithFilterFunction() {
        ListResult<Connector> connectors = dataManager.fetchAll(
            Connector.KIND,
            resultList -> new ListResult.Builder<Connector>().createFrom(resultList).items(resultList.getItems().subList(0, 1)).build()
        );
        for (Connector connector : connectors.getItems()) {
            System.out.print(connector.getId().get() + ",");
        }
        assertTrue(connectors.getTotalCount() > 1);
        assertEquals(1, connectors.getItems().size());
    }

    @Test
    public void getConnector() {
        Connector connector = dataManager.fetch(Connector.KIND, "org.foo_twitter-mention-connector_1.0");
        System.out.println(connector.getName());
        assertEquals("First Connector in the deployment.json is TwitterMention", "TwitterMention", connector.getName());
    }

}
