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

import java.util.Arrays;

import io.syndesis.common.util.Names;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class NamesTest {

    @Parameter(0)
    public String projectName;

    @Parameter(1)
    public String integrationName;

    @Test
    public void testGetSanitizedName() throws Exception {
        final String sanitized = Names.sanitize(integrationName);
        assertEquals(projectName, sanitized);
        assertTrue("Sanitized name: `" + sanitized + "` is not valid", Names.isValid(sanitized));
    }

    @Parameters
    public static Iterable<Object[]> integrationToProjectNames() {
        return Arrays.asList(pair("bla", "bla"),
            pair("012345678901234567890123456789012345678901234567890123456789012",
                "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"),
            pair("how-are-you", "how are you?"), //
            pair("yet-sth-with-spaces", "yet sth  with !#ä spaceS"),
            pair("aaa-big-and-small-zzz", "AaA BIG and Small ZzZ"),
            pair("twitter-mention-salesforce-upsert-contact", "Twitter Mention -> Salesforce upsert contact"),
            pair("twitter-mention-salesforce-upsert-contact-0",
                "??? Twitter Mention <-> Salesforce upsert contact !!!"),
            pair("s-p-a-c-e-the-final-frontier", "    s   p  a  c  e   , the final frontier"),
            pair("walking-on-sunshine-0", "_walking_on_sunshine_"),
            pair("dot-0", "dot."),
            pair("01234567890123456789012345678901234567890123456789012345678901x", "01234567890123456789012345678901234567890123456789012345678901X"),
            pair("0123456789012345678901234567890123456789012345678901234567890x", "0123456789012345678901234567890123456789012345678901234567890X"),
            pair("012345678901234567890123456789012345678901234567890123456789012", "012345678901234567890123456789012345678901234567890123456789012."),
            pair("012345678901234567890123456789012345678901234567890123456789010", "01234567890123456789012345678901234567890123456789012345678901."));
    }

    private static Object[] pair(final String projectName, final String integrationName) {
        return new Object[] {projectName, integrationName};
    }
}
