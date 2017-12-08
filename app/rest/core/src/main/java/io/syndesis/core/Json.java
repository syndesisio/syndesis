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
package io.syndesis.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import io.syndesis.core.json.SyndesisModule;

/**
 * JSON helper class.
 */
public final class Json {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModules(new Jdk8Module(), new SyndesisModule())
        .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
        .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);

    private Json() {
    }

    public static ObjectMapper mapper() {
        return OBJECT_MAPPER;
    }
}
