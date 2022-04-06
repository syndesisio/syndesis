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
package io.syndesis.connector.meta;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.syndesis.common.model.DataShape;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = SyndesisMetadataDeserializer.class)
public final class SyndesisMetadataDeserializer extends JsonDeserializer<SyndesisMetadata> {

    private static final TypeReference<Map<String, List<PropertyPair>>> PROPERTIES_TYPE = new TypeReference<Map<String, List<PropertyPair>>>() {
    };

    @Override
    public SyndesisMetadata deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Map<String, List<PropertyPair>> properties = null;
        DataShape inputShape = null;
        DataShape outputShape = null;

        while (p.nextToken() != JsonToken.END_OBJECT) {
            if (p.currentToken() == JsonToken.FIELD_NAME) {
                switch (p.getCurrentName()) {
                case "properties":
                    p.nextToken();
                    properties = p.readValueAs(PROPERTIES_TYPE);
                    break;
                case "inputShape":
                    p.nextToken();
                    inputShape = p.readValueAs(DataShape.class);
                    break;
                case "outputShape":
                    p.nextToken();
                    outputShape = p.readValueAs(DataShape.class);
                    break;
                }
            }
        }

        return new SyndesisMetadata(properties, inputShape, outputShape);
    }
}