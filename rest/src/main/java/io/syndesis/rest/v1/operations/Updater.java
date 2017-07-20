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
package io.syndesis.rest.v1.operations;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import io.swagger.annotations.ApiParam;
import io.syndesis.dao.manager.WithDataManager;
import io.syndesis.model.WithId;

public interface Updater<T extends WithId<T>> extends Resource, WithDataManager {

    @PUT
    @Path(value = "/{id}")
    @Consumes("application/json")
    default void update(@NotNull @PathParam("id") @ApiParam(required = true) String id, @NotNull @Valid T obj) {
        getDataManager().update(obj);
    }

}
