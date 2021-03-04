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

package io.syndesis.integration.component.proxy;

import java.io.IOException;

import org.apache.camel.catalog.DefaultCamelCatalog;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ComponentDefinitionTest {

    @Test
    public void testForScheme() throws IOException {
        ComponentDefinition definition = ComponentDefinition.forScheme(new DefaultCamelCatalog(), "direct");
        assertThat(definition).isNotNull();
        assertThat(definition.getComponent().getSyntax()).isEqualTo("direct:name");
    }

    @Test
    public void testForSchemeNotFound() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> ComponentDefinition.forScheme(new DefaultCamelCatalog(), "unknown"))
            .withMessage(
                "Failed to find component definition for scheme 'unknown'. Missing component definition in classpath 'org/apache/camel/catalog/components/unknown.json'");
    }
}