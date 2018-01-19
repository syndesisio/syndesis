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
package io.syndesis.runtime.contracts;

import java.io.IOException;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import io.syndesis.runtime.BaseITCase;

import org.junit.Before;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.http.HttpHeaders;

public abstract class ContractsBase extends BaseITCase {

    @LocalServerPort
    private int port;

    protected WebTarget webTarget;

    @Provider
    public final class SetupToken implements ClientRequestFilter {

        @Override
        public void filter(final ClientRequestContext requestContext) throws IOException {
            final String token = tokenRule.validToken();

            final MultivaluedMap<String, Object> headers = requestContext.getHeaders();
            headers.putSingle(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            headers.putSingle("X-Forwarded-User", "someone_important");
            headers.putSingle("X-Forwarded-Access-Token", token);
        }

    }

    @Before
    public void setupWebTarget() {
        webTarget = ClientBuilder.newClient().register(new SetupToken()).target("http://localhost:" + port);
    }

}
