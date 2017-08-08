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
import java.util.Optional;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.syndesis.credential.CredentialFlowState;
import io.syndesis.credential.Credentials;
import io.syndesis.rest.v1.state.ClientSideState;
import io.syndesis.rest.v1.util.Urls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Path("/credentials")
@Api(value = "credentials")
@Component
public class CredentialHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CredentialHandler.class);

    private final Credentials credentials;

    private final ClientSideState state;

    public CredentialHandler(final Credentials credentials, final ClientSideState state) {
        this.credentials = credentials;
        this.state = state;
    }

    @GET
    @Path("/callback")
    public Response callback(@Context final HttpServletRequest request, @Context final HttpServletResponse response) {
        final Optional<CredentialFlowState> maybeUpdatedFlowState = CredentialFlowState.Builder
            .restoreFrom(state::restoreFrom, request, response).map(s -> s.updateFrom(request))
            .map(s -> tryToFinishAcquisition(request, s)).findFirst();

        final CredentialFlowState flowState = maybeUpdatedFlowState
            .orElseThrow(() -> new EntityNotFoundException("Unable to finish OAuth authorization via callback, "
                + "cannot find the OAuth flow state this callback request relates to"));
        final URI returnUrl = flowState.getReturnUrl();

        return Response.temporaryRedirect(returnUrl)
            .cookie(state.persist(flowState.persistenceKey(), "/", flowState)).build();
    }

    protected CredentialFlowState tryToFinishAcquisition(final HttpServletRequest request,
        final CredentialFlowState flowState) {
        try {
            return credentials.finishAcquisition(flowState, Urls.apiBase(request));
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") final RuntimeException e) {
            LOG.debug("Unable to perform OAuth callback on flow state: {}", flowState, e);

            return null;
        }
    }

}
