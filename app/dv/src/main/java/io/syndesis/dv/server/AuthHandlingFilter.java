/*
 * Copyright (C) 2013 Red Hat, Inc.
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

public class AuthHandlingFilter implements HandlerInterceptor, CredentialsProvider {

    public static class AuthToken {
        private String token;

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
        private AuthToken token;
        private String user;

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

    public static ThreadLocal<OAuthCredentials> threadOAuthCredentials  = new ThreadLocal<OAuthCredentials>();

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object contentHandler) throws Exception {
        String accessToken = request.getHeader("X-Forwarded-Access-Token");
        String user = request.getHeader("X-Forwarded-User");
        if (KLog.getLogger().isTraceEnabled()) {
            KLog.getLogger().trace("URL =" + request.getRequestURI());
            KLog.getLogger().trace("X-Forwarded-Access-Token = " + accessToken);
            KLog.getLogger().trace("X-Forwarded-User = " + user);
        }
        OAuthCredentials creds = new OAuthCredentials(accessToken, user);
        threadOAuthCredentials.set(creds);
        return true;
    }

    @Override
    public OAuthCredentials getCredentials() {
        return threadOAuthCredentials.get();
    }
}
