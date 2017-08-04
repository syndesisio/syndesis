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
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Cookie;
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

    private static final Cookie[] NONE = new Cookie[0];

    private final Credentials credentials;

    private final ClientSideState state;

    public CredentialHandler(final Credentials credentials, final ClientSideState state) {
        this.credentials = credentials;
        this.state = state;
    }

    @GET
    @Path("/callback")
    public Response callback(@Context final HttpServletRequest request, @Context final HttpServletResponse response) {
        final Cookie[] servletCookies = Optional.ofNullable(request.getCookies()).orElse(NONE);

        final Optional<URI> maybeLocation = Arrays.stream(servletCookies)
            .filter(c -> c.getName().startsWith(CredentialFlowState.CREDENTIAL_PREFIX))
            .map(CredentialHandler::toJaxRsCookie).map(c -> restoreOrDrop(response, c)).filter(Objects::nonNull)
            .map(s -> s.updateFrom(request)).map(s -> tryToFinishAcquisition(request, response, s)).findFirst();

        return Response.temporaryRedirect(maybeLocation
            .orElseThrow(() -> new EntityNotFoundException("Unable to finish OAuth authorization via callback, "
                + "cannot find the OAuth flow state this callback request relates to")))
            .build();
    }

    protected void removeCookie(final HttpServletResponse response, final String cookieName) {
        final Cookie removal = new Cookie(cookieName, "");
        removal.setMaxAge(0);
        removal.setHttpOnly(true);
        removal.setSecure(true);

        response.addCookie(removal);
    }

    protected CredentialFlowState restoreOrDrop(final HttpServletResponse response,
        final javax.ws.rs.core.Cookie cookie) {
        try {
            final CredentialFlowState flowState = state.restoreFrom(cookie, CredentialFlowState.class);

            if (cookie.getName().endsWith(flowState.getKey())) {
                // prevent tampering
                return flowState;
            }
        } catch (final IllegalArgumentException e) {
            LOG.debug("Unable to restore flow state from HTTP cookie: {}", cookie, e);
        }

        // remove cookies that can't be restored or have mismatched name/value
        removeCookie(response, cookie.getName());

        return null;
    }

    protected URI tryToFinishAcquisition(HttpServletRequest request, final HttpServletResponse response,
        final CredentialFlowState flowState) {
        try {
            final URI location = credentials.finishAcquisition(flowState, Urls.apiBase(request));

            removeCookie(response, flowState.persistenceKey());

            return location;
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") final RuntimeException e) {
            LOG.debug("Unable to perform OAuth callback on flow state: {}", flowState, e);

            return null;
        }
    }

    protected static javax.ws.rs.core.Cookie toJaxRsCookie(final Cookie cookie) {
        return new javax.ws.rs.core.Cookie(cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getDomain());
    }
}
