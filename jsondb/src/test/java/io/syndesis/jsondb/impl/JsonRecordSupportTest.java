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
package io.syndesis.jsondb.impl;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Usd to unit test JsonRecordSupport
 */
public class JsonRecordSupportTest {

    @Test
    public void testDataTypes() {

        // see: http://www.zanopha.com/docs/elen.pdf

        int intitialValue = 1234567890;
        String r1 = JsonRecordSupport.toLexSortableString(intitialValue, '+');
        assertThat(r1).isEqualTo("+++2101234567890");

        int r2 = JsonRecordSupport.fromLexSortableStringToInt(r1, '+');
        assertThat(r2).isEqualTo(intitialValue);
    }

    @Test
    public void testSmallValues() {
        for (int i = 0; i < 10; i++) {
            String r1 = JsonRecordSupport.toLexSortableString(i, '+');
            int r2 = JsonRecordSupport.fromLexSortableStringToInt(r1, '+');
            assertThat(r2).isEqualTo(i);
        }
    }
}
