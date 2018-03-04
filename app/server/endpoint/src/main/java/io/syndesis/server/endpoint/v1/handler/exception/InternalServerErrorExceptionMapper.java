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

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;

@Component
@Provider
public class InternalServerErrorExceptionMapper implements ExceptionMapper<InternalServerErrorException> {

    @Override
    public final Response toResponse(final InternalServerErrorException exception) {
        final Response response = exception.getResponse();
        try {
            final RestError error = response.readEntity(RestError.class);

            return Response.status(error.errorCode).type(MediaType.APPLICATION_JSON_TYPE).entity(error).build();
        } catch (final ProcessingException e) {
            final RestError error = new RestError(
                "A remote call failed without a detailed error message, status message is: " + response.getStatus()
                    + " - " + response.getStatusInfo().getReasonPhrase(),
                "Please contact the administrator and file a bug report", null, response.getStatus());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(error).build();
        }
    }

}
