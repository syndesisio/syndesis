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
import com.redhat.ipaas.core.KeyGenerator;
import com.redhat.ipaas.dao.init.ModelData;
import com.redhat.ipaas.dao.init.ReadApiClientData;
import com.redhat.ipaas.model.ChangeEvent;
import com.redhat.ipaas.model.Kind;
import com.redhat.ipaas.model.ListResult;
import com.redhat.ipaas.model.WithId;
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
    }

    public void resetDeploymentData() {
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
            Class<? extends WithId> clazz = (Class<? extends WithId>) modelData.getKind().modelClass;
            Kind kind = modelData.getKind();

            LOGGER.debug(kind + ":" + modelData.getDataAsJson());
            WithId entity = (WithId) modelData.getData();
            Optional<String> id = entity.getId();
            if (!id.isPresent()) {
                LOGGER.warn("Cannot load entity from file since it's missing an id: " + modelData.toJson());
            } else {
                WithId prev = null;
                try {
                    prev = fetch(kind.getModelClass(), id.get());
                } catch (RuntimeException e) {
                    // Lets try to wipe out the previous record in case
                    // we are running into something like a schema change.
                    delete(kind.getModelClass(), id.get());
                }
                if (prev == null) {
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

    @SuppressWarnings("unchecked")
    public <T extends WithId> ListResult<T> fetchAll(Class<T> model, Function<ListResult<T>, ListResult<T>>... operators) {

        ListResult<T> result;
        if( getDataAccessObject(model)!=null ) {
            result = (ListResult<T>) doWithDataAccessObject(model, d -> d.fetchAll());
        } else {
            Kind kind = Kind.from(model);
            Cache<String, WithId> cache = caches.getCache(kind.getModelName());
            result = ListResult.of((Collection<T>) cache.values());
        }

        for (Function<ListResult<T>, ListResult<T>> operator : operators) {
            result = operator.apply(result);
        }
        return result;
    }

    public <T extends WithId> T fetch(Class<T> model, String id) {
        Kind kind = Kind.from(model);
        Map<String, WithId> cache = caches.getCache(kind.getModelName());

        T value = (T) cache.get(id);
        if ( value == null) {
            value = doWithDataAccessObject(model, d -> (T) d.fetch(id));
            if (value != null) {
                cache.put(id, value);
            }
        }
        return value;
    }

    public <T extends WithId> T create(T entity) {
        Kind kind = entity.getKind();
        Cache<String, WithId> cache = caches.getCache(kind.getModelName());
        Optional<String> id = entity.getId();
        String idVal;
        if (!id.isPresent()) {
            idVal = KeyGenerator.createKey();
            entity = (T) entity.withId(idVal);
        } else {
            idVal = id.get();
            if (cache.keySet().contains(idVal)) {
                throw new EntityExistsException("There already exists a "
                    + kind + " with id " + idVal);
            }
        }

        T finalEntity = entity;
        doWithDataAccessObject(kind.getModelClass(), d -> d.create(finalEntity));
        cache.put(idVal, finalEntity);
        broadcast("created", kind.getModelName(), idVal);
        return finalEntity;
    }

    public void update(WithId entity) {
        Kind kind = entity.getKind();
        Map<String, WithId> cache = caches.getCache(kind.getModelName());

        Optional<String> id = entity.getId();
        if (!id.isPresent()) {
            throw new EntityNotFoundException("Setting the id on the entity is required for updates");
        }

        String idVal = id.get();
        if (!cache.containsKey(idVal)) {
            throw new EntityNotFoundException("Can not find " + kind + " with id " + idVal);
        }

        doWithDataAccessObject(kind.getModelClass(), d -> d.update(entity));
        cache.put(idVal, entity);
        broadcast("updated", kind.getModelName(), idVal);

        //TODO 1. properly merge the data ? + add data validation in the REST Resource
    }


    public <T extends WithId> boolean delete(Class<T> model, String id) {
        Kind kind = Kind.from(model);
        Map<String, WithId> cache = caches.getCache(kind.getModelName());
        if (id == null || id.equals(""))
            throw new EntityNotFoundException("Setting the id on the entity is required for updates");

        // Remove it out of the cache
        WithId entity = cache.remove(id);
        boolean deletedInCache = entity != null;

        // And out of the DAO
        boolean deletedFromDAO = doWithDataAccessObject(model, d -> d.delete(id)) == Boolean.TRUE;

        // Return true if the entity was found in any of the two.
        if ( deletedInCache || deletedFromDAO ) {
            broadcast("deleted", kind.getModelName(), id);
            return true;
        } else {
            return false;
        }
    }

    public <T extends WithId> void deleteAll(Class<T> model) {
        Kind kind = Kind.from(model);
        Map<String, WithId> cache = caches.getCache(kind.getModelName());
        cache.clear();

        doWithDataAccessObject(model, d -> {
            d.deleteAll();
            return null;
        });
    }

    @Override
    public Map<Class, DataAccessObject> getDataAccessObjectMapping() {
        return dataAccessObjectMapping;
    }


    /**
     * Perform a simple action if a {@link DataAccessObject} for the specified kind exists.
     * This is just a way to avoid, duplicating the dao lookup and checks, which are going to change.
     * @param model         The model class of the {@link DataAccessObject}.
     * @param function      The function to perfom on the {@link DataAccessObject}.
     * @param <O>           The return type.
     * @return              The outcome of the function.
     */
    private <O> O doWithDataAccessObject(Class model, Function<DataAccessObject, O> function) {
        DataAccessObject dataAccessObject = getDataAccessObject(model);
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

    public void clearCache() {
        for (Kind kind : Kind.values()) {
            caches.getCache(kind.modelName).clear();
        }
    }
}
