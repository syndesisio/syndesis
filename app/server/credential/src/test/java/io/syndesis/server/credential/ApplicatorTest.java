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
package io.syndesis.server.credential;

import io.syndesis.common.model.connection.Connection;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicatorTest {

    @Test
    public void shouldApplyPropertyValues() {
        final Connection.Builder connection = new Connection.Builder();

        Applicator.applyProperty(connection, "property", "value");

        final Connection built = connection.build();

        assertThat(built.getConfiguredProperties()).containsEntry("property", "value");
    }

    @Test
    public void shouldNotApplyNullOrEmptyPropertyValues() {
        final Connection.Builder connection = new Connection.Builder();

        Applicator.applyProperty(connection, "emptyProperty", "");

        Applicator.applyProperty(connection, "nullProperty", "");

        final Connection built = connection.build();

        assertThat(built.getConfiguredProperties()).isEmpty();
    }
}
