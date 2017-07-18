/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.rest.v1.handler.credential;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.syndesis.credential.Credentials;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;

@Path("/credentials")
@Api(value = "credentials")
@Component
public class CredentialHandler {

    private final Credentials credentials;

    public CredentialHandler(final Credentials credentials) {
        this.credentials = credentials;
    }

    @GET
    @Path("/callback")
    public Response callback(@Context final HttpServletRequest httpRequest) {
        final ServletWebRequest webRequest = new ServletWebRequest(httpRequest);

        final URI location = credentials.finishAcquisition(webRequest);

        return Response.temporaryRedirect(location).build();
    }
}
