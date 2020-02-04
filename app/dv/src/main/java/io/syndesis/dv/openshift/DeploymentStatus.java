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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.syndesis.dv.openshift.BuildStatus.RouteStatus;

@JsonInclude(Include.NON_NULL)
public class DeploymentStatus {

    public enum Status {
        NOTFOUND,
        DEPLOYING,
        RUNNING,
        FAILED,
    }

    private volatile String deploymentName;
    private volatile Status status = Status.NOTFOUND;
    private volatile String statusMessage;
    private List<RouteStatus> routes = null;
    private List<String> usedBy = Collections.emptyList();
    private Long version;

    public List<RouteStatus> getRoutes() {
        if (this.routes == null) {
            return Collections.emptyList();
        }
        return this.routes;
    }

    public void addRoute(RouteStatus route) {
        if (route == null) {
            return;
        }
        if (this.routes == null) {
            this.routes = new ArrayList<RouteStatus>();
        }
        this.routes.add(route);
    }

    public void setRoutes(List<RouteStatus> routes) {
        this.routes = routes;
    }

    public List<String> getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(List<String> usedBy) {
        this.usedBy = usedBy;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

}
