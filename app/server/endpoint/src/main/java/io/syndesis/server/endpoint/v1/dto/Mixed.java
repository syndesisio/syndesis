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
package io.syndesis.server.endpoint.v1.dto;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.syndesis.server.endpoint.v1.dto.Mixed.MixedSerializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.JsonGeneratorDelegate;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = MixedSerializer.class)
abstract class Mixed {

    private final List<Object> parts;

    public static final class MixedSerializer extends JsonSerializer<Mixed> {

        private static final class EnclosedJsonGenerator extends JsonGeneratorDelegate {

            private int level;

            public EnclosedJsonGenerator(final JsonGenerator d) {
                super(d);
            }

            @Override
            public void writeEndObject() throws IOException {
                if (level > 1) {
                    super.writeEndObject();
                }
                level--;
            }

            @Override
            public void writeStartObject() throws IOException {
                if (level != 0) {
                    super.writeStartObject();
                }
                level++;
            }

            @Override
            public void writeStartObject(final Object forValue) throws IOException {
                if (level != 0) {
                    super.writeStartObject();
                }
                level++;
            }
        }

        @Override
        public void serialize(final Mixed mixed, final JsonGenerator gen, final SerializerProvider serializers)
            throws IOException, JsonProcessingException {
            gen.writeStartObject();

            for (final Object part : mixed.parts) {
                serializers.defaultSerializeValue(part, new EnclosedJsonGenerator(gen));
            }

            gen.writeEndObject();
        }

    }

    public Mixed(final Object... parts) {
        this.parts = Arrays.asList(parts);
    }

    public List<Object> getParts() {
        return parts;
    }

}
