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
package io.syndesis.rest.v1;

import java.io.InputStream;

import static java.util.concurrent.TimeUnit.HOURS;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.annotations.ApiOperation;

import org.springframework.stereotype.Component;

@CacheFor(value = 1, unit = HOURS)
@Path("/index.html")
@Component
public class ApiDocumentationEndpoint {

    @GET
    @Produces("text/html")
    @ApiOperation(value = "Get the REST API documentation")
    public InputStream doGet() {
        return ApiDocumentationEndpoint.class.getResourceAsStream("/static/index.html");
    }
}
