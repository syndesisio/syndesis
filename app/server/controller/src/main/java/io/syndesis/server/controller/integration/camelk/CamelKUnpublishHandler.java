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
import java.util.Set;

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.server.controller.StateChangeHandler;
import io.syndesis.server.controller.StateUpdate;
import io.syndesis.server.controller.integration.IntegrationPublishValidator;
import io.syndesis.server.controller.integration.camelk.crd.DoneableIntegration;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationList;
import io.syndesis.server.dao.IntegrationDao;
import io.syndesis.server.dao.IntegrationDeploymentDao;
import io.syndesis.server.openshift.OpenShiftService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Qualifier("camel-k")
@ConditionalOnProperty(value = "controllers.integration", havingValue = "camel-k")
public class CamelKUnpublishHandler extends BaseCamelKHandler implements StateChangeHandler {

    protected CamelKUnpublishHandler(
            OpenShiftService openShiftService,
            IntegrationDao iDao,
            IntegrationDeploymentDao idDao,
            IntegrationPublishValidator validator) {
        super(openShiftService, iDao, idDao, validator);
    }

    @Override
    public Set<IntegrationDeploymentState> getTriggerStates() {
        return Collections.singleton(IntegrationDeploymentState.Unpublished);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public StateUpdate execute(IntegrationDeployment integrationDeployment) {
        //
        // Validation
        //

        if (!integrationDeployment.getUserId().isPresent()) {
            throw new IllegalStateException("Couldn't find the user of the integration");
        }
        if (!integrationDeployment.getIntegrationId().isPresent()) {
            throw new IllegalStateException("IntegrationDeployment should have an integrationId");
        }

        io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition crd = getCustomResourceDefinition();
        io.syndesis.server.controller.integration.camelk.crd.Integration cr = CamelKSupport.getIntegrationCR(getOpenShiftService(), crd, integrationDeployment);

        boolean deleted = getOpenShiftService().deleteCR(
            crd,
            io.syndesis.server.controller.integration.camelk.crd.Integration.class,
            IntegrationList.class,
            DoneableIntegration.class,
            cr);

        return deleted
            ? new StateUpdate(IntegrationDeploymentState.Unpublished, Collections.emptyMap())
            : new StateUpdate(CamelKSupport.getState(cr), Collections.emptyMap());
    }
}
