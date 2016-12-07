package com.redhat.ipaas.rest;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redhat.ipaas.api.Component;
import com.redhat.ipaas.api.IPaasEntity;

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

}
