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
package com.redhat.ipaas.core;

import org.keycloak.TokenVerifier;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.common.VerificationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class Tokens {

    private static ThreadLocal<String> OAUTH_TOKEN = new InheritableThreadLocal<>();

    public static String getAuthenticationToken() {
        String stringToken = OAUTH_TOKEN.get();
        if (stringToken != null) {
            return stringToken;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Cannot set authorization header because there is no authenticated principal");
        } else if (!KeycloakAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            throw new IllegalStateException(String.format("Cannot set authorization header because Authentication is of type %s but %s is required",
                new Object[]{authentication.getClass(), KeycloakAuthenticationToken.class}));
        } else {
            KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) authentication;
            return token.getAccount().getKeycloakSecurityContext().getTokenString();
        }
    }

    public static boolean isTokenExpired(String token) {
        TokenVerifier verifier = TokenVerifier.create(token);
        try {
            return verifier.parse().getToken().isExpired();
        } catch (VerificationException e) {
            return false;
        }
    }

    public static void setAuthenticationToken(String token) {
        OAUTH_TOKEN.set(token);
    }

}
