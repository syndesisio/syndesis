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

import com.redhat.ipaas.api.v1.model.Integration;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/integrations")
@Api(value = "integrations")
public class Integrations {

    @Inject
    private DataManager dataMgr;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List integrations")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success", response = Integration.class)})
    public Collection<Integration> list() {
        return dataMgr.fetchAll(Integration.KIND);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}")
    @ApiOperation(value = "Get an integration by ID")
    public Integration get(
        @ApiParam(value = "id of the Integration", required = true) @PathParam("id") String id) {
        Integration i = dataMgr.fetch(Integration.KIND, id);

        return i;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/json")
    @ApiOperation(value = "Create an integration")
    public Integration create(Integration integration) {
        return dataMgr.create(integration);
    }

    @PUT
    @Path(value = "/{id}")
    @Consumes("application/json")
    @ApiOperation(value = "Update a connection")
    public void update(
        @ApiParam(value = "id of the connection", required = true) @PathParam("id") String id,
        Integration integration) {
        dataMgr.update(integration);

    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}")
    @ApiOperation(value = "Delete a connection")
    public void delete(
        @ApiParam(value = "id of the Integration", required = true) @PathParam("id") String id) {
        dataMgr.delete(Integration.KIND, id);
    }


}
