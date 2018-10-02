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
package io.syndesis.connector.apiprovider;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.web.filter.OncePerRequestFilter;

public final class SpecificationContentTypeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
        final FilterChain filterChain) throws ServletException, IOException {

        final String requestURI = request.getRequestURI();
        final String determined;
        if (requestURI.endsWith(".json")) {
            determined = "application/vnd.oai.openapi+json;version=2.0";
        } else if (requestURI.endsWith(".yaml")) {
            determined = "application/vnd.oai.openapi;version=2.0";
        } else {
            determined = null;
        }

        filterChain.doFilter(request, new HttpServletResponseWrapper(response) {
            @Override
            public void setContentType(final String given) {
                if (determined != null) {
                    super.setContentType(determined);
                } else {
                    super.setContentType(given);
                }
            }
        });
    }

}
