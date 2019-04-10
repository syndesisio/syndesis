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
package io.syndesis.server.endpoint.v1.handler.extension;

import java.io.InputStream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;

import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.server.dao.file.FileDataManager;
import io.syndesis.server.verifier.MetadataConfigurationProperties;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "openshift.enabled", havingValue = "true", matchIfMissing=true)
public class DefaultVerifierExtensionUploader implements VerifierExtensionUploader {
    private final MetadataConfigurationProperties verificationConfig;
    private final FileDataManager fileDataManager;
    private final NamespacedOpenShiftClient openShiftClient;

    private volatile Client client;

    public DefaultVerifierExtensionUploader(
            MetadataConfigurationProperties verificationConfig,
            FileDataManager fileDataManager,
            NamespacedOpenShiftClient openShiftClient) {

        this.verificationConfig = verificationConfig;
        this.fileDataManager = fileDataManager;
        this.openShiftClient = openShiftClient;
    }

    @PostConstruct
    public void init() {
        if (this.client == null) {
            this.client = ClientBuilder.newClient();
        }

    }

    @PreDestroy
    public void destroy() {
        if (this.client != null) {
            this.client.close();
            this.client = null;
        }
    }

    @Override
    public void uploadToVerifier(Extension extension) {
        final WebTarget target = client.target(String.format("http://%s/api/v1/drivers", verificationConfig.getService()));
        final MultipartFormDataOutput multipart = new MultipartFormDataOutput();

        String fileName = ExtensionActivator.getConnectorIdForExtension(extension);
        multipart.addFormData("fileName", fileName, MediaType.TEXT_PLAIN_TYPE);

        InputStream is = fileDataManager.getExtensionBinaryFile(extension.getExtensionId());
        multipart.addFormData("file", is, MediaType.APPLICATION_OCTET_STREAM_TYPE);

        GenericEntity<MultipartFormDataOutput> genericEntity = new GenericEntity<MultipartFormDataOutput>(multipart) {};
        Entity<?> entity = Entity.entity(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE);

        Boolean isDeployed = target.request().post(entity, Boolean.class);
        if (isDeployed) {
            openShiftClient.deploymentConfigs().withName("syndesis-meta").deployLatest();
        }
    }

    @Override
    public void deleteFromVerifier(Extension extension) {
        final String service = verificationConfig.getService();
        final String connectorId = ExtensionActivator.getConnectorIdForExtension(extension);
        final WebTarget target = client.target(String.format("http://%s/api/v1/drivers/%s", service, connectorId));

        Boolean isDeleted = target.request().delete(Boolean.class);
        if (isDeleted) {
            openShiftClient.deploymentConfigs().withName("syndesis-meta").deployLatest();
        }
    }
}
