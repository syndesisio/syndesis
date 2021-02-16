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
package io.syndesis.connector.rest.swagger;

import java.util.List;

import org.apache.camel.http.common.HttpOperationFailedException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.base.Joiner;

public final class HttpErrorDetail implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(final ExtensionContext context, final Throwable throwable) throws Throwable {
        final Throwable cause = throwable.getCause();

        if (!(cause instanceof HttpOperationFailedException)) {
            throw throwable;
        }

        final HttpOperationFailedException httpError = (HttpOperationFailedException) cause;

        final List<ServeEvent> events = WireMock.getAllServeEvents();
        final String message = "Received HTTP status: " + httpError.getStatusCode() + " " + httpError.getStatusText() + "\n\n"
            + httpError.getResponseBody() + "\nRequests received:\n"
            + Joiner.on('\n').join(events.stream().map(ServeEvent::getRequest).iterator());

        throw new AssertionError(message, httpError);
    }

}
