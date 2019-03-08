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
package io.syndesis.server.controller.integration;

import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.util.Names;
import io.syndesis.server.dao.IntegrationDao;
import io.syndesis.server.dao.IntegrationDeploymentDao;
import io.syndesis.server.openshift.OpenShiftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@SuppressWarnings("PMD.LoggerIsNotStaticFinal")
public class BaseHandler {

    private final OpenShiftService openShiftService;
    private final IntegrationDao integrationDao;
    private final IntegrationDeploymentDao integrationDeploymentDao;
    private final IntegrationPublishValidator validator;
    private final Logger log;

    protected BaseHandler(OpenShiftService openShiftService,
                          IntegrationDao integrationDao,
                          IntegrationDeploymentDao integrationDeploymentDao,
                          IntegrationPublishValidator validator) {
        this.openShiftService = openShiftService;
        this.integrationDao = integrationDao;
        this.integrationDeploymentDao = integrationDeploymentDao;
        this.validator = validator;
        this.log = LoggerFactory.getLogger(getClass());
    }

    protected OpenShiftService getOpenShiftService() {
        return openShiftService;
    }

    protected IntegrationDao getIntegrationDao() {
        return integrationDao;
    }

    protected IntegrationDeploymentDao getIntegrationDeploymentDao() {
        return integrationDeploymentDao;
    }

    protected IntegrationPublishValidator getValidator() {
        return validator;
    }

    protected void logInfo(IntegrationDeployment integrationDeployment, String format, Object ... args) {
        if (log.isInfoEnabled()) {
            log.info(getLabel(integrationDeployment) + ": " + format, args);
        }
    }

    protected void logInfo(Integration integration, String format, Object ... args) {
        if (log.isInfoEnabled()) {
            log.info(getLabel(integration) + ": " + format, args);
        }
    }

    protected void logError(Integration integration, String format, Object ... args) {
        if (log.isErrorEnabled()) {
            log.error(getLabel(integration) + ": " + format, args);
        }
    }

    protected void logError(IntegrationDeployment integrationDeployment, String format, Object ... args) {
        if (log.isErrorEnabled()) {
            log.error(getLabel(integrationDeployment) + ": " + format, args);
        }
    }

    private String getLabel(Integration integration) {
        return String.format("Integration [%s]", Names.sanitize(integration.getName()));
    }

    private String getLabel(IntegrationDeployment integrationDeployment) {
        return String.format("Integration [%s]", Names.sanitize(integrationDeployment.getSpec().getName()));
    }

    protected void setVersion(IntegrationDeployment integrationDeployment) {
        String id = integrationDeployment.getIntegrationId()
                                         .orElseThrow(() -> new IllegalArgumentException("No id given in IntegrationDeployment"));
        getIntegrationDao().updateVersion(id, integrationDeployment.getVersion());
    }

    protected void updateDeploymentState(IntegrationDeployment integrationDeployment, IntegrationDeploymentState state) {
        IntegrationDeployment d = getIntegrationDeploymentDao().fetch(integrationDeployment.getId().get());
        getIntegrationDeploymentDao().update(d.withCurrentState(state));
    }

    protected void deactivatePreviousDeployments(IntegrationDeployment integrationDeployment) {
        String id = integrationDeployment.getId().orElseThrow(() -> new IllegalArgumentException("internal: No id given"));
        IntegrationDeploymentDao dao = getIntegrationDeploymentDao();
        Set<String> ids = dao.fetchIdsByPropertyValue("integrationId", id);
        ids.retainAll(dao.fetchIdsByPropertyValue("targetState", IntegrationDeploymentState.Published.name()));

        ids.stream()
            .map(dao::fetch)
            .filter(r -> r.getVersion() != integrationDeployment.getVersion())
            .map(IntegrationDeployment::unpublishing)
            .forEach(dao::update);
    }
}
