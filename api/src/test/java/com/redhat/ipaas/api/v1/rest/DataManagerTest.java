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

import com.redhat.ipaas.api.v1.model.Component;
import com.redhat.ipaas.api.v1.model.Integration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.persistence.EntityExistsException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DataManagerTest {

    @Rule
    public InfinispanCache infinispan = new InfinispanCache();
    private DataManager dataManager = null;

    @Before
    public void setupCache() {
        dataManager = new DataManager("com/redhat/ipaas/api/v1/deployment.json", infinispan.getCache());
        dataManager.init();
    }

    @Test
    public void getComponents() {
        Collection<Component> components = dataManager.fetchAll(Component.KIND);
        for (Component component : components) {
            System.out.print(component.getId().get() + ",");
        }
        assertTrue(components.size() > 10);
    }

    @Test
    public void getComponent() {
        Component component = dataManager.fetch(Component.KIND, "1");
        System.out.println(component.getName());
        assertEquals("First Component in the deployment.json is non", "non", component.getName());
    }

    @Test(expected = EntityExistsException.class)
    public void createIntegration() {
        Integration integration = new Integration.Builder().name("new integration name").build();
        integration = dataManager.create(integration);
        assertTrue("A new ID should be created", integration.getId().isPresent());
        System.out.println("id=" + integration.getId().get());

        dataManager.create(integration);
        fail("We just created the entity with this id, so this should fail");
    }

    @Test
    public void updateIntegration() {
        Integration integration = new Integration.Builder().name("new integration name").build();
        integration = dataManager.create(integration);
        assertTrue("A new ID should be created", integration.getId().isPresent());
        integration = new Integration.Builder().createFrom(integration).name("new updated name").build();
        dataManager.update(integration);

        Integration i = dataManager.fetch(Integration.KIND, integration.getId().get());
        assertEquals("Name should be updated", "new updated name", i.getName());
    }

}
