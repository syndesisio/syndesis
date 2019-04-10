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
package io.syndesis.connector.meta;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.StringJoiner;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import io.syndesis.common.util.SyndesisServerException;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

@Component
@Provider
public class VerifierExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(VerifierExceptionMapper.class);

    @SuppressWarnings("JavaLangClash")
    public static class Error {
        public final String developerMsg;

        public final Integer errorCode;

        public final String userMsg;

        public Error(final Integer errorCode, final String userMsg, final String developerMsg) {
            this.errorCode = errorCode;
            this.userMsg = userMsg;
            this.developerMsg = developerMsg;
        }

    }

    @Override
    public Response toResponse(final Throwable exception) {
        // the proxy @Context would provide would not let us access the wrapped
        // request
        final HttpServletRequest request = ResteasyProviderFactory.getContextData(HttpServletRequest.class);

        LOG.error("Exception while handling request: {} {}", request.getMethod(), request.getRequestURI(), exception);
        if (LOG.isDebugEnabled()) {
            final ContentCachingRequestWrapper requestCache = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);

            final Enumeration<String> headers = request.getHeaderNames();
            final StringJoiner headersJoined = new StringJoiner("\n");
            while (headers.hasMoreElements()) {
                final String header = headers.nextElement();
                headersJoined.add(header + ": " + String.join("|", Collections.list(request.getHeaders(header))));
            }
            LOG.debug("Headers: \n{}", headersJoined.toString());
            LOG.debug("Request content: \n{}", new String(requestCache.getContentAsByteArray(), StandardCharsets.UTF_8));
        }

        final Error error = new Error(500, rootCauseMessage(exception), exception.getMessage());

        return Response.serverError().entity(error).build();
    }

    private static String rootCauseMessage(final Throwable exception) {
        if (exception instanceof SyndesisServerException) {
            return exception.getMessage();
        }

        Throwable rootCause = exception;
        while (rootCause.getCause() != null && rootCause != rootCause.getCause()) {
            rootCause = rootCause.getCause();
        }

        return rootCause.getMessage();
    }

}
