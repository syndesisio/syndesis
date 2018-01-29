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

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.internal.PodOperationsImpl;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.syndesis.model.ListResult;
import io.syndesis.model.integration.Integration;
import io.syndesis.openshift.OpenShiftConfigurationProperties;
import io.syndesis.rest.v1.handler.integration.IntegrationHandler;
import io.syndesis.rest.v1.handler.integration.IntegrationSupportHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@ConditionalOnProperty(value = "openshift.enabled", matchIfMissing = true, havingValue = "true")
public class SupportUtil {
    public static final Logger LOG = LoggerFactory.getLogger(SupportUtil.class);

    private final NamespacedOpenShiftClient client;
    private final OpenShiftConfigurationProperties config;
    private final IntegrationHandler integrationHandler;
    private final IntegrationSupportHandler integrationSupportHandler;

    private final OkHttpClient okHttpClient;

    protected ExecutorService executor;

    public SupportUtil(NamespacedOpenShiftClient client, OpenShiftConfigurationProperties config, IntegrationHandler integrationHandler, IntegrationSupportHandler integrationSupportHandler) {
        this.client = client;
        this.config = config;
        this.integrationHandler = integrationHandler;
        this.integrationSupportHandler = integrationSupportHandler;
        this.okHttpClient = this.client == null ? null : HttpClientUtils.createHttpClient(this.client.getConfiguration());
        this.executor =  Executors.newCachedThreadPool(threadFactory("Logs Controller"));
    }

//    public OutputStream streamLogs(String container) throws IOException, InterruptedException {
//        ProcessBuilder pb = new ProcessBuilder("oc logs ", container);
//        LOG.info("Running `oc logs {}`", container);
//        Process process = pb.start();
//        int errCode = process.waitFor();
//        if(errCode != 0){
//            LOG.warn("Error running previous command");
//        }
//        return process.getOutputStream();
//    }

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
            configurationMap.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).forEach(integrationName -> {
                switch(integrationName){
                    case "platformLogs":
                        Stream.of("syndesis-atlasmap","syndesis-db", "syndesis-oauthproxy", "syndesis-rest", "syndesis-ui", "syndesis-verifier").forEach(componentName -> {
                            getComponentLogs(componentName).ifPresent((Reader reader) -> {
                                try {
                                    addEntryToZip(componentName, reader, os);
                                } catch (IOException e) {
                                    LOG.error("Error preparing logs for: " + componentName, e);
                                }
                            });
                        });
                        break;
                    default:
                        getIntegrationLogs(integrationName).ifPresent((String fileContent) -> {
                            try {
                                addEntryToZip(integrationName, fileContent, os);
                            } catch (IOException e) {
                                LOG.error("Error preparing logs for: " + integrationName, e);
                            }

                            ListResult<Integration> list = integrationHandler.list(uriInfo);
                            list.getItems().stream().filter(integration -> integrationName.equalsIgnoreCase(integration.getName().replace(' ', '-'))).forEach(
                                integration -> {
                                    integration.getId().ifPresent(id -> {
                                        try {
                                            addSourceEntryToZip(integrationName, id, os);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                }
                            );
                        });

                }
            });
            LOG.info("Created Support file: {}", zipFile);
        } catch (IOException e) {
            LOG.error("Error producing Support zip file", e);
            throw new WebApplicationException(500);
        }

        return zipFile;
    }

    protected void addSourceEntryToZip(String integrationName, String integrationId, ZipOutputStream os) throws IOException {
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


    public Collection<String> getIntegrationPods() {
        Collection<String> collect = client.pods().list().getItems().stream()
                .filter(pod -> pod.getMetadata().getLabels().containsKey("integration"))
                .map(pod -> pod.getMetadata().getLabels().get("integration"))
                .collect(Collectors.toList());
        return collect;
    }

    public Optional<String> getLogs(String label, String integrationName) {
       Optional<String> result =  client.pods().list().getItems().stream()
                .filter(p -> integrationName.equals(p.getMetadata().getLabels().get(label))).findAny().
                        flatMap(p ->
                        {
                            InputStream is = client.pods().inNamespace(config.getNamespace()).withName(p.getMetadata().getName()).sinceTime("0").watchLog(System.out).getOutput();
                            StringWriter writer = new StringWriter();
                            try {
                                IOUtils.copy(is, writer, Charset.defaultCharset());
                                String theString = writer.toString();
                                LOG.info(theString);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return Optional.of(client.pods().inNamespace(config.getNamespace()).withName(p.getMetadata().getName()).getLog());
    });
        return result;
    }



//    public InputStream getLogsBadHackedWorkaround(String label, String integrationName) throws MalformedURLException {
//        InputStream result = null;
//
//        Consumer<InputStream> handler = (InputStream input ) -> {
//
//        };
//
//        PodOperationsImpl pod = (PodOperationsImpl) client.pods().withName(integrationName);
//        StringBuilder url = new StringBuilder()
//            .append(pod.getResourceUrl().toString())
//            .append("/log?pretty=false&follow=true&timestamps=true&sinceTime=0");
//
//        Request request = new Request.Builder().url(new URL(url.toString())).get().build();
//        OkHttpClient clone = okHttpClient.newBuilder().readTimeout(0, TimeUnit.MILLISECONDS).build();
//        clone.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                LOG.info("Failure occurred getting  controller for pod: {},", integrationName, e);
//                handler.accept(null);
//            }
//
//            @Override
//            public void onResponse(final Call call, final Response response) throws IOException {
//                executor.execute(() -> {
//                    try {
//                        if( response.code() == 200 ) {
//                            handler.accept(response.body().byteStream());
//                        } else {
//                            LOG.info("Failure occurred while processing controller for pod: {}, http status: {}, details: {}", integrationName, response.code(), response.body().string());
//                            handler.accept(null);
//                        }
//                    } catch (IOException e) {
//                        LOG.error("Unexpected Error", e);
//                    }
//                });
//            }
//        });
//        return result;
//    }

    public Optional<String> getIntegrationLogs(String integrationName){
        return getLogs("integration", integrationName);
    }


    public Optional<Reader> getComponentLogs(String componentName){
        return streamLogs("component", componentName);
    }

    /**
     * This controller can potentially spin up lots of threads, at last one for each
     * pod that's being processed.  Lets reduce thread stack size since we don't need
     * a very large stack to do log processing.
     *
     * @param name
     * @return
     */
    private static ThreadFactory threadFactory(String name) {
        return r -> new Thread(null, r, name, 1024);
    }


}
