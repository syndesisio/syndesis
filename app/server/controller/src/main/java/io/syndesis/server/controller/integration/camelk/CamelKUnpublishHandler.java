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

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.util.Labels;
import io.syndesis.common.util.Names;
import io.syndesis.server.controller.StateChangeHandler;
import io.syndesis.server.controller.StateUpdate;
import io.syndesis.server.controller.integration.IntegrationPublishValidator;
import io.syndesis.server.controller.integration.camelk.crd.DoneableIntegration;
import io.syndesis.server.controller.integration.camelk.crd.Integration;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationList;
import io.syndesis.server.dao.IntegrationDao;
import io.syndesis.server.dao.IntegrationDeploymentDao;
import io.syndesis.server.openshift.OpenShiftService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        Map<String, String> labels = new HashMap<>();
        labels.put(OpenShiftService.INTEGRATION_ID_LABEL, Labels.validate(integrationDeployment.getIntegrationId().get()));
        labels.put(OpenShiftService.DEPLOYMENT_VERSION_LABEL, String.valueOf(integrationDeployment.getVersion()));
        List<Integration> integrations = CamelKSupport.getIntegrationCRbyLabels(getOpenShiftService(), crd, labels);

        if(integrations.size()==1){
            //it is still deployed, delete it
            Integration cr = integrations.get(0);

            boolean deleted = getOpenShiftService().deleteCR(
                crd,
                Integration.class,
                IntegrationList.class,
                DoneableIntegration.class,
                CamelKSupport.integrationName(integrationDeployment.getSpec().getName()),
                true);

            logInfo(integrationDeployment,"Deleted Integration: "+Names.sanitize(integrationDeployment.getIntegrationId().get()));
            return deleted
                ? new StateUpdate(IntegrationDeploymentState.Unpublished, Collections.emptyMap())
                : new StateUpdate(CamelKSupport.getState(cr), Collections.emptyMap(), "Unpublished for good!");
        }else if(integrations.size()>1){
            throw new IllegalStateException("There are more than one Camel k Integrations CR with labels: "+labels);
        }

        //there were no Camel k Integration CR, they have already been deleted
        return new StateUpdate(IntegrationDeploymentState.Unpublished, Collections.emptyMap(), "Already unpublished");
    }
}
