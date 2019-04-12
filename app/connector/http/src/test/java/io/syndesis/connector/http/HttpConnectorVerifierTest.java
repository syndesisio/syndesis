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
package io.syndesis.connector.http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.syndesis.connector.http.util.BasicValidationHandler;
import io.syndesis.connector.support.verifier.api.ComponentVerifier;
import io.syndesis.connector.support.verifier.api.Verifier;
import io.syndesis.connector.support.verifier.api.VerifierResponse;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.localserver.RequestBasicAuth;
import org.apache.http.localserver.ResponseBasicUnauthorized;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseContent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpConnectorVerifierTest {
    private HttpServer localServer;

    @Before
    public void setUp() throws Exception {
        localServer = ServerBootstrap.bootstrap()
            .setHttpProcessor(getHttpProcessor())
            .registerHandler("/", new BasicValidationHandler("GET", null, null, null))
            .registerHandler("/withPath", new BasicValidationHandler("GET", null, null, null))
            .registerHandler("/with/nested/path", new BasicValidationHandler("GET", null, null, null))
            .create();

        localServer.start();
    }

    @After
    public void tearDown() {
        if (localServer != null) {
            localServer.stop();
        }
    }

    private HttpProcessor getHttpProcessor() {
        return new ImmutableHttpProcessor(
            Arrays.asList(
                new RequestBasicAuth()
            ),
            Arrays.asList(
                new ResponseContent(),
                new ResponseBasicUnauthorized())
        );
    }

    private String getLocalServerHostAndPort() {
        return new StringBuilder()
            .append(localServer.getInetAddress().getHostName())
            .append(":")
            .append(localServer.getLocalPort())
            .toString();
    }

    @Test
    public void testBaseUrlWithoutScheme() throws Exception {
        CamelContext context = new DefaultCamelContext();
        try {
            context.disableJMX();
            context.start();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("baseUrl", getLocalServerHostAndPort());

            Verifier verifier = new HttpVerifier();
            List<VerifierResponse> responses = verifier.verify(context, "http4", parameters);

            assertThat(responses).hasSize(2);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
            assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
        } finally {
            context.stop();
        }
    }

    @Test
    public void testBaseUrlWithScheme() throws Exception {
        CamelContext context = new DefaultCamelContext();
        try {
            context.disableJMX();
            context.start();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("baseUrl", "http://" + getLocalServerHostAndPort());

            Verifier verifier = new HttpVerifier();
            List<VerifierResponse> responses = verifier.verify(context, "http4", parameters);

            assertThat(responses).hasSize(2);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
            assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
        } finally {
            context.stop();
        }
    }

    @Test
    public void testWithPath1() throws Exception {
        CamelContext context = new DefaultCamelContext();
        try {
            context.disableJMX();
            context.start();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("baseUrl", getLocalServerHostAndPort());
            parameters.put("path", "/withPath");

            Verifier verifier = new HttpVerifier();
            List<VerifierResponse> responses = verifier.verify(context, "http4", parameters);

            assertThat(responses).hasSize(2);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
            assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
        } finally {
            context.stop();
        }
    }

    @Test
    public void testWithPath3() throws Exception {
        CamelContext context = new DefaultCamelContext();
        try {
            context.disableJMX();
            context.start();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("baseUrl", getLocalServerHostAndPort() + "/");
            parameters.put("path", "withPath");

            Verifier verifier = new HttpVerifier();
            List<VerifierResponse> responses = verifier.verify(context, "http4", parameters);

            assertThat(responses).hasSize(2);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
            assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
        } finally {
            context.stop();
        }
    }

    @Test
    public void testWithPath4() throws Exception {
        CamelContext context = new DefaultCamelContext();
        try {
            context.disableJMX();
            context.start();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("baseUrl", getLocalServerHostAndPort() + "/");
            parameters.put("path", "/withPath");

            Verifier verifier = new HttpVerifier();
            List<VerifierResponse> responses = verifier.verify(context, "http4", parameters);

            assertThat(responses).hasSize(2);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
            assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
        } finally {
            context.stop();
        }
    }

    @Test
    public void testWithNestedPath1() throws Exception {
        CamelContext context = new DefaultCamelContext();
        try {
            context.disableJMX();
            context.start();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("baseUrl", getLocalServerHostAndPort() + "/with/nested");
            parameters.put("path", "path");

            Verifier verifier = new HttpVerifier();
            List<VerifierResponse> responses = verifier.verify(context, "http4", parameters);

            assertThat(responses).hasSize(2);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
            assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
        } finally {
            context.stop();
        }
    }

    @Test
    public void testWrongScheme() throws Exception {
        CamelContext context = new DefaultCamelContext();
        try {
            context.disableJMX();
            context.start();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("baseUrl", "https://" + getLocalServerHostAndPort());

            Verifier verifier = new HttpVerifier();
            List<VerifierResponse> responses = verifier.verify(context, "http4", parameters);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0)).hasFieldOrPropertyWithValue("scope", Verifier.Scope.PARAMETERS);
            assertThat(responses.get(0)).hasFieldOrPropertyWithValue("status", Verifier.Status.ERROR);

            assertThat(responses.get(0).getErrors()).hasSize(1);
            assertThat(responses.get(0).getErrors().get(0)).hasFieldOrPropertyWithValue("code", "unsupported_scheme");
        } finally {
            context.stop();
        }
    }
}
