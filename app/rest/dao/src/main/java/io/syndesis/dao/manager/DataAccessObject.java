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

import java.util.Set;
import java.util.function.Function;

import io.syndesis.model.ListResult;
import io.syndesis.model.WithId;

public interface DataAccessObject<T extends WithId<T>> {

    /**
     * @return true if this object cannot be used to create/update entities.
     */
    default boolean isReadOnly() {
        return false;
    }

    /**
     * @return The Type of entity it supports.
     */
    Class<T> getType();

    /**
     * Fetches an entity by id.
     * @param id    The id.
     * @return      The matching entity.
     */
    T fetch(String id);

    /**
     * Fetches all ids that have the specified property with the given value.
     *
     * @param property      The name of the property.
     * @param propertyValue The value of the property.
     * @return              All identifiers with specified property and value combination.
     */
    Set<String> fetchIdsByPropertyValue(String property, String propertyValue);

    /**
     * Fetches a {@link ListResult} containing all entities.
     * @return  The {@link ListResult}.
     */
    ListResult<T> fetchAll();

    @SuppressWarnings("unchecked")
    default ListResult<T> fetchAll(Function<ListResult<T>, ListResult<T>>... operators) {
        ListResult<T> result = fetchAll();
        if (operators == null) {
            return result;
        }
        for (Function<ListResult<T>, ListResult<T>> operator : operators) {
            result = operator.apply(result);
        }
        return result;
    }

    /**
     * Creates a new entity.
     * @param entity    The entity.
     * @return          The created entity.
     */
    T create(T entity);


    /**
     * Updates the specified entity.
     * @param entity    The entity.
     * @return          The previous value or null.
     */
    T update(T entity);


    /**
     * Delete the specified entity.
     * @param entity    The entity.
     * @return          True on successful deletion, false otherwise.
     */
    boolean delete(WithId<T> entity);


    /**
     * Delete the entity with the specified id.
     * @param id        The id of the entity.
     * @return          True on successful deletion, false otherwise.
     */
    boolean delete(String id);


    default void deleteAll() {
        ListResult<T> l = fetchAll();
        for (T entity : l.getItems()) {
            delete(entity);
        }
    }

}
