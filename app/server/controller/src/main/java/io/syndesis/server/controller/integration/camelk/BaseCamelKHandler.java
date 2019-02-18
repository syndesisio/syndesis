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

import java.util.Collection;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.util.Names;
import io.syndesis.server.controller.integration.BaseHandler;
import io.syndesis.server.controller.integration.IntegrationPublishValidator;
import io.syndesis.server.controller.integration.camelk.crd.DoneableIntegration;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationList;
import io.syndesis.server.dao.IntegrationDao;
import io.syndesis.server.dao.IntegrationDeploymentDao;
import io.syndesis.server.openshift.OpenShiftService;

abstract class BaseCamelKHandler extends BaseHandler {
    protected BaseCamelKHandler(OpenShiftService openShiftService, IntegrationDao integrationDao, IntegrationDeploymentDao integrationDeploymentDao, IntegrationPublishValidator validator) {
        super(openShiftService, integrationDao, integrationDeploymentDao, validator);
    }

    protected CustomResourceDefinition getCustomResourceDefinition() {
        return getOpenShiftService().getCRD(CamelKSupport.CAMEL_K_INTEGRATION_CRD_NAME).orElseThrow(
            () -> new IllegalArgumentException("No Camel-k Integration CRD found for name: " + CamelKSupport.CAMEL_K_INTEGRATION_CRD_NAME)
        );
    }

    protected boolean isBuildStarted(IntegrationDeployment integrationDeployment, CustomResourceDefinition integrationCRD) {
        return isInPhase(CamelKSupport.CAMEL_K_STARTED_STATES, integrationDeployment, integrationCRD);

    }

    protected boolean isBuildFailed(IntegrationDeployment integrationDeployment, CustomResourceDefinition integrationCRD) {
        return isInPhase(CamelKSupport.CAMEL_K_FAILED_STATES, integrationDeployment, integrationCRD);
    }

    protected boolean isRunning(IntegrationDeployment integrationDeployment, CustomResourceDefinition integrationCRD) {
        return isInPhase(CamelKSupport.CAMEL_K_RUNNING_STATES, integrationDeployment, integrationCRD);
    }

    @SuppressWarnings("unchecked")
    protected boolean isInPhase(Collection<String> phases, IntegrationDeployment integrationDeployment, CustomResourceDefinition integrationCRD) {
        io.syndesis.server.controller.integration.camelk.crd.Integration camelkIntegration = (io.syndesis.server.controller.integration.camelk.crd.Integration)getOpenShiftService().getCR(
            integrationCRD,
            io.syndesis.server.controller.integration.camelk.crd.Integration.class,
            IntegrationList.class,
            DoneableIntegration.class,
            Names.sanitize(integrationDeployment.getIntegrationId().get())
        ).get();

        return camelkIntegration != null && phases.contains(camelkIntegration.getStatus().getPhase());
    }
}
