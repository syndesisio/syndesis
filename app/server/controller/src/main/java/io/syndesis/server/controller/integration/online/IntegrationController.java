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
package io.syndesis.server.controller.integration.online;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.util.EventBus;
import io.syndesis.common.util.Labels;
import io.syndesis.server.controller.ControllersConfigurationProperties;
import io.syndesis.server.controller.StateChangeHandlerProvider;
import io.syndesis.server.controller.integration.BaseIntegrationController;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.openshift.OpenShiftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * This class tracks changes to Integrations and attempts to process them so that
 * their current status matches their desired status.
 */
@Service
@ConditionalOnProperty(value = "controllers.integration", havingValue = "s2i", matchIfMissing = true)
public class IntegrationController extends BaseIntegrationController {
    private static final Logger LOG = LoggerFactory.getLogger(IntegrationController.class);

    @Autowired
    public IntegrationController(
            OpenShiftService openShiftService,
            DataManager dataManager,
            EventBus eventBus,
            StateChangeHandlerProvider handlerFactory,
            ControllersConfigurationProperties properties) {
        super(openShiftService, dataManager, eventBus, handlerFactory, properties);
    }

    @PostConstruct
    @Override
    public void start() {
        LOG.info("Starting IntegrationController (s2i)");
        super.doStart();
    }

    @PreDestroy
    @Override
    public void stop() {
        LOG.info("Stopping IntegrationController (s2i)");
        super.doStop();
    }

    @Override
    protected IntegrationDeploymentState determineState(IntegrationDeployment integrationDeployment) {
        Map<String, String> labels = new HashMap<>();
        labels.put(OpenShiftService.INTEGRATION_ID_LABEL, Labels.validate(integrationDeployment.getIntegrationId().get()));
        labels.put(OpenShiftService.DEPLOYMENT_VERSION_LABEL, String.valueOf(integrationDeployment.getVersion()));

        if (!getOpenShiftService().exists(integrationDeployment.getSpec().getName()) ||
            !getOpenShiftService().isScaled(integrationDeployment.getSpec().getName(), 1, labels)) {
            return IntegrationDeploymentState.Unpublished;
        } else {
            return IntegrationDeploymentState.Published;
        }
    }
}
