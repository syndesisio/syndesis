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
package io.syndesis.credential;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CredentialFlowStateHelperTest {

    @Test
    public void ifCookieNameDoesNotMatchShouldReturnEmptySet() {
        final Cookie cookie = new Cookie("notMatching", "anyValue");

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});

        final Set<CredentialFlowState> got = CredentialFlowStateHelper.restoreFrom((cookies, cls) -> {
            assertThat(cookies).isEmpty();
            return Collections.emptySet();
        }, request);

        assertThat(got).isEmpty();
    }

    @Test
    public void ifRestoreFailsShouldReturnEmptySet() {
        final Cookie cookie = new Cookie(CredentialFlowState.CREDENTIAL_PREFIX + "key", "anyValue");

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});

        final Set<CredentialFlowState> got = CredentialFlowStateHelper.restoreFrom((cookies, cls) -> {
            assertThat(cookies.get(0).getValue()).isEqualTo("anyValue");
            throw new IllegalArgumentException();
        }, request);

        assertThat(got).isEmpty();
    }

    @Test
    public void shouldConvertServletCookieToJaxRsCookie() {
        final Cookie given = new Cookie("myCookie", "myValue");
        given.setDomain("example.com");
        given.setPath("/path");
        given.setMaxAge(1800);
        given.setHttpOnly(true);
        given.setSecure(true);

        final javax.ws.rs.core.Cookie expected = new javax.ws.rs.core.Cookie("myCookie", "myValue", "/path",
            "example.com");

        assertThat(CredentialFlowStateHelper.toJaxRsCookie(given)).isEqualTo(expected);
    }

    @Test
    public void shouldRestoreCookiesToStreamOfState() {
        final CredentialFlowState expected1 = new OAuth2CredentialFlowState.Builder().connectorId("connectorId")
            .key("key1").build();
        final CredentialFlowState expected2 = new OAuth2CredentialFlowState.Builder().connectorId("connectorId")
            .key("key2").build();

        final Cookie cookie1 = new Cookie(CredentialFlowState.CREDENTIAL_PREFIX + "key1", "anyValue");
        final Cookie cookie2 = new Cookie(CredentialFlowState.CREDENTIAL_PREFIX + "key2", "anyValue");

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[] {cookie1, cookie2});

        when(request.getCookies()).thenReturn(new Cookie[] {cookie1, cookie2});
        final Set<CredentialFlowState> states = CredentialFlowStateHelper.restoreFrom((cookies, cls) -> {
            assertThat(cookies).allSatisfy(cookie -> assertThat(cookie.getValue()).isEqualTo("anyValue"));

            return cookies.stream()
                .map(cookie -> new OAuth2CredentialFlowState.Builder().connectorId("connectorId")
                    .key(cookie.getName().substring(CredentialFlowState.CREDENTIAL_PREFIX.length())).build())
                .collect(Collectors.toSet());
        }, request);

        assertThat(states).containsOnly(expected1, expected2);
    }

    @Test
    public void shouldReturnEmptyStreamIfNoCookiesPresent() {
        final HttpServletRequest request = mock(HttpServletRequest.class);

        final Set<CredentialFlowState> streamFromNullCookies = CredentialFlowStateHelper.restoreFrom((c, cls) -> null,
            request);

        assertThat(streamFromNullCookies).isEmpty();

        when(request.getCookies()).thenReturn(new Cookie[0]);
        final Set<CredentialFlowState> streamFromEmptyCookies = CredentialFlowStateHelper.restoreFrom((c, cls) -> null,
            request);

        assertThat(streamFromEmptyCookies).isEmpty();
    }
}
