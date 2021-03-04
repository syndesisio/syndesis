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
package io.syndesis.server.endpoint.v1.util;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UrlsTest {

    @ParameterizedTest(name = "{index}: header=\"{0}\"")
    @MethodSource("invalidValues")
    public void shouldDiscardInvalidOriginHeaders(final String invalidHeader) {
        assertThat(Urls.parseOrigin(invalidHeader)).isEmpty();
    }

    public static Collection<String> invalidValues() {
        return Arrays.asList(null, "", "for;host;proto", "for=;host=;proto=", "for=;host=:;proto=",
            "for=;host=:;proto=https", "for=;host=abc:;proto=https", "for=;host=:abc;proto=https",
            "for=;host=:123;proto=https", "for=;host=::;proto=https", "for=;host=abc:-123;proto=https");
    }

    @ParameterizedTest(name = "{index}: header=\"{0}\"")
    @MethodSource("validValues")
    public void shouldDiscardInvalidOriginHeaders(final String header, final URI expected) {
        assertThat(Urls.parseOrigin(header)).hasValue(expected);
    }

    public static Collection<Object[]> validValues() {
        return Arrays.asList(
            new Object[] {"for=localhost;host=host:8080;proto=https", URI.create("https://host:8080")},
            new Object[] {"proto=http; for=localhost; host=host", URI.create("http://host")},
            new Object[] {"host=hostname:12345;proto=http; for=localhost", URI.create("http://hostname:12345")});
    }

    @Test
    public void shouldAbsolutizeReturnUrl() {
        final HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        when(httpRequest.getRequestURL())
            .thenReturn(new StringBuffer("https://syndesis.io/api/v1/connections/1/credentials"));

        final URI uri = Urls.absoluteTo(httpRequest, URI.create("/ui?ret=true#state"));

        assertThat(uri).isEqualTo(URI.create("https://syndesis.io/ui?ret=true#state"));
    }

    @Test
    public void shouldComputeApiBase() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL())
            .thenReturn(new StringBuffer("https://syndesis.io/api/v1/resource/subresource"));

        assertThat(Urls.apiBase(request)).isEqualTo(URI.create("https://syndesis.io/api/v1/"));
    }

    @Test
    public void shouldComputeBasePath() {
        assertThat(Urls.basePath("/api/v1/resource/subresource")).isEqualTo("/api/v1/");
        assertThat(Urls.basePath("/api/v1")).isEqualTo("/api/v1/");
        assertThat(Urls.basePath("/api/v1/")).isEqualTo("/api/v1/");
        assertThat(Urls.basePath("/api/v1/resource")).isEqualTo("/api/v1/");
    }

    @Test
    public void shouldDetermineCurrentUriFromRequest() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL())
            .thenReturn(new StringBuffer("https://syndesis.io/api/v1/resource/subresource"));

        assertThat(Urls.currentUri(request))
            .isEqualTo(URI.create("https://syndesis.io/api/v1/resource/subresource"));
    }

    @Test
    public void shouldDetermineCurrentUriFromRequestAndOriginHeader() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL())
            .thenReturn(new StringBuffer("https://syndesis.io/api/v1/resource/subresource"));
        when(request.getHeader(Urls.ORIGIN_HEADER)).thenReturn("for=127.0.0.1;host=localhost:4200;proto=https");

        assertThat(Urls.currentUri(request))
            .isEqualTo(URI.create("https://localhost:4200/api/v1/resource/subresource"));
    }

    @Test
    public void shouldDetermineCurrentUriFromRequestAndOriginHeaderWithoutPortNumber() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL())
            .thenReturn(new StringBuffer("https://syndesis.io/api/v1/resource/subresource"));
        when(request.getHeader(Urls.ORIGIN_HEADER)).thenReturn("for=127.0.0.1;host=localhost;proto=https");

        assertThat(Urls.currentUri(request)).isEqualTo(URI.create("https://localhost/api/v1/resource/subresource"));
    }
}
