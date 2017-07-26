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
package io.syndesis.dao;

import java.io.IOException;
import java.util.ArrayList;

import io.syndesis.core.Json;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.Kind;
import io.syndesis.model.ListResult;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DataManagerTest {

    @Rule
    public InfinispanCache infinispan = new InfinispanCache();

    private DataManager dataManager = null;

    @Before
    public void setup() {
        //Create Data Manager
        dataManager = new DataManager(infinispan.getCaches(), new ArrayList<>(), "io/syndesis/dao/deployment.json");
        dataManager.init();
        dataManager.resetDeploymentData();
    }

    @Test
    public void getConnectors() {
        ListResult<Connector> connectors = dataManager.fetchAll(Connector.class);
        for (Connector connector : connectors.getItems()) {
            System.out.print(connector.getId().get() + ",");
        }
        Assert.assertTrue(connectors.getTotalCount() > 1);
        Assert.assertTrue(connectors.getItems().size() > 1);
        Assert.assertEquals(connectors.getTotalCount(), connectors.getItems().size());
    }

    @Test
    public void getConnections() {
        ListResult<Connection> connections = dataManager.fetchAll(Connection.class);
        for (Connection connection : connections.getItems()) {
            System.out.print(connection.getId().get() + ",");
        }
        Assert.assertEquals(4, connections.getTotalCount());
        Assert.assertEquals(4, connections.getItems().size());
        Assert.assertEquals(connections.getTotalCount(), connections.getItems().size());
    }

    @Test
    public void getConnectorsWithFilterFunction() {
        ListResult<Connector> connectors = dataManager.fetchAll(
            Connector.class,
            resultList -> new ListResult.Builder<Connector>().createFrom(resultList).items(resultList.getItems().subList(0, 2)).build()
        );
        for (Connector connector : connectors.getItems()) {
            System.out.print(connector.getId().get() + ",");
        }
        Assert.assertEquals(13, connectors.getTotalCount());
        Assert.assertEquals(2, connectors.getItems().size());
    }

    @Test
    public void getTwitterConnector() {
        Connector connector = dataManager.fetch(Connector.class, "twitter");
        System.out.println(connector.getName());
        Assert.assertEquals("First Connector in the deployment.json is Twitter", "Twitter", connector.getName());
        Assert.assertEquals(7, connector.getActions().size());
    }

    @Test
    public void getSalesforceConnector() {
        Connector connector = dataManager.fetch(Connector.class, "salesforce");
        System.out.println(connector.getName());
        Assert.assertEquals("Second Connector in the deployment.json is Salesforce", "Salesforce", connector.getName());
        Assert.assertEquals(7, connector.getActions().size());
    }

    @Test
    public void getIntegration() throws IOException {
        Integration integration = dataManager.fetch(Integration.class, "1");
        System.out.println(integration.getName());
        Assert.assertEquals("Example Integration", "Twitter to Salesforce Example", integration.getName());
        Assert.assertEquals(4, integration.getSteps().get().size());
        Assert.assertTrue(integration.getTags().get().contains("example"));

        //making sure we can deserialize Enums such as StatusType
        Integration int2 = new Integration.Builder().createFrom(integration).desiredStatus(Integration.Status.Activated).build();
        String json = Json.mapper().writeValueAsString(int2);
        Integration int3 = Json.mapper().readValue(json, Integration.class);
        Assert.assertEquals(int2.getDesiredStatus(), int3.getDesiredStatus());
    }

    @Test
    public void createShouldCreateWithUnspecifiedIds() {
        final Connector given = new Connector.Builder().icon("my-icon").build();
        final Connector got = dataManager.create(given);

        assertThat(got).isEqualToIgnoringGivenFields(given, "id");
        assertThat(got.getId()).isPresent();
        assertThat(infinispan.getCaches().getCache(Kind.Connector.modelName).get(got.getId().get())).isSameAs(got);
    }

    @Test
    public void createShouldCreateWithSpecifiedId() {
        final Connector connector = new Connector.Builder().id("custom-id").build();
        final Connector got = dataManager.create(connector);

        assertThat(got).isSameAs(connector);
        assertThat(infinispan.getCaches().getCache(Kind.Connector.modelName).get("custom-id")).isSameAs(connector);
    }
}
