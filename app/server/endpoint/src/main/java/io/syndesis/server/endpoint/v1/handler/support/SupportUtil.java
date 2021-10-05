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
package io.syndesis.server.endpoint.v1.handler.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.ImageStreamTag;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.WithResourceId;
import io.syndesis.common.model.integration.IntegrationOverview;
import io.syndesis.server.endpoint.v1.handler.integration.IntegrationHandler;
import io.syndesis.server.endpoint.v1.handler.integration.support.IntegrationSupportHandler;
import io.syndesis.server.openshift.OpenShiftService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;


@Service
@ConditionalOnProperty(value = "openshift.enabled", matchIfMissing = true, havingValue = "true")
public class SupportUtil {

    public Logger log = LoggerFactory.getLogger(SupportUtil.class);
    static final String[] PLATFORM_PODS = {"syndesis-db", "syndesis-oauthproxy", "syndesis-server", "syndesis-ui", "syndesis-meta"};
    static final Map<String, String> PLATFORM_PODS_CONTAINER = new HashMap<>();
    protected static final Yaml YAML;
    public static final String COMPONENT_LABEL = "syndesis.io/component";

    private final NamespacedOpenShiftClient client;
    private final IntegrationHandler integrationHandler;
    private final IntegrationSupportHandler integrationSupportHandler;

    static {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        YAML = new Yaml(options);
        PLATFORM_PODS_CONTAINER.put("syndesis-db", "postgresql");
    }

    /**
     * Used in SupportUtilUnitTest
     */
    SupportUtil(NamespacedOpenShiftClient client, IntegrationHandler integrationHandler,
                IntegrationSupportHandler integrationSupportHandler, Logger log) {
        this(client, integrationHandler, integrationSupportHandler);
        this.log = log;
    }

    @Autowired
    public SupportUtil(NamespacedOpenShiftClient client, IntegrationHandler integrationHandler,
                       IntegrationSupportHandler integrationSupportHandler) {
        this.client = Objects.requireNonNull(client, "client");
        this.integrationHandler = integrationHandler;
        this.integrationSupportHandler = integrationSupportHandler;
    }

    public File createSupportZipFile(Map<String, Boolean> configurationMap, UriInfo uriInfo) {
        File zipFile = null;
        try {
            zipFile = File.createTempFile("syndesis.", ".zip");
        } catch (IOException e) {
            log.error("Error creating Support zip file", e);
            throw new WebApplicationException(e, 500);
        }

        try (ZipOutputStream os = new ZipOutputStream(new FileOutputStream(zipFile))) {
            addPlatformPodsLogs(os);
            addResourceDescriptors(os);
            addIntegrationsFiles(configurationMap, uriInfo, os);
            log.info("Created Support file: {}", zipFile);
        } catch (IOException e) {
            log.error("Error producing Support zip file", e);
            if (zipFile != null && !zipFile.delete()) {
                zipFile.deleteOnExit();
            }
            throw new WebApplicationException(e, 500);
        }

        return zipFile;
    }

    protected void addIntegrationsFiles(Map<String, Boolean> configurationMap, UriInfo uriInfo, ZipOutputStream os) {
        configurationMap.keySet().stream().forEach(integrationName -> {
            addIntegrationLogs(os, integrationName);
            addSourceFiles(uriInfo, os, integrationName);
        });
    }

    @SuppressWarnings( {"PMD.AvoidCatchingGenericException", "PMD.ExceptionAsFlowControl"})
    protected void addSourceFiles(UriInfo uriInfo, ZipOutputStream os, String integrationName) {
        ListResult<IntegrationOverview> list = integrationHandler.list(uriInfo);
        list.getItems().stream()
            .filter(integration -> integrationName.equalsIgnoreCase(integration.getName().replace(' ', '-')))
            .map(WithResourceId::getId)
            .forEach(
                integrationId -> {
                    integrationId.ifPresent(id -> {
                        try {
                            addSource(integrationName, id, os);
                        } catch (Exception e) {
                            log.error("Error adding source files for integration: {}", integrationName, e);
                        }
                    });
                });
    }

    @SuppressWarnings( {"PMD.AvoidCatchingGenericException", "PMD.ExceptionAsFlowControl"})
    protected void addResourceDescriptors(ZipOutputStream os) {

        Stream<BuildConfig> bcStream = client.buildConfigs().list().getItems().stream();
        Stream<DeploymentConfig> dcStream = client.deploymentConfigs().list().getItems().stream();
        Stream<ConfigMap> cmStream = client.configMaps().list().getItems().stream();
        Stream<ImageStreamTag> istStream = client.imageStreamTags().list().getItems().stream();

        Stream<? extends HasMetadata> stream = Stream.concat(bcStream, dcStream);
        stream = Stream.concat(stream, cmStream);
        stream = Stream.concat(stream, istStream);

        stream.forEach(res -> {
            try {
                ZipEntry ze = new ZipEntry("descriptors/" + res.getKind() + '/' + res.getMetadata().getName() + ".YAML");
                os.putNextEntry(ze);
                dumpAsYaml(res, os);
                os.closeEntry();
            } catch (Exception e) {
                log.error("Error adding resource {} {}", res.getKind(), res.getMetadata().getName(), e);
            }
        });
    }

    @SuppressWarnings( {"PMD.ExceptionAsFlowControl"})
    protected void addIntegrationLogs(ZipOutputStream os, String integrationName) {
        getIntegrationLogs(integrationName).ifPresent((fileContent) -> {
            try {
                addEntryToZip(integrationName, fileContent, os);
            } catch (IOException e) {
                log.error("Error preparing logs for integration: {}", integrationName, e);
            }
        });
    }

    protected void addPlatformPodsLogs(ZipOutputStream os) {
        Stream.of(PLATFORM_PODS).forEach(componentName -> {
            getComponentLogs(componentName).ifPresent((Reader reader) -> {
                try {
                    addEntryToZip("platform_logs/" + componentName, reader, os);
                } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
                    log.error("Error preparing logs for component: {}", componentName, e);
                }
            });
        });
    }

    protected void addSource(String integrationName, String integrationId, ZipOutputStream os) throws IOException {
        StreamingOutput export = integrationSupportHandler.export(Arrays.asList(integrationId));
        ZipEntry ze = new ZipEntry(integrationName + ".src.zip");
        os.putNextEntry(ze);
        File file = null;
        try {
            file = File.createTempFile(integrationName, ".src.zip");
            try (FileOutputStream tempStream = FileUtils.openOutputStream(file)) {
                export.write(tempStream);
            }
            FileUtils.copyFile(file, os);
            os.closeEntry();
        } finally {
            if (file != null && !file.delete()) {
                file.deleteOnExit();
            }
        }

    }

    protected void addEntryToZip(String integrationName, Reader fileContent, ZipOutputStream os) throws IOException {
        ZipEntry ze = new ZipEntry(integrationName + ".log");
        os.putNextEntry(ze);
        IOUtils.copy(fileContent, os, StandardCharsets.UTF_8);
        os.closeEntry();
    }

    protected void addEntryToZip(String integrationName, InputStream fileContent, ZipOutputStream os) throws IOException {
        ZipEntry ze = new ZipEntry(integrationName + ".log");
        os.putNextEntry(ze);
        IOUtils.copy(fileContent, os);
        os.closeEntry();
    }

    public Collection<String> getIntegrationPods() {
        return client.pods().list().getItems().stream()
                .filter(pod -> pod.getMetadata().getLabels().containsKey("integration"))
                .map(pod -> pod.getMetadata().getLabels().get("integration"))
                .collect(Collectors.toList());
    }

    public Optional<Reader> getLogs(String label, String component) {
        return client.pods().list().getItems().stream()
            .filter(pod -> component.equals(pod.getMetadata().getLabels().get(label)))
            .findAny()
            .map(pod -> pod.getMetadata().getName())
            .map(this::fetchLogsFor);
    }

    public Optional<Reader> getIntegrationLogs(String integrationName) {
        return client.pods().list().getItems().stream()
            .filter(pod -> integrationName.equals(pod.getMetadata().getAnnotations().get(OpenShiftService.INTEGRATION_NAME_ANNOTATION)))
            .findAny()
            .map(pod -> pod.getMetadata().getName())
            .map(this::fetchLogsFor);
    }

    public Optional<Reader> getComponentLogs(String componentName) {
        return getLogs(COMPONENT_LABEL, componentName);
    }

    public static void dumpAsYaml(HasMetadata obj, OutputStream outputStream) {
        YAML.dump(obj, new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
    }

    private Reader fetchLogsFor(String podName) {
        final PodResource<Pod, DoneablePod> pod = client.pods().withName(podName);
        String componentName = pod.get().getMetadata().getLabels().get(COMPONENT_LABEL);
        String container = PLATFORM_PODS_CONTAINER.get(componentName);
        if (container != null) {
            return pod.inContainer(container).getLogReader();
        }

        return pod.getLogReader();
    }

}
