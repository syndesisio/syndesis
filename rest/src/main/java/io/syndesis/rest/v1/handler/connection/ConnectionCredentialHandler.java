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
package io.syndesis.rest.v1.handler.connection;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.syndesis.credential.Acquisition;
import io.syndesis.credential.AcquisitionRequest;
import io.syndesis.credential.Credentials;

import org.springframework.web.context.request.ServletWebRequest;

@Api(value = "credentials")
public class ConnectionCredentialHandler {

    private final String connectionId;

    private final String connectorId;

    private final Credentials credentials;

    public ConnectionCredentialHandler(@Nonnull final Credentials credentials, @Nonnull final String connectionId,
        @Nonnull final String connectorId) {
        this.credentials = credentials;
        this.connectionId = connectionId;
        this.connectorId = connectorId;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Acquisition create(final AcquisitionRequest request, @Context final HttpServletRequest httpRequest) {
        final ServletWebRequest webRequest = new ServletWebRequest(httpRequest);

        return credentials.acquire(connectionId, connectorId, absoluteTo(httpRequest, request), webRequest);
    }

    protected static URI absoluteTo(final HttpServletRequest httpRequest, final AcquisitionRequest request) {
        final URI current = URI.create(httpRequest.getRequestURL().toString());
        final URI returnUrl = request.returnUrl();

        try {
            return new URI(current.getScheme(), null, current.getHost(), current.getPort(), returnUrl.getPath(),
                returnUrl.getQuery(), returnUrl.getFragment());
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
