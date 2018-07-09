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
package io.syndesis.server.endpoint.monitoring;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.model.monitoring.IntegrationDeploymentStateDetails;
import io.syndesis.server.dao.manager.DataManager;

@Component
@ConditionalOnProperty(value = "monitoring.kind", havingValue = "default")
public class MonitoringProviderImpl implements MonitoringProvider {

    private final DataManager dataManager;

    protected MonitoringProviderImpl(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public IntegrationDeploymentStateDetails getIntegrationStateDetails(String integrationId) {
        // get current integration version for IntegrationDeployment id
        final Integration integration = dataManager.fetch(Integration.class, integrationId);
        final int version = integration.getVersion();

        final String compositeId = IntegrationDeployment.compositeId(integrationId, version);
        return dataManager.fetch(IntegrationDeploymentStateDetails.class, compositeId);
    }

    @Override
    public List<IntegrationDeploymentStateDetails> getAllIntegrationStateDetails() {
        // TODO remove published integration deployment details???
        // get all pending publish deployments
        final Set<String> pendingIds = dataManager.fetchIdsByPropertyValue(IntegrationDeployment.class,
                "currentState", IntegrationDeploymentState.Pending.toString(),
                "targetState", IntegrationDeploymentState.Published.toString());
        return dataManager.fetchAll(IntegrationDeploymentStateDetails.class)
                .getItems()
                .stream()
                .filter(d -> pendingIds.contains(d.getId().orElse("")))
                .collect(Collectors.toList());
    }
}
