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
package io.syndesis.server.metrics.prometheus;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

/**
 * Catch all for utility methods.
 *
 * @author dhirajsb
 */
public final class Utils {

    private static final ObjectReader OBJECT_READER;

    static {
        ObjectMapper objectMapper = new ObjectMapper()
            .registerModules(new Jdk8Module(), new EpochMillisTimeModule())
            .setPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY))
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
            .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
            .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        OBJECT_READER = objectMapper.reader();
    }

    private Utils() {
    }

    public static ObjectReader getObjectReader() {
        return OBJECT_READER;
    }

    public static class EpochMillisTimeModule extends SimpleModule {
        public EpochMillisTimeModule() {
            super();
            addSerializer(Date.class, new EpochMillisTimeSerializer());
            addDeserializer(Date.class, new EpochMillisTimeDeserializer());
        }

        private static class EpochMillisTimeSerializer extends StdSerializer<Date> {
            protected EpochMillisTimeSerializer() {
                super(Date.class);
            }

            @Override
            public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeNumber(value.getTime());
            }
        }

        private static class EpochMillisTimeDeserializer extends StdDeserializer<Date> {
            protected EpochMillisTimeDeserializer() {
                super(Date.class);
            }

            @Override
            public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                final String strDate = _parseString(p, ctxt);
                return new Date(Long.parseLong(strDate));
            }
        }
    }
}
