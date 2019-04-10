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
package io.syndesis.server.endpoint.v1.handler.activity;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;

/**
 * Provides an REST api to an optional ActivityTrackingService.
 */
@Path("/activity")
@Api(value = "activity")
@Component
public class ActivityHandler {

    private final Optional<ActivityTrackingService> service;
    public ActivityHandler(Optional<ActivityTrackingService> resource) {
        this.service = resource;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/feature")
    public Feature getFeature() {
        return new Feature(service.isPresent());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/integrations/{integrationId}")
    public List<Activity> getActivities(
        @PathParam("integrationId") String integrationId,
        @QueryParam("from") String from,
        @QueryParam("limit") Integer limit
    ) throws IOException {
        if( !service.isPresent() ) {
            throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
        }
        return service.get().getActivities(integrationId, from, limit);
    }

}
