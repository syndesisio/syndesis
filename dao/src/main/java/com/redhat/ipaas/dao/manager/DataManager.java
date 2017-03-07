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
package com.redhat.ipaas.dao.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.ipaas.core.EventBus;
import com.redhat.ipaas.core.IPaasServerException;
import com.redhat.ipaas.dao.init.ModelData;
import com.redhat.ipaas.dao.init.ReadApiClientData;
import com.redhat.ipaas.model.ChangeEvent;
import com.redhat.ipaas.model.ListResult;
import com.redhat.ipaas.model.WithId;
import com.redhat.ipaas.model.connection.Action;
import com.redhat.ipaas.model.connection.Connection;
import com.redhat.ipaas.model.connection.Connector;
import com.redhat.ipaas.model.connection.ConnectorGroup;
import com.redhat.ipaas.model.environment.Environment;
import com.redhat.ipaas.model.environment.EnvironmentType;
import com.redhat.ipaas.model.environment.Organization;
import com.redhat.ipaas.model.integration.*;
import com.redhat.ipaas.model.user.Permission;
import com.redhat.ipaas.model.user.Role;
import com.redhat.ipaas.model.user.User;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.function.Function;

@Service
public class DataManager implements DataAccessObjectRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataManager.class.getName());

    private ObjectMapper mapper;
    private CacheContainer caches;
    private final EventBus eventBus;

    @Value("${deployment.file}")
    private String dataFileName;

    private final List<DataAccessObject> dataAccessObjects = new ArrayList<>();
    private final Map<Class, DataAccessObject> dataAccessObjectMapping = new HashMap<>();

    // Constructor to help with testing.
    public DataManager(CacheContainer caches, ObjectMapper mapper, List<DataAccessObject> dataAccessObjects, String dataFileName) {
        this(caches, mapper, dataAccessObjects, (EventBus)null);
        this.dataFileName = dataFileName;
    }

    // Inject mandatory via constructor injection.
    @Autowired
    public DataManager(CacheContainer caches, ObjectMapper mapper, List<DataAccessObject> dataAccessObjects, EventBus eventBus) {
        this.mapper = mapper;
        this.caches = caches;
        this.eventBus = eventBus;
        if (dataAccessObjects != null) {
            this.dataAccessObjects.addAll(dataAccessObjects);
        }
    }

    @PostConstruct
    public void init() {
        for (DataAccessObject dataAccessObject : dataAccessObjects) {
            registerDataAccessObject(dataAccessObject);
        }

        if (dataFileName != null) {
            ReadApiClientData reader = new ReadApiClientData();
            try {
                List<ModelData> mdList = reader.readDataFromFile(dataFileName);
                for (ModelData modelData : mdList) {
                    store(modelData);
                }
            } catch (Exception e) {
                throw new IllegalStateException("Cannot read dummy startup data due to: " + e.getMessage(), e);
            }
        }

    }

    public void store(ModelData modelData) {
        try {
            Class<? extends WithId> clazz = getClass(modelData.getKind());
            String kind = modelData.getKind().toLowerCase();

            LOGGER.debug(modelData.getKind() + ":" + modelData.getData());
            WithId entity = clazz.cast(mapper.readValue(modelData.getData(), clazz));
            Optional<String> id = entity.getId();
            if (!id.isPresent()) {
                LOGGER.warn("Cannot load entity from file since it's missing an id: " + modelData.toJson());
            } else {
                if (fetch(kind, id.get()) == null) {
                    create(entity);
                } else {
                    update(entity);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot load entity from file: " + e);
            throw IPaasServerException.launderThrowable(e);
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
            case Connector.KIND:
                return Connector.class;
            case ConnectorGroup.KIND:
                return ConnectorGroup.class;
            case Connection.KIND:
                return Connection.class;
            case Environment.KIND:
                return Environment.class;
            case EnvironmentType.KIND:
                return EnvironmentType.class;
            case Integration.KIND:
                return Integration.class;
            case IntegrationConnectionStep.KIND:
                return IntegrationConnectionStep.class;
            case IntegrationPattern.KIND:
                return IntegrationPattern.class;
            case IntegrationPatternGroup.KIND:
                return IntegrationPatternGroup.class;
            case IntegrationRuntime.KIND:
                return IntegrationRuntime.class;
            case IntegrationTemplate.KIND:
                return IntegrationTemplate.class;
            case IntegrationTemplateConnectionStep.KIND:
                return IntegrationTemplateConnectionStep.class;
            case Organization.KIND:
                return Organization.class;
            case Permission.KIND:
                return Permission.class;
            case Role.KIND:
                return Role.class;
            case Step.KIND:
                return Step.class;
            case Tag.KIND:
                return Tag.class;
            case User.KIND:
                return User.class;
            case Action.KIND:
                return Action.class;
            default:
                break;
        }
        throw IPaasServerException.launderThrowable(new IllegalArgumentException("No matching class found for model " + kind));
    }

    @SuppressWarnings("unchecked")
    public <T extends WithId> ListResult<T> fetchAll(String kind, Function<ListResult<T>, ListResult<T>>... operators) {
        Cache<String, WithId> cache = caches.getCache(kind);

        ListResult<T> result = (ListResult<T>) doWithDataAccessObject(kind, d -> d.fetchAll());
        if( result == null ) {
            // fall back to using the cache for getting values..
            result = ListResult.of((Collection<T>) cache.values());
        }

        for (Function<ListResult<T>, ListResult<T>> operator : operators) {
            result = operator.apply(result);
        }
        return result;
    }

    public <T extends WithId> T fetch(String kind, String id) {
        Map<String, WithId> cache = caches.getCache(kind);

        // TODO: report bug in cache.computeIfAbsent, it breaks if the mappingFunction returns null
        // return (T) cache.computeIfAbsent(id, i -> doWithDataAccessObject(kind, d -> (T) d.fetch(i)));
        T value = (T) cache.get(id);
        if ( value == null) {
            value = doWithDataAccessObject(kind, d -> (T) d.fetch(id));
            if (value != null) {
                cache.put(id, value);
            }
        }
        return value;
    }

    public <T extends WithId> T create(T entity) {
        String kind = entity.getKind();
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
        doWithDataAccessObject(kind, d -> d.create(finalEntity));
        cache.put(idVal, finalEntity);
        broadcast("created", kind, idVal);
        return finalEntity;
    }

    public void update(WithId entity) {
        String kind = entity.getKind();
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
        broadcast("updated", kind, idVal);

        //TODO 1. properly merge the data ? + add data validation in the REST Resource
    }


    public boolean delete(String kind, String id) {
        Map<String, WithId> cache = caches.getCache(kind);
        if (id == null || id.equals(""))
            throw new EntityNotFoundException("Setting the id on the entity is required for updates");

        // Remove it out of the cache
        WithId entity = cache.remove(id);
        // And out of the DAO
        boolean deletedInDAO = Optional.ofNullable(
            doWithDataAccessObject(kind, d -> d.delete(entity))
        ).orElse(Boolean.FALSE).booleanValue();

        // Return true if the entity was found in any of the two.
        if ( deletedInDAO || entity != null ) {
            broadcast("deleted", kind, id);
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
     * This is just a way to avoid, duplicating the dao lookup and checks, which are going to change.
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

    private void broadcast(String event, String type, String id) {
        if( eventBus !=null ) {
            eventBus.broadcast("change-event", ChangeEvent.of(event, type, id).toJson());
        }
    }

}
