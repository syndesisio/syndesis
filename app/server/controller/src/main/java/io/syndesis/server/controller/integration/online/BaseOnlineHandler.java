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

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.util.Labels;
import io.syndesis.server.controller.integration.BaseHandler;
import io.syndesis.server.controller.integration.IntegrationPublishValidator;
import io.syndesis.server.dao.IntegrationDao;
import io.syndesis.server.dao.IntegrationDeploymentDao;
import io.syndesis.server.openshift.OpenShiftService;

import java.util.HashMap;
import java.util.Map;

abstract class BaseOnlineHandler extends BaseHandler {
    protected BaseOnlineHandler(OpenShiftService openShiftService, IntegrationDao integrationDao, IntegrationDeploymentDao integrationDeploymentDao, IntegrationPublishValidator validator) {
        super(openShiftService, integrationDao, integrationDeploymentDao, validator);
    }

    protected boolean isRunning(IntegrationDeployment integrationDeployment) {
        Map<String, String> labels = new HashMap<>();
        labels.put(OpenShiftService.INTEGRATION_ID_LABEL, Labels.validate(integrationDeployment.getIntegrationId().get()));
        labels.put(OpenShiftService.DEPLOYMENT_VERSION_LABEL, String.valueOf(integrationDeployment.getVersion()));
        return getOpenShiftService().isScaled(integrationDeployment.getSpec().getName(), 1, labels);
    }
}
