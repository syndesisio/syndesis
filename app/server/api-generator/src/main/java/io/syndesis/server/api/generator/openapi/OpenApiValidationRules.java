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
package io.syndesis.server.api.generator.openapi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.openapi.v2.Oas20ValidationRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains Syndesis custom validation rules for Open API specifications.
 */
public final class OpenApiValidationRules implements Function<OpenApiModelInfo, OpenApiModelInfo> {

    private static final Logger LOG = LoggerFactory.getLogger(OpenApiValidationRules.class);

    private final List<Function<OpenApiModelInfo, OpenApiModelInfo>> v2Rules = new ArrayList<>();
    private final List<Function<OpenApiModelInfo, OpenApiModelInfo>> v3Rules = new ArrayList<>();

    private OpenApiValidationRules(final APIValidationContext context) {
        switch (context) {
        case CONSUMED_API:
            v2Rules.add(Oas20ValidationRules::validateResponses);
            v2Rules.add(Oas20ValidationRules::validateConsumedAuthTypes);
            v2Rules.add(Oas20ValidationRules::validateScheme);
            v2Rules.add(Oas20ValidationRules::validateUniqueOperationIds);
            v2Rules.add(Oas20ValidationRules::validateCyclicReferences);
            v2Rules.add(Oas20ValidationRules::validateOperationsGiven);
            return;
        case PROVIDED_API:
            v2Rules.add(Oas20ValidationRules::validateResponses);
            v2Rules.add(Oas20ValidationRules::validateProvidedAuthTypes);
            v2Rules.add(Oas20ValidationRules::validateUniqueOperationIds);
            v2Rules.add(Oas20ValidationRules::validateNoMissingOperationIds);
            v2Rules.add(Oas20ValidationRules::validateCyclicReferences);
            v2Rules.add(Oas20ValidationRules::validateOperationsGiven);
            return;
        case NONE:
            return;
        default:
            throw new IllegalArgumentException("Unsupported validation context " + context);
        }
    }

    @Override
    public OpenApiModelInfo apply(final OpenApiModelInfo modelInfo) {
        switch (modelInfo.getApiVersion()) {
            case V2:
                return v2Rules.stream().reduce(Function::compose).map(f -> f.apply(modelInfo)).orElse(modelInfo);
            case V3:
                return v3Rules.stream().reduce(Function::compose).map(f -> f.apply(modelInfo)).orElse(modelInfo);
            default:
                LOG.warn(String.format("Unable to apply custom validation rules on OpenAPI document type '%s'", modelInfo.getModel().getClass()));
                break;
        }

        return modelInfo;
    }

    public static OpenApiValidationRules get(final APIValidationContext context) {
        return new OpenApiValidationRules(context);
    }
}
