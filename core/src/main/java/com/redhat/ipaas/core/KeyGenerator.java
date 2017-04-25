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
package com.redhat.ipaas.core;

import org.keycloak.common.util.Base64;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Generates lexically sortable unique keys based on:
 *
 * https://firebase.googleblog.com/2015/02/the-2120-ways-to-ensure-unique_68.html
 */
public class KeyGenerator {

    static private long lastTimestamp = System.currentTimeMillis();
    static private final byte randomnessByte;
    static private long randomnessLong;

    private KeyGenerator() {}

    static {
        Random random = new Random();
        randomnessByte = (byte) random.nextInt();
        randomnessLong = random.nextLong();
    }

    static public String createKey() {
        long now = System.currentTimeMillis();

        ByteBuffer buffer = ByteBuffer.wrap(new byte[8 + 1 + 8]);
        buffer.putLong(now);
        buffer.put(randomnessByte);
        buffer.putLong(getRandomPart(now));

        try {
            return Base64.encodeBytes(buffer.array(), 2, 15, Base64.ORDERED);
        } catch (IOException e) {
            throw new IPaasServerException(e);
        }
    }

    protected synchronized static long getRandomPart(long timeStamp) {
        if( timeStamp == lastTimestamp ) {
            // increment the randomness.
            randomnessLong ++;
        } else {
            lastTimestamp = timeStamp;
        }
        return randomnessLong;
    }
}
