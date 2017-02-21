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
package com.redhat.ipaas.rest.v1.controller.operations;

import com.redhat.ipaas.rest.v1.dao.WithDataManager;
import com.redhat.ipaas.rest.v1.model.ListResult;
import com.redhat.ipaas.rest.v1.model.WithId;
import com.redhat.ipaas.rest.v1.util.PaginationFilter;
import com.redhat.ipaas.rest.v1.util.ReflectiveSorter;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

public interface Lister<T extends WithId> extends Resource<T>, WithDataManager {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiImplicitParams({
        @ApiImplicitParam(
            name = "sort", value = "Sort the result list according to the given field value",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(
            name = "direction", value = "Sorting direction when a 'sort' field is provided. Can be 'asc' " +
            "(ascending) or 'desc' (descending)", paramType = "query", dataType = "string"),
        @ApiImplicitParam(
            name = "page", value = "Page number to return", paramType = "query", dataType = "integer", defaultValue = "1"),
        @ApiImplicitParam(
            name = "per_page", value = "Number of records per page", paramType = "query", dataType = "integer", defaultValue = "20")

    })
    default ListResult<T> list(@Context UriInfo uriInfo) {
        return getDataManager().fetchAll(
            resourceKind(),
            new ReflectiveSorter<>(resourceClass(), new SortOptionsFromQueryParams(uriInfo)),
            new PaginationFilter<>(new PaginationOptionsFromQueryParams(uriInfo))
        );
    }

}
