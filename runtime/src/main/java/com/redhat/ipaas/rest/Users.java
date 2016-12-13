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
package com.redhat.ipaas.rest;

import com.redhat.ipaas.api.v1.model.User;
import com.redhat.ipaas.rest.util.ReflectiveSorter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

@Path("/users")
@Api(value = "users")
public class Users {

    @Inject
    private DataManager dataMgr;

    @Context
    private UriInfo uri;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List users")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success", response = User.class)})
    public Collection<User> list() {
        return dataMgr.fetchAll(User.KIND,
            new ReflectiveSorter<>(User.class, new SortOptionsFromQueryParams(uri)));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}")
    @ApiOperation(value = "Get a user by ID")
    public User get(
        @ApiParam(value = "id of the User", required = true) @PathParam("id") String id) {
        User user = dataMgr.fetch(User.KIND, id);

        return user;
    }

}
