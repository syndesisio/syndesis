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
package io.syndesis.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.currentTimeMillis;

import org.keycloak.common.util.Base64;

/**
 * Generates lexically sortable unique keys based on:
 *
 * https://firebase.googleblog.com/2015/02/the-2120-ways-to-ensure-unique_68.html
 *
 * You can also consider the generated kys to be like UUIDS except:
 * (1) strictly increment from the generating node's point of view
 * (2) loosely increment based on relative machine time when viewed across nodes.
 */
public class KeyGenerator {

    private static AtomicLong lastTimestamp = new AtomicLong(currentTimeMillis());
    private static final byte randomnessByte;
    private static AtomicLong randomnessLong;

    static {
        final Random random = ThreadLocalRandom.current();
        randomnessByte = (byte) random.nextInt();
        randomnessLong = new AtomicLong(random.nextLong());
    }

    private KeyGenerator() {}

    public static String createKey() {
        final long now = currentTimeMillis();

        final ByteBuffer buffer = ByteBuffer.wrap(new byte[8 + 1 + 8]);
        buffer.putLong(now);
        buffer.put(randomnessByte);

        buffer.putLong(getRandomPart(now));

        try {
            return Base64.encodeBytes(buffer.array(), 2, 15, Base64.ORDERED);
        } catch (final IOException e) {
            throw new SyndesisServerException(e);
        }
    }

    protected static long getRandomPart(final long timeStamp) {
        return randomnessLong.updateAndGet(randomVal -> {
            long current;
            do {
                current = lastTimestamp.get();
                randomVal++;
            } while (!lastTimestamp.compareAndSet(current, timeStamp));

            return randomVal;
        });
    }
}
