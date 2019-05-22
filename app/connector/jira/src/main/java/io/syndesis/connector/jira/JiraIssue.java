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
package io.syndesis.connector.jira;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"PMD.GodClass", "PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
public class JiraIssue {

    private String issueKey;
    private String projectKey;
    private Long issueTypeId;
    private String issueTypeName;
    private String summary;
    private Long priorityId;
    private String priorityName;
    private String assignee;
    private List<String> components = new ArrayList<>();
    private List<String> watchers = new ArrayList<>();
    private String description;
    private Integer transitionId;
    private String comment;

    public String getIssueKey() {
        return issueKey;
    }

    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public Long getIssueTypeId() {
        return issueTypeId;
    }

    public void setIssueTypeId(Long issueTypeId) {
        this.issueTypeId = issueTypeId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getPriorityName() {
        return priorityName;
    }

    public void setPriorityName(String priorityName) {
        this.priorityName = priorityName;
    }

    public String getIssueTypeName() {
        return issueTypeName;
    }

    public void setIssueTypeName(String issueTypeName) {
        this.issueTypeName = issueTypeName;
    }

    public Long getPriorityId() {
        return priorityId;
    }

    public void setPriorityId(Long priorityId) {
        this.priorityId = priorityId;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public List<String> getComponents() {
        return components;
    }

    public void setComponents(List<String> components) {
        this.components = components;
    }

    public List<String> getWatchers() {
        return watchers;
    }

    public void setWatchers(List<String> watchers) {
        this.watchers = watchers;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTransitionId() {
        return transitionId;
    }

    public void setTransitionId(Integer transitionId) {
        this.transitionId = transitionId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Replace the null attributes of this instance for the otherIssue attributes.
     */
    public void replaceNullValues(JiraIssue otherIssue) {
        if (otherIssue == null) {
            return;
        }
        if (issueKey == null && otherIssue.getIssueKey() != null) {
            issueKey = otherIssue.getIssueKey();
        }
        if (projectKey == null && otherIssue.getProjectKey() != null) {
            projectKey = otherIssue.getProjectKey();
        }
        if (issueTypeId == null && otherIssue.getIssueTypeId() != null) {
            issueTypeId = otherIssue.getIssueTypeId();
        }
        if (issueTypeName == null && otherIssue.getIssueTypeName() != null) {
            issueTypeName = otherIssue.getIssueTypeName();
        }
        if (summary == null && otherIssue.getSummary() != null) {
            summary = otherIssue.getSummary();
        }
        if (priorityId == null && otherIssue.getPriorityId() != null) {
            priorityId = otherIssue.getPriorityId();
        }
        if (priorityName == null && otherIssue.getPriorityName() != null) {
            priorityName = otherIssue.getPriorityName();
        }
        if (assignee == null && otherIssue.getAssignee() != null) {
            assignee = otherIssue.getAssignee();
        }
        if (components.isEmpty() && !otherIssue.components.isEmpty()) {
            components = otherIssue.components;
        }
        if (watchers.isEmpty() && !otherIssue.watchers.isEmpty()) {
            watchers = otherIssue.watchers;
        }
        if (description == null && otherIssue.getDescription() != null) {
            description = otherIssue.getDescription();
        }
        if (transitionId == null && otherIssue.getTransitionId() != null) {
            transitionId = otherIssue.getTransitionId();
        }
        if (comment == null && otherIssue.getComment() != null) {
            comment = otherIssue.getComment();
        }
    }
}
