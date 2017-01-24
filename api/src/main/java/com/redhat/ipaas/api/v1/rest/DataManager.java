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
import com.redhat.ipaas.api.v1.model.ListResult;
import com.redhat.ipaas.api.v1.model.Organization;
import com.redhat.ipaas.api.v1.model.Permission;
import com.redhat.ipaas.api.v1.model.Role;
import com.redhat.ipaas.api.v1.model.Step;
import com.redhat.ipaas.api.v1.model.Tag;
import com.redhat.ipaas.api.v1.model.User;
import com.redhat.ipaas.api.v1.model.WithId;
import com.redhat.ipaas.api.v1.rest.exception.IPaasServerException;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@ApplicationScoped
public class DataManager implements DataAccessObjectRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataManager.class.getName());

    private ObjectMapper mapper;
    private CacheContainer caches;

    // Inject optional data file by field injection.
    @Inject
    @ConfigurationValue("deployment.file")
    private String dataFileName;

    private final List<DataAccessObject> dataAccessObjects = new ArrayList<>();
    private final Map<Class, DataAccessObject> dataAccessObjectMapping = new HashMap<>();

    // This is needed by CDI to create a proxy, but the actual instance created will use the constructor below which
    // has properly configured constructor injection.
    public DataManager() {
    }

    // Constructor to help with testing.
    public DataManager(CacheContainer caches, ObjectMapper mapper, DataAccessObjectProvider dataAccessObjects, String dataFileName) {
        this(caches, mapper, dataAccessObjects);
        this.dataFileName = dataFileName;
    }

    // Inject mandatory via constructor injection.
    @Inject
    public DataManager(CacheContainer caches, ObjectMapper mapper,  DataAccessObjectProvider dataAccessObjects) {
        this.mapper = mapper;
        this.caches = caches;
        if (dataAccessObjects != null) {
            this.dataAccessObjects.addAll(dataAccessObjects.getDataAccessObjects());
        }
    }

    @PostConstruct
    public void init() {
        if (dataFileName != null) {
            ReadApiClientData reader = new ReadApiClientData();
            try {
                List<ModelData> mdList = reader.readDataFromFile(dataFileName);
                for (ModelData modelData : mdList) {
                    addToCache(modelData);
                }
            } catch (Exception e) {
                throw new IllegalStateException("Cannot read dummy startup data due to: " + e.getMessage(), e);
            }
        }

        for (DataAccessObject dataAccessObject : dataAccessObjects) {
            registerDataAccessObject(dataAccessObject);
        }
    }

    public void addToCache(ModelData modelData) {
        try {
            Class<? extends WithId> clazz;
            clazz = getClass(modelData.getKind());
            Cache<String, WithId> cache = caches.getCache(modelData.getKind().toLowerCase());

            LOGGER.debug(modelData.getKind() + ":" + modelData.getData());
            WithId entity = clazz.cast(mapper.readValue(modelData.getData(), clazz));
            Optional<String> id = entity.getId();
            String idVal;
            if (!id.isPresent()) {
                idVal = generatePK(cache);
                entity = entity.withId(idVal);
            } else {
                idVal = id.get();
            }
            cache.put(idVal, entity);
        } catch (Exception e) {
            IPaasServerException.launderThrowable(e);
        }
    }

    /**
     * Simple generator to mimic behavior in api-client project. When we start
     * hooking up the back-end systems we may need to query those for the PK
     *
     * @param entityMap
     * @return
     */
    public String generatePK(Cache<String, WithId> entityMap) {
        int counter = 1;
        while (true) {
            String pk = String.valueOf(entityMap.size() + counter++);
            if (!entityMap.containsKey(pk)) {
                return pk;
            }
        }
    }

    public Class<? extends WithId> getClass(String kind) {
        switch (kind.toLowerCase()) {
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
        throw IPaasServerException.launderThrowable(new IllegalArgumentException("No matching class found for model " + kind));
    }

    @SuppressWarnings("unchecked")
    public <T extends WithId> ListResult<T> fetchAll(String kind, Function<ListResult<T>, ListResult<T>>... operators) {
        Cache<String, WithId> cache = caches.getCache(kind);

        //TODO: This is currently broken and needs to be properly addressed.
        //... until then just use the cache for pre-loaded data.
        if (cache.isEmpty()) {
            return (ListResult<T>) doWithDataAccessObject(kind, d -> d.fetchAll());
        }

        ListResult<T> result = new ListResult.Builder<T>()
            .items((Collection<T>) cache.values())
            .totalCount(cache.values().size())
            .build();

        for (Function<ListResult<T>, ListResult<T>> operator : operators) {
            result = operator.apply(result);
        }
        return result;
    }

    public <T extends WithId> T fetch(String kind, String id) {
        Map<String, WithId> cache = caches.getCache(kind);
        return (T) cache.computeIfAbsent(id, i -> doWithDataAccessObject(kind, d -> (T) d.fetch(i)));
    }

    public <T extends WithId> T create(T entity) {
        String kind = entity.kind();
        Cache<String, WithId> cache = caches.getCache(kind);
        Optional<String> id = entity.getId();
        String idVal;
        if (!id.isPresent()) {
            idVal = generatePK(cache);
            entity = (T) entity.withId(idVal);
        } else {
            idVal = id.get();
            if (cache.keySet().contains(idVal)) {
                throw new EntityExistsException("There already exists a "
                    + kind + " with id " + idVal);
            }
        }

        T finalEntity = entity;
        cache.put(idVal, (T) doWithDataAccessObject(kind, d -> d.create(finalEntity)));
        return entity;
    }

    public void update(WithId entity) {
        String kind = entity.kind();
        Map<String, WithId> cache = caches.getCache(kind);

        Optional<String> id = entity.getId();
        if (!id.isPresent()) {
            throw new EntityNotFoundException("Setting the id on the entity is required for updates");
        }

        String idVal = id.get();
        if (!cache.containsKey(idVal)) {
            throw new EntityNotFoundException("Can not find " + kind + " with id " + idVal);
        }

        doWithDataAccessObject(kind, d -> d.update(entity));
        cache.put(idVal, entity);
        //TODO 1. properly merge the data ? + add data validation in the REST Resource
    }


    public boolean delete(String kind, String id) {
        Map<String, WithId> cache = caches.getCache(kind);
        if (id == null || id.equals(""))
            throw new EntityNotFoundException("Setting the id on the entity is required for updates");
        if (!cache.containsKey(id))
            throw new EntityNotFoundException("Can not find " + kind + " with id " + id);

        WithId entity = cache.get(id);
        if (entity != null && doWithDataAccessObject(kind, d -> d.delete(entity))) {
            cache.remove(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Map<Class, DataAccessObject> getDataAccessObjectMapping() {
        return dataAccessObjectMapping;
    }


    /**
     * Perform a simple action if a {@link DataAccessObject} for the specified kind exists.
     * This is just a way to avoid, duplivating the dao lookup and chekcs, which are going to change.
     * @param kind          The kind of the {@link DataAccessObject}.
     * @param function      The function to perfom on the {@link DataAccessObject}.
     * @param <O>           The return type.
     * @return              The outcome of the function.
     */
    private <O> O doWithDataAccessObject(String kind, Function<DataAccessObject, O> function) {
        DataAccessObject dataAccessObject = getDataAccessObject(getClass(kind));
        if (dataAccessObject != null) {
            return function.apply(dataAccessObject);
        }
        return null;
    }

}
