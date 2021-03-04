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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NamesTest {

    @ParameterizedTest(name = "{1}")
    @CsvSource({
        "bla, bla",
        "012345678901234567890123456789012345678901234567890123456789012, 012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
        "how-are-you, how are you?",
        "yet-sth-with-spaces, yet sth  with !#ä spaceS",
        "aaa-big-and-small-zzz, AaA BIG and Small ZzZ",
        "twitter-mention-salesforce-upsert-contact, Twitter Mention -> Salesforce upsert contact",
        "twitter-mention-salesforce-upsert-contact-0, ??? Twitter Mention <-> Salesforce upsert contact !!!",
        "s-p-a-c-e-the-final-frontier, '    s   p  a  c  e   , the final frontier'",
        "walking-on-sunshine-0, _walking_on_sunshine_",
        "dot-0, dot.",
        "01234567890123456789012345678901234567890123456789012345678901x, 01234567890123456789012345678901234567890123456789012345678901X",
        "0123456789012345678901234567890123456789012345678901234567890x, 0123456789012345678901234567890123456789012345678901234567890X",
        "012345678901234567890123456789012345678901234567890123456789012, 012345678901234567890123456789012345678901234567890123456789012.",
        "012345678901234567890123456789012345678901234567890123456789010, 01234567890123456789012345678901234567890123456789012345678901."
    })
    public void testGetSanitizedName(final String projectName, final String integrationName) throws Exception {
        final String sanitized = Names.sanitize(integrationName);
        assertEquals(projectName, sanitized);
        assertTrue(Names.isValid(sanitized), "Sanitized name: `" + sanitized + "` is not valid");
    }
}
