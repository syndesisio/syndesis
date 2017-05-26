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

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KeyGeneratorTest {

    @Test
    public void testCreateKey() {
        // Check to make sure we don't generate dup keys
        String last = KeyGenerator.createKey();
        for (int i = 0; i < 1000000; i++) {
            String key = KeyGenerator.createKey();
            Assert.assertNotEquals(key, last);
            last = key;
        }
    }

    @Test
    public void testGetRandomPart() {
        long last = KeyGenerator.getRandomPart(0);
        for (int i = 0; i < 1000000; i++) {
            long key = KeyGenerator.getRandomPart(0);
            Assert.assertNotEquals(key, last);
        }
    }

}
