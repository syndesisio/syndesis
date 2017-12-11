/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.generator.swagger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * This class contains Syndesis custom validation rules for swagger definitions.
 */
public class SyndesisSwaggerValidationRules implements Function<SwaggerModelInfo, SwaggerModelInfo> {

    private static final SyndesisSwaggerValidationRules INSTANCE = new SyndesisSwaggerValidationRules();

    private List<Function<SwaggerModelInfo, SwaggerModelInfo>> rules = new ArrayList<>();

    private SyndesisSwaggerValidationRules() {
        rules.add(this::validateResponses);
    }

    public static SyndesisSwaggerValidationRules getInstance() {
        return INSTANCE;
    }

    /**
     * Check if a request/response JSON schema is present
     */
    private SwaggerModelInfo validateResponses(SwaggerModelInfo swaggerModelInfo) {

        return swaggerModelInfo;
    }

    @Override
    public SwaggerModelInfo apply(SwaggerModelInfo swaggerModelInfo) {
        return rules.stream().reduce(Function::compose)
            .map(f -> f.apply(swaggerModelInfo))
            .orElse(swaggerModelInfo);
    }

}
