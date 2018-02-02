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
package io.syndesis.verifier.api.support;

import java.util.HashMap;
import java.util.Map;

import io.syndesis.verifier.api.SyndesisMetadata;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

public class SyndesisMetadataConverter implements Converter<SyndesisMetadata<?>, Map<String, Object>> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Map<String, Object> convert(final SyndesisMetadata<?> value) {
        if (value == null || value.isNull()) {
            return null;
        }

        final Map<String, Object> converted = new HashMap<>();

        converted.put("properties", value.properties);
        converted.put("inputSchema", MAPPER.valueToTree(value.inputSchema));
        converted.put("outputSchema", MAPPER.valueToTree(value.outputSchema));

        return converted;
    }

    @Override
    public JavaType getInputType(final TypeFactory typeFactory) {
        return typeFactory.constructMapLikeType(HashMap.class, String.class, Object.class);
    }

    @Override
    public JavaType getOutputType(final TypeFactory typeFactory) {
        return typeFactory.constructMapLikeType(HashMap.class, String.class, Object.class);
    }

}
