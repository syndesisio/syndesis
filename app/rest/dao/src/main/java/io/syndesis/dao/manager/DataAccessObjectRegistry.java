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
package io.syndesis.dao.manager;

import io.syndesis.model.WithId;

public interface DataAccessObjectRegistry {

    /**
     * Finds the {@link DataAccessObject} for the specified type.
     * @param type  The class of the specified type.
     * @param <T>   The specified type.
     * @return      The {@link DataAccessObject} if found, or null no matching {@link DataAccessObject} was found.
     */
    <T extends WithId<T>> DataAccessObject<T> getDataAccessObject(Class<T> type);

    /**
     * Finds the {@link DataAccessObject} for the specified type.
     * @param type  The class of the specified type.
     * @param <T>   The specified type.
     * @return      The {@link DataAccessObject} or throws SyndesisServerException.
     */
    default <T extends WithId<T>> DataAccessObject<T> getDataAccessObjectRequired(Class<T> type) {
        final DataAccessObject<T> dao = getDataAccessObject(type);
        if (dao != null) {
            return dao;
        }
        throw new IllegalArgumentException("No data access object found for type: [" + type + "].");
    }

    /**
     * Register a {@link DataAccessObject}.
     * @param dataAccessObject  The {@link DataAccessObject} to register.
     * @param <T>               The type of the {@link DataAccessObject}.
     */
    <T extends WithId<T>> void registerDataAccessObject(DataAccessObject<T> dataAccessObject);
}
