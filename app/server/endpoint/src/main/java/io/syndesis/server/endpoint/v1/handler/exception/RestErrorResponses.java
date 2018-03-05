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

/**
 * Holds static utility methods for constructing Response objects containing a RestError
 */
public final class RestErrorResponses {

    private RestErrorResponses() {
        // utility class
    }

    public static Response badRequest(Throwable e) {
        return create(Response.Status.BAD_REQUEST, e.getMessage(), e.toString());
    }
    public static Response badRequest(String userMsg) {
        return create(Response.Status.BAD_REQUEST, userMsg, userMsg);
    }
    public static Response badRequest(String userMsg, String developerMsg) {
        return create(Response.Status.BAD_REQUEST, userMsg, developerMsg);
    }

    public static Response internalServerError(Throwable e) {
        return create(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage(), e.toString());
    }
    public static Response internalServerError(String userMsg) {
        return create(Response.Status.INTERNAL_SERVER_ERROR, userMsg, userMsg);
    }
    public static Response internalServerError(String userMsg, String developerMsg) {
        return create(Response.Status.INTERNAL_SERVER_ERROR, userMsg, developerMsg);
    }

    public static Response create(Response.Status status, String userMsg) {
        return create(status, userMsg, userMsg);
    }

    public static Response create(Response.Status status, String userMsg, String developerMsg) {
        RestError error = new RestError();
        error.setErrorCode(status.getStatusCode());
        error.setDeveloperMsg(developerMsg);
        error.setUserMsg(userMsg);
        return Response.status(status)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(error)
            .build();
    }
}
