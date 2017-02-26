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
package com.redhat.ipaas.rest.v1.dao.jsondb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.redhat.ipaas.rest.v1.controller.handler.exception.IPaasServerException;
import com.redhat.ipaas.jsondb.JsonDB;
import com.redhat.ipaas.rest.v1.dao.DataAccessObject;
import com.redhat.ipaas.rest.v1.model.ListResult;
import com.redhat.ipaas.rest.v1.model.WithId;
import com.redhat.ipaas.rest.v1.util.Json;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Implements a DataAccessObject using the {@see: JsonDB}.
 */
abstract public class JsonDBDAO<T extends WithId> implements DataAccessObject<T> {

    private final JsonDB rtdb;

    public JsonDBDAO(JsonDB rtdb) {
        this.rtdb = rtdb;
    }

    abstract public String getCollectionPath();

    @Override
    public T fetch(String id) {
        try {
            String dbPath = getCollectionPath()+"/:"+id;
            byte[] json = rtdb.getAsByteArray(dbPath);
            if( json==null || json.length == 0 ) {
                return null;
            }
            return Json.mapper().readValue(json, getType());
        } catch (RuntimeException|IOException e) {
            throw IPaasServerException.launderThrowable(e);
        }
    }

    @Override
    public ListResult<T> fetchAll() {
        try {
            // get the data out..
            byte[] json = rtdb.getAsByteArray(getCollectionPath());
            if( json!=null && json.length > 0 ) {

                // Lets use jackson to parse the map of keys to our model instances
                ObjectMapper mapper = Json.mapper();
                TypeFactory typeFactory = mapper.getTypeFactory();
                MapType mapType = typeFactory.constructMapType(LinkedHashMap.class, String.class, getType());
                LinkedHashMap<String, T> map = mapper.readValue(json, mapType);

                return ListResult.of(map.values());
            } else {
                return ListResult.of(Collections.EMPTY_LIST);
            }
        } catch (RuntimeException|IOException e) {
            throw IPaasServerException.launderThrowable(e);
        }
    }

    @Override
    public T create(T entity) {
        try {

            String dbPath = getCollectionPath()+"/:"+entity.getId().get();

            // Only create if it did not exist.
            if( rtdb.exists(dbPath) ) {
                return null;
            }

            byte[] json = Json.mapper().writeValueAsBytes(entity);
            rtdb.set(dbPath, json);

            return entity;

        } catch (RuntimeException|IOException e) {
            throw IPaasServerException.launderThrowable(e);
        }
    }

    @Override
    public T update(T entity) {
        try {
            T previousValue = this.fetch((String) entity.getId().get());

            // Only update if the entity existed.
            if( previousValue !=null ) {
                String dbPath = getCollectionPath()+"/:"+entity.getId().get();
                byte[] json = Json.mapper().writeValueAsBytes(entity);
                rtdb.set(dbPath, json);
            }
            return previousValue;

        } catch (RuntimeException|IOException e) {
            throw IPaasServerException.launderThrowable(e);
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
            return rtdb.delete(dbPath);
        } catch (RuntimeException e) {
            throw IPaasServerException.launderThrowable(e);
        }
    }
}
