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

import org.junit.Test;

import static org.junit.Assert.*;

public class NamesTest {

    @Test
    public void testGetSanitizedName() throws Exception {
        String data[] = {
            "bla", "bla",

            "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789",
            "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",

            "how-are-you", "how are you?",

            "yet-sth--with--spaces", "yet sth  with !#ä spaceS",

            "aaa-big-and-small-zzz", "AaA BIG and Small ZzZ",
        };

        for (int i = 0; i < data.length; i +=2) {
            assertEquals(data[i], Names.sanitize(data[i+1]));
        }
    }
}
