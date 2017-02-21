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
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.redhat.ipaas.rest.v1.model.connection.Connection;
import com.redhat.ipaas.rest.v1.model.connection.Connector;
import com.redhat.ipaas.rest.v1.model.integration.Integration;
import com.redhat.ipaas.rest.v1.model.ListResult;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.server.mock.KubernetesMockServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.persistence.EntityExistsException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DataManagerTest {

    @Rule
    public InfinispanCache infinispan = new InfinispanCache();

    private DataManager dataManager = null;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

    private static final KubernetesMockServer MOCK = new KubernetesMockServer();

    @Before
    public void setup() {
        DataAccessObjectProvider dataAccessObjectProvider = () -> Arrays.asList(new IntegrationDAO(MOCK.createClient()));
        //Create Data Manager
        dataManager = new DataManager(infinispan.getCaches(), objectMapper, dataAccessObjectProvider, "com/redhat/ipaas/rest/v1/deployment.json");
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

    @Test(expected = EntityExistsException.class)
    public void createIntegration() {
        ConfigMap configMap1 = new ConfigMapBuilder()
            .withNewMetadata()
            .withName("integration1")
            .addToLabels(Integration.LABEL_ID, "id1")
            .addToLabels(Integration.LABEL_NAME, "integration one")
            .endMetadata()
            .addToData(IntegrationDAO.CONFIGURATION_KEY, "someconfig")
            .build();


        MOCK.expect().post().withPath("/api/v1/namespaces/test/configmaps").andReturn(201, configMap1).once();
        MOCK.expect().get().withPath("/api/v1/namespaces/test/configmaps/integration1").andReturn(201, configMap1).once();

        Integration integration = new Integration.Builder().name("new integration name").build();
        integration = dataManager.create(integration);
        assertTrue("A new ID should be created", integration.getId().isPresent());

        dataManager.create(integration);
        fail("We just created the entity with this id:["+ integration.getId().orElse("")+"], so this should fail");
    }

    @Test
    public void updateIntegration() {
        ConfigMap configMap2 = new ConfigMapBuilder()
            .withNewMetadata()
            .withName("integration2")
            .addToLabels(Integration.LABEL_ID, "id2")
            .addToLabels(Integration.LABEL_NAME, "integration two")
            .endMetadata()
            .addToData(IntegrationDAO.CONFIGURATION_KEY, "someconfig")
            .build();


        MOCK.expect().post().withPath("/api/v1/namespaces/test/configmaps").andReturn(201, configMap2).once();
        MOCK.expect().get().withPath("/api/v1/namespaces/test/configmaps/integration2").andReturn(200, configMap2).once();
        MOCK.expect().put().withPath("/api/v1/namespaces/test/configmaps/integration2").andReturn(200, configMap2).once();

        Integration integration = new Integration.Builder().id("integration2").name("new integration name").build();
        integration = dataManager.create(integration);
        assertTrue("A new ID should be created", integration.getId().isPresent());
        integration = new Integration.Builder().createFrom(integration).name("new updated name").build();
        dataManager.update(integration);

        Integration i = dataManager.fetch(Integration.KIND, integration.getId().get());
        assertEquals("Name should be updated", "new updated name", i.getName());
    }

}
