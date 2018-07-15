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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.api.model.DeploymentConfigStatus;
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
        final String sName = openshiftName(name);
        LOGGER.debug("Deploy {}", sName);

        ensureDeploymentConfig(sName, deploymentData);
        ensureSecret(sName, deploymentData);
        ensureExposure(sName, deploymentData);

        DeploymentConfig deployment = openShiftClient.deploymentConfigs().withName(sName).deployLatest();
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

        return
            removeImageStreams(sName) &&
            removeDeploymentConfig(sName) &&
            removeSecret(sName) &&
            removeBuildConfig(sName);
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
            .map(d -> new DeploymentConfigBuilder(d).editSpec().withReplicas(desiredReplicas).endSpec().build())
            .findAny().ifPresent(d -> openShiftClient.deploymentConfigs().createOrReplace(d));
    }


    @Override
    public boolean isScaled(String name, int desiredReplicas, Map<String, String> labels) {
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
        return desiredReplicas == allReplicas && desiredReplicas == availableReplicas;
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

    protected void ensureDeploymentConfig(String name, DeploymentData deploymentData) {
        openShiftClient.deploymentConfigs().withName(name).createOrReplaceWithNew()
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
                            new EnvVar("AB_JMX_EXPORTER_CONFIG", "/tmp/src/prometheus-config.yml", null))
                        .addNewPort()
                            .withName("jolokia")
                            .withContainerPort(8778)
                        .endPort()
                        .addNewVolumeMount()
                            .withName("secret-volume")
                            .withMountPath("/deployments/config")
                            .withReadOnly(false)
                        .endVolumeMount()
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
            .endSpec()
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
        openShiftClient.secrets().withName(name).createOrReplaceWithNew()
            .withNewMetadata()
                .withName(name)
                .addToAnnotations(deploymentData.getAnnotations())
                .addToLabels(deploymentData.getLabels())
            .endMetadata()
            .withStringData(deploymentData.getSecret())
            .done();
    }


    private boolean removeSecret(String projectName) {
       return openShiftClient.secrets().withName(projectName).delete();
    }

    private void ensureExposure(String name, DeploymentData deploymentData) {
        Exposure exposure = deploymentData.getExposure();
        if (exposure == null || Exposure.NONE.equals(exposure)) {
            removeRoute(name);
            removeService(name);
        } else if (Exposure.DIRECT.equals(exposure)) {
            ensureService(name, deploymentData);
            ensureRoute(name, deploymentData);
        } else {
            LOGGER.error("Unsupported exposure method {}", exposure);
        }
    }

    private boolean removeExposure(String name) {
        boolean res = removeRoute(name);
        return removeService(name) && res;
    }

    private void ensureService(String name, DeploymentData deploymentData) {
        openShiftClient.services().withName(name).createOrReplaceWithNew()
            .withNewMetadata()
                .withName(name)
                .addToAnnotations(deploymentData.getAnnotations())
                .addToLabels(deploymentData.getLabels())
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

    private static Map<String, String> defaultLabels() {
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

}
