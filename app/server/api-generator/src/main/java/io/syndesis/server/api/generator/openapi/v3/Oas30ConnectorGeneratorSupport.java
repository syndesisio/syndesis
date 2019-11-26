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

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connector;

/**
 * @author Christoph Deppisch
 */
public final class Oas30ConnectorGeneratorSupport {

    private Oas30ConnectorGeneratorSupport() {
        // utility class.
    }

    public static ConnectorDescriptor.Builder createDescriptor(final ObjectNode json,
                                                               final Oas30Document openApiDoc, final Oas30Operation operation) {
        final ConnectorDescriptor.Builder actionDescriptor = new ConnectorDescriptor.Builder();

        //TODO: add logic for OpenAPI 3.x

        actionDescriptor.putConfiguredProperty("operationId", operation.operationId);

        return actionDescriptor;
    }

    public static void addGlobalParameters(Connector.Builder builder, Oas30Document openApiDoc) {
        //TODO: add logic for OpenAPI 3.x
    }
}
