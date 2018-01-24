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
package io.syndesis.rest.v1.handler.logs;

import io.swagger.annotations.Api;
import io.syndesis.rest.dblogging.jaxrs.LogResource;
import io.syndesis.rest.dblogging.jaxrs.model.Exchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

/**
 * Created by chirino on 1/18/18.
 */
@Path("/logs")
@Api(value = "logs")
@Component
@ConditionalOnProperty(value = "features.dblogging.enabled", havingValue = "true", matchIfMissing = false)
public class LogHandler {

    private final LogResource resource;

    public LogHandler(LogResource resource) {
        this.resource = resource;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{integrationId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Exchange> getLogs(
        @PathParam("integrationId") String integrationId,
        @QueryParam("from") String from,
        @QueryParam("limit") Integer limit
    ) throws IOException {
        return resource.getLogs(integrationId, from, limit);
    }
}
