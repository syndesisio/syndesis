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
package io.syndesis.server.endpoint.v1.handler.continuousdelivery;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.syndesis.common.model.connection.ConnectionOverview;
import io.syndesis.common.model.integration.ContinuousDeliveryEnvironment;
import io.syndesis.common.model.integration.ContinuousDeliveryImportResults;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.monitoring.IntegrationDeploymentStateDetails;

@Component
@ConditionalOnProperty(value = "continuousdelivery.enabled", havingValue = "false", matchIfMissing=true)
public class NoopContinuousDeliveryHandlerImpl implements ContinuousDeliveryHandler {

    @Override
    public List<String> getReleaseEnvironments() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, ContinuousDeliveryEnvironment> getReleaseTags(String integrationId) {
        return Collections.emptyMap();
    }

    @Override
    public void deleteReleaseTag(String integrationId, String environment) {
        // no-op
    }

    @Override
    public Map<String, ContinuousDeliveryEnvironment> tagForRelease(String integrationId, List<String> environments) {
        return Collections.emptyMap();
    }

    @Override
    public StreamingOutput exportResources(String environment, boolean exportAll) throws IOException {
        return null;
    }

    @Override
    public ContinuousDeliveryImportResults importResources(SecurityContext sec, ImportFormDataInput formInput) throws IOException {
        return null;
    }

    @Override
    public ConnectionOverview configureConnection(SecurityContext sec, String name, Map<String, String> properties) throws IOException {
        return null;
    }

    @Override
    public IntegrationDeploymentStateDetails getIntegrationState(SecurityContext sec, String integrationId) throws
            IOException {
        return null;
    }

    @Override
    public IntegrationDeployment publishIntegration(SecurityContext sec, String integrationId) {
        return null;
    }

    @Override
    public void stopIntegration(SecurityContext sec, String integrationId, int version) {
        // no-op
    }
}
