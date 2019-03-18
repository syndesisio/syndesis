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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentError;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.util.Labels;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.integration.api.IntegrationErrorHandler;
import io.syndesis.integration.api.IntegrationProjectGenerator;
import io.syndesis.server.controller.StateChangeHandler;
import io.syndesis.server.controller.StateUpdate;
import io.syndesis.server.controller.integration.IntegrationPublishValidator;
import io.syndesis.server.controller.integration.online.customizer.DeploymentDataCustomizer;
import io.syndesis.server.dao.IntegrationDao;
import io.syndesis.server.dao.IntegrationDeploymentDao;
import io.syndesis.server.openshift.DeploymentData;
import io.syndesis.server.openshift.OpenShiftService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Qualifier("s2i")
@Component()
@ConditionalOnProperty(value = "controllers.integration", havingValue = "s2i", matchIfMissing = true)
public class PublishHandler extends BaseOnlineHandler implements StateChangeHandler {

    private final IntegrationProjectGenerator projectGenerator;
    private final List<DeploymentDataCustomizer> customizers;
    @SuppressWarnings("PMD.DefaultPackage")
    PublishHandler(
        OpenShiftService openShiftService,
        IntegrationDao iDao,
        IntegrationDeploymentDao idDao,
        IntegrationProjectGenerator projectGenerator,
        List<DeploymentDataCustomizer> customizers,
        IntegrationPublishValidator validator) {

        super(openShiftService, iDao, idDao, validator);

        this.projectGenerator = projectGenerator;
        this.customizers = customizers;
    }

    @Override
    public Set<IntegrationDeploymentState> getTriggerStates() {
        return Collections.singleton(IntegrationDeploymentState.Published);
    }

    @Override
    public StateUpdate execute(final IntegrationDeployment integrationDeployment) {
        StateUpdate updateViaValidation = getValidator().validate(integrationDeployment);
        if (updateViaValidation != null) {
            return updateViaValidation;
        }

        logInfo(integrationDeployment, "Build started: {}, isRunning: {}, Deployment ready: {}",
            isBuildStarted(integrationDeployment), isRunning(integrationDeployment), isReady(integrationDeployment));

        BuildStepOncePerformer stepOncePerformer = new BuildStepOncePerformer(integrationDeployment);
        logInfo(integrationDeployment, "Steps performed so far: " + stepOncePerformer.getStepsPerformed());

        if (hasError(integrationDeployment) || isBuildFailed(integrationDeployment)){
            logError(integrationDeployment, "[ERROR] Build is in failed state");
            return new StateUpdate(IntegrationDeploymentState.Error, stepOncePerformer.getStepsPerformed(), "Error", integrationDeployment.getError());
        }

        final Integration integration = integrationOf(integrationDeployment);
        try {
            setVersion(integrationDeployment);
            deactivatePreviousDeployments(integrationDeployment);

            DeploymentData deploymentData = createDeploymentData(integration, integrationDeployment);
            String buildLabel = "buildv" + deploymentData.getVersion();
            stepOncePerformer.perform(buildLabel, this::build, deploymentData);

            if (stepOncePerformer.hasError()) {
                logError(integrationDeployment, "[ERROR] Build failed with {} - {}",
                        stepOncePerformer.getError().getType(), stepOncePerformer.getError().getMessage());
                return new StateUpdate(IntegrationDeploymentState.Error, stepOncePerformer.getStepsPerformed(), "Error", stepOncePerformer.getError());
            }

            if (hasPublishedDeployments(integrationDeployment)) {
                return new StateUpdate(IntegrationDeploymentState.Unpublished, integrationDeployment.getStepsDone(), "Integration has still active deployments. Will retry shortly");
            }

            deploymentData = new DeploymentData.Builder().createFrom(deploymentData).withImage(stepOncePerformer.stepsPerformed.get(buildLabel)).build();
            stepOncePerformer.perform("deploy", this::deploy, deploymentData);
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            logError(integrationDeployment, "[ERROR] Activation failure", e);
            // Setting a message to update means implicitly that the deployment is in an error state (for the UI)
            return new StateUpdate(IntegrationDeploymentState.Pending, stepOncePerformer.getStepsPerformed(), e.getMessage());
        }

        // Set status to activate if finally running. Also clears the previous step which has been performed
        if (isRunning(integrationDeployment)) {
            logInfo(integrationDeployment, "[ACTIVATED] bc. integration is running with 1 pod");
            updateDeploymentState(integrationDeployment, IntegrationDeploymentState.Published);
            return new StateUpdate(IntegrationDeploymentState.Published, stepOncePerformer.getStepsPerformed());
        }

        logInfo(integrationDeployment, "Build started: {}, isRunning: {}, Deployment ready: {}",
            isBuildStarted(integrationDeployment), isRunning(integrationDeployment), isReady(integrationDeployment));
        logInfo(integrationDeployment, "[PENDING] [" + stepOncePerformer.getStepsPerformed() + "]");

        return new StateUpdate(IntegrationDeploymentState.Pending, stepOncePerformer.getStepsPerformed());
    }

    private DeploymentData createDeploymentData(Integration integration, IntegrationDeployment integrationDeployment) {
        Properties applicationProperties = projectGenerator.generateApplicationProperties(integration);

        String username = integrationDeployment.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));

        String integrationId = integrationDeployment.getIntegrationId().orElseThrow(() -> new IllegalStateException("IntegrationDeployment should have an integrationId"));
        String version = Integer.toString(integrationDeployment.getVersion());
        final DeploymentData.Builder deploymentDataBuilder = DeploymentData.builder()
            .withVersion(integrationDeployment.getVersion())
            .addLabel(OpenShiftService.INTEGRATION_ID_LABEL, Labels.validate(integrationId))
            .addLabel(OpenShiftService.DEPLOYMENT_VERSION_LABEL, version)
            .addLabel(OpenShiftService.USERNAME_LABEL, Labels.sanitize(username))
            .addAnnotation(OpenShiftService.INTEGRATION_NAME_ANNOTATION, integration.getName())
            .addAnnotation(OpenShiftService.INTEGRATION_ID_LABEL, integrationId)
            .addAnnotation(OpenShiftService.DEPLOYMENT_VERSION_LABEL, version)
            .addSecretEntry("application.properties", propsToString(applicationProperties));

        integration.getConfiguredProperties().forEach((k, v) -> deploymentDataBuilder.addProperty(k, v));

        DeploymentData data = deploymentDataBuilder.build();

        if (this.customizers != null && !this.customizers.isEmpty()) {
            for (DeploymentDataCustomizer customizer : customizers) {
                data = customizer.customize(data, integrationDeployment);
            }
        }

        return data;
    }


    // =============================================================================
    // Various steps to perform:

    private String build(IntegrationDeployment integration, DeploymentData data, IntegrationErrorHandler errorHandler)  {
        InputStream tarInputStream = createProjectFiles(integration.getSpec(), errorHandler);
        logInfo(integration, "Created project files and starting build");
        try {
            return getOpenShiftService().build(integration.getSpec().getName(), data, tarInputStream);
        } catch (InterruptedException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    private String deploy(IntegrationDeployment integration, DeploymentData data) {
        logInfo(integration, "Starting deployment");
        String revision = getOpenShiftService().deploy(integration.getSpec().getName(), data);
        logInfo(integration, "Deployment done");
        return revision;
    }


    // =================================================================================

    private boolean hasError(IntegrationDeployment integrationDeployment) {
        return integrationDeployment.getCurrentState() == IntegrationDeploymentState.Error ||
                integrationDeployment.hasError() ||
                getIntegrationDeploymentDao().hasError(integrationDeployment.getId().get());
    }

    private boolean isBuildStarted(IntegrationDeployment integrationDeployment) {
        return getOpenShiftService().isBuildStarted(integrationDeployment.getSpec().getName());
    }

    private boolean isBuildFailed(IntegrationDeployment integrationDeployment) {
        return getOpenShiftService().isBuildFailed(integrationDeployment.getSpec().getName());
    }

    private boolean isReady(IntegrationDeployment integrationDeployment) {
        return getOpenShiftService().isDeploymentReady(integrationDeployment.getSpec().getName());
    }

    private static String propsToString(Properties data) {
        if (data == null) {
            return "";
        }
        try {
            StringWriter w = new StringWriter();
            data.store(w, "");
            return w.toString();
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    private static Integration integrationOf(IntegrationDeployment integrationDeployment) {
        return integrationDeployment.getSpec();
    }

    /**
     * Check if Integration has active deployments.
     * @param deployment The specified {@link IntegrationDeployment}.
     * @return  The true if there are, false otherwise.
     */
    private boolean hasPublishedDeployments(IntegrationDeployment deployment) {
        Integration integration = deployment.getSpec();
        String id = Labels.validate(integration.getId().orElseThrow(() -> new IllegalStateException("Couldn't find the id of the integration")));
        String version = String.valueOf(integration.getVersion());

        Map<String, String> labels = new HashMap<>();
        labels.put(OpenShiftService.INTEGRATION_ID_LABEL, id);

        return (int) getOpenShiftService().getDeploymentsByLabel(labels)
                                          .stream()
                                          .filter(d -> !version.equals(d.getMetadata().getLabels().get(OpenShiftService.DEPLOYMENT_VERSION_LABEL)))
                                          .filter(d -> d.getSpec().getReplicas() > 0)
                                          .count() > 0;
    }

    private InputStream createProjectFiles(Integration integration, IntegrationErrorHandler errorHandler) {
        try {
            return projectGenerator.generate(integration, errorHandler);
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    // ===============================================================================
    // Some helper method to conditional execute certain steps
    @FunctionalInterface
    public interface IoCheckedFunction<T> {
        String apply(T t, DeploymentData data);
    }

    @FunctionalInterface
    public interface IoCheckedErrorHandlingFunction<T> {
        String apply(T t, DeploymentData data, IntegrationErrorHandler errorHandler);
    }

    private class BuildStepOncePerformer {
        private final Map<String, String> stepsPerformed;
        private final IntegrationDeployment integrationDeployment;
        private IntegrationDeploymentError error;

        BuildStepOncePerformer(IntegrationDeployment integrationDeployment) {
            this.integrationDeployment = integrationDeployment;
            this.stepsPerformed = new HashMap<>(integrationDeployment.getStepsDone());
        }

        void perform(String step, IoCheckedFunction<IntegrationDeployment> callable, DeploymentData data) {
            if (!stepsPerformed.containsKey(step)) {
                stepsPerformed.put(step, callable.apply(integrationDeployment, data));
            } else {
                logInfo(integrationDeployment, "Skipped step {} because already performed", step);
            }
        }

        void perform(String step, IoCheckedErrorHandlingFunction<IntegrationDeployment> callable, DeploymentData data) {
            if (!stepsPerformed.containsKey(step)) {
                stepsPerformed.put(step, callable.apply(integrationDeployment, data,
                        (throwable) -> {
                        logError(integrationDeployment, "Error for step {}: {} {}",
                                step,
                                throwable.getClass().getName(),
                                Optional.ofNullable(throwable.getMessage()).orElse(""));

                        error = new IntegrationDeploymentError.Builder()
                                        .type(throwable.getClass().getName())
                                        .message(throwable.getMessage())
                                        .build();
                    }));
            } else {
                logInfo(integrationDeployment, "Skipped step {} because already performed", step);
            }
        }

        boolean hasError() {
            return error != null;
        }

        IntegrationDeploymentError getError() {
            return error;
        }

        Map<String, String> getStepsPerformed() {
            return stepsPerformed;
        }
    }
}
