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
package com.redhat.ipaas.api.v1.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.redhat.ipaas.api.v1.model.Component;
import com.redhat.ipaas.api.v1.model.Integration;
import com.redhat.ipaas.api.v1.model.ListResult;
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
        dataManager = new DataManager(infinispan.getCaches(), objectMapper, dataAccessObjectProvider, "com/redhat/ipaas/api/v1/deployment.json");
        dataManager.init();
    }

    @Test
    public void getComponents() {
        ListResult<Component> components = dataManager.fetchAll(Component.KIND);
        for (Component component : components.getItems()) {
            System.out.print(component.getId().get() + ",");
        }
        assertTrue(components.getTotalCount() > 10);
        assertTrue(components.getItems().size() > 10);
        assertEquals(components.getTotalCount(), components.getItems().size());
    }

    @Test
    public void getComponentsWithFilterFunction() {
        ListResult<Component> components = dataManager.fetchAll(
            Component.KIND,
            resultList -> new ListResult.Builder<Component>().createFrom(resultList).items(resultList.getItems().subList(0, 1)).build()
        );
        for (Component component : components.getItems()) {
            System.out.print(component.getId().get() + ",");
        }
        assertTrue(components.getTotalCount() > 10);
        assertEquals(1, components.getItems().size());
    }

    @Test
    public void getComponent() {
        Component component = dataManager.fetch(Component.KIND, "1");
        System.out.println(component.getName());
        assertEquals("First Component in the deployment.json is non", "non", component.getName());
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
