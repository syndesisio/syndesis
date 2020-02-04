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
package io.syndesis.dv.openshift;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * This class pulls double duty.  It's a status, but it's also the state holder
 * for the build / deployment tracking state machine.  The instance of
 * DeploymentStatus here is only for tracking the deployment of this build
 * - not the general deployment state.
 */
@JsonSerialize(as = BuildStatus.class)
@JsonInclude(Include.NON_NULL)
public class BuildStatus {

    public enum Status {
        NOTFOUND,
        SUBMITTED,
        CONFIGURING,
        BUILDING,
        FAILED,
        CANCELLED,
        COMPLETE,
        DELETE_SUBMITTED,
        DELETE_REQUEUE,
        DELETE_DONE
    }

    @JsonSerialize(as = RouteStatus.class)
    @JsonInclude(Include.NON_NULL)
    public static class RouteStatus {
        private final String name;
        private final ProtocolType protocol;
        private String host;
        private String path;
        private String target;
        private String port;
        private boolean secure;

        public RouteStatus(String name, ProtocolType kind) {
            this.name = name;
            this.protocol = kind;
        }

        public String getName() {
            return name;
        }

        public ProtocolType getProtocol() {
            return protocol;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public boolean isSecure() {
            return this.secure;
        }

        public void setSecure(boolean secure) {
            this.secure = secure;
        }
    }

    private volatile Status status = Status.NOTFOUND;
    @JsonIgnore
    private volatile PublishConfiguration publishConfiguration;
    private volatile String name;
    private volatile String namespace;
    @JsonIgnore
    private volatile String publishPodName;
    private volatile long lastUpdated = 0L;
    private volatile String statusMessage;

    private String openShiftName;
    private String dataVirtualizationName;
    private Long version;

    @JsonIgnore
    private DeploymentStatus deploymentStatus = new DeploymentStatus();

    public BuildStatus(String openShiftName) {
        this.openShiftName = openShiftName;
    }

    public PublishConfiguration getPublishConfiguration() {
        return publishConfiguration;
    }

    public void setPublishConfiguration(PublishConfiguration publishConfiguration) {
        this.publishConfiguration = publishConfiguration;
    }

    public String getName() {
        return name;
    }

    public void setName(String buildName) {
        this.name = buildName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated() {
        this.lastUpdated = System.currentTimeMillis();
    }

    public void setPublishPodName(String name) {
        this.publishPodName = name;
    }

    public String getPublishPodName() {
        return publishPodName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getOpenShiftName() {
        return openShiftName;
    }

    public String getDataVirtualizationName() {
        return dataVirtualizationName;
    }

    public void setDataVirtualizationName(String dataVirtualizationName) {
        this.dataVirtualizationName = dataVirtualizationName;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }
}
