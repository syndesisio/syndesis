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
package io.syndesis.server.endpoint.v1.handler.connection;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.syndesis.server.credential.AcquisitionFlow;
import io.syndesis.server.credential.AcquisitionMethod;
import io.syndesis.server.credential.AcquisitionRequest;
import io.syndesis.server.credential.AcquisitionResponse;
import io.syndesis.server.credential.AcquisitionResponse.State;
import io.syndesis.server.credential.CredentialFlowState;
import io.syndesis.server.credential.Credentials;
import io.syndesis.server.endpoint.v1.state.ClientSideState;

import static io.syndesis.server.endpoint.v1.util.Urls.absoluteTo;
import static io.syndesis.server.endpoint.v1.util.Urls.apiBase;

@Api(value = "credentials")
public class ConnectorCredentialHandler {

    private final String connectorId;

    private final Credentials credentials;

    private final ClientSideState state;

    public ConnectorCredentialHandler(@Nonnull final Credentials credentials, final ClientSideState state,
        @Nonnull final String connectorId) {
        this.credentials = credentials;
        this.state = state;
        this.connectorId = connectorId;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@NotNull @Valid final AcquisitionRequest request,
        @Context final HttpServletRequest httpRequest) {

        final AcquisitionFlow acquisitionFlow = credentials.acquire(connectorId, apiBase(httpRequest),
            absoluteTo(httpRequest, request.getReturnUrl()));

        final CredentialFlowState flowState = acquisitionFlow.state().get();
        final NewCookie cookie = state.persist(flowState.persistenceKey(), "/", flowState);

        final AcquisitionResponse acquisitionResponse = AcquisitionResponse.Builder.from(acquisitionFlow)
            .state(State.Builder.cookie(cookie.toString())).build();

        return Response.accepted().entity(acquisitionResponse).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public AcquisitionMethod get() {
        return credentials.acquisitionMethodFor(connectorId);
    }

}
