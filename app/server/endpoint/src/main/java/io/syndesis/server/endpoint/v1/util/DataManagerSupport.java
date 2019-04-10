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
package io.syndesis.server.endpoint.v1.util;

import java.util.Optional;

import io.syndesis.common.model.WithId;
import io.syndesis.common.model.bulletin.BulletinBoard;
import io.syndesis.server.dao.manager.DataManager;

public final class DataManagerSupport {
    private DataManagerSupport() {
    }

    public static <T extends WithId<T>> Optional<T> fetch(DataManager dataManager, Class<T> model, WithId<T> withId) {
        if (!withId.hasId()) {
            return Optional.empty();
        }

        return fetch(dataManager, model, withId.getId().get());
    }

    public static <T extends WithId<T>> Optional<T> fetch(DataManager dataManager, Class<T> model, String id) {
        return Optional.ofNullable(dataManager.fetch(model, id));
    }

    public static <T extends BulletinBoard<T>> Optional<T> fetchBoard(DataManager dataManager, Class<T> type, String id) {
       return dataManager.fetchByPropertyValue(type, "targetResourceId", id);
    }
}
