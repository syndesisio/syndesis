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
package io.syndesis.server.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

public class SyndesisCsrfRepository implements CsrfTokenRepository {

    private static final String XSRF_HEADER_NAME = "SYNDESIS-XSRF-TOKEN";
    private static final String XSRF_HEADER_VALUE = "awesome";

    private static final Logger LOG = LoggerFactory.getLogger(SyndesisCsrfRepository.class);

    @Override
    public CsrfToken generateToken(HttpServletRequest httpServletRequest) {
        return new DefaultCsrfToken(XSRF_HEADER_NAME, XSRF_HEADER_NAME, XSRF_HEADER_VALUE);
    }

    @Override
    public void saveToken(CsrfToken csrfToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (csrfToken != null && csrfToken.getHeaderName() != null && csrfToken.getToken() != null) {
            httpServletResponse.setHeader(csrfToken.getHeaderName(), csrfToken.getToken());
        }
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest httpServletRequest) {
        Optional<String> token = extractToken(httpServletRequest);
        if (token.isPresent()) {
            LOG.trace("Xsrf token found in request to uri {}. Value is: {}", httpServletRequest.getRequestURI(), token.get());
        } else {
            LOG.trace("Xsrf token not found in request to uri {}", httpServletRequest.getRequestURI());
        }
        return token.map(val -> new DefaultCsrfToken(XSRF_HEADER_NAME, XSRF_HEADER_NAME, val)).orElse(null);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String token = request.getHeader(XSRF_HEADER_NAME);
        return Optional.ofNullable(token);
    }
}
