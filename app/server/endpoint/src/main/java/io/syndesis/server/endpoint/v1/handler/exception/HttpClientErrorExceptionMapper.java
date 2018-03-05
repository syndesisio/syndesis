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
package io.syndesis.server.endpoint.v1.handler.exception;

import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@Provider
public class HttpClientErrorExceptionMapper implements ExceptionMapper<HttpClientErrorException> {

    @Override
    public Response toResponse(HttpClientErrorException exception) {
        RestError error = new RestError(
                exception.getMessage(),
                exception.getMessage(),
                ErrorMap.from(new String(exception.getResponseBodyAsByteArray(), StandardCharsets.UTF_8)),
                exception.getStatusCode().value());
        return Response.status(exception.getStatusCode().value()).type(MediaType.APPLICATION_JSON_TYPE).entity(error).build();
    }
}
