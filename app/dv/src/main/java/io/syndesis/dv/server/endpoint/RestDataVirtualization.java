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
package io.syndesis.dv.server.endpoint;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.syndesis.dv.KException;
import io.syndesis.dv.model.DataVirtualization;
import io.syndesis.dv.openshift.BuildStatus;

/**
 * A Dataservice.
 */
@JsonSerialize(as = RestDataVirtualization.class)
@JsonInclude(Include.NON_NULL)
public final class RestDataVirtualization {

    private String id;
    private String name;
    private String description;
    private String publishedState;
    private String podNamespace;
    private String publishPodName;
    private String odataHostName;
    private boolean empty = true;
    private List<String> usedBy;
    private Long publishedRevision;
    private Long deployedRevision;
    private boolean modified;
    private long editionCount;
    private String publishedMessage;
    private String deployedMessage;
    private String deployedState;
    private boolean secured;

    /**
     * Constructor for use when deserializing
     */
    public RestDataVirtualization() {
        super();
    }

    /**
     * Constructor for use when serializing.
     * @param dataService the dataService
     * @throws KException if error occurs
     */
    public RestDataVirtualization(DataVirtualization dataService) throws KException {
        setName(dataService.getName());

        setId(dataService.getId());

        setDescription(dataService.getDescription());

        // Initialize the published state to NOTFOUND
        setPublishedState(BuildStatus.Status.NOTFOUND.name());

        setModified(dataService.isModified());
    }

    /**
     * @return the VDB description (can be empty)
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the service published state (never empty)
     */
    public String getPublishedState() {
        return this.publishedState;
    }

    /**
     * @param publishedState the published state
     */
    public void setPublishedState(String publishedState) {
        this.publishedState = publishedState;
    }

    /**
     * @return the pod namespace (can be empty)
     */
    public String getPodNamespace() {
        return this.podNamespace;
    }

    /**
     * @param podNamespace the service pod namespace to set
     */
    public void setPodNamespace(String podNamespace) {
        this.podNamespace = podNamespace;
    }

    /**
     * @return the service pod name (can be empty)
     */
    public String getPublishPodName() {
        return this.publishPodName;
    }

    /**
     * @param publishPodName the service pod name to set
     */
    public void setPublishPodName(String publishPodName) {
        this.publishPodName = publishPodName;
    }

    /**
     * @return the service pod name (can be empty)
     */
    public String getOdataHostName() {
        return this.odataHostName;
    }

    /**
     * @param odataHostName the service pod name to set
     */
    public void setOdataHostName(String odataHostName) {
        this.odataHostName = odataHostName;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RestDataVirtualization other = (RestDataVirtualization) obj;
        return Objects.equals(id, other.id) &&
                Objects.equals(name, other.name);
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    /**
     * If there is nothing defined in this data virtualization
     * @return
     */
    public boolean isEmpty() {
        return empty;
    }

    public List<String> getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(List<String> usedBy) {
        this.usedBy = usedBy;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public Long getPublishedRevision() {
        return publishedRevision;
    }

    public void setPublishedRevision(Long publishedRevision) {
        this.publishedRevision = publishedRevision;
    }

    public long getEditionCount() {
        return editionCount;
    }

    public void setEditionCount(long editionCount) {
        this.editionCount = editionCount;
    }

    public void setDeployedRevision(Long version) {
        this.deployedRevision = version;
    }

    public Long getDeployedRevision() {
        return deployedRevision;
    }

    public String getPublishedMessage() {
        return publishedMessage;
    }

    public void setPublishedMessage(String statusMessage) {
        this.publishedMessage = statusMessage;
    }

    public String getDeployedState() {
        return deployedState;
    }

    public void setDeployedState(String status) {
        this.deployedState = status;
    }

    public String getDeployedMessage() {
        return deployedMessage;
    }

    public void setDeployedMessage(String statusMessage) {
        this.deployedMessage = statusMessage;
    }

    public boolean isSecured() {
        return secured;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }
}
