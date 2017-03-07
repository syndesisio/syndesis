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
package com.redhat.ipaas.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.ipaas.core.Json;
import com.redhat.ipaas.dao.manager.DataManager;
import com.redhat.ipaas.model.ListResult;
import com.redhat.ipaas.model.connection.Connection;
import com.redhat.ipaas.model.connection.Connector;
import org.junit.*;

import java.util.ArrayList;

@Ignore
public class DataManagerTest {

    @Rule
    public InfinispanCache infinispan = new InfinispanCache();

    private DataManager dataManager = null;

    private ObjectMapper objectMapper = Json.mapper();

    @Before
    public void setup() {
        //Create Data Manager
        dataManager = new DataManager(infinispan.getCaches(), objectMapper, new ArrayList<>(), "com/redhat/ipaas/dao/deployment.json");
        dataManager.init();
    }

    @Test
    public void getConnectors() {
        ListResult<Connector> connectors = dataManager.fetchAll(Connector.KIND);
        for (Connector connector : connectors.getItems()) {
            System.out.print(connector.getId().get() + ",");
        }
        Assert.assertTrue(connectors.getTotalCount() > 1);
        Assert.assertTrue(connectors.getItems().size() > 1);
        Assert.assertEquals(connectors.getTotalCount(), connectors.getItems().size());
    }

    @Test
    public void getConnections() {
        ListResult<Connection> connections = dataManager.fetchAll(Connection.KIND);
        for (Connection connection : connections.getItems()) {
            System.out.print(connection.getId().get() + ",");
        }
        Assert.assertTrue(connections.getTotalCount() > 1);
        Assert.assertTrue(connections.getItems().size() > 1);
        Assert.assertEquals(connections.getTotalCount(), connections.getItems().size());
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
        Assert.assertTrue(connectors.getTotalCount() > 1);
        Assert.assertEquals(1, connectors.getItems().size());
    }

    @Test
    public void getConnector() {
        Connector connector = dataManager.fetch(Connector.KIND, "twitter");
        System.out.println(connector.getName());
        Assert.assertEquals("First Connector in the deployment.json is Twitter", "Twitter", connector.getName());
    }

}
