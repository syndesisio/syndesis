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

package io.syndesis.server.api.generator.openapi.v3;

import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.server.api.generator.ProvidedApiTemplate;
import io.syndesis.server.api.generator.openapi.OpenApiFlowGenerator;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;

/**
 * @author Christoph Deppisch
 */
public class Oas30FlowGenerator implements OpenApiFlowGenerator<Oas30Document> {

    @Override
    public void generateFlows(Oas30Document openApiDoc, Integration.Builder integration,
                              OpenApiModelInfo info, ProvidedApiTemplate template) {

    }
}
