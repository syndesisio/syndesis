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
package io.syndesis.jsondb.dao;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.syndesis.core.SyndesisServerException;
import io.syndesis.core.Json;
import io.syndesis.dao.manager.DataAccessObject;
import io.syndesis.jsondb.JsonDB;
import io.syndesis.model.Kind;
import io.syndesis.model.ListResult;
import io.syndesis.model.WithId;

/**
 * Implements a DataAccessObject using the {@see: JsonDB}.
 */
public abstract class JsonDbDao<T extends WithId<T>> implements DataAccessObject<T> {

    private final JsonDB jsondb;

    public JsonDbDao(JsonDB jsondb) {
        this.jsondb = jsondb;
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
            return Json.mapper().readValue(json, getType());
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") RuntimeException|IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    @Override
    public ListResult<T> fetchAll() {
        try {
            // get the data out..
            byte[] json = jsondb.getAsByteArray(getCollectionPath());
            if( json!=null && json.length > 0 ) {

                // Lets use jackson to parse the map of keys to our model instances
                ObjectMapper mapper = Json.mapper();
                TypeFactory typeFactory = mapper.getTypeFactory();
                MapType mapType = typeFactory.constructMapType(LinkedHashMap.class, String.class, getType());
                LinkedHashMap<String, T> map = mapper.readValue(json, mapType);

                return ListResult.of(map.values());
            }

            return ListResult.of(Collections.<T>emptyList());
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

            byte[] json = Json.mapper().writeValueAsBytes(entity);
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
                byte[] json = Json.mapper().writeValueAsBytes(entity);
                jsondb.set(dbPath, json);
            }
            return previousValue;

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
