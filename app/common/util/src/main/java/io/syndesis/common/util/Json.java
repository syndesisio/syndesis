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
package io.syndesis.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

/**
 * JSON helper class.
 */
public final class Json {

    private static final ObjectMapper OBJECT_MAPPER;
    private static final ObjectWriter OBJECT_WRITER;
    private static final ObjectReader OBJECT_READER;

    static {
        OBJECT_MAPPER = new ObjectMapper()
            .registerModules(new Jdk8Module())
            .setPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY))
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
            .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
            .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        OBJECT_READER = OBJECT_MAPPER.reader();
        OBJECT_WRITER = OBJECT_MAPPER.writer();
    }

    private Json() {
    }

    /**
     * Returns an immutable and thread-safe instance of an ObjectReader, used for object deserialization.
     * @return
     */
    public static ObjectReader reader() {
        return OBJECT_READER;
    }

    public static <T> T readFromStream(final InputStream stream, final Class<T> type) throws IOException {
        return OBJECT_MAPPER.readValue(stream, type);
    }

    /**
     * Returns an immutable and thread-safe instance of an ObjectWriter, used for object serialization
     * @return
     */
    public static ObjectWriter writer() {
        return OBJECT_WRITER;
    }

    /**
     * The name of this method is super awkward to remind you that there aren't many cases for you to use it.
     * It's main usage is in tests, where you might have reason to configure differently advanced parameters.
     *
     * This method creates a copy of an ObjectMapper.
     * @return
     */
    public static ObjectMapper copyObjectMapperConfiguration() {
        return OBJECT_MAPPER.copy();
    }

    public static String toString(Object value) {
        try {
            return writer().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new SyndesisServerException(e);
        }
    }

    public static String toPrettyString(Object value) {
        try {
            return writer().withDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new SyndesisServerException(e);
        }
    }

    // Helper method to help construct json style object maps with concise syntax
    public static Map<String, Object> map(Object... values) {
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("values argument should contain an even number of entries.");
        }
        Map<String, Object> rc = new HashMap<String, Object>() {
            @Override
            public String toString() {
                try {
                    return OBJECT_MAPPER.writeValueAsString(this);
                } catch (JsonProcessingException e) {
                    throw new SyndesisServerException(e);
                }
            }
        };
        for (int i = 0; i + 1 < values.length; i += 2) {
            rc.put(values[i].toString(), values[i + 1]);
        }
        return rc;
    }

    public static <T> T convertValue(final Object fromValue, final Class<T> toValueType) {
        return OBJECT_MAPPER.convertValue(fromValue, toValueType);
    }
}
