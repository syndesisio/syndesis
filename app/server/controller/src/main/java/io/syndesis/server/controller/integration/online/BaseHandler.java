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

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.syndesis.common.util.Names;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.server.openshift.OpenShiftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseHandler {
    private final OpenShiftService openShiftService;

    private static final Logger LOG = LoggerFactory.getLogger(BaseHandler.class);


    protected BaseHandler(OpenShiftService openShiftService) {
        this.openShiftService = openShiftService;
    }

    protected OpenShiftService openShiftService() {
        return openShiftService;
    }

    protected void logInfo(IntegrationDeployment integrationDeployment, String format, Object ... args) {
        if (LOG.isInfoEnabled()) {
            LOG.info(getLabel(integrationDeployment) + ": " + format, args);
        }
    }

    protected void logInfo(Integration integration, String format, Object ... args) {
        if (LOG.isInfoEnabled()) {
            LOG.info(getLabel(integration) + ": " + format, args);
        }
    }

    protected void logError(Integration integration, String format, Object ... args) {
        if (LOG.isErrorEnabled()) {
            LOG.error(getLabel(integration) + ": " + format, args);
        }
    }

    protected void logError(IntegrationDeployment integrationDeployment, String format, Object ... args) {
        if (LOG.isErrorEnabled()) {
            LOG.error(getLabel(integrationDeployment) + ": " + format, args);
        }
    }

    private String getLabel(Integration integration) {
        return String.format("Integration [%s]", Names.sanitize(integration.getName()));
    }

    private String getLabel(IntegrationDeployment integrationDeployment) {
        return String.format("Integration [%s]", Names.sanitize(integrationDeployment.getSpec().getName()));
    }

    /**
     * Fetch the Connectors
     */
    protected static Map<String, Connector> fetchConnectorsMap(DataManager dataManager) {
        return dataManager.fetchAll(Connector.class).getItems()
            .stream()
            .collect(Collectors.toMap(o -> o.getId().get(), Function.identity()));
    }
}
