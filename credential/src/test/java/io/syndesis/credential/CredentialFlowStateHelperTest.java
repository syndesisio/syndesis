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

import java.util.stream.Stream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CredentialFlowStateHelperTest {

    private static final HttpServletResponse NOT_USED = null;

    @Test
    public void ifCookieNameDoesNotMatchShouldReturnNullAndRemoveTheCookie() {
        final javax.ws.rs.core.Cookie cookie = new javax.ws.rs.core.Cookie(
            CredentialFlowState.CREDENTIAL_PREFIX + "notMatching", "anyValue");

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final CredentialFlowState got = CredentialFlowStateHelper.restoreOrDrop((c, cls) -> {
            assertThat(c.getValue()).isEqualTo("anyValue");
            return new OAuth2CredentialFlowState.Builder().key("key").build();
        }, response, cookie);

        assertThat(got).isNull();
        final ArgumentCaptor<Cookie> cookieArgument = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieArgument.capture());

        assertThat(cookieArgument.getValue())
            .isEqualToComparingFieldByField(removalOf(CredentialFlowState.CREDENTIAL_PREFIX + "notMatching"));
    }

    @Test
    public void ifRestoreFailsShouldReturnNullAndRemoveTheCookie() {
        final javax.ws.rs.core.Cookie cookie = new javax.ws.rs.core.Cookie(
            CredentialFlowState.CREDENTIAL_PREFIX + "key", "anyValue");

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final CredentialFlowState got = CredentialFlowStateHelper.restoreOrDrop((c, cls) -> {
            assertThat(c.getValue()).isEqualTo("anyValue");
            throw new IllegalArgumentException();
        }, response, cookie);

        assertThat(got).isNull();
        final ArgumentCaptor<Cookie> cookieArgument = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieArgument.capture());

        assertThat(cookieArgument.getValue())
            .isEqualToComparingFieldByField(removalOf(CredentialFlowState.CREDENTIAL_PREFIX + "key"));
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
    public void shouldRemoveCookies() {
        final HttpServletResponse response = mock(HttpServletResponse.class);
        CredentialFlowStateHelper.removeCookie(response, "myCookie");

        final Cookie expected = removalOf("myCookie");

        final ArgumentCaptor<Cookie> cookieArgument = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieArgument.capture());

        // javax.servlet.http.Cookie has no equals/hashCode
        assertThat(expected).isEqualToComparingFieldByField(cookieArgument.getValue());
    }

    @Test
    public void shouldRestoreCookiesToStreamOfState() {
        final CredentialFlowState expected1 = new OAuth2CredentialFlowState.Builder().key("key1").build();
        final CredentialFlowState expected2 = new OAuth2CredentialFlowState.Builder().key("key2").build();

        final Cookie cookie1 = new Cookie(CredentialFlowState.CREDENTIAL_PREFIX + "key1", "anyValue");
        final Cookie cookie2 = new Cookie(CredentialFlowState.CREDENTIAL_PREFIX + "key2", "anyValue");

        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getCookies()).thenReturn(new Cookie[] {cookie1, cookie2});
        final Stream<CredentialFlowState> stream = CredentialFlowStateHelper.restoreFrom((c, cls) -> {
            assertThat(c.getValue()).isEqualTo("anyValue");
            return new OAuth2CredentialFlowState.Builder()
                .key(c.getName().substring(CredentialFlowState.CREDENTIAL_PREFIX.length())).build();
        }, request, response);

        assertThat(stream).contains(expected1, expected2);
    }

    @Test
    public void shouldRestoreStateFromCookies() {
        final CredentialFlowState expected = new OAuth2CredentialFlowState.Builder().key("key").build();

        final javax.ws.rs.core.Cookie cookie = new javax.ws.rs.core.Cookie(
            CredentialFlowState.CREDENTIAL_PREFIX + "key", "anyValue");

        final CredentialFlowState got = CredentialFlowStateHelper.restoreOrDrop((c, cls) -> {
            assertThat(c.getValue()).isEqualTo("anyValue");
            return expected;
        }, NOT_USED, cookie);

        assertThat(got).isEqualTo(expected);
    }

    @Test
    public void shouldReturnEmptyStreamIfNoCookiesPresent() {
        final HttpServletRequest request = mock(HttpServletRequest.class);

        final Stream<CredentialFlowState> streamFromNullCookies = CredentialFlowStateHelper
            .restoreFrom((c, cls) -> null, request, NOT_USED);

        assertThat(streamFromNullCookies).isEmpty();

        when(request.getCookies()).thenReturn(new Cookie[0]);
        final Stream<CredentialFlowState> streamFromEmptyCookies = CredentialFlowStateHelper
            .restoreFrom((c, cls) -> null, request, NOT_USED);

        assertThat(streamFromEmptyCookies).isEmpty();
    }

    private Cookie removalOf(final String name) {
        final Cookie removal = new Cookie(name, "");
        removal.setMaxAge(0);
        removal.setHttpOnly(true);
        removal.setSecure(true);

        return removal;
    }
}
