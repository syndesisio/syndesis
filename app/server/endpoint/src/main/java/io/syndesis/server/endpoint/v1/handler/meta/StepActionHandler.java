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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.connection.DynamicActionMetadata;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.v1.dto.Meta;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;
import org.springframework.stereotype.Component;

@Api(value = "steps")
@Path("/steps")
@Component
public class StepActionHandler extends BaseHandler {

    public static final DataShape ANY_SHAPE = new DataShape.Builder().kind(DataShapeKinds.ANY).build();
    public static final DataShape NO_SHAPE = new DataShape.Builder().kind(DataShapeKinds.NONE).build();

    private final List<StepMetadataHandler> metadataHandlers = Arrays.asList(new SplitMetadataHandler(),
                                                                             new AggregateMetadataHandler());

    public StepActionHandler(final DataManager dataMgr) {
        super(dataMgr);
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

        final Optional<StepMetadataHandler> metadataHandler = findStepMetadataHandler(kind);

        final Meta<StepDescriptor> metaResult;
        if (metadataHandler.isPresent()) {
            final DynamicActionMetadata enrichedMetadata = metadataHandler.get().handle(metadata);
            metaResult = Meta.verbatim(applyMetadata(enrichedMetadata));
        } else {
            metaResult = Meta.verbatim(applyMetadata(metadata));
        }

        return Response.status(Status.OK).entity(metaResult).build();
    }

    private Optional<StepMetadataHandler> findStepMetadataHandler(String kind) {
        return metadataHandlers.stream()
                .filter(handler -> handler.canHandle(StepKind.valueOf(kind)))
                .findFirst();
    }

    private static StepDescriptor applyMetadata(final DynamicActionMetadata dynamicMetadata) {
        final StepDescriptor.Builder enriched = new StepDescriptor.Builder();

        final DataShape input = dynamicMetadata.inputShape();
        if (shouldEnrichDataShape(input)) {
            enriched.inputDataShape(adaptDataShape(input));
        } else {
            enriched.inputDataShape(NO_SHAPE);
        }

        final DataShape output = dynamicMetadata.outputShape();
        if (shouldEnrichDataShape(output)) {
            enriched.outputDataShape(adaptDataShape(output));
        } else {
            enriched.outputDataShape(NO_SHAPE);
        }

        return enriched.build();
    }

    private static DataShape adaptDataShape(final DataShape dataShape) {
        if (dataShape.getKind() != DataShapeKinds.ANY && dataShape.getKind() != DataShapeKinds.NONE
                && dataShape.getSpecification() == null && dataShape.getType() == null) {
            return ANY_SHAPE;
        }

        return dataShape;
    }

    private static boolean shouldEnrichDataShape(final DataShape received) {
        return received != null && received.getKind() != null;
    }
}
