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
package com.redhat.ipaas.rest;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityExistsException;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redhat.ipaas.api.Component;
import com.redhat.ipaas.api.IPaasEntity;
import com.redhat.ipaas.api.Integration;

public class DataManagerTest {

	static DataManager dataManager = null;
	
	@BeforeClass
	public static void setupCache(){
		Cache<String, Map<String,IPaasEntity>> cache = new DefaultCacheManager().getCache();
		dataManager = new DataManager(cache);
		dataManager.init();
	}
	
	@Test
	public void getComponents() {
		Collection<Component> components = dataManager.fetchAll(Component.class);
		for (Component component : components) {
			System.out.print(component.getId() + ",");
		}
		assertTrue(components.size() > 10);
	}
	
	@Test
	public void getComponent() {
	    Component component = dataManager.fetch(Component.class,"1");
		System.out.println(component.getName());
		assertTrue("First Component in the deployment.json is Facebook", component.getName().equals("Facebook"));
	}
	
	@Test
	public void createIntegration() {
		Integration integration = new Integration();
		integration.setName("new integration name");
		String id = dataManager.create(integration);
		System.out.println("id=" + id);
		assertTrue("A new ID should be created", id!=null);
		
		try {
			dataManager.create(integration);
			assertTrue("We just created the entity with this id, so this should fail",true);
		} catch (EntityExistsException e) {}
	}
	
	@Test
	public void updateIntegration() {
		Integration integration = new Integration();
		integration.setName("new integration name");
		String id = dataManager.create(integration);
		integration.setId(id);
		assertTrue("A new ID should be created", id!=null);
		integration.setName("new updated name");
		dataManager.update(integration);
		
		Integration i = dataManager.fetch(Integration.class, id);
		assertTrue("Name should be updated", "new updated name".equals(i.getName()));
	}

}
