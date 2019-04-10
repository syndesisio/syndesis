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
package io.syndesis.server.endpoint.v1.handler.atlas;

import io.atlasmap.java.service.JavaService;
import io.atlasmap.java.v2.MavenClasspathResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Component
@ConditionalOnProperty(name="atlas.enabled", havingValue = "true", matchIfMissing = true)
@Path("/atlas/java")
public class SyndesisJavaService extends JavaService {

    /**
     * Stub out mavenclasspath processing for now.
     *
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/mavenclasspath")
    public Response generateClasspath(InputStream request) {
        MavenClasspathResponse response = new MavenClasspathResponse();
        response.setExecutionTime(0L);
        response.setClasspath("");
        return Response.ok().entity(response).build();
    }
}
