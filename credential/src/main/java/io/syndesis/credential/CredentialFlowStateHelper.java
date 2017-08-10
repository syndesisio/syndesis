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
package io.syndesis.credential;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CredentialFlowStateHelper {

    private static final Logger LOG = LoggerFactory.getLogger(CredentialFlowStateHelper.class);

    private CredentialFlowStateHelper() {
        // utility class
    }

    /* default */ static void removeCookie(final HttpServletResponse response, final String cookieName) {
        final Cookie removal = new Cookie(cookieName, "");
        removal.setPath("/");
        removal.setMaxAge(0);
        removal.setHttpOnly(true);
        removal.setSecure(true);

        response.addCookie(removal);
    }

    /* default */ static Stream<CredentialFlowState> restoreFrom(
        final BiFunction<javax.ws.rs.core.Cookie, Class<CredentialFlowState>, CredentialFlowState> restore,
        final HttpServletRequest request, final HttpServletResponse response) {
        final Cookie[] servletCookies = request.getCookies();

        if (servletCookies == null || servletCookies.length == 0) {
            return Stream.of();
        }

        return Arrays.stream(servletCookies).filter(c -> c.getName().startsWith(CredentialFlowState.CREDENTIAL_PREFIX))
            .map(CredentialFlowStateHelper::toJaxRsCookie).map(c -> restoreOrDrop(restore, response, c))
            .filter(Objects::nonNull);
    }

    /* default */ static CredentialFlowState restoreOrDrop(
        final BiFunction<javax.ws.rs.core.Cookie, Class<CredentialFlowState>, CredentialFlowState> restore,
        final HttpServletResponse response, final javax.ws.rs.core.Cookie cookie) {
        try {
            final CredentialFlowState flowState = restore.apply(cookie, CredentialFlowState.class);

            // prevent tampering
            if (cookie.getName().endsWith(flowState.getKey())) {
                return flowState;
            }
        } catch (final IllegalArgumentException e) {
            LOG.debug("Unable to restore flow state from HTTP cookie: {}", cookie, e);
        }

        // remove cookies that can't be restored or have mismatched
        // name/value
        removeCookie(response, cookie.getName());

        return null;
    }

    /* default */ static javax.ws.rs.core.Cookie toJaxRsCookie(final Cookie cookie) {
        return new javax.ws.rs.core.Cookie(cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getDomain());
    }
}
