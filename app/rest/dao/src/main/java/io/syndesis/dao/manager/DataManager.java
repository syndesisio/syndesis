/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.dao.manager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import io.syndesis.core.EventBus;
import io.syndesis.core.Json;
import io.syndesis.core.KeyGenerator;
import io.syndesis.core.SyndesisServerException;
import io.syndesis.core.cache.CacheManager;
import io.syndesis.dao.init.ReadApiClientData;
import io.syndesis.model.ChangeEvent;
import io.syndesis.model.Kind;
import io.syndesis.model.ListResult;
import io.syndesis.model.ModelData;
import io.syndesis.model.WithId;
import io.syndesis.model.connection.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
@SuppressWarnings("PMD.GodClass")
public class DataManager implements DataAccessObjectRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataManager.class.getName());

    @SuppressWarnings("rawtypes")
    private static final Function[] NO_OPERATORS = new Function[0];

    private final CacheManager caches;
    private final EventBus eventBus;
    private final EncryptionComponent encryptionComponent;
    private final ResourceLoader resourceLoader;

    @Value("${deployment.file:io/syndesis/dao/deployment.json}")
    @SuppressWarnings("PMD.ImmutableField") // @Value cannot be applied to final properties
    private String dataFileName = "io/syndesis/dao/deployment.json";
    @SuppressWarnings("PMD.ImmutableField") // @Value cannot be applied to final properties
    @Value("${deployment.load-demo-data:true}")
    private boolean loadDemoData = true;

    private final List<DataAccessObject<?>> dataAccessObjects = new ArrayList<>();
    private final Map<Class<? extends WithId<?>>, DataAccessObject<?>> dataAccessObjectMapping = new ConcurrentHashMap<>();

    // Inject mandatory via constructor injection.
    @Autowired
    public DataManager(CacheManager caches,
                       List<DataAccessObject<?>> dataAccessObjects,
                       EventBus eventBus,
                       EncryptionComponent encryptionComponent,
                       ResourceLoader resourceLoader) {
        this.caches = caches;
        this.eventBus = eventBus;
        this.encryptionComponent = encryptionComponent;
        this.resourceLoader = resourceLoader;
        if (dataAccessObjects != null) {
            this.dataAccessObjects.addAll(dataAccessObjects);
        }
    }

    @PostConstruct
    public void init() {
        for (DataAccessObject<?> dataAccessObject : dataAccessObjects) {
            registerDataAccessObject(dataAccessObject);
        }
    }

    public void resetDeploymentData() {
        loadData();

        if (dataFileName != null) {
            loadData(this.dataFileName);
        }
        if( loadDemoData ) {
            loadData("io/syndesis/dao/demo-data.json");
        }
    }

    private void loadData(String file) {
        ReadApiClientData reader = new ReadApiClientData(encryptionComponent);
        try {
            List<ModelData<?>> mdList = reader.readDataFromFile(file);
            for (ModelData<?> modelData : mdList) {
                store(modelData);
            }
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            throw new IllegalStateException("Cannot read startup data due to: " + e.getMessage(), e);
        }
    }

    private void loadData() {
        try {
            final ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
            final Resource[] resources = resolver.getResources("classpath:/META-INF/syndesis/connector/*.json");

            if (resources != null) {
                ReadApiClientData reader = new ReadApiClientData(encryptionComponent);

                for (Resource resource: resources) {
                    try (InputStream is = resource.getInputStream()) {
                        // Replace placeholders
                        final String text = reader.findAndReplaceTokens(
                            StreamUtils.copyToString(is, StandardCharsets.UTF_8),
                            System.getenv()
                        );

                        Connector connector = Json.reader().forType(Connector.class).readValue(text);

                        if (connector != null) {
                            LOGGER.info("Load connector: {} from resource: {}", connector.getId().orElse(""), resource.getURI());
                            store(connector, Connector.class);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // ignore
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load connector from resources due to: " + e.getMessage(), e);
        }
    }

    public <T extends WithId<T>> void store(ModelData<T> modelData) {
        try {
            final Kind kind = modelData.getKind();
            final T entity = modelData.getData();

            LOGGER.debug("{}:{}", kind, modelData.getDataAsJson());

            store(entity, kind.getModelClass());
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            LOGGER.warn("Cannot load entity from file: ", e);
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    public <T extends WithId<T>> void store(T entity, Class<T> modelClass) {
        try {
            Optional<String> id = entity.getId();
            if (!id.isPresent()) {
                LOGGER.warn("Cannot load entity since it's missing an id: {}", entity);
            } else {
                WithId<?> prev = null;
                try {
                    prev = this.fetch(modelClass, id.get());
                } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") RuntimeException e) {
                    // Lets try to wipe out the previous record in case
                    // we are running into something like a schema change.
                    this.delete(modelClass, id.get());
                }
                if (prev == null) {
                    create(entity);
                } else {
                    update(entity);
                }
            }
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            LOGGER.warn("Cannot load entity: ", e);
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends WithId<T>> ListResult<T> fetchAll(Class<T> model) {
        return fetchAll(model, noOperators());
    }

    @SafeVarargs
    @SuppressWarnings({"unchecked", "varargs"})
    public final <T extends WithId<T>> ListResult<T> fetchAll(Class<T> model, Function<ListResult<T>, ListResult<T>>... operators) {

        ListResult<T> result;
        if (getDataAccessObject(model) != null) {
            return doWithDataAccessObject(model, d -> d.fetchAll(operators));
        } else {
            Kind kind = Kind.from(model);
            Map<String, T> cache = caches.getCache(kind.getModelName());
            result = ListResult.of(cache.values());

            if (operators == null) {
                return result;
            }

            for (Function<ListResult<T>, ListResult<T>> operator : operators) {
                result = operator.apply(result);
            }
            return result;
        }
    }

    public <T extends WithId<T>> T fetch(Class<T> model, String id) {
        Kind kind = Kind.from(model);
        Map<String, T> cache = caches.getCache(kind.getModelName());

        T value = cache.get(id);
        if ( value == null) {
            value = doWithDataAccessObject(model, d -> d.fetch(id));
            if (value != null) {
                cache.put(id, value);
            }
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T extends WithId<T>> Set<String> fetchIds(Class<T> model) {

        if (getDataAccessObject(model) != null) {
            return doWithDataAccessObject(model, d -> d.fetchIds());
        } else {
            Kind kind = Kind.from(model);
            Map<String, T> cache = caches.getCache(kind.getModelName());
            return cache.keySet();
        }
    }

    public <T extends WithId<T>> Set<String> fetchIdsByPropertyValue(Class<T> model, String property, String value, String... additionalPropValues) {
        if (additionalPropValues.length % 2 != 0) {
            throw new IllegalArgumentException("You must provide a even number of additional property/value pairs. " +
                "Found: " + additionalPropValues.length);
        }

        return doWithDataAccessObject(model, d -> {
            Set<String> matchingIds = new HashSet<>(d.fetchIdsByPropertyValue(property, value));
            for (int i = 0; i < additionalPropValues.length - 1; i += 2) {
                if (matchingIds.isEmpty()) {
                    // short circuit
                    return matchingIds;
                }

                String propKey = additionalPropValues[i];
                String propValue = additionalPropValues[i + 1];
                Set<String> ids = d.fetchIdsByPropertyValue(propKey, propValue);
                matchingIds.retainAll(ids);
            }
            return matchingIds;
        });
    }

    public <T extends WithId<T>> Set<String> fetchIdsByPropertyValue(Class<T> model, String property, String value) {
        return doWithDataAccessObject(model, d -> d.fetchIdsByPropertyValue(property, value));
    }

    public <T extends WithId<T>> T create(final T entity) {
        Kind kind = entity.getKind();
        Map<String, T> cache = caches.getCache(kind.getModelName());
        Optional<String> id = entity.getId();
        String idVal;

        final T entityToCreate;
        if (!id.isPresent()) {
            idVal = KeyGenerator.createKey();
            entityToCreate = entity.withId(idVal);
        } else {
            idVal = id.get();
            if (cache.containsKey(idVal)) {
                throw new EntityExistsException("There already exists a "
                    + kind + " with id " + idVal);
            }
            entityToCreate = entity;
        }

        this.<T, T>doWithDataAccessObject(kind.getModelClass(), d -> d.create(entityToCreate));
        cache.put(idVal, entityToCreate);
        broadcast("created", kind.getModelName(), idVal);
        return entityToCreate;
    }

    public <T extends WithId<T>> void update(T entity) {
        Optional<String> id = entity.getId();
        if (!id.isPresent()) {
            throw new EntityNotFoundException("Setting the id on the entity is required for updates");
        }

        String idVal = id.get();

        Kind kind = entity.getKind();
        T previous = this.<T, T>doWithDataAccessObject(kind.getModelClass(), d -> d.update(entity));

        Map<String, T> cache = caches.getCache(kind.getModelName());
        if (!cache.containsKey(idVal) && previous==null) {
            throw new EntityNotFoundException("Can not find " + kind + " with id " + idVal);
        }

        cache.put(idVal, entity);
        broadcast("updated", kind.getModelName(), idVal);

        //TODO 1. properly merge the data ? + add data validation in the REST Resource
    }


    public <T extends WithId<T>> boolean delete(Class<T> model, String id) {
        if (id == null || id.equals("")) {
            throw new EntityNotFoundException("Setting the id on the entity is required for updates");
        }

        Kind kind = Kind.from(model);
        Map<String, WithId<T>> cache = caches.getCache(kind.getModelName());

        // Remove it out of the cache
        WithId<T> entity = cache.remove(id);
        boolean deletedInCache = entity != null;

        // And out of the DAO
        boolean deletedFromDAO = Boolean.TRUE.equals(doWithDataAccessObject(model, d -> d.delete(id)));

        // Return true if the entity was found in any of the two.
        if ( deletedInCache || deletedFromDAO ) {
            broadcast("deleted", kind.getModelName(), id);
            return true;
        }

        return false;
    }

    public <T extends WithId<T>> void deleteAll(Class<T> model) {
        Kind kind = Kind.from(model);
        Map<String, WithId<T>> cache = caches.getCache(kind.getModelName());
        cache.clear();

        doWithDataAccessObject(model, d -> {
            d.deleteAll();
            return null;
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends WithId<T>> DataAccessObject<T> getDataAccessObject(Class<T> type) {
        return (DataAccessObject<T>) dataAccessObjectMapping.get(type);
    }

    @Override
    public <T extends WithId<T>> void registerDataAccessObject(DataAccessObject<T> dataAccessObject) {
        dataAccessObjectMapping.put(dataAccessObject.getType(), dataAccessObject);
    }

    /**
     * Perform a simple action if a {@link DataAccessObject} for the specified kind exists.
     * This is just a way to avoid, duplicating the dao lookup and checks, which are going to change.
     * @param model         The model class of the {@link DataAccessObject}.
     * @param function      The function to perfom on the {@link DataAccessObject}.
     * @param <R>           The return type.
     * @return              The outcome of the function.
     */
    private <T extends WithId<T>, R> R doWithDataAccessObject(Class<T> model, Function<DataAccessObject<T>, R> function) {
        DataAccessObject<T> dataAccessObject = getDataAccessObject(model);
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

    @SuppressWarnings("unchecked")
    private static <T> Function<ListResult<T>, ListResult<T>>[] noOperators() {
        return (Function<ListResult<T>, ListResult<T>>[]) NO_OPERATORS;
    }
}
