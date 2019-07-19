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
package io.syndesis.connector.jira.customizer;

import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;
import io.syndesis.connector.jira.JiraIssue;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;

import static org.apache.camel.component.jira.JiraConstants.ISSUE_ASSIGNEE;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_PROJECT_KEY;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_SUMMARY;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_WATCHERS_ADD;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_COMPONENTS;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_TYPE_ID;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_TYPE_NAME;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_PRIORITY_ID;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_PRIORITY_NAME;

@SuppressWarnings({"PMD.NPathComplexity"})
public class AddIssueCustomizer implements ComponentProxyCustomizer {

    private JiraIssue jiraIssue;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        jiraIssue = new JiraIssue();
        String issueProjectKey = ConnectorOptions.extractOption(options, ISSUE_PROJECT_KEY);
        if (issueProjectKey != null) {
            jiraIssue.setProjectKey(issueProjectKey);
        }
        String issueSummary = ConnectorOptions.extractOption(options, ISSUE_SUMMARY);
        if (issueSummary != null) {
            jiraIssue.setSummary(issueSummary);
        }
        String issueAssignee = ConnectorOptions.extractOption(options, ISSUE_ASSIGNEE);
        if (issueAssignee != null) {
            jiraIssue.setAssignee(issueAssignee);
        }
        String description = ConnectorOptions.extractOption(options, "description");
        if (description != null) {
            jiraIssue.setDescription(description);
        }

        List<String> addWatchers = ConnectorOptions.extractOptionAndMap(options, ISSUE_WATCHERS_ADD,
            (String watchers) -> Splitter.on(",").omitEmptyStrings().trimResults().splitToList(watchers), null);
        if (addWatchers != null) {
            jiraIssue.setWatchers(addWatchers);
        }

        List<String> issueComponents = ConnectorOptions.extractOptionAndMap(options, ISSUE_COMPONENTS,
            (String comps) -> Splitter.on(",").omitEmptyStrings().trimResults().splitToList(comps), null);
        if (issueComponents != null) {
            jiraIssue.setComponents(issueComponents);
        }

        String issueTypeId = ConnectorOptions.extractOption(options, ISSUE_TYPE_ID);
        if (issueTypeId != null) {
            String issueTypeName = issueTypeId;
            try {
                int issueType = Integer.parseInt(issueTypeName);
                jiraIssue.setIssueTypeId((long) issueType);
            } catch (NumberFormatException e) {
                // the value is a type name
                jiraIssue.setIssueTypeName(issueTypeName);
            }
        }

        String issuePriorityId = ConnectorOptions.extractOption(options, ISSUE_PRIORITY_ID);
        if (issuePriorityId != null) {
            String issuePriorityName = issuePriorityId;
            try {
                int prio = Integer.parseInt(issuePriorityName);
                jiraIssue.setPriorityId((long) prio);
            } catch (NumberFormatException e) {
                // the value is a type name
                jiraIssue.setPriorityName(issuePriorityName);
            }
        }
        component.setBeforeProducer(this::beforeProducer);
    }

    private void beforeProducer(Exchange exchange) {
        JiraIssue bodyIssue;
        if (exchange.getIn().getBody() instanceof JiraIssue) {
            bodyIssue = (JiraIssue) exchange.getIn().getBody();
        } else {
            bodyIssue = new JiraIssue();
        }
        bodyIssue.replaceNullValues(jiraIssue);
        exchange.getIn().setHeader(ISSUE_PROJECT_KEY, bodyIssue.getProjectKey());
        if (bodyIssue.getIssueTypeId() != null) {
            exchange.getIn().setHeader(ISSUE_TYPE_ID, bodyIssue.getIssueTypeId());
        } else {
            exchange.getIn().setHeader(ISSUE_TYPE_NAME, bodyIssue.getIssueTypeName());
        }
        if (bodyIssue.getPriorityId() != null) {
            exchange.getIn().setHeader(ISSUE_PRIORITY_ID, bodyIssue.getPriorityId());
        } else {
            exchange.getIn().setHeader(ISSUE_PRIORITY_NAME, bodyIssue.getPriorityName());
        }
        if (bodyIssue.getSummary() != null) {
            exchange.getIn().setHeader(ISSUE_SUMMARY, bodyIssue.getSummary());
        }
        if (!bodyIssue.getComponents().isEmpty()) {
            exchange.getIn().setHeader(ISSUE_COMPONENTS, bodyIssue.getComponents());
        }
        if (!bodyIssue.getWatchers().isEmpty()) {
            exchange.getIn().setHeader(ISSUE_WATCHERS_ADD, bodyIssue.getWatchers());
        }
        if (bodyIssue.getAssignee() != null) {
            exchange.getIn().setHeader(ISSUE_ASSIGNEE, bodyIssue.getAssignee());
        }
        if (bodyIssue.getDescription() != null) {
            exchange.getIn().setBody(bodyIssue.getDescription());
        }
    }
}
