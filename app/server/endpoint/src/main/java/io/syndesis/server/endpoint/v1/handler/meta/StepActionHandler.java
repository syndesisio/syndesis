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
package io.syndesis.server.endpoint.v1.handler.meta;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.connection.DynamicActionMetadata;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.v1.dto.Meta;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;
import org.springframework.stereotype.Component;

@Api(value = "steps")
@Path("/steps")
@Component
public class StepActionHandler extends BaseHandler {

    private final List<StepMetadataHandler> metadataHandlers = Arrays.asList(new SplitMetadataHandler(),
                                                                             new AggregateMetadataHandler(),
                                                                             new ChoiceMetadataHandler());

    public StepActionHandler(final DataManager dataMgr) {
        super(dataMgr);
    }

    @POST
    @Path("/descriptor")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Retrieves enriched step definitions, that is a list of steps where each step has adapted input/output data shapes " +
                    "defined with respect to the given shape variants")
    @ApiResponses(@ApiResponse(code = 200, reference = "#/definitions/Step",
        responseContainer = "List",
        message = "List of enriched steps"))
    public Response enrichStepMetadata(List<Step> steps) {
        List<Step> enriched = new ArrayList<>(steps.size());

        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            final Optional<StepMetadataHandler> metadataHandler = findStepMetadataHandler(step.getStepKind());

            if (metadataHandler.isPresent()) {
                StepMetadataHandler handler = metadataHandler.get();

                List<Step> previousSteps;
                if (i == 0) {
                    previousSteps = Collections.singletonList(step);
                } else {
                    previousSteps = enriched.subList(0, i);
                }

                final DynamicActionMetadata metadata = handler.createMetadata(step, previousSteps, steps.subList(i + 1, steps.size()));
                final DynamicActionMetadata enrichedMetadata = handler.handle(metadata);
                if (enrichedMetadata.equals(DynamicActionMetadata.NOTHING)) {
                    enriched.add(step);
                } else {
                    enriched.add(applyMetadata(step, enrichedMetadata));
                }
            } else {
                enriched.add(step);
            }
        }

        return Response.status(Status.OK).entity(enriched).build();
    }

    @POST
    @Path("/{kind}/descriptor")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Retrieves enriched step definition, that is an step descriptor that has input/output data shapes " +
                    "defined with respect to the given shape variants")
    @ApiResponses(@ApiResponse(code = 200, reference = "#/definitions/StepDescriptor",
        message = "Enriched step descriptor"))
    public Response enrichStepMetadata(
        @PathParam("kind") @ApiParam(required = true) final String kind,
        DynamicActionMetadata metadata) {

        final Optional<StepMetadataHandler> metadataHandler = findStepMetadataHandler(StepKind.valueOf(kind));

        final Meta<StepDescriptor> metaResult;
        if (metadataHandler.isPresent()) {
            final DynamicActionMetadata enrichedMetadata = metadataHandler.get().handle(metadata);
            if (enrichedMetadata.equals(DynamicActionMetadata.NOTHING)) {
                metaResult = Meta.verbatim(applyMetadata(metadata));
            } else {
                metaResult = Meta.verbatim(applyMetadata(enrichedMetadata));
            }
        } else {
            metaResult = Meta.verbatim(applyMetadata(metadata));
        }

        return Response.status(Status.OK).entity(metaResult).build();
    }

    private Optional<StepMetadataHandler> findStepMetadataHandler(StepKind kind) {
        return metadataHandlers.stream()
                .filter(handler -> handler.canHandle(kind))
                .findFirst();
    }

    private static StepDescriptor applyMetadata(final DynamicActionMetadata dynamicMetadata) {
        final StepDescriptor.Builder enriched = new StepDescriptor.Builder();

        final DataShape input = dynamicMetadata.inputShape();
        if (shouldEnrichDataShape(input)) {
            enriched.inputDataShape(adaptDataShape(input));
        } else {
            enriched.inputDataShape(StepMetadataHelper.NO_SHAPE);
        }

        final DataShape output = dynamicMetadata.outputShape();
        if (shouldEnrichDataShape(output)) {
            enriched.outputDataShape(adaptDataShape(output));
        } else {
            enriched.outputDataShape(StepMetadataHelper.ANY_SHAPE);
        }

        return enriched.build();
    }

    private static Step applyMetadata(final Step original, final DynamicActionMetadata dynamicMetadata) {
        StepAction originalAction = original.getActionAs(StepAction.class)
                .orElse(new StepAction.Builder().build());

        StepDescriptor originalDescriptor;
        if (originalAction.getDescriptor() != null) {
            originalDescriptor = originalAction.getDescriptor();
        } else {
            originalDescriptor = new StepDescriptor.Builder().build();
        }

        StepDescriptor enrichedDescriptor = applyMetadata(dynamicMetadata);

        StepAction enrichedAction = new StepAction.Builder()
                .createFrom(originalAction)
                .descriptor(new StepDescriptor.Builder()
                    .createFrom(originalDescriptor)
                    .inputDataShape(enrichedDescriptor.getInputDataShape())
                    .outputDataShape(enrichedDescriptor.getOutputDataShape())
                    .build())
                .build();

        return new Step.Builder()
                .createFrom(original)
                .action(enrichedAction)
                .build();
    }

    private static DataShape adaptDataShape(final DataShape dataShape) {
        if (dataShape.getKind() != DataShapeKinds.ANY && dataShape.getKind() != DataShapeKinds.NONE
                && dataShape.getSpecification() == null && dataShape.getType() == null) {
            return StepMetadataHelper.ANY_SHAPE;
        }

        return dataShape;
    }

    private static boolean shouldEnrichDataShape(final DataShape received) {
        return received != null && received.getKind() != null;
    }
}
