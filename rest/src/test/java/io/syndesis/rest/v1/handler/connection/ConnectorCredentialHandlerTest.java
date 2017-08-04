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
package io.syndesis.rest.v1.handler.connection;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectorCredentialHandlerTest {
    @Test
    public void shouldAbsolutizeReturnUrl() {
        final HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        when(httpRequest.getRequestURL())
            .thenReturn(new StringBuffer("https://syndesis.io/api/v1/connections/1/credentials"));

        final URI uri = ConnectorCredentialHandler.absoluteTo(httpRequest, URI.create("/ui?ret=true#state"));

        assertThat(uri).isEqualTo(URI.create("https://syndesis.io/ui?ret=true#state"));
    }
}
