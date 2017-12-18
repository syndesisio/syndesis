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
package io.syndesis.verifier.v1;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import io.syndesis.verifier.api.MetadataAdapter;
import io.syndesis.verifier.api.PropertyPair;
import io.syndesis.verifier.api.SyndesisMetadata;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;

import static org.assertj.core.api.Assertions.assertThat;

public class PetstoreAdapter implements MetadataAdapter<ObjectSchema> {

    private final Map<String, List<PropertyPair>> adaptedProperties;

    private final Map<String, String> expectedPayload;

    private final ObjectSchema inputSchema;

    private final ObjectSchema outputSchema;

    public PetstoreAdapter(final Map<String, String> expectedPayload,
        final Map<String, List<PropertyPair>> adaptedProperties, final ObjectSchema inputSchema,
        final ObjectSchema outputSchema) {
        this.adaptedProperties = adaptedProperties;
        this.expectedPayload = expectedPayload;
        this.inputSchema = inputSchema;
        this.outputSchema = outputSchema;
    }

    @Override
    public SyndesisMetadata<ObjectSchema> adapt(final String actionId, final Map<String, Object> properties, final MetaData metadata) {
        @SuppressWarnings("unchecked")
        final Map<String, String> payload = metadata.getPayload(Map.class);

        assertThat(payload).isSameAs(expectedPayload);

        return new SyndesisMetadata<>(adaptedProperties, inputSchema, outputSchema);
    }

}
