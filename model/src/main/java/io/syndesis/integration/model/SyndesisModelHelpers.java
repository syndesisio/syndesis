/*
 * Copyright 2016 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package io.syndesis.integration.model;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.syndesis.integration.model.steps.Step;

public final class SyndesisModelHelpers {
    private SyndesisModelHelpers() {
    }

    public static ObjectMapper createObjectMapper() {
        final YAMLFactory yamlFactory = new YAMLFactory()
            .configure(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID, false)
            .configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
            .configure(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS, true)
            .configure(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID, false);

        ObjectMapper mapper = new ObjectMapper(yamlFactory)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)
            .disable(SerializationFeature.WRITE_NULL_MAP_VALUES);

        return mapper;
    }

    public static List<Step> loadDefaultSteps() {
        List<Step> steps = new ArrayList<>();
        ServiceLoader.load(Step.class, SyndesisModelBuilder.class.getClassLoader()).forEach(steps::add);

        return steps;
    }

    public static ObjectMapper registerDefaultSteps(ObjectMapper mapper) {
        for (Step step : loadDefaultSteps()) {
            mapper.registerSubtypes(new NamedType(step.getClass(), step.getKind()));
        }

        return mapper;
    }
}
