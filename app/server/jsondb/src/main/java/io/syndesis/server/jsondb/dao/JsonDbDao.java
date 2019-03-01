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
package io.syndesis.server.jsondb.dao;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.server.dao.manager.DataAccessObject;
import io.syndesis.server.dao.manager.operators.IdPrefixFilter;
import io.syndesis.server.jsondb.GetOptions;
import io.syndesis.server.jsondb.JsonDB;

/**
 * Implements a DataAccessObject using the {@see: JsonDB}.
 */
public abstract class JsonDbDao<T extends WithId<T>> implements DataAccessObject<T> {

    private final ObjectReader reader;

    private final JsonDB jsondb;

    public JsonDbDao(JsonDB jsondb) {
        this.jsondb = jsondb;

        reader = Json.copyObjectMapperConfiguration()
            .addMixIn(Connection.Builder.class, CanWriteUsage.class)
            .addMixIn(Extension.Builder.class, CanWriteUsage.class)
            .reader();
    }

    public String getCollectionPath() {
        return "/"+Kind.from(getType()).getModelName()+"s";
    }

    @Override
    public T fetch(String id) {
        try {
            String dbPath = getCollectionPath()+"/:"+id;
            byte[] json = jsondb.getAsByteArray(dbPath);
            if( json==null || json.length == 0 ) {
                return null;
            }
            return reader.forType(getType()).readValue(json);
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") RuntimeException|IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    @Override
    @SuppressWarnings({"rawtypes","unchecked"})
    public ListResult<T> fetchAll() {
        return fetchAll(new Function[]{});
    }

    @Override
    @SuppressWarnings({"unchecked", "PMD.CyclomaticComplexity"})
    public ListResult<T> fetchAll(Function<ListResult<T>, ListResult<T>>... operators) {
        try {

            GetOptions options = new GetOptions();

            // Try to convert operators to equivalent DB queries.
            if( operators!=null ) {
                for (int i = 0; i < operators.length; i++) {
                    Function<ListResult<T>, ListResult<T>> operator = operators[i];
                    if( operator.getClass() == IdPrefixFilter.class ) {
                        IdPrefixFilter<T> filter = (IdPrefixFilter<T>) operator;
                        options.startAt(":"+filter.getPrefix());
                        options.endAt(":"+filter.getPrefix());
                        operators[i] = null; // Take it out of the list.
                    }
                }
            }

            // get the data out..
            byte[] json = jsondb.getAsByteArray(getCollectionPath(), options);
            ListResult<T> result;
            if( json!=null && json.length > 0 ) {

                // Lets use jackson to parse the map of keys to our model instances
                TypeFactory typeFactory = reader.getTypeFactory();
                MapType mapType = typeFactory.constructMapType(LinkedHashMap.class, String.class, getType());
                LinkedHashMap<String, T> map = reader.forType(mapType).readValue(json);

                result = ListResult.of(map.values());
            } else {
                result = ListResult.of(Collections.<T>emptyList());
            }

            if (operators == null) {
                return result;
            }
            for (Function<ListResult<T>, ListResult<T>> operator : operators) {
                if( operator!=null ) {
                    result = operator.apply(result);
                }
            }
            return result;
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") RuntimeException|IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    @Override
    public Set<String> fetchIds() {
        try {

            String json = jsondb.getAsString(getCollectionPath(), new GetOptions().depth(1));
            if (json != null) {
                Map<String,Boolean> map = reader.forType(new TypeReference<Map<String,Boolean>>() {}).readValue(json);
                return map.keySet()
                     .stream().map(path -> path.substring(path.indexOf(':') + 1)).collect(Collectors.toSet());
            } else {
                return Collections.emptySet();
            }

        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") RuntimeException|IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    @Override
    public Set<String> fetchIdsByPropertyValue(final String property, final String propertyValue) {
        return jsondb.fetchIdsByPropertyValue(getCollectionPath(), property.replace('.', '/'), propertyValue)
            .stream().map(path -> path.substring(path.indexOf(':') + 1)).collect(Collectors.toSet());
    }

    @Override
    public T create(T entity) {
        try {

            String dbPath = getCollectionPath()+"/:"+entity.getId().get();

            // Only create if it did not exist.
            if( jsondb.exists(dbPath) ) {
                return null;
            }

            byte[] json = Json.writer().writeValueAsBytes(entity);
            jsondb.set(dbPath, json);

            return entity;

        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") RuntimeException|IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    @Override
    public T update(T entity) {
        try {
            T previousValue = this.fetch(entity.getId().get());

            // Only update if the entity existed.
            if( previousValue !=null ) {
                String dbPath = getCollectionPath()+"/:"+entity.getId().get();
                byte[] json = Json.writer().writeValueAsBytes(entity);
                jsondb.set(dbPath, json);
            }
            return previousValue;

        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") RuntimeException|IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    @Override
    public void set(T entity) {
        try {
            String dbPath = getCollectionPath()+"/:"+entity.getId().get();
            byte[] json = Json.writer().writeValueAsBytes(entity);
            jsondb.set(dbPath, json);
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") RuntimeException|IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    @Override
    public boolean delete(WithId<T> entity) {
        return this.delete(entity.getId().get());
    }

    @Override
    public boolean delete(String id) {
        try {
            if( id == null ) {
                throw new IllegalArgumentException("id not set");
            }
            String dbPath = getCollectionPath()+"/:"+id;
            return jsondb.delete(dbPath);
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") RuntimeException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            String dbPath = getCollectionPath();
            jsondb.set(dbPath, "{}");
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") RuntimeException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }
}
