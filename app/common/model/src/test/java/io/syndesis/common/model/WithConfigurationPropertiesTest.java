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
package io.syndesis.common.model;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WithConfigurationPropertiesTest {

    @Test
    public void shouldDetectProxyEndpointParameters() {
        assertThat(new ConnectorAction.Builder().build().isProxyEndpointProperty("nonexistant")).isFalse();

        assertThat(new ConnectorAction.Builder()
            .descriptor(new ConnectorDescriptor.Builder().build())
            .build()
            .isProxyEndpointProperty("nonexistant")).isFalse();

        assertThat(new ConnectorAction.Builder()
            .descriptor(new ConnectorDescriptor.Builder()
                .withActionDefinitionStep("step", "description", s -> s.putProperty("key", new ConfigurationProperty.Builder().build()))
                .build())
            .build()
            .isProxyEndpointProperty("key")).isFalse();

        assertThat(new ConnectorAction.Builder()
            .descriptor(new ConnectorDescriptor.Builder()
                .withActionDefinitionStep("step", "description", s -> s.putProperty("key", new ConfigurationProperty.Builder()
                    .kind("proxyParameter")
                    .build()))
                .build())
            .build()
            .isProxyEndpointProperty("key")).isTrue();
    }
}
