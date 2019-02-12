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
package io.syndesis.common.util.immutable;

import java.util.Set;

import org.immutables.value.Value;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SkipNullsTest {

    @Value.Immutable
    @Value.Style(jdkOnly = true)
    public interface TestValue {
        @SkipNulls
        Set<String> noNulls();
    }

    @Test
    public void shouldNotHoldNullValues() {
        final TestValue testValue = ImmutableTestValue.builder().addNoNulls(null, "value").build();

        assertThat(testValue.noNulls()).containsExactly("value");
    }
}
