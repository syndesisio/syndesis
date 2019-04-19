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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class BaseExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {

    @SuppressWarnings("PMD.LoggerIsNotStaticFinal")
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Status status;

    private final String userMessage;

    protected BaseExceptionMapper(final Status status, final String userMessage) {
        this.status = status;
        this.userMessage = userMessage;
    }

    @Override
    public final Response toResponse(final E exception) {
        final String developerMessage = developerMessage(exception);

        log.error(developerMessage, exception);

        final RestError error = new RestError(developerMessage, userMessage, null, status.getStatusCode());

        return Response.status(error.errorCode).type(MediaType.APPLICATION_JSON_TYPE).entity(error).build();
    }

    protected String developerMessage(final E exception) {
        return exception.getMessage();
    }
}
