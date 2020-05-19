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
package io.syndesis.dv.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import io.syndesis.dv.utils.KLog;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthHandlingFilter implements HandlerInterceptor {

    private static final ThreadLocal<OAuthCredentials> THREAD_OAUTH_CREDENTIALS  = new ThreadLocal<OAuthCredentials>();

    public static class AuthToken {
        private final String token;

        public AuthToken(String token) {
            this.token = token;
        }

        @Override
        public String toString() {
            return token;
        }

        public String getHttpAuthentication() {
            return "Bearer " + toString();
        }
    }

    public static class OAuthCredentials {
        private final AuthToken token;
        private final String user;

        public OAuthCredentials(String token, String user) {
            this.token = new AuthToken(token);
            this.user = user;
        }

        public AuthToken getToken() {
            return token;
        }
        public String getUser() {
            return user;
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object contentHandler) {
        String accessToken = request.getHeader("X-Forwarded-Access-Token");
        String user = request.getHeader("X-Forwarded-User");
        if (KLog.getLogger().isTraceEnabled()) {
            KLog.getLogger().trace("URL = %s", request.getRequestURI());
            KLog.getLogger().trace("X-Forwarded-Access-Token = %s", accessToken);
            KLog.getLogger().trace("X-Forwarded-User = %s", user);
        }
        OAuthCredentials creds = new OAuthCredentials(accessToken, user);
        THREAD_OAUTH_CREDENTIALS.set(creds);
        return true;
    }

    public OAuthCredentials getCredentials() {
        return THREAD_OAUTH_CREDENTIALS.get();
    }
}
