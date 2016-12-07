package com.redhat.ipaas.rest;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.ipaas.api.Component;
import com.redhat.ipaas.api.ComponentGroup;
import com.redhat.ipaas.api.IPaasEntity;
import com.redhat.ipaas.rest.DataManager;
import com.redhat.ipaas.rest.ModelData;
import com.redhat.ipaas.rest.ReadApiClientData;

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
