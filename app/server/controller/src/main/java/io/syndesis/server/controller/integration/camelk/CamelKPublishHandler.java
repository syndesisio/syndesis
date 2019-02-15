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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.ImmutableSet;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.Labels;
import io.syndesis.common.util.Names;
import io.syndesis.integration.api.IntegrationProjectGenerator;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.integration.project.generator.mvn.MavenGav;
import io.syndesis.server.controller.StateChangeHandler;
import io.syndesis.server.controller.StateUpdate;
import io.syndesis.server.controller.integration.BaseHandler;
import io.syndesis.server.controller.integration.IntegrationPublishValidator;
import io.syndesis.server.controller.integration.camelk.crd.ConfigurationSpec;
import io.syndesis.server.controller.integration.camelk.crd.DoneableIntegration;
import io.syndesis.server.controller.integration.camelk.crd.ImmutableIntegrationSpec;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationList;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationSpec;
import io.syndesis.server.controller.integration.camelk.crd.SourceSpec;
import io.syndesis.server.dao.IntegrationDao;
import io.syndesis.server.dao.IntegrationDeploymentDao;
import io.syndesis.server.endpoint.v1.VersionService;
import io.syndesis.server.openshift.OpenShiftService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
@Qualifier("camel-k")
@ConditionalOnProperty(value = "controllers.integration", havingValue = "camel-k")
public class CamelKPublishHandler extends BaseHandler implements StateChangeHandler {

//    // IntegrationPhaseInitial --
//    IntegrationPhaseInitial IntegrationPhase = ""
//    // IntegrationPhaseWaitingForPlatform --
//    IntegrationPhaseWaitingForPlatform IntegrationPhase = "Waiting For Platform"
//    // IntegrationPhaseBuildingContext --
//    IntegrationPhaseBuildingContext IntegrationPhase = "Building Context"
//    // IntegrationPhaseBuildImageSubmitted --
//    IntegrationPhaseBuildImageSubmitted IntegrationPhase = "Build Image Submitted"
//    // IntegrationPhaseBuildImageRunning --
//    IntegrationPhaseBuildImageRunning IntegrationPhase = "Build Image Running"
//    // IntegrationPhaseDeploying --
//    IntegrationPhaseDeploying IntegrationPhase = "Deploying"
//    // IntegrationPhaseRunning --
//    IntegrationPhaseRunning IntegrationPhase = "Running"
//    // IntegrationPhaseError --
//    IntegrationPhaseError IntegrationPhase = "Error"
//    // IntegrationPhaseBuildFailureRecovery --
//    IntegrationPhaseBuildFailureRecovery IntegrationPhase = "Building Failure Recovery"
    private static final String CAMEL_K_INTEGRATIONCRD_NAME = "integrations.camel.apache.org";
    private static final ImmutableSet<String> CAMEL_K_STARTED_STATES = ImmutableSet.of(
        "Waiting For Platform",
        "Building Context",
        "Build Image Submitted",
        "Build Image Running",
        "Deploying");
    private static final ImmutableSet<String> CAMEL_K_FAILED_STATES = ImmutableSet.of(
        "Error",
        "Building Failure Recovery");
    private static final ImmutableSet<String> CAMEL_K_READY_STATES = ImmutableSet.of(
        "Running"
    );
    private static final ImmutableSet<String> CAMEL_K_RUNNING_STATES = ImmutableSet.of(
        "Running"
    );
    private final IntegrationResourceManager resourceManager;
    private final IntegrationProjectGenerator projectGenerator;
    private final VersionService versionService;

    public CamelKPublishHandler(OpenShiftService openShiftService,
                                IntegrationDao iDao,
                                IntegrationDeploymentDao idDao,
                                IntegrationProjectGenerator projectGenerator,
                                IntegrationPublishValidator validator,
                                IntegrationResourceManager resourceManager,
                                VersionService versionService) {
        super(openShiftService, iDao, idDao, validator);
        this.projectGenerator = projectGenerator;
        this.resourceManager = resourceManager;
        this.versionService = versionService;
    }

    @Override
    public Set<IntegrationDeploymentState> getTriggerStates() {
        return Collections.singleton(IntegrationDeploymentState.Published);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public StateUpdate execute(IntegrationDeployment integrationDeployment) {
        StateUpdate updateViaValidation = getValidator().validate(integrationDeployment);
        if (updateViaValidation != null) {
            return updateViaValidation;
        }

        CustomResourceDefinition integrationCRD = getOpenShiftService().getCRD(CAMEL_K_INTEGRATIONCRD_NAME)
            .orElseThrow(() -> new IllegalArgumentException("No Camel-k Integration CRD found for name: "+CAMEL_K_INTEGRATIONCRD_NAME));

        if (isBuildFailed(integrationDeployment, integrationCRD)){
            return new StateUpdate(IntegrationDeploymentState.Error, Collections.emptyMap(), "Error");
        }

        logInfo(integrationDeployment, "Build started: {}, isRunning: {}, Deployment ready: {}",
                isBuildStarted(integrationDeployment, integrationCRD), isRunning(integrationDeployment), isReady(integrationDeployment, integrationCRD));

        Map<String, String> stepsDone = new HashMap<>(integrationDeployment.getStepsDone());

        logInfo(integrationDeployment,"Creating Camel-K resource");

        io.syndesis.server.controller.integration.camelk.crd.Integration camelkIntegration = createCamelkIntegration(integrationDeployment);

        getOpenShiftService().createOrReplaceCR(integrationCRD,
                                                io.syndesis.server.controller.integration.camelk.crd.Integration.class,
                                                IntegrationList.class,
                                                DoneableIntegration.class,
                                                camelkIntegration);
        stepsDone.put("deploy", "camel-k");
        logInfo(integrationDeployment,"Camel-K resource created");

        return new StateUpdate(IntegrationDeploymentState.Pending, stepsDone);
    }

    private io.syndesis.server.controller.integration.camelk.crd.Integration createCamelkIntegration(IntegrationDeployment integrationDeployment) {
        final Integration integration = integrationDeployment.getSpec();
        prepareDeployment(integrationDeployment);

        Properties applicationProperties = projectGenerator.generateApplicationProperties(integration);

        String username = integrationDeployment.getUserId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration"));

        String integrationId = integrationDeployment.getIntegrationId().orElseThrow(() -> new IllegalStateException("IntegrationDeployment should have an integrationId"));
        String version = Integer.toString(integrationDeployment.getVersion());

        io.syndesis.server.controller.integration.camelk.crd.Integration result = new io.syndesis.server.controller.integration.camelk.crd.Integration();
        //add CR metadata
        result.getMetadata().setName(Names.sanitize(integrationId));
//        result.getMetadata().setResourceVersion(String.valueOf(integrationDeployment.getVersion()));
        result.getMetadata().setLabels(new HashMap<>());
        result.getMetadata().getLabels().put(OpenShiftService.INTEGRATION_ID_LABEL, Labels.validate(integrationId));
        result.getMetadata().getLabels().put(OpenShiftService.DEPLOYMENT_VERSION_LABEL, version);
        result.getMetadata().getLabels().put(OpenShiftService.USERNAME_LABEL, Labels.sanitize(username));
        result.getMetadata().setAnnotations(new HashMap<>());
        result.getMetadata().getAnnotations().put(OpenShiftService.INTEGRATION_NAME_ANNOTATION, integration.getName());
        result.getMetadata().getAnnotations().put(OpenShiftService.INTEGRATION_ID_LABEL, integrationId);
        result.getMetadata().getAnnotations().put(OpenShiftService.DEPLOYMENT_VERSION_LABEL, version);

        ImmutableIntegrationSpec.Builder integratinSpecBuilder = new IntegrationSpec.Builder();
        //add configuration properties
        integration.getConfiguredProperties().forEach((k, v) ->
            integratinSpecBuilder.addConfiguration(new ConfigurationSpec.Builder()
                                                            .type("property")
                                                            .value(k+"="+v)
                                                        .build()));
        //add application properties
        applicationProperties.stringPropertyNames().forEach(k ->
            integratinSpecBuilder.addConfiguration(new ConfigurationSpec.Builder()
                                                            .type("property")
                                                            .value(k+"="+applicationProperties.getProperty(k))
                                                        .build()));
        //add customizers
        integratinSpecBuilder.addConfiguration(new ConfigurationSpec.Builder()
            .type("property")
            .value("camel.k.customizer=metadata,logging")
            .build());
        result.setSpec(integratinSpecBuilder.build());

        //add dependencies
        getDependencies(integration).forEach( gav ->
                integratinSpecBuilder.addDependencies("mvn:"+gav.getId()));
        integratinSpecBuilder.addDependencies("mvn:io.syndesis.integration:integration-runtime-camelk:"+versionService.getVersion());


        String integrationJson = extractIntegrationJson(integration);
        logInfo(integration,"integration.json: {}", integrationJson);
        //TODO: add extensions as dependencies
        //TODO: add atlasmapFiles as dependencies

        integratinSpecBuilder.addSources(new SourceSpec.Builder()
                                            .content(integrationJson)
                                            .language("syndesis")
                                            .name(Names.sanitize(integrationId))
                                        .build());

        result.setSpec(integratinSpecBuilder.build());
        return result;
    }

    private Set<MavenGav> getDependencies(Integration integration){
        return resourceManager.collectDependencies(integration).stream()
            .filter(Dependency::isMaven)
            .map(Dependency::getId)
            .map(MavenGav::new)
//            .filter(ProjectGeneratorHelper::filterDefaultDependencies)
            .collect(Collectors.toCollection(TreeSet::new));
    }

    private String extractIntegrationJson(Integration fullIntegration) {
        Integration integration = resourceManager.sanitize(fullIntegration);
        ObjectWriter writer = Json.writer();
        try {

            return new String(writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValueAsBytes(integration), UTF_8);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot convert integration " + integration.getName() + " to JSON: " + e,e);
        }
    }

    private void prepareDeployment(IntegrationDeployment integrationDeployment) {
        setVersion(integrationDeployment);
//        deactivatePreviousDeployments(integrationDeployment);
    }

    @SuppressWarnings({"unchecked"})
    private Optional<String> getCamelkIntegrationPhase(IntegrationDeployment integrationDeployment, CustomResourceDefinition integrationCRD) {
        io.syndesis.server.controller.integration.camelk.crd.Integration camelkIntegration = ((io.syndesis.server.controller.integration.camelk.crd.Integration) getOpenShiftService().getCR(integrationCRD,
            io.syndesis.server.controller.integration.camelk.crd.Integration.class,
            IntegrationList.class,
            DoneableIntegration.class,
            Names.sanitize(integrationDeployment.getIntegrationId().orElseThrow(() -> new IllegalStateException("Couldn't find the user of the integration")))).get());
        return camelkIntegration == null ? Optional.empty() : Optional.of(camelkIntegration.getStatus().getPhase());
    }

    private boolean isBuildStarted(IntegrationDeployment integrationDeployment, CustomResourceDefinition integrationCRD) {
        logInfo(integrationDeployment, "isBuildStarted");
        return CAMEL_K_STARTED_STATES.contains(getCamelkIntegrationPhase(integrationDeployment, integrationCRD).orElse(null));

    }

    private boolean isBuildFailed(IntegrationDeployment integrationDeployment, CustomResourceDefinition integrationCRD) {
        logInfo(integrationDeployment, "isBuildFailed");
        return CAMEL_K_FAILED_STATES.contains(getCamelkIntegrationPhase(integrationDeployment, integrationCRD).orElse(null));
    }

    private boolean isReady(IntegrationDeployment integrationDeployment, CustomResourceDefinition integrationCRD) {
        logInfo(integrationDeployment, "isReady");
        return CAMEL_K_READY_STATES.contains(getCamelkIntegrationPhase(integrationDeployment, integrationCRD).orElse(null));
    }

    @Override
    protected boolean isRunning(IntegrationDeployment integrationDeployment) {
        CustomResourceDefinition integrationCRD = getOpenShiftService().getCRD(CAMEL_K_INTEGRATIONCRD_NAME)
            .orElseThrow(() -> new IllegalArgumentException("No Camel-k Integration CRD found for name: "+CAMEL_K_INTEGRATIONCRD_NAME));
        logInfo(integrationDeployment, "isRunning");
        return CAMEL_K_RUNNING_STATES.contains(getCamelkIntegrationPhase(integrationDeployment, integrationCRD).orElse(null));
    }
}
