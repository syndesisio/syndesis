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
package io.syndesis.server.endpoint.v1.handler.resource;

import java.util.Optional;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.integration.api.IntegrationResourceManager;

import org.springframework.stereotype.Component;

@Path("/resources")
@Api(value = "resources")
@Component
public class ResourceHandler {

    private static final Response NOT_FOUND = Response.status(Response.Status.NOT_FOUND).build();
    private final IntegrationResourceManager resourceManager;

    public ResourceHandler(IntegrationResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @GET
    @Path(value = "/{kind}/{id}")
    public Response get(@NotNull @PathParam("kind") @ApiParam(required = true) Kind kind, @NotNull @PathParam("id") @ApiParam(required = true) String id) {
        if (kind == Kind.OpenApi) {
            Optional<OpenApi> maybeOpenApi = resourceManager.loadOpenApiDefinition(id);

            return maybeOpenApi.map(r -> Response.ok(r.getDocument(), "application/vnd.oai.openapi+json").build())
                .orElse(NOT_FOUND);
        }

        return NOT_FOUND;
    }

}
