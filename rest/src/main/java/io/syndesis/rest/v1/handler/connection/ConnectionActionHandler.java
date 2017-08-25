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
package io.syndesis.rest.v1.handler.connection;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.syndesis.model.connection.Action;
import io.syndesis.model.connection.ActionDefinition;
import io.syndesis.model.connection.Connector;

@Api(value = "actions")
public class ConnectionActionHandler {

    private final List<Action> actions;

    public ConnectionActionHandler(final Connector connector) {
        actions = connector.getActions();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}/definition")
    @ApiOperation("Retrieves action definition")
    @ApiResponses(@ApiResponse(code = 200, response = ActionDefinition.class, message = "Definition of the action."))
    public ActionDefinition definition(@PathParam("id") @ApiParam(required = true,
        example = "io.syndesis:salesforce-create-or-update:latest") final String id) {

        final Optional<Action> action = actions.stream().filter(a -> a.idEquals(id)).findAny();

        return action.map(Action::definition).orElseThrow(() -> new EntityNotFoundException("Action with id: " + id));
    }

}
