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
package io.syndesis.server.runtime.swagger;

import java.util.Map;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ResourceIdentifier;
import io.syndesis.common.util.json.JsonUtils;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceIdentifierTest {

    final JsonNode node = JsonUtils.convertValue(new ResourceIdentifier.Builder()
        .kind(Kind.Icon)
        .build(), JsonNode.class);

    @Test
    public void generateOpenAPISchemaShouldContainTheKindProperty() {
        final ModelConverters converters = ModelConverters.getInstance();
        final ResolvedSchema resolvedSchema = converters.resolveAsResolvedSchema(new AnnotatedType(ResourceIdentifier.class));

        @SuppressWarnings("unchecked")
        final Map<String, ?> properties = resolvedSchema.schema.getProperties();
        assertThat(properties).containsKey("kind");
    }

    @Test
    public void serializedJSONShouldContainTheKindProperty() {
        assertThat(node.has("kind")).isTrue();
    }

    @Test
    public void serializedJSONShouldNotContainTheWithIdProperty() {
        assertThat(node.has("withId")).isFalse();
    }
}
