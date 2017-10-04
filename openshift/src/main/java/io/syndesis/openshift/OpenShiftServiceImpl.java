/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.openshift;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigStatus;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.syndesis.core.Names;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

public class OpenShiftServiceImpl implements OpenShiftService {

    private final NamespacedOpenShiftClient openShiftClient;
    private final OpenShiftConfigurationProperties config;

    public OpenShiftServiceImpl(NamespacedOpenShiftClient openShiftClient, OpenShiftConfigurationProperties config) {
        this.openShiftClient = openShiftClient;
        this.config = config;
    }

    @Override
    public void create(String name, DeploymentData deploymentData) {
        String sName = Names.sanitize(name);
        DockerImage builderImage = new DockerImage(this.config.getBuilderImage());
        ensureImageStreams(sName, deploymentData, builderImage);
        ensureDeploymentConfig(sName, deploymentData, this.config.getIntegrationServiceAccount());
        ensureSecret(sName, deploymentData);
        ensureBuildConfig(sName, deploymentData, builderImage);
    }

    @Override
    public void build(String name, Path runtimeDir) throws IOException {
        String sName = Names.sanitize(name);
        openShiftClient.buildConfigs().withName(sName)
                       .instantiateBinary()
                       .fromInputStream(createTarInputStream(runtimeDir));
    }

    private InputStream createTarInputStream(Path runtimeDir) throws IOException {
        PipedInputStream is = new PipedInputStream();
        PipedOutputStream os = new PipedOutputStream(is);
        TarArchiveOutputStream tos = new TarArchiveOutputStream(os);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        addFileToTar(tos, runtimeDir.toFile(), "");
        return is;
    }


    private void addFileToTar(TarArchiveOutputStream tos, File toAdd, String base) throws IOException {
        String entryName = base + toAdd.getName();
        TarArchiveEntry tarEntry = new TarArchiveEntry(toAdd, entryName);
        tos.putArchiveEntry(tarEntry);

        if (toAdd.isFile()) {
            Files.copy(toAdd.toPath(), tos);
            tos.closeArchiveEntry();
        } else {
            tos.closeArchiveEntry();
            File[] entries = toAdd.listFiles();
            if (entries != null) {
                for (File child : entries) {
                    addFileToTar(tos, child, entryName + "/");
                }
            }
        }
    }

    @Override
    public boolean delete(String name) {
        String sName = Names.sanitize(name);
        return
            removeImageStreams(new DockerImage(config.getBuilderImage())) &&
            removeDeploymentConfig(sName) &&
            removeSecret(sName) &&
            removeBuildConfig(sName);
    }

    @Override
    public boolean exists(String name) {
        String sName = Names.sanitize(name);
        return openShiftClient.deploymentConfigs().withName(sName).get() != null;
    }

    @Override
    public void scale(String name, int desiredReplicas) {
        String sName = Names.sanitize(name);
        openShiftClient.deploymentConfigs().withName(sName).edit()
                       .editSpec()
                       .withReplicas(desiredReplicas)
                       .endSpec()
                       .done();
    }


    @Override
    public boolean isScaled(String name, int desiredReplicas) {
        String sName = Names.sanitize(name);
        DeploymentConfig dc = openShiftClient.deploymentConfigs().withName(sName).get();

        int allReplicas = 0;
        int readyReplicas = 0;
        if (dc != null && dc.getStatus() != null) {
            DeploymentConfigStatus status = dc.getStatus();
            allReplicas = nullSafe(status.getReplicas());
            readyReplicas = nullSafe(status.getReadyReplicas());
        }
        return desiredReplicas == allReplicas && desiredReplicas == readyReplicas;
    }

    @Override
    public List<DeploymentConfig> getDeploymentsByLabel(Map<String, String> labels) {
        return openShiftClient.deploymentConfigs().withLabels(labels).list().getItems();
    };

    private int nullSafe(Integer nr) {
        return nr != null ? nr : 0;
    }

//==================================================================================================

    private void ensureImageStreams(String name, DeploymentData deploymentData, DockerImage img) {
        openShiftClient.imageStreams().withName(img.getShortName()).createOrReplaceWithNew()
                .withNewMetadata()
                    .withName(img.shortName)
                    .addToAnnotations(deploymentData.getAnnotations())
                    .addToLabels(deploymentData.getLabels())
                .endMetadata()
                    .withNewSpec().addNewTag().withNewFrom().withKind("DockerImage").withName(img.getImage()).endFrom().withName(img.getTag()).endTag().endSpec()
                .done();
        openShiftClient.imageStreams().withName(name).createOrReplaceWithNew()
            .withNewMetadata().withName(name).endMetadata().done();
    }

    private boolean removeImageStreams(DockerImage img) {
        return openShiftClient.imageStreams().withName(img.getShortName()).delete();
    }

    private void ensureDeploymentConfig(String name, DeploymentData deploymentData, String serviceAccount) {
        openShiftClient.deploymentConfigs().withName(name).createOrReplaceWithNew()
            .withNewMetadata()
            .withName(name)
            .addToAnnotations(deploymentData.getAnnotations())
            .addToLabels(deploymentData.getLabels())
            .endMetadata()
            .withNewSpec()
            .withReplicas(1)
            .addToSelector("integration", name)
            .withNewTemplate()
            .withNewMetadata().addToLabels("integration", name).endMetadata()
            .withNewSpec()
            .withServiceAccount(serviceAccount)
            .withServiceAccountName(serviceAccount)
            .addNewContainer()
            .withImage(" ").withImagePullPolicy("Always").withName(name)
            .addNewPort().withName("jolokia").withContainerPort(8778).endPort()
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
            .addNewTrigger().withType("ConfigChange").endTrigger()
            .addNewTrigger().withType("ImageChange")
            .withNewImageChangeParams()
            .withAutomatic(true).addToContainerNames(name)
            .withNewFrom().withKind("ImageStreamTag").withName(name + ":latest").endFrom()
            .endImageChangeParams()
            .endTrigger()
            .endSpec()
            .done();
    }


    private boolean removeDeploymentConfig(String projectName) {
        return openShiftClient.deploymentConfigs().withName(projectName).delete();
    }

    private void ensureBuildConfig(String name, DeploymentData deploymentData, DockerImage builderImage) {
        openShiftClient.buildConfigs().withName(name).createOrReplaceWithNew()
            .withNewMetadata()
                .withName(name)
                .addToAnnotations(deploymentData.getAnnotations())
                .addToLabels(deploymentData.getLabels())
            .endMetadata()
            .withNewSpec()
            .withRunPolicy("SerialLatestOnly")
            .withNewSource().withType("Binary").endSource()
            .withNewStrategy()
              .withType("Source")
              .withNewSourceStrategy()
                .withNewFrom().withKind("ImageStreamTag").withName(builderImage.getShortName() + ":" + builderImage.getTag()).endFrom()
                .withIncremental(true)
                .withEnv(new EnvVar("MAVEN_OPTS","-XX:+UseG1GC -XX:+UseStringDeduplication -Xmx500m", null))
              .endSourceStrategy()
            .endStrategy()
            .withNewOutput().withNewTo().withKind("ImageStreamTag").withName(name + ":latest").endTo().endOutput()
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


    /* default */ static class DockerImage {
        private final String image;

        private String tag = "latest";

        private final String shortName;

        /* default */ DockerImage(String fullImage) {
            image = fullImage;

            int colonIndex = fullImage.lastIndexOf(':');

            String builderImageStreamName = fullImage;
            if (colonIndex > -1) {
                builderImageStreamName = fullImage.substring(0, colonIndex);
                tag = fullImage.substring(colonIndex + 1);
            }
            shortName = builderImageStreamName.substring(builderImageStreamName.lastIndexOf('/') + 1);
        }

        public String getImage() {
            return image;
        }

        public String getTag() {
            return tag;
        }

        public String getShortName() {
            return shortName;
        }
    }

}
