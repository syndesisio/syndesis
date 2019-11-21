/*
 * Copyright (C) 2013 Red Hat, Inc.
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
package io.syndesis.dv.openshift;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.teiid.adminapi.impl.VDBMetaData;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.syndesis.dv.StringConstants;
import io.syndesis.dv.server.AuthHandlingFilter.OAuthCredentials;

public class PublishConfiguration implements StringConstants {

    private OAuthCredentials oauthCreds;
    private VDBMetaData vdb;
    private boolean enableOdata;
    private String containerMemorySize;
    private String containerDiskSize;
    private List<EnvVar> allEnvironmentVariables = new ArrayList<>();
    private HashMap<String, String> buildNodeSelector = new HashMap<>();
    private String buildImageStream = "syndesis-s2i:latest";
    private Map<String, String> secretVariables = new HashMap<>();

    // cpu units
    private int cpuUnits = 500; // 100m is 0.1 of CPU, at 500m we have 1/2 CPU as default
    private long publishedRevision;

    public String getBuildImageStream() {
        String stream = System.getenv("BUILD_IMAGE_STREAM");
        if (stream != null) {
            buildImageStream = stream;
        }
        return buildImageStream;
    }

    public VDBMetaData getVDB() {
        return this.vdb;
    }

    public void setVDB(VDBMetaData vdb) {
        this.vdb = vdb;
    }

    public boolean isEnableOData() {
        return this.enableOdata;
    }

    public void setEnableOData(boolean flag) {
        this.enableOdata = flag;
    }

    public String getContainerMemorySize() {
        return this.containerMemorySize;
    }

    public void setContainerMemorySize(int size) {
        this.containerMemorySize = Integer.toString(size) + "Mi";
    }

    public void addEnvironmentVariables(Collection<EnvVar> envs) {
        if (envs != null && !envs.isEmpty()) {
            this.allEnvironmentVariables.addAll(envs);
        }
    }

    public void addSecretVariables(Map<String, String> properties) {
        if (properties != null && !properties.isEmpty()) {
            this.secretVariables.putAll(properties);
        }
    }

    public Map<String, String> getSecretVariables() {
        return secretVariables;
    }

    public void setSecretVariables(Map<String, String> secretVariables) {
        this.secretVariables = secretVariables;
    }

    protected String getUserJavaOptions() {
        StringBuilder sb = new StringBuilder();
        sb.append(" -XX:+UnlockExperimentalVMOptions");
        sb.append(" -XX:+UseCGroupMemoryLimitForHeap");
        sb.append(" -Djava.net.preferIPv4Addresses=true");
        sb.append(" -Djava.net.preferIPv4Stack=true");

        // CPU specific JVM options
        sb.append(" -XX:ParallelGCThreads="+cpuLimit());
        sb.append(" -XX:ConcGCThreads="+cpuLimit());
        sb.append(" -Djava.util.concurrent.ForkJoinPool.common.parallelism="+cpuLimit());
        sb.append(" -Dio.netty.eventLoopThreads="+(2*cpuLimit()));

        sb.append(" -Dorg.teiid.hiddenMetadataResolvable=false");
        sb.append(" -Dorg.teiid.allowAlter=false");
        return sb.toString();
    }


    protected Map<String, String> getUserEnvironmentVariables() {
        Map<String, String> envs = new TreeMap<>();
        envs.put("GC_MAX_METASPACE_SIZE", "256");
        return envs;
    }

    protected List<EnvVar> getUserEnvVars() {
        ArrayList<EnvVar> envs = new ArrayList<>();
        getUserEnvironmentVariables().forEach((k, v) -> envs.add(new EnvVar(k, v, null)));
        return envs;
    }

    public String getCpuUnits() {
        return Integer.toString(cpuUnits) + "m";
    }

    public void setCpuUnits(int units) {
        this.cpuUnits = units;
    }

    private int cpuLimit() {
        return Math.max(cpuUnits/1000, 1);
    }

    public OAuthCredentials getOAuthCredentials() {
        return this.oauthCreds;
    }

    public void setOAuthCredentials(OAuthCredentials creds) {
        this.oauthCreds = creds;
    }

    public HashMap<String, String> getBuildNodeSelector() {
        return buildNodeSelector;
    }

    public String getContainerDiskSize() {
        return containerDiskSize;
    }

    public void setContainerDiskSize(int containerDiskSize) {
        this.containerDiskSize = Integer.toString(containerDiskSize) + "Gi";
    }

    public List<EnvVar> getEnvironmentVariables() {
        return allEnvironmentVariables;
    }

    public String getDataVirtualizationName() {
        if (this.vdb == null) {
            return null;
        }
        return this.vdb.getName();
    }

    public long getPublishedRevision() {
        return publishedRevision;
    }

    public void setPublishedRevision(long publishedRevision) {
        this.publishedRevision = publishedRevision;
    }

}

