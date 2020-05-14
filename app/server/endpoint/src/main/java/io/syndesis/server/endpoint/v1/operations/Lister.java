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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.swagger.v3.oas.annotations.Parameter;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.WithId;
import io.syndesis.server.dao.manager.WithDataManager;
import io.syndesis.server.endpoint.util.PaginationFilter;

public interface Lister<T extends WithId<T>> extends Resource, WithDataManager {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    default ListResult<T> list(
        @Parameter(required = false, description = "Page number to return") @QueryParam("page") @DefaultValue("1") int page,
        @Parameter(required = false, description = "Number of records per page") @QueryParam("per_page") @DefaultValue("20") int perPage
    ) {
        Class<T> clazz = resourceKind().getModelClass();
        return getDataManager().fetchAll(
            clazz,
        new PaginationFilter<>(new PaginationOptionsFromQueryParams(page, perPage))
        );
    }

}
