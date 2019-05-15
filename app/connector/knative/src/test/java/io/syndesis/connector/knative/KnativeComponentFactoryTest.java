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
package io.syndesis.connector.knative;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import static io.syndesis.connector.knative.KnativeComponentProxyFactory.computeKnativeUri;
import static org.assertj.core.api.Assertions.assertThat;

public class KnativeComponentFactoryTest {

    @Test
    public void testComputeKnativeUri() throws Exception {
        assertThat(computeKnativeUri("knative",
            mapOf("type", "channel", "name", "test")))
            .isEqualTo("knative://channel/test");

        assertThat(computeKnativeUri("knative",
            mapOf("type", "channel", "name", "test", "unknown", "false")))
            .isEqualTo("knative://channel/test?unknown=false");

        assertThat(computeKnativeUri("knative",
            mapOf("type", "endpoint", "name", "test2")))
            .isEqualTo("knative://endpoint/test2");

    }

    // *************************
    // Helpers
    // *************************

    private static Map<String, String> mapOf(String key, String value, String... values) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(key, value);

        for (int i = 0; i < values.length; i += 2) {
            map.put(
                values[i],
                values[i + 1]
            );
        }

        return map;
    }

}
