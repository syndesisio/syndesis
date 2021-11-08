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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @SuppressWarnings("PMD.InvalidLogMessageFormat")
    protected void logInfo(IntegrationDeployment integrationDeployment, String format, Object ... args) {
        if (log.isInfoEnabled()) {
            log.info(getLabel(integrationDeployment) + ": " + format, args);
        }
    }

    @SuppressWarnings("PMD.InvalidLogMessageFormat")
    protected void logInfo(Integration integration, String format, Object ... args) {
        if (log.isInfoEnabled()) {
            log.info(getLabel(integration) + ": " + format, args);
        }
    }

    @SuppressWarnings("PMD.InvalidLogMessageFormat")
    protected void logError(Integration integration, String format, Object ... args) {
        if (log.isErrorEnabled()) {
            log.error(getLabel(integration) + ": " + format, args);
        }
    }

    @SuppressWarnings("PMD.InvalidLogMessageFormat")
    protected void logError(IntegrationDeployment integrationDeployment, String format, Object ... args) {
        if (log.isErrorEnabled()) {
            log.error(getLabel(integrationDeployment) + ": " + format, args);
        }
    }

    private static String getLabel(Integration integration) {
        return String.format("Integration [%s]", Names.sanitize(integration.getName()));
    }

    private static String getLabel(IntegrationDeployment integrationDeployment) {
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

    protected List<IntegrationDeployment> deactivatePreviousDeployments(IntegrationDeployment integrationDeployment) {
        final String id = integrationDeployment.getSpec().getId().get();
        final Set<String> ids = integrationDeploymentDao.fetchIdsByPropertyValue("integrationId", id);

        final List<IntegrationDeployment> previousDeployments = ids.stream()
            .map(integrationDeploymentDao::fetch)
            .collect(Collectors.toList());

        for (IntegrationDeployment deployment : previousDeployments) {
            final IntegrationDeployment unpublished = deployment.unpublishing();
            if (!unpublished.equals(deployment)) {
                // if deployment was changed by IntegrationDeployment::unpublishing it is on longer
                // equal to the IntegrationDeployment it originated from, so we need to update in
                // JSONDB
                integrationDeploymentDao.update(deployment);
            }
        }

        return previousDeployments;
    }
}
