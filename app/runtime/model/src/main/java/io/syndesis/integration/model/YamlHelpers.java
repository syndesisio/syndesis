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
package io.syndesis.integration.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.syndesis.integration.model.steps.Step;

public final class YamlHelpers {
    private YamlHelpers() {
    }

    public static ObjectMapper createObjectMapper() {
        final YAMLFactory yamlFactory = new YAMLFactory()
            .configure(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID, false)
            .configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
            .configure(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS, true)
            .configure(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID, false);

        ObjectMapper mapper = new ObjectMapper(yamlFactory)
            .registerModule(new Jdk8Module())
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_NULL_MAP_VALUES);

        for (Step step : ServiceLoader.load(Step.class, YamlHelpers.class.getClassLoader())) {
            mapper.registerSubtypes(new NamedType(step.getClass(), step.getKind()));
        }

        return mapper;
    }

    public static SyndesisModel load(InputStream source) throws IOException {
        return YamlHelpers.createObjectMapper().readValue(source, SyndesisModel.class);
    }
}
