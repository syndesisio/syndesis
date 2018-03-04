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
package io.syndesis.server.credential;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import io.syndesis.server.credential.CredentialFlowState.Builder.Restorer;

import org.apache.commons.lang3.ArrayUtils;

final class CredentialFlowStateHelper {

    private CredentialFlowStateHelper() {
        // utility class
    }

    static Set<CredentialFlowState> restoreFrom(final Restorer restore,
        final HttpServletRequest request) {
        final Cookie[] servletCookies = request.getCookies();

        if (ArrayUtils.isEmpty(servletCookies)) {
            return Collections.emptySet();
        }

        final List<javax.ws.rs.core.Cookie> credentialCookies = Arrays.stream(servletCookies)
            .filter(c -> c.getName().startsWith(CredentialFlowState.CREDENTIAL_PREFIX))
            .map(CredentialFlowStateHelper::toJaxRsCookie).collect(Collectors.toList());

        try {
            return restore.apply(credentialCookies, CredentialFlowState.class);
        } catch (final IllegalArgumentException e) {
            return Collections.emptySet();
        }
    }

    static javax.ws.rs.core.Cookie toJaxRsCookie(final Cookie cookie) {
        return new javax.ws.rs.core.Cookie(cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getDomain());
    }
}
