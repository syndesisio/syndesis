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
package io.syndesis.server.jsondb.impl;

import static io.syndesis.server.jsondb.impl.JsonRecordSupport.toLexSortableString;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;

import org.junit.Test;


/**
 * Usd to unit test JsonRecordSupport
 */
public class JsonRecordSupportTest {

    @Test
    public void testDataTypes() {

        // see: http://www.zanopha.com/docs/elen.pdf

        int intitialValue = 1234567890;
        String r1 = toLexSortableString(intitialValue);
        assertThat(r1).isEqualTo("[[[2101234567890");

        int r2 = JsonRecordSupport.fromLexSortableStringToInt(r1);
        assertThat(r2).isEqualTo(intitialValue);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSmallValues() {
        String last=null;
        for (int i = -1000; i < 1000; i++) {
            String next = toLexSortableString(i);
            if( last != null ) {
                assertThat((Comparable)last).isLessThan(next);
            }
            last = next;
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFloatsValues() {

        assertThat(toLexSortableString("1.01")).isEqualTo("[101-");
        assertThat(toLexSortableString("-100.5")).isEqualTo("--68994[");

        String last=null;
        for (int i = 0; i < 9000; i++) {
            String value = rtrim(String.format("23.%04d",i),"0");
            String next = toLexSortableString(value);
            if( last != null ) {
                assertThat((Comparable)last).isLessThan(next);
            }
            last = next;
        }
    }

    private String rtrim(String value, String suffix) {
        return value.replaceAll("("+Pattern.quote(suffix)+")+$", "");
    }
}
