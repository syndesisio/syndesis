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
package io.syndesis.server.api.generator.swagger;

import io.swagger.models.auth.BasicAuthDefinition;
import io.syndesis.common.model.connection.ConfigurationProperty;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SupportedAuthenticationTypesTest {

    @Test
    public void shouldDetermineValueFromConfiguredPropertyValue() {
        assertThat(SupportedAuthenticationTypes.fromConfiguredPropertyValue("basic")).isEqualTo(SupportedAuthenticationTypes.basic);
        assertThat(SupportedAuthenticationTypes.fromConfiguredPropertyValue("basic:name")).isEqualTo(SupportedAuthenticationTypes.basic);
        assertThat(SupportedAuthenticationTypes.fromConfiguredPropertyValue("basic:")).isEqualTo(SupportedAuthenticationTypes.basic);
    }

    @Test
    public void shouldDetermineValueFromSecurityDefinitionValue() {
        assertThat(SupportedAuthenticationTypes.fromConfiguredPropertyValue("basic")).isEqualTo(SupportedAuthenticationTypes.basic);
    }

    @Test
    public void shouldGenerateLabelsWithDescription() {
        final BasicAuthDefinition withDescription = new BasicAuthDefinition();
        withDescription.setDescription("description");

        assertThat(SupportedAuthenticationTypes.asPropertyValue("basic_auth", withDescription))
            .isEqualTo(new ConfigurationProperty.PropertyValue.Builder()
                .createFrom(SupportedAuthenticationTypes.basic.propertyValue)
                .label("HTTP Basic Authentication - basic_auth (description)")
                .value("basic:basic_auth")
                .build());
    }

    @Test
    public void shouldGenerateLabelsWithoutDescription() {
        assertThat(SupportedAuthenticationTypes.asPropertyValue("basic_auth", new BasicAuthDefinition()))
            .isEqualTo(new ConfigurationProperty.PropertyValue.Builder()
                .createFrom(SupportedAuthenticationTypes.basic.propertyValue)
                .label("HTTP Basic Authentication - basic_auth")
                .value("basic:basic_auth")
                .build());
    }
}
