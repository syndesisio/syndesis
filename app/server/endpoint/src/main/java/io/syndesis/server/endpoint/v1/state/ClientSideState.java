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
package io.syndesis.server.endpoint.v1.state;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

import io.syndesis.server.credential.CredentialModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Persists given state on the client with these properties:
 * <ul>
 * <li>State remains opaque (encrypted) so client cannot determine what is
 * stored
 * <li>State tampering is detected by using MAC
 * <li>State timeout is enforced (default 30min)
 * </ul>
 * <p>
 * Given a {@link KeySource} construct {@link ClientSideState} as:
 * {@code new ClientSideState(keySource)}, and then persist state into HTTP
 * Cookie with {@link #persist(String, String, Object)} method, and restore the
 * state with {@link #restoreFrom(Cookie, Class)} method.
 * <p>
 * The implementation follows the
 * <a href="https://tools.ietf.org/html/rfc6896">RFC6896</a> Secure Cookie
 * Sessions for HTTP.
 */
public final class ClientSideState {
    // 15 min
    public static final int DEFAULT_TIMEOUT = 30 * 60;

    private static final Decoder DECODER = Base64.getUrlDecoder();

    private static final Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private static final int IV_LEN = 16;

    private static final Logger LOG = LoggerFactory.getLogger(ClientSideState.class);

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new CredentialModule());

    private final BiFunction<Class<?>, byte[], Object> deserialization;

    private final Edition edition;

    private final Supplier<byte[]> ivSource;

    private final Function<Object, byte[]> serialization;

    private final int timeout;

    private final LongSupplier timeSource;

    static class TimestampedState<T> implements Comparable<TimestampedState<T>> {

        private final T state;

        private final long timestamp;

        TimestampedState(final T state, final long timestamp) {
            this.state = Objects.requireNonNull(state, "state");
            this.timestamp = timestamp;
        }

        @Override
        public int compareTo(final TimestampedState<T> other) {
            return Long.compare(other.timestamp, timestamp);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }

            if (!(obj instanceof TimestampedState)) {
                return false;
            }

            @SuppressWarnings("unchecked")
            final TimestampedState<T> other = (TimestampedState<T>) obj;

            return timestamp == other.timestamp && Objects.equals(state, other.state);
        }

        @Override
        public int hashCode() {
            return (int) (31 * timestamp + 31 * Objects.hashCode(state));
        }
    }

    protected static final class RandomIvSource implements Supplier<byte[]> {
        private static final SecureRandom RANDOM = new SecureRandom();

        @Override
        public byte[] get() {
            final byte[] iv = new byte[IV_LEN];
            RANDOM.nextBytes(iv);

            return iv;
        }
    }

    public ClientSideState(final Edition edition) {
        this(edition, ClientSideState::currentTimestmpUtc, new RandomIvSource(), ClientSideState::serialize,
            ClientSideState::deserialize, DEFAULT_TIMEOUT);
    }

    public ClientSideState(final Edition edition, final int timeout) {
        this(edition, ClientSideState::currentTimestmpUtc, new RandomIvSource(), ClientSideState::serialize, ClientSideState::deserialize,
            timeout);
    }

    ClientSideState(final Edition edition, final LongSupplier timeSource, final int timeout) {
        this(edition, timeSource, new RandomIvSource(), ClientSideState::serialize, ClientSideState::deserialize, timeout);
    }

    ClientSideState(final Edition edition, final LongSupplier timeSource, final Supplier<byte[]> ivSource,
        final Function<Object, byte[]> serialization, final BiFunction<Class<?>, byte[], Object> deserialization, final int timeout) {
        this.edition = edition;
        this.timeSource = timeSource;
        this.ivSource = ivSource;
        this.serialization = serialization;
        this.deserialization = deserialization;
        this.timeout = timeout;
    }

    public NewCookie persist(final String key, final String path, final Object value) {
        final Date expiry = Date.from(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(timeout).toInstant());
        return new NewCookie(key, protect(value), path, null, Cookie.DEFAULT_VERSION, null, timeout, expiry, true, false);
    }

    public <T> Set<T> restoreFrom(final Collection<Cookie> cookies, final Class<T> type) {
        return cookies.stream().flatMap(c -> {
            try {
                return Stream.of(restoreWithTimestamp(c, type));
            } catch (final IllegalArgumentException e) {
                LOG.warn("Unable to restore client side state: {}", e.getMessage());
                LOG.debug("Unable to restore client side state from cookie: {}", c, e);

                return Stream.empty();
            }
        }).sorted().map(t -> t.state).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public <T> T restoreFrom(final Cookie cookie, final Class<T> type) {
        return restoreWithTimestamp(cookie, type).state;
    }

    byte[] atime() {
        final long nowInSec = timeSource.getAsLong();
        final String nowAsStr = Long.toString(nowInSec);

        return nowAsStr.getBytes(StandardCharsets.US_ASCII);
    }

    byte[] iv() {
        return ivSource.get();
    }

    String protect(final Object value) {
        final byte[] clear = serialization.apply(value);

        final byte[] iv = iv();

        final KeySource keySource = edition.keySource();
        final SecretKey encryptionKey = keySource.encryptionKey();
        final byte[] cipher = encrypt(edition.encryptionAlgorithm, iv, clear, encryptionKey);

        final byte[] atime = atime();

        final StringBuilder base = new StringBuilder().append(ENCODER.encodeToString(cipher)).append('|')

            .append(ENCODER.encodeToString(atime)).append('|')

            .append(ENCODER.encodeToString(edition.tid)).append('|')

            .append(ENCODER.encodeToString(iv));

        final byte[] mac = mac(edition.authenticationAlgorithm, base, keySource.authenticationKey());

        base.append('|').append(ENCODER.encodeToString(mac));

        return base.toString();
    }

    <T> TimestampedState<T> restoreWithTimestamp(final Cookie cookie, final Class<T> type) {
        final String value = cookie.getValue();

        final String[] parts = value.split("\\|", 5);

        final byte[] atime = DECODER.decode(parts[1]);

        final long atimeLong = atime(atime);

        if (atimeLong + timeout < timeSource.getAsLong()) {
            throw new IllegalArgumentException("Given value has timed out at: " + Instant.ofEpochSecond(atimeLong));
        }

        final byte[] tid = DECODER.decode(parts[2]);
        if (!MessageDigest.isEqual(tid, edition.tid)) {
            throw new IllegalArgumentException(String.format("Given TID `%s`, mismatches current TID `%s`",
                new BigInteger(tid).toString(16), new BigInteger(edition.tid).toString(16)));
        }

        final KeySource keySource = edition.keySource();
        final int lastSeparatorIdx = value.lastIndexOf('|');
        final byte[] mac = DECODER.decode(parts[4]);
        final byte[] calculated = mac(edition.authenticationAlgorithm, value.substring(0, lastSeparatorIdx),
            keySource.authenticationKey());
        if (!MessageDigest.isEqual(mac, calculated)) {
            throw new IllegalArgumentException("Cookie value fails authenticity check");
        }

        final byte[] iv = DECODER.decode(parts[3]);
        final byte[] encrypted = DECODER.decode(parts[0]);
        final byte[] clear = decrypt(edition.encryptionAlgorithm, iv, encrypted, keySource.encryptionKey());

        @SuppressWarnings("unchecked")
        final T ret = (T) deserialization.apply(type, clear);

        return new TimestampedState<>(ret, atimeLong);
    }

    static long atime(final byte[] atime) {
        final String timeAsStr = new String(atime, StandardCharsets.US_ASCII);

        return Long.parseLong(timeAsStr);
    }

    static long currentTimestmpUtc() {
        return Instant.now().toEpochMilli() / 1000;
    }

    static byte[] decrypt(final String encryptionAlgorithm, final byte[] iv, final byte[] encrypted,
        final SecretKey encryptionKey) {
        try {
            final Cipher cipher = Cipher.getInstance(encryptionAlgorithm);

            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new IvParameterSpec(iv));

            return cipher.doFinal(encrypted);
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException("Unable to encrypt the given value", e);
        }
    }

    static Object deserialize(final Class<?> type, final byte[] pickle) {
        final ObjectReader reader = MAPPER.readerFor(type);

        try {
            return reader.readValue(pickle);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Unable to deserialize given pickle to value", e);
        }
    }

    static byte[] encrypt(final String encryptionAlgorithm, final byte[] iv, final byte[] clear,
        final SecretKey encryptionKey) {
        try {
            final Cipher cipher = Cipher.getInstance(encryptionAlgorithm);

            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new IvParameterSpec(iv));

            return cipher.doFinal(clear);
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException("Unable to encrypt the given value", e);
        }
    }

    static byte[] mac(final String authenticationAlgorithm, final CharSequence base,
        final SecretKey authenticationKey) {
        try {
            final String baseString = base.toString();

            final Mac mac = Mac.getInstance(authenticationAlgorithm);
            mac.init(authenticationKey);

            // base contains only BASE64 characters and '|', so we use ASCII
            final byte[] raw = baseString.getBytes(StandardCharsets.US_ASCII);

            return mac.doFinal(raw);
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException("Unable to compute MAC of the given value", e);
        }
    }

    static byte[] serialize(final Object value) {
        final ObjectWriter writer = MAPPER.writerFor(value.getClass());

        try {
            return writer.writeValueAsBytes(value);
        } catch (final JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize given value: " + value, e);
        }
    }

}
