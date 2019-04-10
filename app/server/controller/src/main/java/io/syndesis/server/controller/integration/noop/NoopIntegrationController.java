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
package io.syndesis.server.controller.integration.noop;

import io.syndesis.common.util.EventBus;
import io.syndesis.server.controller.ControllersConfigurationProperties;
import io.syndesis.server.controller.StateChangeHandlerProvider;
import io.syndesis.server.controller.integration.online.IntegrationController;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.openshift.OpenShiftService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "controllers.integration", havingValue = "noop")
public class NoopIntegrationController extends IntegrationController {

    public NoopIntegrationController(OpenShiftService openShiftService, DataManager dataManager, EventBus eventBus, StateChangeHandlerProvider handlerFactory, ControllersConfigurationProperties properties) {
        super(openShiftService, dataManager, eventBus, handlerFactory, properties);
    }
}
