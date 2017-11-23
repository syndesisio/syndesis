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
package io.syndesis.rest.v1.state;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.NewCookie;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.immutables.value.Value.Immutable;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientSideStateTest {

    private static final Edition RFC_EDITION = new Edition(new BigInteger("tid".getBytes(StandardCharsets.US_ASCII)).longValue(),
        "AES/CBC/PKCS5Padding", "HmacSHA1") {
        private final SecretKeySpec authenticationKey = new SecretKeySpec(
            "12345678901234567890".getBytes(StandardCharsets.US_ASCII), "HmacSHA1");

        private final SecretKeySpec encryptionKey = new SecretKeySpec(
            "0123456789abcdef".getBytes(StandardCharsets.US_ASCII), "AES");

        private final KeySource keySource = new KeySource() {
            @Override
            public SecretKey authenticationKey() {
                return authenticationKey;
            }

            @Override
            public SecretKey encryptionKey() {
                return encryptionKey;
            }
        };

        @Override
        protected KeySource keySource() {
            return keySource;
        }
    };

    private static final Supplier<byte[]> RFC_IV_SOURCE = () -> new byte[] {(byte) 0xb4, (byte) 0xbd, (byte) 0xe5, 0x24,
        (byte) 0xf7, (byte) 0xf6, (byte) 0x9d, 0x44, (byte) 0x85, 0x30, (byte) 0xde, (byte) 0x9d, (byte) 0xb5, 0x55,
        (byte) 0xc9, 0x4f};

    private static final LongSupplier RFC_TIME = () -> 1347265955L;

    private static final BiFunction<Class<?>, byte[], Object> SIMPLE_DESERIALIZATION = (t, v) -> new String(v, StandardCharsets.UTF_8);

    private static final Function<Object, byte[]> SIMPLE_SERIALIZATION = o -> ((String) o).getBytes(StandardCharsets.UTF_8);

    @Immutable
    @JsonDeserialize(builder = ImmutableData.Builder.class)
    public interface Data {
        int getInteger();

        String getString();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutStalePickles() {
        final ClientSideState clientSideStateOld = new ClientSideState(RFC_EDITION,
            () -> ClientSideState.currentTimestmpUtc() - ClientSideState.DEFAULT_TIMEOUT - 100,
            ClientSideState.DEFAULT_TIMEOUT);

        final ClientSideState clientSideStateCurrent = new ClientSideState(RFC_EDITION,
            () -> ClientSideState.currentTimestmpUtc(), ClientSideState.DEFAULT_TIMEOUT);

        final NewCookie persisted = clientSideStateOld.persist("key", "/path", "value");

        clientSideStateCurrent.restoreFrom(persisted, String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutTidMismatch() {
        final ClientSideState clientSideState1 = new ClientSideState(RFC_EDITION);
        final ClientSideState clientSideState2 = new ClientSideState(withCustomTid(new byte[] {2}));

        final NewCookie persisted = clientSideState1.persist("key", "/path", "value");

        clientSideState2.restoreFrom(persisted, String.class);
    }

    @Test
    public void shouldPersistAsInRfcErrata() {
        final ClientSideState clientSideState = new ClientSideState(RFC_EDITION, RFC_TIME, RFC_IV_SOURCE,
            SIMPLE_SERIALIZATION, SIMPLE_DESERIALIZATION, ClientSideState.DEFAULT_TIMEOUT);

        final NewCookie cookie = clientSideState.persist("id", "/path", "a state string");

        assertThat(cookie).isNotNull();
        assertThat(cookie.getName()).isEqualTo("id");

        assertThat(cookie.getValue())
            .isEqualTo("pzSOjcNui9-HWS_Qk1Pwpg|MTM0NzI2NTk1NQ|dGlk|tL3lJPf2nUSFMN6dtVXJTw|uea1fgC67RmOxfpNz8gMbnPWfDA");
        assertThat(cookie.getPath()).isEqualTo("/path");
        assertThat(cookie.isHttpOnly()).isFalse();
        assertThat(cookie.isSecure()).isTrue();
    }

    @Test
    public void shouldRestoreMultipleAndOrderByTimestamp() {
        final Iterator<Long> times = Arrays
            .asList(946598400L, 1293753600L, 978220800L, 946598400L, 1293753600L, 978220800L).iterator();
        final ClientSideState clientSideState = new ClientSideState(RFC_EDITION, () -> times.next(),
            ClientSideState.DEFAULT_TIMEOUT);

        final NewCookie cookie1999 = clientSideState.persist("key", "/path", "1");
        final NewCookie cookie2010 = clientSideState.persist("key", "/path", "3");
        final NewCookie cookie2000 = clientSideState.persist("key", "/path", "2");

        final Set<String> restored = clientSideState.restoreFrom(Arrays.asList(cookie1999, cookie2010, cookie2000),
            String.class);

        assertThat(restored).containsExactly("3", "2", "1");
    }

    @Test
    public void shouldRoundtripMaps() {
        final ClientSideState clientSideState = new ClientSideState(RFC_EDITION);

        final Map<String, String> data = new HashMap<>();
        data.put("k1", "v1");
        data.put("k2", "v2");

        final NewCookie cookie = clientSideState.persist("key", "/path", data);

        @SuppressWarnings("unchecked")
        final Map<String, String> value = clientSideState.restoreFrom(cookie, Map.class);
        assertThat(value).isEqualTo(data);
    }

    @Test
    public void shouldRoundtripPojos() {
        final ClientSideState clientSideState = new ClientSideState(RFC_EDITION);

        final Data data = ImmutableData.builder().string("string").integer(14).build();

        final NewCookie cookie = clientSideState.persist("key", "/path", data);

        final Data value = clientSideState.restoreFrom(cookie, Data.class);
        assertThat(value).isEqualTo(data);
    }

    private static Edition withCustomTid(final byte[] tid) {
        return new Edition(new BigInteger(tid).longValue(), "AES/CBC/PKCS5Padding", "HmacSHA1") {
            @Override
            protected KeySource keySource() {
                return RFC_EDITION.keySource();
            }
        };
    }
}
