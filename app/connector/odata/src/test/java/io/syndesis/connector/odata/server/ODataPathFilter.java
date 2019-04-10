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
package io.syndesis.connector.odata.server;

import java.io.IOException;
import java.net.URI;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import io.syndesis.common.util.StringConstants;

/**
 * Fixes an issue with olingo4 not being able to handle URI ending with
 * a '/'. The olingo4 library results in a 'URI is malformed' exception. This
 * filter takes the request and wraps it to exclude the '/'.
 *
 */
public class ODataPathFilter implements Filter, StringConstants {

    private static class FilteredRequest extends HttpServletRequestWrapper {

        public FilteredRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public StringBuffer getRequestURL() {
            StringBuffer url = super.getRequestURL();
            if (url.charAt(url.length() -1) == FORWARD_SLASH.charAt(0)) {
                url.setLength(url.length() - 1);
            }
            return url;
        }
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        // Nothing to do
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        String requestURI = request.getRequestURI();

        URI uri = URI.create(requestURI);
        if (uri.getPath().endsWith(FORWARD_SLASH)) {
            request = new FilteredRequest(request);
        }

        chain.doFilter(request, res);
    }

    @Override
    public void destroy() {
        //
    }
}
