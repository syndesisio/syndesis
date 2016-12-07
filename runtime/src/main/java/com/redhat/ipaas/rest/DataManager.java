package com.redhat.ipaas.rest;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infinispan.Cache;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.ipaas.api.Component;
import com.redhat.ipaas.api.ComponentGroup;
import com.redhat.ipaas.api.Connection;
import com.redhat.ipaas.api.Environment;
import com.redhat.ipaas.api.EnvironmentType;
import com.redhat.ipaas.api.IPaasEntity;
import com.redhat.ipaas.api.Integration;
import com.redhat.ipaas.api.IntegrationConnectionStep;
import com.redhat.ipaas.api.IntegrationPattern;
import com.redhat.ipaas.api.IntegrationPatternGroup;
import com.redhat.ipaas.api.IntegrationRuntime;
import com.redhat.ipaas.api.IntegrationTemplate;
import com.redhat.ipaas.api.IntegrationTemplateConnectionStep;
import com.redhat.ipaas.api.Organization;
import com.redhat.ipaas.api.Permission;
import com.redhat.ipaas.api.Role;
import com.redhat.ipaas.api.Step;
import com.redhat.ipaas.api.Tag;
import com.redhat.ipaas.api.User;

public class DataManager {

	private Cache<String, Map<String,IPaasEntity>> cache;
	private ObjectMapper mapper = new ObjectMapper();
	private String fileName = "com/redhat/ipaas/rest/deployment.json";
	
	public DataManager() {}
	
	public DataManager(Cache<String, Map<String,IPaasEntity>> cache) {
		this.cache = cache;
	}
	
	public void init() {
		if (cache.isEmpty()) {
			ReadApiClientData reader = new ReadApiClientData();
			try {
				List<ModelData> mdList = reader.readDataFromFile(fileName);
				for (ModelData modelData : mdList) {
					addToCache(modelData);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void addToCache(ModelData modelData) {
		
		try {
			Class<? extends IPaasEntity> clazz = getClass(modelData.getModel());
			Map<String,IPaasEntity> entityMap = cache.get(modelData.getModel().toLowerCase());
			if (entityMap == null) {
				entityMap = new HashMap<String, IPaasEntity>();
				cache.put(modelData.getModel().toLowerCase(), entityMap);
			}
			System.out.println(modelData.getModel() + ":" + modelData.getData());
			IPaasEntity entity = clazz.cast(mapper.readValue(modelData.getData(), clazz));
			if (entity.getId()==null) {
				entity.setId(generatePK(entityMap));
			}
			entityMap.put(entity.getId(), entity);
			
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}	
	}
	
	public String generatePK(Map<String,IPaasEntity> entityMap) {
		int counter = 1;
		while (true) {
			String pk = String.valueOf(entityMap.size() + counter++);
			if (!entityMap.containsKey(pk)) return pk;
		}
	}
	
	public Class<? extends IPaasEntity> getClass(String model) throws ClassNotFoundException {
		switch (model.toLowerCase()) {
		case "component":
			return Component.class;
		case "componentgroup":
			return ComponentGroup.class;
		case "connection":
			return Connection.class;
		case "environment":
			return Environment.class;
		case "environmenttype":
			return EnvironmentType.class;
		case "integration":
			return Integration.class;
		case "integrationconnectionstep":
			return IntegrationConnectionStep.class;
		case "integrationpattern":
			return IntegrationPattern.class;
		case "integrationpatterngroup":
			return IntegrationPatternGroup.class;
		case "integrationruntime":
			return IntegrationRuntime.class;
		case "integrationtemplate":
			return IntegrationTemplate.class;
		case "integrationtemplateconnectionstep":
			return IntegrationTemplateConnectionStep.class;
		case "organization":
			return Organization.class;
		case "permission":
			return Permission.class;
		case "role":
			return Role.class;
		case "step":
			return Step.class;
		case "tag":
			return Tag.class;
		case "user":
			return User.class;
		default:
			break;
		}
		throw new ClassNotFoundException("No class found for model " + model);
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<T> fetchAll(Class<T> clazz) {
		String model = clazz.getSimpleName().toLowerCase();
		Map<String,IPaasEntity> entityMap = cache.get(model);
		return (Collection<T>) entityMap.values();
	}
	
	public <T> T fetch(Class<T> clazz, String id) {
		String model = clazz.getSimpleName().toLowerCase();
		Map<String,IPaasEntity> entityMap = cache.get(model);
		return clazz.cast(entityMap.get(id));
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
