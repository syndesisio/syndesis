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

package io.syndesis.server.api.generator.openapi.v2;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Strings;
import io.apicurio.datamodels.openapi.models.OasPaths;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20PathItem;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.FlowMetadata;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.server.api.generator.ProvidedApiTemplate;
import io.syndesis.server.api.generator.openapi.OpenApiFlowGenerator;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Christoph Deppisch
 */
public class Oas20FlowGenerator implements OpenApiFlowGenerator<Oas20Document> {

    private final Oas20DataShapeGenerator dataShapeGenerator;

    public Oas20FlowGenerator() {
        dataShapeGenerator = new UnifiedDataShapeGenerator();
    }

    @Override
    public void generateFlows(Oas20Document openApiDoc, Integration.Builder integration,
                              OpenApiModelInfo info, final ProvidedApiTemplate template) {

        final Set<String> alreadyUsedOperationIds = new HashSet<>();
        final OasPaths paths = Optional.ofNullable(openApiDoc.paths)
            .orElse(openApiDoc.createPaths());

        for (final Oas20PathItem pathEntry : OasModelHelper.getPathItems(paths, Oas20PathItem.class)) {
            for (final Map.Entry<String, Oas20Operation> operationEntry : OasModelHelper.getOperationMap(pathEntry, Oas20Operation.class).entrySet()) {
                final Oas20Operation operation = operationEntry.getValue();

                String operationName = operation.summary;
                final String operationDescription = operationEntry.getKey().toUpperCase(Locale.US) + " " + pathEntry.getPath();

                final String operationId = requireUniqueOperationId(operation.operationId, alreadyUsedOperationIds);
                alreadyUsedOperationIds.add(operationId);
                operation.operationId = operationId; // Update open api spec

                final DataShape startDataShape = dataShapeGenerator.createShapeFromRequest(info.getResolvedJsonGraph(), openApiDoc, operation);
                final Action startAction = template.getStartAction().orElseThrow(() -> new IllegalStateException("cannot find start action"));
                final ConnectorAction.Builder modifiedStartActionBuilder = new ConnectorAction.Builder()
                    .createFrom(startAction)
                    .addTag("locked-action")
                    .descriptor(new ConnectorDescriptor.Builder()
                        .createFrom(startAction.getDescriptor())
                        .outputDataShape(startDataShape)
                        .build());

                final String basePath = openApiDoc.basePath;
                if (!Strings.isNullOrEmpty(basePath)) {
                    // pass the basePath so it gets picked up by
                    // EndpointController
                    modifiedStartActionBuilder.putMetadata("serverBasePath", basePath);
                }

                final Action modifiedStartAction = modifiedStartActionBuilder.build();

                final Step startStep = new Step.Builder()
                    .id(KeyGenerator.createKey())
                    .action(modifiedStartAction)
                    .connection(template.getConnection())
                    .stepKind(StepKind.endpoint)
                    .putConfiguredProperty("name", operationId)
                    .putMetadata("configured", "true")
                    .build();

                final DataShape endDataShape = dataShapeGenerator.createShapeFromResponse(info.getResolvedJsonGraph(), openApiDoc, operation);
                final Action endAction = template.getEndAction().orElseThrow(() -> new IllegalStateException("cannot find end action"));
                final Action modifiedEndAction = new ConnectorAction.Builder()
                    .createFrom(endAction)
                    .addTag("locked-action")
                    .descriptor(new ConnectorDescriptor.Builder()
                        .createFrom(endAction.getDescriptor())
                        .inputDataShape(endDataShape)
                        .replaceConfigurationProperty(ERROR_RESPONSE_CODES_PROPERTY,
                            builder -> builder.extendedProperties(extendedPropertiesMapSet(operation)))
                        .replaceConfigurationProperty(HTTP_RESPONSE_CODE_PROPERTY,
                            builder -> builder.addAllEnum(httpStatusList(operation)))
                        .build())
                    .build();
                final Step endStep = new Step.Builder()
                    .id(KeyGenerator.createKey())
                    .action(modifiedEndAction)
                    .connection(template.getConnection())
                    .stepKind(StepKind.endpoint)
                    .putConfiguredProperty(HTTP_RESPONSE_CODE_PROPERTY, getResponseCode(operation))
                    .putConfiguredProperty(ERROR_RESPONSE_BODY, "false")
                    .putConfiguredProperty(ERROR_RESPONSE_CODES_PROPERTY, "{}")
                    .putMetadata("configured", "true")
                    .build();

                if (Strings.isNullOrEmpty(operationName)) {
                    operationName = OasModelHelper.operationDescriptionOf(
                        openApiDoc,
                        operation,
                        (m, p) -> "Receiving " + m + " request on " + p).description;
                }

                String defaultCode = "200";
                final Optional<Pair<String, OasResponse>> defaultResponse = findResponseCode(operation);
                if (defaultResponse.isPresent() && NumberUtils.isDigits(defaultResponse.get().getKey())) {
                    defaultCode = defaultResponse.get().getKey();
                }

                final String flowId = KeyGenerator.createKey();

                final Flow flow = new Flow.Builder()
                    .id(flowId)
                    .type(Flow.FlowType.API_PROVIDER)
                    .putMetadata(OpenApi.OPERATION_ID, operationId)
                    .putMetadata(FlowMetadata.EXCERPT, "501 Not Implemented")
                    .putMetadata(DEFAULT_RETURN_CODE_METADATA_KEY, defaultCode)
                    .addStep(startStep)
                    .addStep(endStep)
                    .name(operationName)
                    .description(operationDescription)
                    .build();

                integration.addFlow(flow);
            }
        }
    }
}
