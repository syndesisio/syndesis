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

import io.fabric8.kubernetes.client.Watch;
import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.util.EventBus;
import io.syndesis.common.util.Labels;
import io.syndesis.server.controller.ControllersConfigurationProperties;
import io.syndesis.server.controller.StateChangeHandlerProvider;
import io.syndesis.server.controller.integration.BaseIntegrationController;
import io.syndesis.server.controller.integration.camelk.crd.DoneableIntegration;
import io.syndesis.server.controller.integration.camelk.crd.Integration;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationList;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.openshift.OpenShiftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This class tracks changes to Integrations and attempts to process them so that
 * their current status matches their desired status.
 */
@Service
@ConditionalOnProperty(value = "controllers.integration", havingValue = "camel-k")
public class CamelKIntegrationController extends BaseIntegrationController {
    private static final Logger LOG = LoggerFactory.getLogger(CamelKIntegrationController.class);
    private Watch watcher;

    @Autowired
    public CamelKIntegrationController(
            OpenShiftService openShiftService,
            DataManager dataManager,
            EventBus eventBus,
            StateChangeHandlerProvider handlerFactory,
            ControllersConfigurationProperties properties) {
        super(openShiftService, dataManager, eventBus, handlerFactory, properties);
    }

    @PostConstruct
    @Override
    @SuppressWarnings("unchecked")
    public void start() {
        LOG.info("Starting IntegrationController (camel-k)");
        super.doStart();
        watcher = getOpenShiftService().watchCR( CamelKSupport.CAMEL_K_INTEGRATION_CRD,
            Integration.class,
            IntegrationList.class,
            DoneableIntegration.class,
            (action, integration) -> {
                LOG.debug("CamelKIntegrationController watching "+CamelKSupport.CAMEL_K_INTEGRATION_CRD.getMetadata().getName()+" received action: "+action+" and integration:"+integration);
                if( integration != null && integration.getMetadata() != null && integration.getMetadata().getAnnotations() != null ){
                    String deploymentId = integration.getMetadata().getAnnotations().get(OpenShiftService.DEPLOYMENT_ID_ANNOTATION);
                    if( deploymentId == null ){
                        LOG.warn("CamelKIntegrationController DROPPING received action: {} on an {} lacking the [{}] property, so no Deployment Id can be associated to the event.", action, CamelKSupport.CAMEL_K_INTEGRATION_CRD.getMetadata().getName(), OpenShiftService.DEPLOYMENT_ID_ANNOTATION );
                    } else {
                        getEventBus().broadcast(EventBus.Type.CHANGE_EVENT,
                            ChangeEvent.of("", Kind.IntegrationDeployment.getModelName(), deploymentId).toJson());
                    }
                }
            });
    }

    @PreDestroy
    @Override
    public void stop() {
        LOG.info("Stopping IntegrationController (camel-k)");
        super.doStop();
        if(watcher != null){
            watcher.close();
        }
    }

    @Override
    protected IntegrationDeploymentState determineState(IntegrationDeployment integrationDeployment) {
        io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition crd = CamelKSupport.CAMEL_K_INTEGRATION_CRD;

        Map<String, String> labels = new HashMap<>();
        labels.put(OpenShiftService.INTEGRATION_ID_LABEL, Labels.validate(integrationDeployment.getIntegrationId().get()));
        labels.put(OpenShiftService.DEPLOYMENT_VERSION_LABEL, String.valueOf(integrationDeployment.getVersion()));
        List<Integration> integrations = CamelKSupport.getIntegrationCRbyLabels(getOpenShiftService(), crd, labels);

        if(integrations.size()==1){
            //it is still deployed, delete it
            Integration cr = integrations.get(0);

            return CamelKSupport.getState(cr);
        }else if(integrations.size()>1){
            throw new IllegalStateException("There are more than one Camel k Integrations CR with labels: "+labels);
        }

        return IntegrationDeploymentState.Unpublished;
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    @Override
    protected void reschedule(String integrationDeploymentId, String checkKey) {
        LOG.debug("Remove from ScheduledChecks checkKey: {}, keys: {}", checkKey, getScheduledChecks());
        getScheduledChecks().remove(checkKey);
        LOG.debug("Reschedule IntegrationDeployment check, id:{}, keys: {}", integrationDeploymentId, getScheduledChecks());
        getScheduler().schedule(() -> {
                LOG.debug("Trigger checkIntegrationStatus, id:{}", integrationDeploymentId);
                //checkIntegrationStatus(i);
                getEventBus().broadcast(EventBus.Type.CHANGE_EVENT,
                    ChangeEvent.of("", Kind.IntegrationDeployment.getModelName(), integrationDeploymentId).toJson());
            },
            getProperties().getIntegrationStateCheckInterval(),
            TimeUnit.SECONDS
        );
    }
}
