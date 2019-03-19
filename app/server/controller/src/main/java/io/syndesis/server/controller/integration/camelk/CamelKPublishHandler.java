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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ResourceIdentifier;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.Labels;
import io.syndesis.common.util.Names;
import io.syndesis.integration.api.IntegrationProjectGenerator;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.integration.project.generator.mvn.MavenGav;
import io.syndesis.server.controller.StateChangeHandler;
import io.syndesis.server.controller.StateUpdate;
import io.syndesis.server.controller.integration.IntegrationPublishValidator;
import io.syndesis.server.controller.integration.camelk.crd.ConfigurationSpec;
import io.syndesis.server.controller.integration.camelk.crd.DataSpec;
import io.syndesis.server.controller.integration.camelk.crd.DoneableIntegration;
import io.syndesis.server.controller.integration.camelk.crd.ImmutableIntegrationSpec;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationList;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationSpec;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationTraitSpec;
import io.syndesis.server.controller.integration.camelk.crd.ResourceSpec;
import io.syndesis.server.controller.integration.camelk.crd.SourceSpec;
import io.syndesis.server.controller.integration.camelk.customizer.CamelKIntegrationCustomizer;
import io.syndesis.server.dao.IntegrationDao;
import io.syndesis.server.dao.IntegrationDeploymentDao;
import io.syndesis.server.endpoint.v1.VersionService;
import io.syndesis.server.openshift.OpenShiftService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Qualifier("camel-k")
@ConditionalOnProperty(value = "controllers.integration", havingValue = "camel-k")
public class CamelKPublishHandler extends BaseCamelKHandler implements StateChangeHandler {

    private final IntegrationResourceManager resourceManager;
    private final IntegrationProjectGenerator projectGenerator;
    private final VersionService versionService;
    private final List<CamelKIntegrationCustomizer> customizers;

    private boolean compress;

    public CamelKPublishHandler(
        OpenShiftService openShiftService,
        IntegrationDao iDao,
        IntegrationDeploymentDao idDao,
        IntegrationProjectGenerator projectGenerator,
        IntegrationPublishValidator validator,
        IntegrationResourceManager resourceManager,
        VersionService versionService,
        List<CamelKIntegrationCustomizer> customizers) {
        super(openShiftService, iDao, idDao, validator);
        this.projectGenerator = projectGenerator;
        this.resourceManager = resourceManager;
        this.versionService = versionService;
        this.customizers = customizers;

        // this should be taken from a configuration
        this.compress = false;
    }

    @Override
    public Set<IntegrationDeploymentState> getTriggerStates() {
        return Collections.singleton(IntegrationDeploymentState.Published);
    }

    @Override
    @SuppressWarnings("PMD.NPathComplexity")
    public StateUpdate execute(IntegrationDeployment integrationDeployment) {
        //
        // Validation
        //
        StateUpdate updateViaValidation = getValidator().validate(integrationDeployment);
        if (updateViaValidation != null) {
            return updateViaValidation;
        }

        if (!integrationDeployment.getUserId().isPresent()) {
            throw new IllegalStateException("Couldn't find the user of the integration");
        }
        if (!integrationDeployment.getIntegrationId().isPresent()) {
            throw new IllegalStateException("IntegrationDeployment should have an integrationId");
        }

        //get CRD
        CustomResourceDefinition integrationCRD = getCustomResourceDefinition();
        io.syndesis.server.controller.integration.camelk.crd.Integration camelkIntegration = getCamelkIntegration(integrationDeployment, integrationCRD);

        //update integration version and deactivate previous deployment for being checked
        setVersion(integrationDeployment);
        deactivatePreviousDeployments(integrationDeployment);

        if(camelkIntegration == null) {
            //if we reach here it means the camel k integration CR has not yet been created
            return createIntegration(integrationDeployment, integrationCRD);
        }

        //check if it was unpublished
        if (camelkIntegration.getMetadata()!=null &&
            camelkIntegration.getMetadata().getLabels()!=null &&
            integrationDeployment.getVersion()!= Integer.parseInt(camelkIntegration.getMetadata().getLabels().get(OpenShiftService.DEPLOYMENT_VERSION_LABEL))) {
            logInfo(integrationDeployment, "Unpublished");
            return new StateUpdate(IntegrationDeploymentState.Unpublished, Collections.emptyMap(), "Unpublished");
        }

        //check if build failed
        if (CamelKSupport.isBuildFailed(camelkIntegration)) {
            logInfo(integrationDeployment, "Build Failed");
            return new StateUpdate(IntegrationDeploymentState.Error, Collections.emptyMap(), "Build Failed");
        }

        //check other states
        if (camelkIntegration.getStatus() == null) {
            //Camelk Integration CR has been created but not yet picked up by camelk operator
            logInfo(integrationDeployment, "Build not yet started");
            return new StateUpdate(IntegrationDeploymentState.Pending, Collections.emptyMap(), "Build not yet started");
        }
        if (CamelKSupport.isBuildStarted(camelkIntegration)) {
            logInfo(integrationDeployment, "Build Started");
            return new StateUpdate(IntegrationDeploymentState.Pending, Collections.emptyMap(), "Build Started");
        }
        if (CamelKSupport.isRunning(camelkIntegration)) {
            logInfo(integrationDeployment, "Running");
            //update integrationDeployment state on DB
            updateDeploymentState(integrationDeployment, IntegrationDeploymentState.Published);
            return new StateUpdate(IntegrationDeploymentState.Published, Collections.emptyMap(), "Running");
        }

        //we should not reach here
        return new StateUpdate(IntegrationDeploymentState.Error, Collections.emptyMap(), "Unknown state try to handle from "+integrationDeployment.getCurrentState()+" to "+integrationDeployment.getTargetState()+" for integration is: "+integrationDeployment.getIntegrationId().orElse("Unknown id"));
    }

    @SuppressWarnings({"unchecked"})
    protected StateUpdate createIntegration(IntegrationDeployment integrationDeployment, CustomResourceDefinition integrationCRD) {

        logInfo(integrationDeployment,"Creating Camel-K resource");

        prepareDeployment(integrationDeployment);

        io.syndesis.server.controller.integration.camelk.crd.Integration camelkIntegration = createIntegrationCR(integrationDeployment);
        Secret camelkSecret = createIntegrationSecret(integrationDeployment);

        if (this.customizers != null && !this.customizers.isEmpty()) {
            for (CamelKIntegrationCustomizer customizer : this.customizers) {
                camelkIntegration = customizer.customize(integrationDeployment, camelkIntegration, camelkSecret);
            }
        }

        getOpenShiftService().createOrReplaceSecret(camelkSecret);
        getOpenShiftService().createOrReplaceCR(integrationCRD,
            io.syndesis.server.controller.integration.camelk.crd.Integration.class,
            IntegrationList.class,
            DoneableIntegration.class,
            camelkIntegration);
        logInfo(integrationDeployment,"Camel-K resource created "+camelkIntegration.getMetadata().getName());
        return new StateUpdate(IntegrationDeploymentState.Pending, Collections.emptyMap());
    }

    protected Secret createIntegrationSecret(IntegrationDeployment integrationDeployment) {
        final Integration integration = integrationDeployment.getSpec();

        Properties applicationProperties = projectGenerator.generateApplicationProperties(integration);

        //TODO: maybe add owner reference
        Secret secret = new SecretBuilder()
            .withNewMetadata()
                .withName(Names.sanitize(integration.getId().get()))
            .endMetadata()
            .addToStringData("application.properties", CamelKSupport.propsToString(applicationProperties))
            .build();

        return secret;
    }

    protected io.syndesis.server.controller.integration.camelk.crd.Integration createIntegrationCR(IntegrationDeployment integrationDeployment) {
        final Integration integration = integrationDeployment.getSpec();

        String username = integrationDeployment.getUserId().get();
        String integrationId = integrationDeployment.getIntegrationId().get();
        String version = Integer.toString(integrationDeployment.getVersion());
        String integrationDeploymentId = integrationDeployment.getId().get();

        io.syndesis.server.controller.integration.camelk.crd.Integration result = new io.syndesis.server.controller.integration.camelk.crd.Integration();
        //add CR metadata
        result.getMetadata().setName(CamelKSupport.integrationName(integration.getName()));
        result.getMetadata().setLabels(new HashMap<>());
        result.getMetadata().getLabels().put(OpenShiftService.INTEGRATION_ID_LABEL, Labels.sanitize(integrationId));
        result.getMetadata().getLabels().put(OpenShiftService.DEPLOYMENT_VERSION_LABEL, version);
        result.getMetadata().getLabels().put(OpenShiftService.USERNAME_LABEL, Labels.sanitize(username));
        result.getMetadata().getLabels().put(OpenShiftService.COMPONENT_LABEL, "integration");
        result.getMetadata().getLabels().put(OpenShiftService.INTEGRATION_NAME_LABEL, Labels.sanitize(integration.getName()));
        result.getMetadata().getLabels().put("syndesis.io/type", "integration");
        result.getMetadata().getLabels().put("syndesis.io/app", "syndesis");
        result.getMetadata().setAnnotations(new HashMap<>());
        result.getMetadata().getAnnotations().put(OpenShiftService.INTEGRATION_NAME_ANNOTATION, integration.getName());
        result.getMetadata().getAnnotations().put(OpenShiftService.INTEGRATION_ID_LABEL, integrationId);
        result.getMetadata().getAnnotations().put(OpenShiftService.DEPLOYMENT_VERSION_LABEL, version);
        result.getMetadata().getAnnotations().put("syndesis.io/deploy-id", integrationDeploymentId);
        result.getMetadata().getAnnotations().put("prometheus.io/port", "9779");
        result.getMetadata().getAnnotations().put("prometheus.io/scrape", "true");

        ImmutableIntegrationSpec.Builder integrationSpecBuilder = new IntegrationSpec.Builder();

        //add customizers
        //TODO: make all this configurable, where makes sense
        integrationSpecBuilder.addConfiguration(new ConfigurationSpec.Builder()
            .type("property")
            .value("camel.k.customizer=metadata,logging")
            .build());
        integrationSpecBuilder.addConfiguration(new ConfigurationSpec.Builder()
            .type("env")
            .value("AB_JMX_EXPORTER_CONFIG=/etc/camel/resources/prometheus-config.yml")
            .build());
        integrationSpecBuilder.addResources(new ResourceSpec.Builder()
            .dataSpec(new DataSpec.Builder()
                .name("prometheus-config.yml")
                .contentRef("syndesis-prometheus-agent-config")
                .contentKey("prometheus-config.yml")
                .build())
            .mountPath("/etc/camel/resources")
            .type("data")
            .build());
        integrationSpecBuilder.addConfiguration(new ConfigurationSpec.Builder()
            .type("secret")
            .value(Names.sanitize(integration.getId().get()))
            .build());
        integrationSpecBuilder.putTraits(
            "jolokia",
            new IntegrationTraitSpec.Builder()
                .putConfiguration("enabled", "true")
                .putConfiguration("port", "8778")
                .build());
        integrationSpecBuilder.putTraits(
            "prometheus",
            new IntegrationTraitSpec.Builder()
                .putConfiguration("enabled", "true")
                .putConfiguration("port", "9779")
                .putConfiguration("service-monitor", "false")
                .build());
        integrationSpecBuilder.putTraits(
            "camel",
            new IntegrationTraitSpec.Builder()
                .putConfiguration("version", versionService.getCamelVersion())
                .putConfiguration("runtime-version", versionService.getCamelkRuntimeVersion())
                .build());
        integrationSpecBuilder.putTraits(
            "owner",
            new IntegrationTraitSpec.Builder()
                .putConfiguration("target-labels", OpenShiftService.COMPONENT_LABEL+","
                                                    +OpenShiftService.INTEGRATION_ID_LABEL+","
                                                    +OpenShiftService.DEPLOYMENT_VERSION_LABEL+","
                                                    +OpenShiftService.USERNAME_LABEL+","
                                                    +OpenShiftService.INTEGRATION_NAME_LABEL+","
                                                    +"syndesis.io/type"+","
                                                    +"syndesis.io/app")
                .putConfiguration("target-annotations", "prometheus.io/port"+","
                                                    +"prometheus.io/scrape")
                .build());

        //add dependencies
        getDependencies(integration).forEach( gav -> integrationSpecBuilder.addDependencies("mvn:"+gav.getId()));
        integrationSpecBuilder.addDependencies("mvn:io.syndesis.integration:integration-runtime-camelk:"+versionService.getVersion());

        try {
            addMappingRules(integration, integrationSpecBuilder);
            addOpenAPIDefinition(integration, integrationSpecBuilder);
            addIntegrationSource(integration, integrationSpecBuilder);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        result.setSpec(integrationSpecBuilder.build());
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
        deactivatePreviousDeployments(integrationDeployment);
    }

    // ************************************
    //
    // Add resources to Integration Spec
    //
    // ************************************

    private void addIntegrationSource(Integration integration, ImmutableIntegrationSpec.Builder builder) throws IOException {
        final String json = extractIntegrationJson(integration);
        final String content = compress ? CamelKSupport.compress(json) : json;
        final String name = integration.getId().get();

        logInfo(integration,"integration.json: {}", content);

        builder.addSources(new SourceSpec.Builder()
            .dataSpec(new DataSpec.Builder()
                .compression(compress)
                .content(content)
                .name(Names.sanitize(name))
            .build())
            .language("syndesis")
            .build());
    }

    private void addMappingRules(Integration integration, ImmutableIntegrationSpec.Builder builder) throws IOException {
        final List<Flow> flows = integration.getFlows();
        for (int f = 0; f < flows.size(); f++) {
            final Flow flow = flows.get(f);
            final List<Step> steps = flow.getSteps();

            for (int s = 0; s < steps.size(); s++) {
                final Step step = steps.get(s);

                if (StepKind.mapper == step.getStepKind()) {
                    final Map<String, String> properties = step.getConfiguredProperties();
                    final String name = "mapping-flow-" + f + "-step-"  + s + ".json";
                    final String mapping = properties.get("atlasmapping");
                    final String content = compress ? CamelKSupport.compress(mapping) : mapping;

                    if (content != null) {
                        builder.addResources(
                            new ResourceSpec.Builder()
                                .dataSpec(new DataSpec.Builder()
                                    .compression(compress)
                                    .name(name)
                                    .content(content)
                                .build())
                                .type("data")
                            .build()
                        );
                    }
                }
            }
        }
    }

    private void addOpenAPIDefinition(Integration integration, ImmutableIntegrationSpec.Builder builder) throws IOException {
        // assuming that we have a single swagger definition for the moment
        Optional<ResourceIdentifier> rid = integration.getResources().stream().filter(Kind.OpenApi::sameAs).findFirst();
        if (!rid.isPresent()) {
            return;
        }

        final ResourceIdentifier openApiResource = rid.get();
        final Optional<String> maybeOpenApiResourceId = openApiResource.getId();
        if (!maybeOpenApiResourceId.isPresent()) {
            return;
        }

        final String openApiResourceId = maybeOpenApiResourceId.get();
        Optional<OpenApi> res = resourceManager.loadOpenApiDefinition(openApiResourceId);
        if (!res.isPresent()) {
            return;
        }

        final byte[] openApiBytes = res.get().getDocument();
        final String content = compress ? CamelKSupport.compress(openApiBytes) : new String(openApiBytes, UTF_8);
        final String name = openApiResource.name().orElse(maybeOpenApiResourceId.get());

        builder.addResources(
            new ResourceSpec.Builder()
                .dataSpec(new DataSpec.Builder()
                    .compression(compress)
                    .name(Names.sanitize(name))
                    .content(content)
                .build())
                .type("openapi")
                .build()
        );
    }

}
