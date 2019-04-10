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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import io.syndesis.server.dao.manager.WithDataManager;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.validation.AllValidations;

public interface Creator<T extends WithId<T>> extends Resource, WithDataManager {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/json")
    default T create(@Context SecurityContext sec,
        @NotNull @Valid @ConvertGroup(from = Default.class, to = AllValidations.class) T obj) {
        return getDataManager().create(obj);
    }

}
