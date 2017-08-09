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
package io.syndesis.rest.v1.handler.integration;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.syndesis.core.Tokens;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.inspector.ClassInspector;
import io.syndesis.model.Kind;
import io.syndesis.model.connection.DataShape;
import io.syndesis.model.connection.DataShapeKinds;
import io.syndesis.model.filter.FilterOptions;
import io.syndesis.model.filter.Op;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.Integration.Status;
import io.syndesis.rest.v1.handler.BaseHandler;
import io.syndesis.rest.v1.operations.Creator;
import io.syndesis.rest.v1.operations.Deleter;
import io.syndesis.rest.v1.operations.Getter;
import io.syndesis.rest.v1.operations.Lister;
import io.syndesis.rest.v1.operations.Updater;
import org.springframework.stereotype.Component;

@Path("/integrations")
@Api(value = "integrations")
@Component
public class IntegrationHandler extends BaseHandler implements Lister<Integration>, Getter<Integration>, Creator<Integration>, Deleter<Integration>, Updater<Integration> {

    private final ClassInspector classInspector;

    public IntegrationHandler(DataManager dataMgr, ClassInspector classInspector) {
        super(dataMgr);
        this.classInspector = classInspector;
    }

    @Override
    public Kind resourceKind() {
        return Kind.Integration;
    }

    @Override
    public Integration get(String id) {
        Integration integration = Getter.super.get(id);

        //fudging the timesUsed for now
        Optional<Status> currentStatus = integration.getCurrentStatus();
        if (currentStatus.isPresent() && currentStatus.get() == Integration.Status.Activated) {
            return new Integration.Builder()
                    .createFrom(integration)
                    .timesUsed(BigInteger.valueOf(new Date().getTime()/1000000))
                    .build();
        }

        return integration;
    }

    @Override
    public Integration create(Integration integration) {
        Date rightNow = new Date();
        Integration updatedIntegration = new Integration.Builder()
            .createFrom(integration)
            .token(Tokens.getAuthenticationToken())
            .statusMessage(Optional.empty())
            .lastUpdated(rightNow)
            .createdDate(rightNow)
            .currentStatus(determineCurrentStatus(integration))
            .build();
        return Creator.super.create(updatedIntegration);
    }

    @Override
    public void update(String id, Integration integration) {
        Integration updatedIntegration = new Integration.Builder()
            .createFrom(integration)
            .token(Tokens.getAuthenticationToken())
            .lastUpdated(new Date())
            .currentStatus(determineCurrentStatus(integration))
            .build();

        Updater.super.update(id, updatedIntegration);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/filters/options")
    public FilterOptions getFilterOptions(Integration integration) {
        FilterOptions.Builder builder = new FilterOptions.Builder().addOp(Op.DEFAULT_OPTS);

        integration.getSteps().orElse(Collections.emptyList()).forEach(s -> {
            s.getAction().ifPresent(a -> {
                DataShape dataShape = a.getOutputDataShape();
                if (dataShape != null) {
                    String kind = dataShape.getKind();
                    if (kind != null && kind.equals(DataShapeKinds.JAVA)) {
                        String type = dataShape.getType();
                        builder.addAllPaths(classInspector.getPaths(type));
                    }
                }
            });
        });
        return builder.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/filters/options")
    public FilterOptions getGlobalFilterOptions() {
        return new FilterOptions.Builder().addOp(Op.DEFAULT_OPTS).build();
    }

    // Determine the current status to 'pending' or 'draft' immediately depending on
    // the desired stated. This status will be later changed by the activation handlers.
    // This is not the best place to set but should be done by the IntegrationController
    // However because of how the Controller works (i.e. that any change to the integration
    // within the controller will trigger an event again), the initial status must be set
    // from the outside for the moment.
    private Integration.Status determineCurrentStatus(Integration integration) {
        Integration.Status desiredStatus = integration.getDesiredStatus().orElse(Integration.Status.Draft);
        return desiredStatus == Integration.Status.Draft ?
            Integration.Status.Draft :
            Integration.Status.Pending;
    }
}
