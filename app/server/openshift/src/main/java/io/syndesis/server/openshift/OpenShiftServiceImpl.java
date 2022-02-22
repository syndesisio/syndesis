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
package io.syndesis.server.openshift;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigSpec;
import io.fabric8.openshift.api.model.DeploymentConfigSpecBuilder;
import io.fabric8.openshift.api.model.DeploymentConfigStatus;
import io.fabric8.openshift.api.model.DeploymentStrategy;
import io.fabric8.openshift.api.model.DeploymentTriggerPolicyBuilder;
import io.fabric8.openshift.api.model.DoneableDeploymentConfig;
import io.fabric8.openshift.api.model.ImageStreamTag;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteSpec;
import io.fabric8.openshift.api.model.User;
import io.fabric8.openshift.api.model.UserBuilder;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.dsl.DeployableScalableResource;
import io.syndesis.common.util.Names;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.server.openshift.OpenShiftConfigurationProperties.SchedulingConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.GodClass")
public class OpenShiftServiceImpl implements OpenShiftService {

    // Labels used for generated objects
    private static final Map<String, String> INTEGRATION_DEFAULT_LABELS = defaultLabels();

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenShiftServiceImpl.class);

    private static final String OPENSHIFT_PREFIX = "i-";

    private final OpenShiftConfigurationProperties config;

    private final NamespacedOpenShiftClient openShiftClient;

    public OpenShiftServiceImpl(final NamespacedOpenShiftClient openShiftClient, final OpenShiftConfigurationProperties config) {
        this.openShiftClient = openShiftClient;
        this.config = config;
    }

    @Override
    public String build(final String name, final DeploymentData deploymentData, final InputStream tarInputStream)
        throws InterruptedException {
        final String sName = openshiftName(name);
        ensureImageStreams(sName);
        ensureBuildConfig(sName, deploymentData, config.getBuilderImageStreamTag(),
            config.getImageStreamNamespace(), config.getBuildNodeSelector());
        final Build build = openShiftClient.buildConfigs().withName(sName).instantiateBinary()
            .fromInputStream(tarInputStream);
        final Build completed = waitForBuild(build, 10, TimeUnit.MINUTES);

        final String imageReference = completed.getStatus().getOutputDockerImageReference();
        final int tagIdx = imageReference.lastIndexOf(':');

        final String imageStreamTagName = sName + imageReference.substring(tagIdx);
        final ImageStreamTag imageStreamTag = openShiftClient.imageStreamTags().withName(imageStreamTagName).get();

        return imageStreamTag.getImage().getDockerImageReference();
    }

    @Override
    public ConfigMap createOrReplaceConfigMap(final ConfigMap configMap) {
        return openShiftClient.configMaps().createOrReplace(configMap);
    }

    @Override
    public List<HasMetadata> createOrReplaceCRD(final InputStream crdYamlStream) {
        return openShiftClient.load(crdYamlStream).createOrReplace();
    }

    @Override
    public void createOrReplaceSecret(final Secret secret) {
        openShiftClient.secrets().createOrReplace(secret);
    }

    @Override
    public boolean delete(final String name) {
        final String sName = openshiftName(name);

        LOGGER.debug("Delete {}", sName);

        // May not be exposed (resources not present)
        removeExposure(sName);

        final boolean removedImageStreams = removeImageStreams(sName);
        final boolean removedDeploymentConfig = removeDeploymentConfig(sName);
        final boolean removedSecret = removeSecret(sName);
        final boolean removeBuildConfig = removeBuildConfig(sName);

        return removedImageStreams && removedDeploymentConfig && removedSecret && removeBuildConfig;
    }

    @Override
    public String deploy(final String name, final DeploymentData deploymentData) {
        final String sanitizedName = openshiftName(name);
        LOGGER.debug("Deploy {}", sanitizedName);

        final int replicas = ensureDeploymentConfig(sanitizedName, deploymentData);
        ensureSecret(sanitizedName, deploymentData);
        ensureExposure(sanitizedName, deploymentData);

        final DeploymentConfig deployment = openShiftClient.deploymentConfigs().withName(sanitizedName).scale(replicas, true);

        return String.valueOf(deployment.getStatus().getLatestVersion());
    }

    @Override
    public boolean exists(final String name) {
        final String sName = openshiftName(name);
        return openShiftClient.deploymentConfigs().withName(sName).get() != null;
    }

    @Override
    public List<DeploymentConfig> getDeploymentsByLabel(final Map<String, String> labels) {
        return openShiftClient.deploymentConfigs().withLabels(labels).list().getItems();
    }

    @Override
    public Optional<String> getExposedHost(final String name) {
        final Route route = openShiftClient.routes().withName(openshiftName(name)).get();
        return Optional.ofNullable(route).flatMap(r -> Optional.ofNullable(r.getSpec())).map(RouteSpec::getHost);
    }

    @Override
    public boolean isBuildFailed(final String name) {
        return checkBuildStatus(name, "Error");
    }

    @Override
    public boolean isBuildStarted(final String name) {
        return checkBuildStatus(name, "Running");
    }

    @Override
    public boolean isDeploymentReady(final String name) {
        final String sName = openshiftName(name);
        final DeployableScalableResource<DeploymentConfig, DoneableDeploymentConfig> dc = openShiftClient.deploymentConfigs().withName(sName);

        return dc.get() != null && dc.isReady();
    }

    @Override
    public boolean isScaled(final String name, final int desiredMinimumReplicas, final Map<String, String> labels) {
        final List<DeploymentConfig> deploymentConfigs = getDeploymentsByLabel(labels);
        if (deploymentConfigs.isEmpty()) {
            return false;
        }

        final DeploymentConfig dc = deploymentConfigs.get(0);
        int allReplicas = 0;
        int availableReplicas = 0;
        if (dc != null && dc.getStatus() != null) {
            final DeploymentConfigStatus status = dc.getStatus();
            allReplicas = nullSafe(status.getReplicas());
            availableReplicas = nullSafe(status.getAvailableReplicas());
        }

        return desiredMinimumReplicas <= allReplicas && desiredMinimumReplicas <= availableReplicas;
    }

    @Override
    public boolean stop(final String name) {
        final DeployableScalableResource<DeploymentConfig, DoneableDeploymentConfig> dc = openShiftClient.deploymentConfigs().withName(openshiftName(name));
        final DeploymentConfig existing = dc.get();
        if (existing == null) {
            return false;
        }

        final Integer currentReplicas = existing.getSpec().getReplicas();

        final DeploymentConfig scaled = dc.scale(0, true);

        dc.edit()
            .editMetadata()
                .addToAnnotations(OpenShiftService.DEPLOYMENT_REPLICAS_ANNOTATION, String.valueOf(currentReplicas))
            .endMetadata()
         .done();

        return scaled.getStatus().getReplicas() == 0;
    }

    @Override
    public User whoAmI(final String username) {
        return new UserBuilder().withNewMetadata().withName(username).and().build();
    }

    private Container determineContainer(final DeploymentConfig existingDeploymentConfig, final String name,
        final DeploymentData deploymentData) {
        // preset variables, we don't allow modifying them
        final List<EnvVar> vars = new ArrayList<>(
            Arrays.asList(new EnvVar("LOADER_HOME", config.getIntegrationDataPath(), null),
                new EnvVar("AB_PROMETHEUS_ENABLE", "true", null),
                new EnvVar("AB_PROMETHEUS_JMX_EXPORTER_PORT", "9779", null),
                new EnvVar("AB_PROMETHEUS_JMX_EXPORTER_CONFIG", "/deployments/data/prometheus-config.yml",
                    null),
                new EnvVar("JAEGER_ENDPOINT", determineJaegerCollectorUri(), null),
                new EnvVar("JAEGER_TAGS", "integration.version=" + deploymentData.getVersion(), null),
                new EnvVar("JAEGER_SAMPLER_TYPE", "const", null),
                new EnvVar("JAEGER_SAMPLER_PARAM", "1", null)));

        final Set<String> presetVariables = vars.stream()
            .map(EnvVar::getName)
            .collect(Collectors.toSet());

        deploymentData.getEnvironment().stream()
            // no funny business like adding `LOADER_HOME` via UI
            .filter(v -> !presetVariables.contains(v.getName()))
            .forEach(v -> {
                vars.add(v);
                // variables added via UI will override variables set manually on the container
                presetVariables.add(v.getName());
            });

        final List<VolumeMount> volumeMounts = new ArrayList<>();
        volumeMounts.add(new VolumeMountBuilder()
            .withName("secret-volume")
            .withMountPath("/deployments/config")
            .withReadOnly(false)
            .build());

        final ContainerBuilder container;
        if (existingDeploymentConfig != null) {
            final DeploymentConfigSpec dcSpec = existingDeploymentConfig.getSpec();
            final PodTemplateSpec podTemplate = dcSpec.getTemplate();
            final PodSpec podSpec = podTemplate.getSpec();
            final Container existingContainer = podSpec.getContainers().stream()
                .filter(c -> name.equals(c.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Container with the name " + name + " could not be found on deployment config with the name: " + existingDeploymentConfig.getMetadata().getName()));

            existingContainer.getEnv().stream()
                // don't allow modifying preset variables
                .filter(v -> !presetVariables.contains(v.getName()))
                // ignore removed variables
                .filter(v -> !deploymentData.getRemovedEnvironment().contains(v.getName()))
                // add any variables added out of bands
                .forEach(vars::add);

            volumeMounts.addAll(existingContainer.getVolumeMounts().stream()
                // don't allow modifying the preset volume mount
                .filter(v -> !"secret-volume".equals(v.getName())).collect(Collectors.toList()));

            container = new ContainerBuilder(existingContainer);
        } else {
            container = new ContainerBuilder();
        }

        return container
            .withImage(deploymentData.getImage())
            .withImagePullPolicy("Always")
            .withName(name)
            // don't chain withEnv as every invocation overrides the previous
            // one, use var-args/collection instead
            .withEnv(vars)
            .withPorts(port("jolokia", 8778),
                port("metrics", 9779),
                port("management", 8081))
            .withVolumeMounts(volumeMounts)
            .withLivenessProbe(new ProbeBuilder()
                .withInitialDelaySeconds(config.getIntegrationLivenessProbeInitialDelaySeconds())
                .withNewHttpGet()
                    .withPath("/actuator/health")
                    .withNewPort(8081)
                .endHttpGet()
                .build())
            .build();
    }

    private DeploymentStrategy determineStrategy(final DeploymentConfig existingDeploymentConfig) {
        if (existingDeploymentConfig != null && existingDeploymentConfig.getSpec().getStrategy() != null) {
            return existingDeploymentConfig.getSpec().getStrategy();
        }

        return new DeploymentConfigSpecBuilder()
            .withNewStrategy()
                .withType("Recreate")
                .withNewResources()
                    .addToLimits("memory", new Quantity(config.getDeploymentMemoryLimitMi() + "Mi"))
                    .addToRequests("memory", new Quantity(config.getDeploymentMemoryRequestMi() + "Mi"))
                .endResources()
            .endStrategy()
            .buildStrategy();
    }

    private void ensureBuildConfig(final String name, final DeploymentData deploymentData, final String builderStreamTag,
        final String imageStreamNamespace, final Map<String, String> buildNodeSelector) {

        openShiftClient.buildConfigs()
            .withName(name)
            .createOrReplaceWithNew()
            .withNewMetadata()
                .withName(name)
                .addToAnnotations(deploymentData.getAnnotations())
                .addToLabels(deploymentData.getLabels())
                .addToLabels(INTEGRATION_DEFAULT_LABELS)
            .endMetadata()
            .withNewSpec()
                .withRunPolicy("SerialLatestOnly")
                .withNewSource()
                    .withType("Binary")
                .endSource()
                .withNewStrategy()
                    .withType("Source")
                    .withNewSourceStrategy()
                        .withNewFrom()
                            .withKind("ImageStreamTag")
                            .withName(builderStreamTag)
                            .withNamespace(imageStreamNamespace)
                        .endFrom()
                        .withIncremental(false)
                        .withEnv(new EnvVar("MAVEN_OPTS", config.getMavenOptions(), null),
                            new EnvVar("MAVEN_ARGS_APPEND", config.getAdditionalMavenArguments(), null),
                            new EnvVar("BUILD_LOGLEVEL", config.isDebug() ? "5" : "1", null),
                            new EnvVar("SCRIPT_DEBUG", config.isDebug() ? "true" : "false", null))
                    .endSourceStrategy()
                .endStrategy()
                .withNewOutput()
                    .withNewTo()
                        .withKind("ImageStreamTag")
                        .withName(name + ":" + deploymentData.getVersion())
                    .endTo()
                .endOutput()
                .withNodeSelector(buildNodeSelector)
            .endSpec()
            .done();
    }

    private int ensureDeploymentConfig(final String name, final DeploymentData deploymentData) {
        final SchedulingConfiguration scheduling = config.getScheduling();

        final DeploymentConfig existingDeploymentConfig = openShiftClient.deploymentConfigs().withName(name).get();

        final int replicas = determineNumberOfReplicas(existingDeploymentConfig);
        openShiftClient.deploymentConfigs().withName(name)
            .createOrReplaceWithNew()
                .editOrNewMetadata()
                    .withName(name)
                    .addToLabels(deploymentData.getLabels())
                    .addToLabels(INTEGRATION_DEFAULT_LABELS)
                    .addToAnnotations(deploymentData.getAnnotations())
                    .addToAnnotations(OpenShiftService.DEPLOYMENT_REPLICAS_ANNOTATION, String.valueOf(replicas))
                .endMetadata()
            .editOrNewSpec()
                .withReplicas(0)
                .addToSelector(INTEGRATION_NAME_LABEL, name)
                .withStrategy(determineStrategy(existingDeploymentConfig))
                .withRevisionHistoryLimit(0)
                .editOrNewTemplate()
                    .editOrNewMetadata()
                        .addToLabels(INTEGRATION_NAME_LABEL, name)
                        .addToLabels(COMPONENT_LABEL, "integration")
                        .addToLabels(INTEGRATION_DEFAULT_LABELS)
                        .addToLabels(deploymentData.getLabels())
                        .addToAnnotations(deploymentData.getAnnotations())
                        .addToAnnotations("prometheus.io/scrape", "true")
                        .addToAnnotations("prometheus.io/port", "9779")
                    .endMetadata()
                    .editOrNewSpec()
                        .withAffinity(scheduling.getAffinity())
                        .withTolerations(scheduling.getTolerations())
                        .withContainers(determineContainer(existingDeploymentConfig, name, deploymentData))
                        .withVolumes(determineVolumes(existingDeploymentConfig, name))
                    .endSpec()
                .endTemplate()
                .withTriggers(new DeploymentTriggerPolicyBuilder()
                    .withType("ImageChange")
                    .withNewImageChangeParams()
                        // set automatic to 'true' when not performing the deployments
                        // on our own
                        .withAutomatic(true)
                        .addToContainerNames(name)
                            .withNewFrom()
                                .withKind("ImageStreamTag")
                                .withName(name + ":" + deploymentData.getVersion())
                            .endFrom()
                        .endImageChangeParams()
                    .build(),
                new DeploymentTriggerPolicyBuilder().withType("ConfigChange").build())
            .endSpec()
            .done();

        return replicas;
    }

    private void ensureExposure(final String sanitizedName, final DeploymentData deploymentData) {
        final EnumSet<Exposure> exposure = deploymentData.getExposure();
        if (exposure == null || exposure.isEmpty()) {
            removeRoute(sanitizedName);
            removeService(sanitizedName);
        } else {
            if (exposure.contains(Exposure.SERVICE)) {
                ensureService(sanitizedName, deploymentData);
            } else {
                removeService(sanitizedName);
            }

            if (exposure.contains(Exposure.ROUTE)) {
                ensureRoute(sanitizedName, deploymentData);
            } else {
                removeRoute(sanitizedName);
            }
        }

    }

    private void ensureImageStreams(final String name) {
        LOGGER.debug("Create or Replace ImageStream {}", name);

        openShiftClient.imageStreams().withName(name)
            .createOrReplaceWithNew()
                .withNewMetadata()
                    .withName(name)
                    .addToLabels(INTEGRATION_DEFAULT_LABELS)
                .endMetadata()
            .done();
    }

    private void ensureRoute(final String name, final DeploymentData deploymentData) {
        openShiftClient.routes().withName(name)
            .createOrReplaceWithNew()
                .withNewMetadata()
                    .withName(name)
                    .addToAnnotations(deploymentData.getAnnotations())
                    .addToLabels(deploymentData.getLabels())
                .endMetadata()
                .withNewSpec()
                    .withNewTo()
                        .withKind("Service")
                        .withName(name)
                    .endTo()
                    .withNewTls()
                        .withTermination("edge")
                    .endTls()
                .endSpec()
            .done();
    }

    private void ensureSecret(final String name, final DeploymentData deploymentData) {
        final Map<String, String> secrets = deploymentData.getSecret();
        if (secrets.isEmpty()) {
            return;
        }

        openShiftClient.secrets()
            .withName(name)
            .createOrReplaceWithNew()
                .withNewMetadata()
                    .withName(name)
                    .addToAnnotations(deploymentData.getAnnotations())
                    .addToLabels(deploymentData.getLabels())
                .endMetadata()
                .withStringData(secrets)
            .done();
    }

    private void ensureService(final String name, final DeploymentData deploymentData) {
        final Map<String, String> labels = prepareServiceLabels(deploymentData);
        final Map<String, String> annotations = prepareServiceAnnotations(deploymentData);

        openShiftClient.services().withName(name)
            .createOrReplaceWithNew()
                .withNewMetadata()
                    .withName(name)
                    .addToAnnotations(annotations)
                    .addToLabels(labels)
                .endMetadata()
                .withNewSpec()
                    .addToSelector(INTEGRATION_NAME_LABEL, name)
                    .addNewPort()
                        .withName("http")
                        .withProtocol("TCP")
                        .withPort(INTEGRATION_SERVICE_PORT)
                            .withNewTargetPort(INTEGRATION_SERVICE_PORT)
                        .endPort()
                .endSpec()
            .done();
    }

    private boolean removeBuildConfig(final String projectName) {
        return openShiftClient.buildConfigs().withName(projectName)
            .withPropagationPolicy(DeletionPropagation.FOREGROUND).delete();
    }

    private boolean removeDeploymentConfig(final String projectName) {
        return openShiftClient.deploymentConfigs().withName(projectName).delete();
    }

    private boolean removeExposure(final String name) {
        final boolean res = removeRoute(name);
        return removeService(name) && res;
    }

    private boolean removeImageStreams(final String name) {
        LOGGER.debug("Remove ImageStream {}", name);

        return openShiftClient.imageStreams().withName(name).delete();
    }

    private boolean removeRoute(final String name) {
        return openShiftClient.routes().withName(name).delete();
    }

    private boolean removeSecret(final String projectName) {
        return openShiftClient.secrets().withName(projectName).delete();
    }

    private boolean removeService(final String name) {
        return openShiftClient.services().withName(name).delete();
    }

    private Build waitForBuild(final Build r, final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        final long end = System.currentTimeMillis() + timeUnit.toMillis(timeout);
        Build next = r;

        int retriesLeft = config.getMaximumRetries();
        while (System.currentTimeMillis() < end) {
            if (next.getStatus() != null && ("Complete".equals(next.getStatus().getPhase())
                || "Failed".equals(next.getStatus().getPhase()))) {
                return next;
            }
            try {
                next = openShiftClient.builds().inNamespace(next.getMetadata().getNamespace())
                    .withName(next.getMetadata().getName()).get();
            } catch (final KubernetesClientException e) {
                checkRetryPolicy(e, retriesLeft--);
            }
            Thread.sleep(config.getPollingInterval());
        }
        throw SyndesisServerException.launderThrowable(new TimeoutException("Timed out waiting for build completion."));
    }

    protected boolean checkBuildStatus(final String name, final String status) {
        final String sName = openshiftName(name);
        return !openShiftClient.builds().withLabel("openshift.io/build-config.name", sName).withField("status", status)
            .list().getItems().isEmpty();
    }

    static Map<String, String> defaultLabels() {
        final HashMap<String, String> labels = new HashMap<>();
        labels.put("syndesis.io/type", "integration");
        labels.put("syndesis.io/app", "syndesis");

        return Collections.unmodifiableMap(labels);
    }

    /**
     * Checks if Exception can be retried and if retries are left.
     */
    private static void checkRetryPolicy(final KubernetesClientException e, final int retries) {
        if (retries == 0) {
            throw new KubernetesClientException("Retries exhausted.", e);
        } else if (e.getCause() instanceof IOException) {
            LOGGER.warn("Got: {}. Retrying", e.getMessage());
        } else if (e.getStatus() != null && (e.getStatus().getCode() == 500 || e.getStatus().getCode() == 503)) {
            LOGGER.warn("Received HTTP {} from server. Retrying", e.getStatus().getCode());
        } else {
            throw e;
        }
    }

    private static String determineJaegerCollectorUri() {
        final String fromEnv = System.getenv("JAEGER_ENDPOINT");
        if (fromEnv != null) {
            return fromEnv;
        }

        return "http://syndesis-jaeger-collector:14268/api/traces";
    }

    private static int determineNumberOfReplicas(final DeploymentConfig existingDeploymentConfig) {
        if (existingDeploymentConfig == null) {
            return 1;
        }

        // make sure replicas is set to at least 1 or restore the previous
        // replica count if present
        final String deploymentReplicas = existingDeploymentConfig.getMetadata().getAnnotations().get(OpenShiftService.DEPLOYMENT_REPLICAS_ANNOTATION);

        if (deploymentReplicas != null) {
            return Integer.parseInt(deploymentReplicas);
        } else {
            final Integer configuredReplicas = existingDeploymentConfig.getSpec().getReplicas();
            return configuredReplicas == null ? 1 : configuredReplicas;
        }
    }

    private static List<Volume> determineVolumes(final DeploymentConfig existingDeploymentConfig, final String name) {
        final List<Volume> volumes = new ArrayList<>();
        volumes.add(new VolumeBuilder()
            .withName("secret-volume")
            .withNewSecret()
                .withSecretName(name)
            .endSecret()
            .build());

        if (existingDeploymentConfig != null) {
            final DeploymentConfigSpec dcSpec = existingDeploymentConfig.getSpec();
            final PodTemplateSpec podTemplate = dcSpec.getTemplate();
            final PodSpec podSpec = podTemplate.getSpec();

            volumes.addAll(podSpec.getVolumes().stream()
                // don't allow modifying the preset volume
                .filter(v -> !"secret-volume".equals(v.getName())).collect(Collectors.toList()));
        }

        return volumes;
    }

    private static int nullSafe(final Integer nr) {
        return nr != null ? nr : 0;
    }

    private static ContainerPort port(final String name, final int port) {
        return new ContainerPortBuilder()
            .withName(name)
            .withContainerPort(port)
            .build();
    }

    private static Map<String, String> prepareServiceAnnotations(final DeploymentData deploymentData) {
        final Map<String, String> annotations = new LinkedHashMap<>(deploymentData.getAnnotations());
        if (deploymentData.getExposure().contains(Exposure._3SCALE)) {
            annotations.put("discovery.3scale.net/scheme", "http");
            annotations.put("discovery.3scale.net/port", "8080");
            annotations.put("discovery.3scale.net/description-path", "/openapi.json");
        }

        return annotations;
    }

    private static Map<String, String> prepareServiceLabels(final DeploymentData deploymentData) {
        final Map<String, String> labels = new LinkedHashMap<>(deploymentData.getLabels());
        if (deploymentData.getExposure().contains(Exposure._3SCALE)) {
            labels.put("discovery.3scale.net", "true");
        }

        return labels;
    }

    protected static String openshiftName(final String name) {
        return OPENSHIFT_PREFIX + Names.sanitize(name);
    }
}
