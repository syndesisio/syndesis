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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.server.controller.StateChangeHandler;
import io.syndesis.server.controller.StateUpdate;
import io.syndesis.server.controller.integration.IntegrationPublishValidator;
import io.syndesis.server.dao.IntegrationDao;
import io.syndesis.server.dao.IntegrationDeploymentDao;
import io.syndesis.server.openshift.OpenShiftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Qualifier("s2i")
@Component()
@ConditionalOnProperty(value = "controllers.integration", havingValue = "s2i", matchIfMissing = true)
public class UnpublishHandler extends BaseOnlineHandler implements StateChangeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UnpublishHandler.class);

    public UnpublishHandler(OpenShiftService openShiftService, IntegrationDao
        iDao, IntegrationDeploymentDao idDao, IntegrationPublishValidator validator) {
        super(openShiftService, iDao, idDao, validator);
    }

    @Override
    public Set<IntegrationDeploymentState> getTriggerStates() {
        return Collections.singleton(IntegrationDeploymentState.Unpublished);
    }

    @Override
    public StateUpdate execute(IntegrationDeployment integrationDeployment) {
        Map<String, String> stepsDone = new HashMap<>(integrationDeployment.getStepsDone());
        stepsDone.remove("deploy"); //we are literally undoing this step.

        IntegrationDeploymentState currentState = IntegrationDeploymentState.Pending;

        LOG.info("Undeploying integration deployment:{} version:{}", integrationDeployment.getSpec().getName(), integrationDeployment.getVersion());
        boolean stopped = getOpenShiftService().stop(integrationDeployment.getSpec().getName());

        if (stopped){
            currentState = IntegrationDeploymentState.Unpublished;
        }

        return new StateUpdate(currentState, stepsDone);
    }

}
