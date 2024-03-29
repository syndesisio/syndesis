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
package io.syndesis.server.endpoint.v1;

import java.io.InputStream;

import static java.util.concurrent.TimeUnit.HOURS;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.stereotype.Component;

@CacheFor(value = 1, unit = HOURS)
@Path("/")
@Component
@Tag(name = "documentation")
public class ApiDocumentationEndpoint {

    private static final MediaType YAML_TYPE = MediaType.valueOf("application/yaml");

    @GET
    @Produces("text/html")
    @Operation(description = "Get the REST API documentation")
    @Path("/internal/index.html")
    public InputStream internalDocumentation() {
        return resource("/static/internal/index.html");
    }

    @GET
    @Operation(description = "Get the REST API documentation")
    @Path("/internal/openapi.{type:json|yaml}")
    public Response internalSwagger(@PathParam("type") final String type) {
        return Response.ok(resource("/static/internal/openapi." + type), mediaTypeFor(type)).build();
    }

    @GET
    @Produces("text/html")
    @Operation(description = "Get the Supported REST API documentation")
    @Path("/index.html")
    public InputStream supportedDocumentation() {
        return resource("/static/index.html");
    }

    @SuppressWarnings("resource")
    @GET
    @Operation(description = "Get the REST API documentation")
    @Path("/openapi.{type:json|yaml}")
    public Response supportedSwagger(@PathParam("type") final String type) {
        return Response.ok(resource("/static/openapi." + type), mediaTypeFor(type)).build();
    }

    private static MediaType mediaTypeFor(final String type) {
        if ("json".equals(type)) {
            return MediaType.APPLICATION_JSON_TYPE;
        }

        return YAML_TYPE;
    }

    private static InputStream resource(final String path) {
        return ApiDocumentationEndpoint.class.getResourceAsStream(path);
    }
}
