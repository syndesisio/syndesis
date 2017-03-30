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
package com.redhat.ipaas.openshift;

import com.redhat.ipaas.core.Names;

import io.fabric8.kubernetes.client.RequestConfig;
import io.fabric8.kubernetes.client.RequestConfigBuilder;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public class OpenShiftServiceImpl implements OpenShiftService {

    private final NamespacedOpenShiftClient openShiftClient;
    private String builderImage;

    public OpenShiftServiceImpl(NamespacedOpenShiftClient openShiftClient, String builderImage) {
        this.openShiftClient = openShiftClient;
        this.builderImage = builderImage;
    }

    @Override
    public boolean isDeploymentConfigReady(String name) {
        String token = getAuthenticationTokenString();
        RequestConfig requestConfig = new RequestConfigBuilder().withOauthToken(token).build();
        return openShiftClient.withRequestConfig(requestConfig).call(c -> c.deploymentConfigs().withName(Names.sanitize(name)).isReady());
    }

    @Override
    public boolean isDeploymentConfigScaled(String name, int replicas) {
        DeploymentConfig dc = openShiftClient.deploymentConfigs().withName(Names.sanitize(name)).get();

        int allReplicas = dc.getStatus() != null ? dc.getStatus().getReplicas() : 0;
        int readyReplicas = dc.getStatus() != null ? dc.getStatus().getReadyReplicas() : 0;

        return replicas == allReplicas && replicas == readyReplicas;
    }

    @Override
    public void scaleDeploymentConfig(String name, int replicas) {
        openShiftClient.deploymentConfigs().withName(Names.sanitize(name)).edit()
            .editSpec()
                .withReplicas(replicas)
            .endSpec()
            .done();
    }

    @Override
    public boolean deploymentConfigExists(String name) {
        return openShiftClient.deploymentConfigs().withName(Names.sanitize(name)).get() != null;
    }

    @Override
    public boolean deleteResources(String name) {
        String sanitizedName = Names.sanitize(name);

        String token = getAuthenticationTokenString();
        RequestConfig requestConfig = new RequestConfigBuilder().withOauthToken(token).build();

        return openShiftClient.withRequestConfig(requestConfig).call(c ->
            removeImageStreams(new DockerImage(builderImage)) &&
            removeeDeploymentConfig(sanitizedName) &&
            removeSecret(sanitizedName) &&
            removeBuildConfig(sanitizedName)
        );
    }

    @Override
    public void createOpenShiftResources(CreateResourcesRequest request) {
        String sanitizedName = Names.sanitize(request.getName());

        String token = getAuthenticationTokenString();
        RequestConfig requestConfig = new RequestConfigBuilder().withOauthToken(token).build();
        openShiftClient.withRequestConfig(requestConfig).<Void>call(c -> {
            DockerImage img = new DockerImage(builderImage);
            ensureImageStreams(sanitizedName, img);
            ensureDeploymentConfig(sanitizedName);
            ensureSecret(sanitizedName, request.getSecretData());
            ensureBuildConfig(sanitizedName, request.getGitRepository(), img, request.getWebhookSecret());
            return null;
        });
    }

    private void ensureImageStreams(String projectName, DockerImage img) {
        openShiftClient.imageStreams().withName(img.getShortName()).createOrReplaceWithNew()
                .withNewMetadata().withName(img.shortName).endMetadata()
                .withNewSpec().addNewTag().withNewFrom().withKind("DockerImage").withName(img.getImage()).endFrom().withName(img.getTag()).endTag().endSpec()
                .done();

        openShiftClient.imageStreams().withName(projectName).createOrReplaceWithNew()
            .withNewMetadata().withName(projectName).endMetadata().done();
    }

    private boolean removeImageStreams(DockerImage img) {
        return openShiftClient.imageStreams().withName(img.getShortName()).delete();
    }

    private void ensureDeploymentConfig(String projectName) {
        openShiftClient.deploymentConfigs().withName(projectName).createOrReplaceWithNew()
            .withNewMetadata().withName(projectName).endMetadata()
            .withNewSpec()
            .withReplicas(1)
            .addToSelector("integration", projectName)
            .withNewTemplate()
            .withNewMetadata().addToLabels("integration", projectName).endMetadata()
            .withNewSpec()
            .addNewContainer()
            .withImage(" ").withImagePullPolicy("Always").withName(projectName).addNewPort().withContainerPort(8778).endPort()
            .addNewVolumeMount()
                .withName("secret-volume")
                .withMountPath("/opt/integration/secrets.properties")
                .withReadOnly(false)
            .endVolumeMount()
            .endContainer()
            .addNewVolume()
                .withName("secret-volume")
                .withNewSecret()
                    .withSecretName(projectName)
                .endSecret()
            .endVolume()
            .endSpec()
            .endTemplate()
            .addNewTrigger().withType("ConfigChange").endTrigger()
            .addNewTrigger().withType("ImageChange")
            .withNewImageChangeParams()
            .withAutomatic(true).addToContainerNames(projectName)
            .withNewFrom().withKind("ImageStreamTag").withName(projectName + ":latest").endFrom()
            .endImageChangeParams()
            .endTrigger()
            .endSpec()
            .done();
    }


    private boolean removeeDeploymentConfig(String projectName) {
        return openShiftClient.deploymentConfigs().withName(projectName).delete();
    }

    private void ensureBuildConfig(String projectName, String gitRepo, DockerImage img, String webhookSecret) {
        openShiftClient.buildConfigs().withName(projectName).createOrReplaceWithNew()
            .withNewMetadata().withName(projectName).endMetadata()
            .withNewSpec()
            .withRunPolicy("SerialLatestOnly")
            .addNewTrigger().withType("ConfigChange").endTrigger()
            .addNewTrigger().withType("ImageChange").endTrigger()
            .addNewTrigger().withType("GitHub").withNewGithub().withSecret(webhookSecret).endGithub().endTrigger()
            .withNewSource().withType("git").withNewGit().withUri(gitRepo).endGit().endSource()
            .withNewStrategy()
            .withType("Source")
            .withNewSourceStrategy()
            .withNewFrom().withKind("ImageStreamTag").withName(img.getShortName() + ":" + img.getTag()).endFrom()
            .withIncremental(true)
            .endSourceStrategy()
            .endStrategy()
            .withNewOutput().withNewTo().withKind("ImageStreamTag").withName(projectName + ":latest").endTo().endOutput()
            .endSpec()
            .done();
    }

    private boolean removeBuildConfig(String projectName) {
        return openShiftClient.buildConfigs().withName(projectName).delete();
    }

    private void ensureSecret(String projectName, Map<String, String> secretData) {
        openShiftClient.secrets().withName(projectName).createOrReplaceWithNew()
            .withNewMetadata().withName(projectName).endMetadata()
            .withStringData(secretData)
            .done();
    }

    private boolean removeSecret(String projectName) {
       return openShiftClient.secrets().withName(projectName).delete();
    }

    private String getAuthenticationTokenString() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Cannot set authorization header because there is no authenticated principal");
        } else if (!KeycloakAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            throw new IllegalStateException(String.format("Cannot set authorization header because Authentication is of type %s but %s is required",
                new Object[]{authentication.getClass(), KeycloakAuthenticationToken.class}));
        } else {
            KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) authentication;
            return token.getAccount().getKeycloakSecurityContext().getTokenString();
        }
    }


    static class DockerImage {
        private String image;

        private String tag = "latest";

        private String shortName;

        DockerImage(String fullImage) {
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
