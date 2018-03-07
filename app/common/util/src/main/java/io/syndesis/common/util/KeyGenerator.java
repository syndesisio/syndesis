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
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.currentTimeMillis;

/**
 * Generates lexically sortable unique keys based on:
 *
 * https://firebase.googleblog.com/2015/02/the-2120-ways-to-ensure-unique_68.html
 *
 * You can also consider the generated kys to be like UUIDS except:
 * (1) strictly increment from the generating node's point of view
 * (2) loosely increment based on relative machine time when viewed across nodes.
 */
public final class KeyGenerator {

    private static final AtomicLong LAST_TIMESTAMP = new AtomicLong(currentTimeMillis());
    private static final byte RANDOMNESS_BYTE;
    private static final AtomicLong RANDOMNESS_LONG;

    static {
        final Random random = ThreadLocalRandom.current();
        RANDOMNESS_BYTE = (byte) random.nextInt();
        RANDOMNESS_LONG = new AtomicLong(random.nextLong());
    }

    private KeyGenerator() {
    }

    /**
     * Generates a new key.
     */
    public static String createKey() {
        final long now = currentTimeMillis();

        final ByteBuffer buffer = ByteBuffer.wrap(new byte[8 + 1 + 8]);
        buffer.putLong(now);
        buffer.put(RANDOMNESS_BYTE);

        buffer.putLong(getRandomPart(now));

        try {
            return Base64.encodeBytes(buffer.array(), 2, 15, Base64.ORDERED);
        } catch (final IOException e) {
            throw new SyndesisServerException(e);
        }
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
        byte[] decoded = Base64.decode(key, Base64.ORDERED);
        if (decoded.length != 15) {
            throw new IOException("Invalid key: size is incorrect.");
        }
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.position(2);
        buffer.put(decoded, 0 ,6);
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

        try {
            return Base64.encodeBytes(buffer.array(), 2, 15, Base64.ORDERED);
        } catch (final IOException e) {
            throw new SyndesisServerException(e);
        }
    }

}
