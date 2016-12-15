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

import com.redhat.ipaas.api.v1.model.Connection;
import com.redhat.ipaas.rest.util.ReflectiveSorter;
import io.swagger.annotations.*;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

@Path("/connections")
@Api(value = "connections")
public class Connections {

    @Inject
    private DataManager dataMgr;

    @Context
    private UriInfo uri;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List connections")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success", response = Connection.class)})
    @ApiImplicitParams({
        @ApiImplicitParam(
            name = "sort", value = "Sort the result list according to the given field value",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(
            name = "direction", value = "Sorting direction when a 'sort' field is provided. Can be 'asc' " +
                                        "(ascending) or 'desc' (descending)", paramType = "query", dataType = "string")

    })
    public Collection<Connection> list() {
        return dataMgr.fetchAll(Connection.KIND, new ReflectiveSorter<>(Connection.class, new SortOptionsFromQueryParams(uri)));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}")
    @ApiOperation(value = "Get connection by ID")
    public Connection get(
        @ApiParam(value = "id of the connection", required = true) @PathParam("id") String id) {
        return dataMgr.fetch(Connection.KIND, id);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/json")
    @ApiOperation(value = "Create a new connection")
    public Connection create(Connection connection) {
        return dataMgr.create(connection);
    }

    @PUT
    @Path(value = "/{id}")
    @Consumes("application/json")
    @ApiOperation(value = "Update a connection")
    public void update(
        @ApiParam(value = "id of the connection", required = true) @PathParam("id") String id,
        Connection connection) {
        dataMgr.update(connection);

    }

    @DELETE
    @Consumes("application/json")
    @Path(value = "/{id}")
    @ApiOperation(value = "Delete a connection")
    public void delete(
        @ApiParam(value = "id of the connection", required = true) @PathParam("id") String id) {
        dataMgr.delete(Connection.KIND, id);

    }


}
