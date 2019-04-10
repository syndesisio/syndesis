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
package io.syndesis.common.model.connection;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;

import io.syndesis.common.model.Ordered;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderedConfigurationPropertiesTest {
    @Test
    public void testOrder() {
        List<ConfigurationProperty> properties = Arrays.asList(
            new ConfigurationProperty.Builder()
                .order(Ordered.LOWEST)
                .label("LOWEST")
                .build(),
            new ConfigurationProperty.Builder()
                .order(Ordered.HIGHEST)
                .label("HIGHEST")
                .build(),
            new ConfigurationProperty.Builder()
                .label("DEFAULT")
                .build()
        );

        properties.sort(Comparator.comparingInt(o -> o.getOrder().orElse(Ordered.DEFAULT)));

        assertThat(properties.get(0)).hasFieldOrPropertyWithValue("order", OptionalInt.of(Ordered.HIGHEST));
        assertThat(properties.get(0)).hasFieldOrPropertyWithValue("label", "HIGHEST");
        assertThat(properties.get(1)).hasFieldOrPropertyWithValue("order", OptionalInt.empty());
        assertThat(properties.get(1)).hasFieldOrPropertyWithValue("label", "DEFAULT");
        assertThat(properties.get(2)).hasFieldOrPropertyWithValue("order", OptionalInt.of(Ordered.LOWEST));
        assertThat(properties.get(2)).hasFieldOrPropertyWithValue("label", "LOWEST");
    }
}
