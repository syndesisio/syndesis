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

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class NamesTest {

    @RunWith(Parameterized.class)
    public static class ParameterizedNamesTest {

        @Parameter(0)
        public String projectName;

        @Parameter(1)
        public String integrationName;

        @Test
        public void testGetSanitizedName() throws Exception {
            final String sanitized = Names.sanitize(integrationName);
            assertTrue("Sanitized name: `" + sanitized + "` is not valid", Names.isValid(sanitized));
            assertThat(sanitized.length()).isGreaterThan(0);
            assertThat(sanitized.length()).isLessThan(64);
            assertThat(sanitized).matches(projectName + "-\\d\\d\\d\\d");
        }

        @Parameters
        public static Iterable<Object[]> integrationToProjectNames() {
            return Arrays.asList(pair("bla", "bla"),
                pair("0123456789012345678901234567890123456789012345678901234567",
                    "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"),
                pair("how-are-you", "how are you?"), //
                pair("yet-sth-with-spaces", "yet sth  with !#ä spaceS"),
                pair("aaa-big-and-small-zzz", "AaA BIG and Small ZzZ"),
                pair("twitter-mention-salesforce-upsert-contact", "Twitter Mention -> Salesforce upsert contact"),
                pair("twitter-mention-salesforce-upsert-contact",
                    "??? Twitter Mention <-> Salesforce upsert contact !!!"),
                pair("s-p-a-c-e-the-final-frontier", "    s   p  a  c  e   , the final frontier"),
                pair("walking-on-sunshine", "_walking_on_sunshine_"),
                pair("dot", "dot."),
                pair("0123456789012345678901234567890123456789012345678901234567", "01234567890123456789012345678901234567890123456789012345678901X"),
                pair("0123456789012345678901234567890123456789012345678901234567", "0123456789012345678901234567890123456789012345678901234567890X"),
                pair("0123456789012345678901234567890123456789012345678901234567", "012345678901234567890123456789012345678901234567890123456789012."),
                pair("0123456789012345678901234567890123456789012345678901234567", "01234567890123456789012345678901234567890123456789012345678901."));
        }

        private static Object[] pair(final String projectName, final String integrationName) {
            return new Object[]{projectName, integrationName};
        }

    }

    public static class SingleNamesTest {
        @Test
        public void testCollisionName() {
            String integration1 = "test_integration_?";
            String integration2 = "test_integration_!";
            String integration3 = "test_integration_!!";
            final String sanitized1 = Names.sanitize(integration1);
            final String sanitized2 = Names.sanitize(integration2);
            final String sanitized3 = Names.sanitize(integration3);

            assertThat(sanitized1).isNotEqualTo(sanitized2);
            assertThat(sanitized1).isNotEqualTo(sanitized3);
            assertThat(sanitized2).isNotEqualTo(sanitized3);
        }

        @Test
        public void testCollisionLengthyNames() {
            String integration1 = "0123456789012345678901234567890123456789012345678901234567-Should be cutting here!";
            String integration2 = "0123456789012345678901234567890123456789012345678901234567-Should be cutting here?";
            String integration3 = "0123456789012345678901234567890123456789012345678901234567-Should be cutting here? Really!?";
            final String sanitized1 = Names.sanitize(integration1);
            final String sanitized2 = Names.sanitize(integration2);
            final String sanitized3 = Names.sanitize(integration3);

            assertThat(sanitized1).isNotEqualTo(sanitized2);
            assertThat(sanitized1).isNotEqualTo(sanitized3);
            assertThat(sanitized2).isNotEqualTo(sanitized3);
        }

        @Test
        public void testSameName() {
            String integration1 = "test-same-name";
            String integration2 = "test-same-name";
            final String sanitized1 = Names.sanitize(integration1);
            final String sanitized2 = Names.sanitize(integration2);

            assertThat(sanitized1).isEqualTo(sanitized2);
        }
    }
}
