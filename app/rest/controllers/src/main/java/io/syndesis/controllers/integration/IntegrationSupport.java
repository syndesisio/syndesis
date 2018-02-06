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
package io.syndesis.controllers.integration;

import java.util.ArrayList;
import java.util.List;

import io.syndesis.dao.manager.DataManager;
import io.syndesis.dao.manager.EncryptionComponent;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.Step;

public final class IntegrationSupport {
    private IntegrationSupport() {
    }

    public static IntegrationDeployment sanitize(IntegrationDeployment integrationDeployment, DataManager dataManager, EncryptionComponent encryptionSupport) {
        final int stepCount = integrationDeployment.getSpec().getSteps().size();
        final List<Step> steps = new ArrayList<>(stepCount);
        final Integration.Builder builder = new Integration.Builder().createFrom(integrationDeployment.getSpec());

        for (int i = 1; i <= stepCount; i++) {
            final Step source = integrationDeployment.getSpec().getSteps().get(i - 1);

            Step.Builder stepBuilder = new Step.Builder();
            stepBuilder.createFrom(source);

            source.getConnection().ifPresent(connection -> {
                // If connector is not set, fetch it from data source and update connection
                if (connection.getConnectorId().isPresent() && !connection.getConnector().isPresent()) {
                    Connector connector = dataManager.fetch(Connector.class, connection.getConnectorId().get());

                    if (connector != null) {
                        stepBuilder.connection(
                            new Connection.Builder()
                                .createFrom(connection)
                                .connector(connector)
                                .build()
                        );
                    } else {
                        throw new IllegalArgumentException("Unable to fetch connector: " + connection.getConnectorId().get());
                    }
                }
            });

            steps.add(stepBuilder.build());
        }

        return new IntegrationDeployment.Builder()
            .createFrom(integrationDeployment)
            .spec(builder.steps(steps).build())
            .build();
    }
}
