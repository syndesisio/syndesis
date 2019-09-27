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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.api.model.DeploymentConfigStatus;
import io.fabric8.openshift.api.model.DeploymentTriggerPolicyBuilder;
import io.fabric8.openshift.api.model.DoneableDeploymentConfig;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteSpec;
import io.fabric8.openshift.api.model.User;
import io.fabric8.openshift.api.model.UserBuilder;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.syndesis.common.util.Names;
import io.syndesis.common.util.SyndesisServerException;

@SuppressWarnings({"PMD.BooleanGetMethodName", "PMD.LocalHomeNamingConvention", "PMD.GodClass"})
public class OpenShiftServiceImpl implements OpenShiftService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenShiftServiceImpl.class);

    private static final String OPENSHIFT_PREFIX = "i-";

    // Labels used for generated objects
    private static final Map<String, String> INTEGRATION_DEFAULT_LABELS = defaultLabels();

    private final NamespacedOpenShiftClient openShiftClient;
    private final OpenShiftConfigurationProperties config;

    public OpenShiftServiceImpl(NamespacedOpenShiftClient openShiftClient, OpenShiftConfigurationProperties config) {
        this.openShiftClient = openShiftClient;
        this.config = config;
    }

    @Override
    public String build(String name, DeploymentData deploymentData, InputStream tarInputStream) throws InterruptedException {
        final String sName = openshiftName(name);
        ensureImageStreams(sName);
        ensureBuildConfig(sName, deploymentData, this.config.getBuilderImageStreamTag(), this.config.getImageStreamNamespace(), this.config.getBuildNodeSelector());
        Build build = openShiftClient.buildConfigs().withName(sName)
                       .instantiateBinary()
                       .fromInputStream(tarInputStream);
        Build complete = waitForBuild(build, 10, TimeUnit.MINUTES);
        if (complete != null && complete.getStatus() != null) {
            return build.getStatus().getOutputDockerImageReference();
        } else {
            return null;
        }
    }

    @Override
    public String deploy(String name, DeploymentData deploymentData) {
        final String sanitizedName = openshiftName(name);
        LOGGER.debug("Deploy {}", sanitizedName);

        ensureDeploymentConfig(sanitizedName, deploymentData);
        ensureSecret(sanitizedName, deploymentData);
        ensureExposure(sanitizedName, deploymentData);

        DeploymentConfig deployment = openShiftClient.deploymentConfigs().withName(sanitizedName).deployLatest();
        return String.valueOf(deployment.getStatus().getLatestVersion());
    }

    @Override
    public boolean isDeploymentReady(String name) {
        String sName = openshiftName(name);
        return openShiftClient.deploymentConfigs().withName(sName).isReady();
    }

    @Override
    public boolean delete(String name) {
        final String sName = openshiftName(name);

        LOGGER.debug("Delete {}", sName);

        // May not be exposed (resources not present)
        removeExposure(sName);

        final boolean removedImageStreams = removeImageStreams(sName);
        final boolean removedDeploymentConfig = removeDeploymentConfig(sName);
        final boolean removedSecret = removeSecret(sName);
        final boolean removeBuildConfig = removeBuildConfig(sName);

        return
            removedImageStreams &&
            removedDeploymentConfig &&
            removedSecret &&
            removeBuildConfig;
    }

    @Override
    public boolean exists(String name) {
        String sName = openshiftName(name);
        return openShiftClient.deploymentConfigs().withName(sName).get() != null;
    }

    @Override
    public void scale(String name, Map<String, String> labels, int desiredReplicas, long amount, TimeUnit timeUnit) throws InterruptedException {
        String sName = openshiftName(name);
        getDeploymentsByLabel(labels)
            .stream()
            .filter(d -> d.getMetadata().getName().equals(sName))
            .map(d -> new DeploymentConfigBuilder(d)
                    // record the previous, possibly user defined custom number of replicas
                    .editSpec()
                        .withReplicas(desiredReplicas)
                        .editTemplate()
                            .editMetadata()
                                .addToAnnotations(OpenShiftService.DEPLOYMENT_REPLICAS_ANNOTATION, d.getSpec().getReplicas().toString())
                            .endMetadata()
                        .endTemplate()
                    .endSpec()
                    .build())
            .findAny().ifPresent(d -> openShiftClient.deploymentConfigs().createOrReplace(d));
    }


    @Override
    public boolean isScaled(String name, int desiredMinimumReplicas, Map<String, String> labels) {
        List<DeploymentConfig> deploymentConfigs = getDeploymentsByLabel(labels);
        if (deploymentConfigs.isEmpty()) {
          return false;
        }


        DeploymentConfig dc = deploymentConfigs.get(0);
        int allReplicas = 0;
        int availableReplicas = 0;
        if (dc != null && dc.getStatus() != null) {
            DeploymentConfigStatus status = dc.getStatus();
            allReplicas = nullSafe(status.getReplicas());
            availableReplicas = nullSafe(status.getAvailableReplicas());
        }

        return desiredMinimumReplicas <= allReplicas && desiredMinimumReplicas <= availableReplicas;
    }

    @Override
    public boolean isBuildStarted(String name) {
        return checkBuildStatus(name, "Running");
    }

    @Override
    public boolean isBuildFailed(String name) {
        return checkBuildStatus(name, "Error");
    }

    protected boolean checkBuildStatus(String name, String status){
        String sName = openshiftName(name);
        return !openShiftClient.builds()
            .withLabel("openshift.io/build-config.name", sName)
            .withField("status", status)
            .list().getItems().isEmpty();
    }

    @Override
    public List<DeploymentConfig> getDeploymentsByLabel(Map<String, String> labels) {
        return openShiftClient.deploymentConfigs().withLabels(labels).list().getItems();
    }

    @Override
    public User whoAmI(String username) {
        return new UserBuilder().withNewMetadata().withName(username).and().build();
    }

    private int nullSafe(Integer nr) {
        return nr != null ? nr : 0;
    }

    // ***********************
    // Image Stream
    // ***********************

    private void ensureImageStreams(String name) {
        LOGGER.debug("Create or Replace ImageStream {}", name);

        openShiftClient.imageStreams().withName(name).createOrReplaceWithNew()
            .withNewMetadata()
                .withName(name)
                .addToLabels(INTEGRATION_DEFAULT_LABELS)
            .endMetadata()
            .done();
    }

    private boolean removeImageStreams(String name) {
        LOGGER.debug("Remove ImageStream {}", name);

        return openShiftClient.imageStreams().withName(name).delete();
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    protected void ensureDeploymentConfig(String name, DeploymentData deploymentData) {
        // check if deployment config exists
        final DoneableDeploymentConfig deploymentConfig;
        final DeploymentConfig oldConfig = openShiftClient.deploymentConfigs().withName(name).get();
        if (oldConfig != null) {
            // make sure replicas is set to at least 1 or restore the previous replica count if present
            final PodTemplateSpec oldTemplate = oldConfig.getSpec().getTemplate();
            final String deploymentReplicas = oldTemplate.getMetadata()
                    .getAnnotations().get(OpenShiftService.DEPLOYMENT_REPLICAS_ANNOTATION);
            Integer replicas = deploymentReplicas != null ? Integer.valueOf(deploymentReplicas) : oldConfig.getSpec().getReplicas();

            // environment variables are stored in a list, so remove duplicates manually before patching
            final EnvVar[] vars = { new EnvVar("LOADER_HOME", config.getIntegrationDataPath(), null),
                    new EnvVar("AB_JMX_EXPORTER_CONFIG", "/tmp/src/prometheus-config.yml", null),
                    new EnvVar("JAEGER_ENDPOINT", "http://syndesis-jaeger-collector:14268/api/traces", null),
                    new EnvVar("JAEGER_TAGS", "integration.version="+deploymentData.getVersion(), null),
                    new EnvVar("JAEGER_SAMPLER_TYPE", "const", null),
                    new EnvVar("JAEGER_SAMPLER_PARAM", "1", null) };
            final Map<String, EnvVar> envVarMap = new HashMap<>();
            for (EnvVar var : vars) {
                envVarMap.put(var.getName(), var);
            }
            final Container container = oldTemplate.getSpec().getContainers().get(0);
            final List<EnvVar> envVars = container.getEnv().stream()
                    .map(e -> envVarMap.containsKey(e.getName()) ? envVarMap.remove(e.getName()) : e)
                    .collect(Collectors.toList());
            // add missing vars
            envVars.addAll(envVarMap.values());

            // edit ports to avoid duplicate or missing ports
            final ContainerPort[] ports = {
                new ContainerPort(8778, null, null, "jolokia", null),
                new ContainerPort(9779, null, null, "metrics", null),
                new ContainerPort(8081, null, null, "management", null)
            };
            final Map<String, ContainerPort> portMap = new HashMap<>();
            for (ContainerPort port : ports) {
                portMap.put(port.getName(), port);
            }
            final List<ContainerPort> newPorts = container.getPorts().stream()
                    .map(p -> portMap.containsKey(p.getName()) ? portMap.remove(p.getName()) : p)
                    .collect(Collectors.toList());
            // add missing ports
            newPorts.addAll(portMap.values());

            deploymentConfig = openShiftClient.deploymentConfigs().withName(name).edit()
                    .editMetadata()
                        .withName(name)
                        .addToAnnotations(deploymentData.getAnnotations())
                        .addToLabels(deploymentData.getLabels())
                        .addToLabels(INTEGRATION_DEFAULT_LABELS)
                    .endMetadata()
                    .editSpec()
                        // if not set to more than 1, force replicas to 1 to start the integration pod
                        .withReplicas(replicas != null && replicas > 1 ? replicas : 1)
                        .addToSelector(INTEGRATION_NAME_LABEL, name)
                        .withRevisionHistoryLimit(0)
                        .editTemplate()
                            .editMetadata()
                                .addToLabels(INTEGRATION_NAME_LABEL, name)
                                .addToLabels(COMPONENT_LABEL, "integration")
                                .addToLabels(INTEGRATION_DEFAULT_LABELS)
                                .addToLabels(deploymentData.getLabels())
                                .addToAnnotations(deploymentData.getAnnotations())
                                .addToAnnotations("prometheus.io/scrape", "true")
                                .addToAnnotations("prometheus.io/port", "9779")
                            .endMetadata()
                            .editSpec()
                                .editFirstContainer()
                                    .withImage(deploymentData.getImage())
                                    .withImagePullPolicy("Always")
                                    .withName(name)
                                    .withEnv(envVars)
                                    .withPorts( newPorts)
                                    .editMatchingVolumeMount(v -> "secret-volume".equals(v.getName()))
                                        .withMountPath("/deployments/config")
                                        .withReadOnly(false)
                                    .endVolumeMount()
                                    .withLivenessProbe(new ProbeBuilder()
                                            .withInitialDelaySeconds(config.getIntegrationLivenessProbeInitialDelaySeconds())
                                            .withNewHttpGet()
                                            .withPath("/health")
                                            .withNewPort(8081)
                                            .endHttpGet()
                                            .build())
                                .endContainer()
                                .editMatchingVolume(v -> "secret-volume".equals(v.getName()))
                                    .withNewSecret()
                                        .withSecretName(name)
                                    .endSecret()
                                .endVolume()
                            .endSpec()
                        .endTemplate()
                        .withTriggers(
                            new DeploymentTriggerPolicyBuilder()
                                .withType("ImageChange")
                                .withNewImageChangeParams()
                                // set automatic to 'true' when not performing the deployments on our own
                                .withAutomatic(true)
                                .addToContainerNames(name)
                                .withNewFrom()
                                .withKind("ImageStreamTag")
                                .withName(name + ":" + deploymentData.getVersion())
                                .endFrom()
                                .endImageChangeParams()
                                .build(),
                            new DeploymentTriggerPolicyBuilder()
                                .withType("ConfigChange")
                                .build())
                    .endSpec();
        } else {
            deploymentConfig = openShiftClient.deploymentConfigs().withName(name).createNew()
                    .withNewMetadata()
                        .withName(name)
                        .addToAnnotations(deploymentData.getAnnotations())
                        .addToLabels(deploymentData.getLabels())
                        .addToLabels(INTEGRATION_DEFAULT_LABELS)
                    .endMetadata()
                    .withNewSpec()
                        .withReplicas(1)
                        .addToSelector(INTEGRATION_NAME_LABEL, name)
                        .withNewStrategy()
                            .withType("Recreate")
                            .withNewResources()
                            .addToLimits("memory", new Quantity(config.getDeploymentMemoryLimitMi()  + "Mi"))
                            .addToRequests("memory", new Quantity(config.getDeploymentMemoryRequestMi() +  "Mi"))
                            .endResources()
                        .endStrategy()
                        .withRevisionHistoryLimit(0)
                        .withNewTemplate()
                            .withNewMetadata()
                                .addToLabels(INTEGRATION_NAME_LABEL, name)
                                .addToLabels(COMPONENT_LABEL, "integration")
                                .addToLabels(INTEGRATION_DEFAULT_LABELS)
                                .addToLabels(deploymentData.getLabels())
                                .addToAnnotations(deploymentData.getAnnotations())
                                .addToAnnotations("prometheus.io/scrape", "true")
                                .addToAnnotations("prometheus.io/port", "9779")
                            .endMetadata()
                            .withNewSpec()
                                .addNewContainer()
                                    .withImage(deploymentData.getImage())
                                    .withImagePullPolicy("Always")
                                    .withName(name)
                                    // don't chain withEnv as every invocation overrides the previous one, use var-args instead
                                    .withEnv(
                                            new EnvVar("LOADER_HOME", config.getIntegrationDataPath(), null),
                                            new EnvVar("AB_JMX_EXPORTER_CONFIG", "/tmp/src/prometheus-config.yml", null),
                                            new EnvVar("JAEGER_ENDPOINT", "http://syndesis-jaeger-collector:14268/api/traces", null),
                                            new EnvVar("JAEGER_TAGS", "integration.version="+deploymentData.getVersion(), null),
                                            new EnvVar("JAEGER_SAMPLER_TYPE", "const", null),
                                            new EnvVar("JAEGER_SAMPLER_PARAM", "1", null))
                                    .addNewPort()
                                        .withName("jolokia")
                                        .withContainerPort(8778)
                                    .endPort()
                                    .addNewPort()
                                        .withName("metrics")
                                        .withContainerPort(9779)
                                    .endPort()
                                    .addNewPort()
                                        .withName("management")
                                        .withContainerPort(8081)
                                    .endPort()
                                    .addNewVolumeMount()
                                        .withName("secret-volume")
                                        .withMountPath("/deployments/config")
                                        .withReadOnly(false)
                                    .endVolumeMount()
                                    .withLivenessProbe(new ProbeBuilder()
                                            .withInitialDelaySeconds(config.getIntegrationLivenessProbeInitialDelaySeconds())
                                            .withNewHttpGet()
                                            .withPath("/health")
                                            .withNewPort(8081)
                                            .endHttpGet()
                                            .build())
                                .endContainer()
                                .addNewVolume()
                                    .withName("secret-volume")
                                    .withNewSecret()
                                        .withSecretName(name)
                                    .endSecret()
                                .endVolume()
                            .endSpec()
                        .endTemplate()
                        .addNewTrigger()
                            .withType("ImageChange")
                            .withNewImageChangeParams()
                                // set automatic to 'true' when not performing the deployments on our own
                                .withAutomatic(true)
                                .addToContainerNames(name)
                                .withNewFrom()
                                .withKind("ImageStreamTag")
                                .withName(name + ":" + deploymentData.getVersion())
                                .endFrom()
                            .endImageChangeParams()
                        .endTrigger()
                        .addNewTrigger()
                            .withType("ConfigChange")
                        .endTrigger()
                    .endSpec();
        }

        deploymentConfig
            .done();
    }

    private boolean removeDeploymentConfig(String projectName) {
        return openShiftClient.deploymentConfigs().withName(projectName).delete();
    }

    private void ensureBuildConfig(String name, DeploymentData deploymentData, String builderStreamTag, String imageStreamNamespace, Map<String, String> buildNodeSelector) {
        openShiftClient.buildConfigs().withName(name).createOrReplaceWithNew()
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
                    // TODO: This environment setup needs to be externalized into application.properties
                    // https://github.com/syndesisio/syndesis-rest/issues/682
                    .withEnv(
                        new EnvVar("MAVEN_OPTS", config.getMavenOptions(), null),
                        new EnvVar("MAVEN_ARGS_APPEND", config.getAdditionalMavenArguments(), null),
                        new EnvVar("BUILD_LOGLEVEL", config.isDebug() ? "5" : "1", null)
                    )
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

    private boolean removeBuildConfig(String projectName) {
        return openShiftClient.buildConfigs().withName(projectName).delete();
    }

    private void ensureSecret(String name, DeploymentData deploymentData) {
        final Map<String, String> secrets = deploymentData.getSecret();
        if (secrets.isEmpty()) {
            return;
        }

        openShiftClient.secrets().withName(name).createOrReplaceWithNew()
            .withNewMetadata()
                .withName(name)
                .addToAnnotations(deploymentData.getAnnotations())
                .addToLabels(deploymentData.getLabels())
            .endMetadata()
            .withStringData(secrets)
            .done();
    }


    private boolean removeSecret(String projectName) {
       return openShiftClient.secrets().withName(projectName).delete();
    }

    private void ensureExposure(String sanitizedName, DeploymentData deploymentData) {
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

    private boolean removeExposure(String name) {
        boolean res = removeRoute(name);
        return removeService(name) && res;
    }

    private void ensureService(String name, DeploymentData deploymentData) {
        final Map<String, String> labels = prepareServiceLabels(deploymentData);
        final Map<String, String> annotations = prepareServiceAnnotations(deploymentData);

        openShiftClient.services().withName(name).createOrReplaceWithNew()
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

    static Map<String, String> prepareServiceLabels(final DeploymentData deploymentData) {
        final Map<String, String> labels = new LinkedHashMap<>(deploymentData.getLabels());
        if (deploymentData.getExposure().contains(Exposure._3SCALE)) {
            labels.put("discovery.3scale.net", "true");
        }

        return labels;
    }

    static Map<String, String> prepareServiceAnnotations(final DeploymentData deploymentData) {
        final Map<String, String> annotations = new LinkedHashMap<>(deploymentData.getAnnotations());
        if (deploymentData.getExposure().contains(Exposure._3SCALE)) {
            annotations.put("discovery.3scale.net/scheme", "http");
            annotations.put("discovery.3scale.net/port", "8080");
            annotations.put("discovery.3scale.net/description-path", "/openapi.json");
        }

        return annotations;
    }

    private boolean removeService(String name) {
        return openShiftClient.services().withName(name).delete();
    }

    private void ensureRoute(String name, DeploymentData deploymentData) {
        openShiftClient.routes().withName(name).createOrReplaceWithNew()
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

    private boolean removeRoute(String name) {
        return openShiftClient.routes().withName(name).delete();
    }

    private Build waitForBuild(Build r, long timeout, TimeUnit timeUnit) throws InterruptedException {
        long end = System.currentTimeMillis() + timeUnit.toMillis(timeout);
        Build next = r;

        int retriesLeft = config.getMaximumRetries();
        while ( System.currentTimeMillis() < end) {
            if (next.getStatus() != null && ("Complete".equals(next.getStatus().getPhase()) || "Failed".equals(next.getStatus().getPhase()))) {
                return next;
            }
            try {
                next = openShiftClient.builds().inNamespace(next.getMetadata().getNamespace()).withName(next.getMetadata().getName()).get();
            } catch (KubernetesClientException e) {
                checkRetryPolicy(e, retriesLeft--);
            }
            Thread.sleep(config.getPollingInterval());
        }
        throw SyndesisServerException.launderThrowable(new TimeoutException("Timed out waiting for build completion."));
    }

    /**
     * Checks if Excpetion can be retried and if retries are left.
     * @param e
     * @param retries
     */
    private static void checkRetryPolicy(KubernetesClientException e, int retries) {
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

    protected static String openshiftName(String name) {
        return OPENSHIFT_PREFIX + Names.sanitize(name);
    }

    static Map<String, String> defaultLabels() {
        final HashMap<String, String> labels = new HashMap<String, String>();
        labels.put("syndesis.io/type", "integration");
        labels.put("syndesis.io/app", "syndesis");

        return Collections.unmodifiableMap(labels);
    }

    @Override
    public Optional<String> getExposedHost(String name) {
        Route route = openShiftClient.routes().withName(openshiftName(name)).get();
        return Optional.ofNullable(route)
            .flatMap(r -> Optional.ofNullable(r.getSpec()))
            .map(RouteSpec::getHost);
    }

    @Override
    public List<HasMetadata> createOrReplaceCRD(InputStream crdYamlStream){
       return openShiftClient.load(crdYamlStream).createOrReplace();
    }

    @Override
    public CustomResourceDefinition createOrReplaceCRD(CustomResourceDefinition crd){
        return openShiftClient.customResourceDefinitions().createOrReplace(crd);
    }

    @Override
    public Optional<CustomResourceDefinition> getCRD(String crdName){
        CustomResourceDefinition result = openShiftClient.customResourceDefinitions().withName(crdName).get();
        return result != null ? Optional.of(result) : Optional.empty();
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T extends HasMetadata, L extends KubernetesResourceList<T>, D extends Doneable<T>> boolean deleteCR(CustomResourceDefinition crd, Class<T> resourceType, Class<L> resourceListType, Class<D> doneableResourceType, String customResourceName){
        return deleteCR(crd, resourceType, resourceListType, doneableResourceType, customResourceName, false);
    }

    @Override
    public <T extends HasMetadata, L extends KubernetesResourceList<T>, D extends Doneable<T>> Watch watchCR(CustomResourceDefinition crd, Class<T> resourceType, Class<L> resourceListType, Class<D> doneableResourceType, BiConsumer<Watcher.Action,T> watcher){
        return getCRDClient(crd, resourceType, resourceListType, doneableResourceType).inNamespace(config.getNamespace()).watch(new Watcher<T>() {
            @Override
            public void eventReceived(Action action, T t) {
                watcher.accept(action, t);
            }

            @Override
            public void onClose(KubernetesClientException e) {
                LOGGER.info("Closing watcher "+this+" on crd "+crd.getMetadata().getName(), e);
            }
        });
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T extends HasMetadata, L extends KubernetesResourceList<T>, D extends Doneable<T>> boolean deleteCR(CustomResourceDefinition crd, Class<T> resourceType, Class<L> resourceListType, Class<D> doneableResourceType, String customResourceName, boolean cascading){
        return getCRDClient(crd, resourceType, resourceListType, doneableResourceType).inNamespace(config.getNamespace()).withName(customResourceName)
            .cascading(cascading)
            .delete();
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T extends HasMetadata, L extends KubernetesResourceList<T>, D extends Doneable<T>> T createOrReplaceCR(CustomResourceDefinition crd, Class<T> resourceType, Class<L> resourceListType, Class<D> doneableResourceType, T customResource){
        return getCRDClient(crd, resourceType, resourceListType, doneableResourceType).inNamespace(config.getNamespace()).createOrReplace(customResource);
    }

    @Override
    public <T extends HasMetadata, L extends KubernetesResourceList<T>, D extends Doneable<T>> T getCR(CustomResourceDefinition crd, Class<T> resourceType, Class<L> resourceListType, Class<D> doneableResourceType, String customResourceName){
        return getCRDClient(crd, resourceType, resourceListType, doneableResourceType).inNamespace(config.getNamespace()).withName(customResourceName).get();
    }

    private <T extends HasMetadata, L extends KubernetesResourceList<T>, D extends Doneable<T>> MixedOperation<T, L, D, Resource<T, D>> getCRDClient(CustomResourceDefinition crd, Class<T> resourceType, Class<L> resourceListType, Class<D> doneableResourceType) {
        return openShiftClient.customResources(crd, resourceType, resourceListType, doneableResourceType);
    }

    @Override
    public <T extends HasMetadata, L extends KubernetesResourceList<T>, D extends Doneable<T>> List<T> getCRBylabel(CustomResourceDefinition crd, Class<T> resourceType, Class<L> resourceListType, Class<D> doneableResourceType, Map<String, String> labels){
        return getCRDClient(crd, resourceType, resourceListType, doneableResourceType).inNamespace(config.getNamespace()).withLabels(labels).list().getItems();
    }

    @Override
    public void createOrReplaceSecret(Secret secret) {
        openShiftClient.secrets().createOrReplace(secret);
    }

    @Override
    public ConfigMap createOrReplaceConfigMap(ConfigMap configMap){
        return openShiftClient.configMaps().createOrReplace(configMap);
    }
}
