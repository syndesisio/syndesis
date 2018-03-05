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
package io.syndesis.server.endpoint.v1.operations;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import io.swagger.annotations.ApiParam;
import io.syndesis.server.dao.manager.WithDataManager;
import io.syndesis.common.model.WithId;

public interface Deleter<T extends WithId<T>> extends Resource, WithDataManager {

    @DELETE
    @Consumes("application/json")
    @Path(value = "/{id}")
    default void delete(@NotNull @PathParam("id") @ApiParam(required = true) String id) {
        Class<T> modelClass = resourceKind().getModelClass();
        getDataManager().delete(modelClass, id);
    }

}
