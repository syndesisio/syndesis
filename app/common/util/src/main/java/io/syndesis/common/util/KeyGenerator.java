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
package io.syndesis.common.util;

import net.iharder.Base64;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * Generates lexically sortable unique keys based on:
 * <p>
 * https://firebase.googleblog.com/2015/02/the-2120-ways-to-ensure-unique_68.html
 * <p>
 * You can also consider the generated kys to be like UUIDS except:
 * (1) strictly increment from the generating node's point of view
 * (2) loosely increment based on relative machine time when viewed across nodes.
 */
public final class KeyGenerator {

    static final LongSupplier DEFAULT_CLOCK = System::currentTimeMillis;

    static LongSupplier clock = DEFAULT_CLOCK;

    private static final AtomicLong LAST_TIMESTAMP = new AtomicLong(clock.getAsLong());
    static byte randomnessByte;
    private static final AtomicLong RANDOMNESS_LONG;

    @SuppressWarnings("boxing")
    private static final Set<Character> BASE64_ORDERED_CHARS = new TreeSet<>(Arrays.asList('-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'));

    static {
        final SecureRandom random = new SecureRandom();
        randomnessByte = (byte) random.nextInt();
        RANDOMNESS_LONG = new AtomicLong(random.nextLong());
    }

    private KeyGenerator() {
    }

    /**
     * Generates a new key.
     */
    public static String createKey() {
        final long now = clock.getAsLong();

        final ByteBuffer buffer = ByteBuffer.wrap(new byte[8 + 1 + 8]);
        buffer.putLong(now);
        buffer.put(randomnessByte);

        buffer.putLong(getRandomPart(now));

        return encodeKey(buffer.array());
    }

    static long getRandomPart(final long timeStamp) {
        return RANDOMNESS_LONG.updateAndGet(randomVal -> {
            long current;
            do {
                current = LAST_TIMESTAMP.get();
                randomVal++;
            } while (!LAST_TIMESTAMP.compareAndSet(current, timeStamp));

            return randomVal;
        });
    }

    /**
     * Used to extract the time information that is encoded to each
     * generated key.
     */
    public static long getKeyTimeMillis(String key) throws IOException {
        byte[] decoded = Base64.decode(stripPreAndSuffix(key), Base64.ORDERED);
        if (decoded.length != 15) {
            throw new IOException("Invalid key: size is incorrect.");
        }
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.position(2);
        buffer.put(decoded, 0, 6);
        buffer.flip();
        return buffer.getLong();
    }

    /**
     * Used to recreate a key using a user supplied timestamp and random data that
     * is encoded into it.
     */
    public static String recreateKey(long timestamp, int random1, long random2) {

        final ByteBuffer buffer = ByteBuffer.wrap(new byte[8 + 1 + 8]);
        buffer.putLong(timestamp);
        buffer.put((byte) random1);
        buffer.putLong(random2);

        return encodeKey(buffer.array());
    }

    private static String encodeKey(byte[] data) {
        try {
            return "i" + Base64.encodeBytes(data, 2, 15, Base64.ORDERED) + "z";
        } catch (final IOException e) {
            throw new SyndesisServerException(e);
        }
    }


    private static String stripPreAndSuffix(String key) {
        if (key.length() >= 3) {
            return key.substring(1, key.length() - 1);
        } else {
            throw new IllegalArgumentException(String.format("Can not parse key %s as identified, prefix and/or suffix is missing", key));
        }
    }

    public static boolean resemblesAKey(final String given) {
        if (given == null) {
            return false;
        }

        return given.startsWith("i") && given.endsWith("z") && areBase64Chars(given.substring(1, given.length() - 1));
    }

    private static boolean areBase64Chars(final String given) {
        for (char c : given.toCharArray()) {
            if (!BASE64_ORDERED_CHARS.contains(c)) {
                return false;
            }
        }

        return true;
    }
}
