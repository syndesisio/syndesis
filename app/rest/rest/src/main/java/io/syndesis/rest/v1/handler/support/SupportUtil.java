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
package io.syndesis.rest.v1.handler.support;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.dsl.internal.PodOperationsImpl;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.ImageStreamTag;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.syndesis.model.ListResult;
import io.syndesis.model.integration.Integration;
import io.syndesis.rest.v1.handler.integration.IntegrationHandler;
import io.syndesis.rest.v1.handler.integration.IntegrationSupportHandler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@ConditionalOnProperty(value = "openshift.enabled", matchIfMissing = true, havingValue = "true")
public class SupportUtil {
    public static final Logger LOG = LoggerFactory.getLogger(SupportUtil.class);
    public static final String[] PLATFORM_PODS = {"syndesis-atlasmap", "syndesis-db", "syndesis-oauthproxy", "syndesis-rest", "syndesis-ui", "syndesis-verifier"};
    protected static Yaml yaml;

    private final NamespacedOpenShiftClient client;
    private final IntegrationHandler integrationHandler;
    private final IntegrationSupportHandler integrationSupportHandler;
    private final OkHttpClient okHttpClient;

    static {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        yaml = new Yaml(options);
    }

    public SupportUtil(NamespacedOpenShiftClient client, IntegrationHandler integrationHandler, IntegrationSupportHandler integrationSupportHandler) {
        this.client = client;
        this.integrationHandler = integrationHandler;
        this.integrationSupportHandler = integrationSupportHandler;
        this.okHttpClient = this.client == null ? null : HttpClientUtils.createHttpClient(this.client.getConfiguration());
    }

    public Optional<Reader> streamLogs(String label, String integrationName) {
        Optional<Reader> result =  client.pods().list().getItems().stream()
        .filter(p -> integrationName.equals(p.getMetadata().getLabels().get(label))).findAny().
            flatMap(p ->
                //Optional.of(client.pods().inNamespace(config.getNamespace()).withName(p.getMetadata().getName()).getLogReader())
                Optional.of(new StringReader("REQUIRES_LIBRARY_UPDATE!!!!"))
            );
        return result;
    }

    public File createSupportZipFile(Map<String, Boolean> configurationMap, UriInfo uriInfo) {
        File zipFile = null;
        try{
            zipFile = File.createTempFile("syndesis.", ".zip");
        } catch (IOException e) {
            LOG.error("Error creating Support zip file", e);
            throw new WebApplicationException(500);
        }

        try ( ZipOutputStream os = new ZipOutputStream(new FileOutputStream(zipFile));) {
            addPlatformPodsLogs(os);
            addIntegrationsFiles(configurationMap, uriInfo, os);
            LOG.info("Created Support file: {}", zipFile);
        } catch (IOException e) {
            LOG.error("Error producing Support zip file", e);
            throw new WebApplicationException(500);
        }

        return zipFile;
    }

    protected void addResourcesDump(Map<String, Boolean> configurationMap, UriInfo uriInfo, ZipOutputStream os) {
    }

    protected void addIntegrationsFiles(Map<String, Boolean> configurationMap, UriInfo uriInfo, ZipOutputStream os) {
        configurationMap.keySet().stream().forEach(integrationName -> {
            addIntegrationLogs(os, integrationName);
            addResourceDescriptors(os);
            addSourceFiles(uriInfo, os, integrationName);
        });
    }

    protected void addSourceFiles(UriInfo uriInfo, ZipOutputStream os, String integrationName) {
        ListResult<Integration> list = integrationHandler.list(uriInfo);
        list.getItems().stream()
            .filter(integration -> integrationName.equalsIgnoreCase(integration.getName().replace(' ', '-')))
            .map(integration -> integration.getId())
            .forEach(
                integrationId -> {
                    integrationId.ifPresent(id -> {
                        try {
                            addSource(integrationName, id, os);
                        } catch (Exception e) {
                            LOG.error("Error preparing logs for integration: {}", integrationName, e);
                        }
                    });
                });
    }

    protected void addResourceDescriptors(ZipOutputStream os) {

        Stream<BuildConfig> bcStream = client.buildConfigs().list().getItems().stream();
        Stream<DeploymentConfig> dcStream = client.deploymentConfigs().list().getItems().stream();
        Stream<ConfigMap> cmStream = client.configMaps().list().getItems().stream();
        Stream<ImageStreamTag> istStream = client.imageStreamTags().list().getItems().stream();

        Stream<? extends HasMetadata    > stream = Stream.concat(bcStream, dcStream);
        stream = Stream.concat(stream, cmStream);
        stream = Stream.concat(stream, istStream);

        stream.forEach( res -> {
            HasMetadata resWithMetadata = (HasMetadata) res;
            try {
                ZipEntry ze = new ZipEntry("descriptors/"+ resWithMetadata.getKind() + '/' + resWithMetadata.getMetadata().getName() + ".yaml");
                os.putNextEntry(ze);
                dumpAsYaml(resWithMetadata, os);
                os.closeEntry();
            } catch (Exception e){
                LOG.error("Error adding resource {} {}", resWithMetadata.getKind(), resWithMetadata.getMetadata().getName(), e);
            }
        });
    }

    protected void addIntegrationLogs(ZipOutputStream os, String integrationName) {
        getIntegrationLogs(integrationName).ifPresent(( fileContent) -> {
            try {
                addEntryToZip(integrationName, fileContent, os);
            } catch (IOException e) {
                LOG.error("Error preparing logs for integration: " + integrationName, e);
            }
        });
    }

    protected void addPlatformPodsLogs(ZipOutputStream os) {
        Stream.of(PLATFORM_PODS).forEach(componentName -> {
            getComponentLogs(componentName).ifPresent((Reader reader) -> {
                try {
                    addEntryToZip("platform_logs/" + componentName, reader, os);
                } catch (Exception e) {
                    LOG.error("Error preparing logs for component: {}", componentName, e);
                }
            });
        });
    }

    protected void addSource(String integrationName, String integrationId, ZipOutputStream os) throws IOException {
        StreamingOutput export = integrationSupportHandler.export(Arrays.asList(integrationId));
        ZipEntry ze = new ZipEntry(integrationName + ".src.zip");
        os.putNextEntry(ze);

        File file = File.createTempFile(integrationName, ".src.zip");
        export.write(FileUtils.openOutputStream(file));
        FileUtils.copyFile(file, os);
        os.closeEntry();
    }

    protected void addEntryToZip(String integrationName, String fileContent, ZipOutputStream os) throws IOException {
        ZipEntry ze = new ZipEntry(integrationName + ".log");
        os.putNextEntry(ze);
        File file = File.createTempFile(integrationName, ".log");
        FileUtils.writeStringToFile( file, fileContent, Charset.defaultCharset() );
        FileUtils.copyFile(file, os);
        os.closeEntry();
    }

    protected void addEntryToZip(String integrationName, Reader fileContent, ZipOutputStream os) throws IOException {
        ZipEntry ze = new ZipEntry(integrationName + ".log");
        os.putNextEntry(ze);
        IOUtils.copy(fileContent, os, Charset.defaultCharset());
        os.closeEntry();
    }

    protected void addEntryToZip(String integrationName, InputStream fileContent, ZipOutputStream os) throws IOException {
        ZipEntry ze = new ZipEntry(integrationName + ".log");
        os.putNextEntry(ze);
        IOUtils.copy(fileContent, os);
        os.closeEntry();
    }

    public Collection<String> getIntegrationPods() {
        Collection<String> collect = client.pods().list().getItems().stream()
                .filter(pod -> pod.getMetadata().getLabels().containsKey("integration"))
                .map(pod -> pod.getMetadata().getLabels().get("integration"))
                .collect(Collectors.toList());
        return collect;
    }

    public Optional<Reader> getLogs(String label, String integrationName) {
        Optional<Reader> result =  client.pods().list().getItems().stream()
            .filter(pod -> integrationName.equals(pod.getMetadata().getLabels().get(label)))
            .findAny()
            .map(pod -> pod.getMetadata().getName())
            .flatMap(podName -> {
                PodOperationsImpl pod = (PodOperationsImpl) client.pods().withName(podName);
                try {
                    Request request = new Request.Builder()
                        .url(pod.getResourceUrl().toString() + "/log?pretty=false&timestamps=true")
                        .build();
                    Response response = null;
                    try {
                        response = okHttpClient.newCall(request).execute();
                        if (!response.isSuccessful())
                            throw new IOException("Unexpected response from /log endpoint: " + response);
                        return Optional.of(response.body().charStream());
                    } catch (IOException e) {
                        LOG.error("Error downloading log file for integration {}" , integrationName, e );
                        if (response != null){
                            response.close();
                        }
                    }
                } catch (MalformedURLException e) {
                    LOG.error("Error downloading log file for integration {}" , integrationName, e );
                }
                return Optional.empty();
            });
        return result;
    }

    public Optional<Reader> getIntegrationLogs(String integrationName){
        return getLogs("integration", integrationName);
    }

    public Optional<Reader> getComponentLogs(String componentName){
        return getLogs("component", componentName);
    }

    public static void dumpAsYaml(HasMetadata obj, OutputStream outputStream) {
        yaml.dump(obj, new OutputStreamWriter(outputStream, Charset.defaultCharset()));;
    }
}
