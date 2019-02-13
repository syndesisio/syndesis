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
package io.syndesis.server.controller.integration.camelk;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.util.Json;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.server.controller.StateChangeHandler;
import io.syndesis.server.controller.StateUpdate;
import io.syndesis.server.controller.integration.BaseHandler;
import io.syndesis.server.controller.integration.IntegrationPublishValidator;
import io.syndesis.server.dao.IntegrationDao;
import io.syndesis.server.dao.IntegrationDeploymentDao;
import io.syndesis.server.openshift.OpenShiftService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
@Qualifier("camel-k")
@ConditionalOnProperty(value = "controllers.integration", havingValue = "camel-k")
public class CamelKPublishHandler extends BaseHandler implements StateChangeHandler {

    private final IntegrationResourceManager resourceManager;

    public CamelKPublishHandler(OpenShiftService openShiftService,
                                IntegrationDao iDao,
                                IntegrationDeploymentDao idDao,
                                IntegrationPublishValidator validator,
                                IntegrationResourceManager resourceManager) {
        super(openShiftService, iDao, idDao, validator);
        this.resourceManager = resourceManager;
    }

    @Override
    public Set<IntegrationDeploymentState> getTriggerStates() {
        return Collections.singleton(IntegrationDeploymentState.Published);
    }

    @Override
    public StateUpdate execute(IntegrationDeployment integrationDeployment) {
        StateUpdate updateViaValidation = getValidator().validate(integrationDeployment);
        if (updateViaValidation != null) {
            return updateViaValidation;
        }

        if (isBuildFailed(integrationDeployment)){
            return new StateUpdate(IntegrationDeploymentState.Error, Collections.emptyMap(), "Error");
        }

        logInfo(integrationDeployment, "Build started: {}, isRunning: {}, Deployment ready: {}",
                isBuildStarted(integrationDeployment), isRunning(integrationDeployment), isReady(integrationDeployment));

        Map<String, String> stepsDone = new HashMap<>(integrationDeployment.getStepsDone());

        final Integration integration = integrationDeployment.getSpec();
        prepareDeployment(integrationDeployment);

        stepsDone.put("deploy", "camel-k");
        logInfo(integrationDeployment,"Creating Camel-K resource");


        String integrationJson = extractIntegrationJson(integration);
        logInfo(integration,"integration.json: {}", integrationJson);

        return new StateUpdate(IntegrationDeploymentState.Pending, stepsDone);
    }

    private String extractIntegrationJson(Integration fullIntegration) {
        Integration integration = resourceManager.sanitize(fullIntegration);
        ObjectWriter writer = Json.writer();
        try {

            return new String(writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValueAsBytes(integration), UTF_8);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot convert integration " + integration.getName() + " to JSON: " + e,e);
        }
    }

    private void prepareDeployment(IntegrationDeployment integrationDeployment) {
        setVersion(integrationDeployment);
        deactivatePreviousDeployments(integrationDeployment);
    }

    private boolean isBuildStarted(IntegrationDeployment integrationDeployment) {
        // TODO: Check if Camel-K resource has the appropriate status
        // return openShiftService().isBuildStarted(integrationDeployment.getSpec().getName());
        logInfo(integrationDeployment, "isBuildStarted");
        return true;
    }

    private boolean isBuildFailed(IntegrationDeployment integrationDeployment) {
        // TODO: Check if Camel-K resource has the appropriate status
        // return openShiftService().isBuildFailed(integrationDeployment.getSpec().getName());
        logInfo(integrationDeployment, "isBuildFailed");
        return false;
    }

    private boolean isReady(IntegrationDeployment integrationDeployment) {
        // TODO: Check if Camel-K resource has the appropriate status
        // return openShiftService().isDeploymentReady(integrationDeployment.getSpec().getName());
        logInfo(integrationDeployment, "isReady");
        return false;
    }

}
