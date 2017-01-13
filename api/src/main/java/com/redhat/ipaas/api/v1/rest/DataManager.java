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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.ipaas.api.v1.model.Component;
import com.redhat.ipaas.api.v1.model.ComponentGroup;
import com.redhat.ipaas.api.v1.model.Connection;
import com.redhat.ipaas.api.v1.model.Environment;
import com.redhat.ipaas.api.v1.model.EnvironmentType;
import com.redhat.ipaas.api.v1.model.Integration;
import com.redhat.ipaas.api.v1.model.IntegrationConnectionStep;
import com.redhat.ipaas.api.v1.model.IntegrationPattern;
import com.redhat.ipaas.api.v1.model.IntegrationPatternGroup;
import com.redhat.ipaas.api.v1.model.IntegrationRuntime;
import com.redhat.ipaas.api.v1.model.IntegrationTemplate;
import com.redhat.ipaas.api.v1.model.IntegrationTemplateConnectionStep;
import com.redhat.ipaas.api.v1.model.Organization;
import com.redhat.ipaas.api.v1.model.Permission;
import com.redhat.ipaas.api.v1.model.Role;
import com.redhat.ipaas.api.v1.model.Step;
import com.redhat.ipaas.api.v1.model.Tag;
import com.redhat.ipaas.api.v1.model.User;
import com.redhat.ipaas.api.v1.model.WithId;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@ApplicationScoped
public class DataManager {

    private static Logger logger = LoggerFactory.getLogger(DataManager.class.getName());

    private ObjectMapper mapper = ObjectMapperHolder.OBJECT_MAPPER;

    // Inject optional data file by field injection.
    @Inject
    @ConfigurationValue("deployment.file")
    private String dataFileName;

    private Cache<String, Map<String, WithId>> cache;

    // This is needed by CDI to create a proxy, but the actual instance created will use the constructor below which
    // has properly configured constructor injection.
    public DataManager() {
    }

    // Constructor to help with testing.
    public DataManager(String dataFileName, Cache<String, Map<String, WithId>> cache) {
        this(cache);
        this.dataFileName = dataFileName;
    }

    // Inject mandatory via constructor injection.
    @Inject
    public DataManager(Cache<String, Map<String, WithId>> cache) {
        this.cache = cache;
    }

    @PostConstruct
    public void init() {
        if (cache.isEmpty() && dataFileName != null) {
            ReadApiClientData reader = new ReadApiClientData();
            try {
                List<ModelData> mdList = reader.readDataFromFile(dataFileName);
                for (ModelData modelData : mdList) {
                    addToCache(modelData);
                }
            } catch (IOException e) {
                logger.error("Cannot read dummy startup data due to: " + e.getMessage(), e);
            }

        }
    }

    public void addToCache(ModelData modelData) {
        try {
            Class<? extends WithId> clazz = getClass(modelData.getModel());
            Map<String, WithId> entityMap = cache.computeIfAbsent(modelData.getModel().toLowerCase(), k -> new HashMap<>());
            logger.debug(modelData.getModel() + ":" + modelData.getData());
            WithId entity = clazz.cast(mapper.readValue(modelData.getData(), clazz));
            Optional<String> id = entity.getId();
            String idVal;
            if (!id.isPresent()) {
                idVal = generatePK(entityMap);
                entity = entity.withId(idVal);
            } else {
                idVal = id.get();
            }
            entityMap.put(idVal, entity);
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

    /**
     * Simple generator to mimic behavior in api-client project. When we start
     * hooking up the back-end systems we may need to query those for the PK
     *
     * @param entityMap
     * @return
     */
    public String generatePK(Map<String, WithId> entityMap) {
        int counter = 1;
        while (true) {
            String pk = String.valueOf(entityMap.size() + counter++);
            if (!entityMap.containsKey(pk)) {
                return pk;
            }
        }
    }

    public Class<? extends WithId> getClass(String model) throws ClassNotFoundException {
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
    public <T> List<T> fetchAll(String model, Function<List<T>, List<T>>... operators) {
        Map<String, WithId> entityMap = cache.computeIfAbsent(model, k -> new HashMap<>());
        List<T> result = new ArrayList<>((Collection<? extends T>) entityMap.values());
        for (Function<List<T>, List<T>> operator : operators) {
            result = operator.apply(result);
        }
        return result;
    }

    public <T> T fetch(String model, String id) {
        Map<String, WithId> entityMap = cache.computeIfAbsent(model, k -> new HashMap<>());
        if (entityMap.get(id) == null) {
            return null;
        }
        return (T) entityMap.get(id);
    }

    public <T extends WithId> T create(T entity) {
        String model = entity.kind();
        Map<String, WithId> entityMap = cache.computeIfAbsent(model, k -> new HashMap<>());
        Optional<String> id = entity.getId();
        String idVal;
        if (!id.isPresent()) {
            idVal = generatePK(entityMap);
            entity = (T) entity.withId(idVal);
        } else {
            idVal = id.get();
            if (entityMap.keySet().contains(idVal)) {
                throw new EntityExistsException("There already exists a "
                    + model + " with id " + idVal);
            }
        }
        entityMap.put(idVal, entity);
        //TODO interact with the back-end system
        return entity;
    }

    public void update(WithId entity) {
        String model = entity.kind();
        Map<String, WithId> entityMap = cache.get(model);
        Optional<String> id = entity.getId();
        if (!id.isPresent()) {
            throw new EntityNotFoundException("Setting the id on the entity is required for updates");
        }
        String idVal = id.get();
        if (entityMap == null || !entityMap.containsKey(idVal)) {
            throw new EntityNotFoundException("Can not find " + model + " with id " + idVal);
        }
        //TODO 1. properly merge the data ? + add data validation in the REST Resource
        //TODO 2. interact with the back-end system
        entityMap.put(idVal, entity);
    }

    public void delete(String model, String id) {
        Map<String, WithId> entityMap = cache.get(model);
        if (id == null || id.equals(""))
            throw new EntityNotFoundException("Setting the id on the entity is required for updates");
        if (entityMap == null || !entityMap.containsKey(id))
            throw new EntityNotFoundException("Can not find " + model + " with id " + id);
        //TODO interact with the back-end system
        entityMap.remove(id);
    }

}
