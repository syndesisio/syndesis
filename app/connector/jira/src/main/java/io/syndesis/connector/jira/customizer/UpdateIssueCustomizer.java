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
import static org.apache.camel.component.jira.JiraConstants.ISSUE_KEY;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_SUMMARY;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_WATCHERS_ADD;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_COMPONENTS;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_TYPE_ID;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_TYPE_NAME;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_PRIORITY_ID;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_PRIORITY_NAME;

@SuppressWarnings({"PMD.NPathComplexity"})
public class UpdateIssueCustomizer implements ComponentProxyCustomizer {

    private JiraIssue jiraIssue;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        jiraIssue = new JiraIssue();
        if (options.get(ISSUE_KEY) != null) {
            jiraIssue.setIssueKey(options.get(ISSUE_KEY).toString());
        }
        if (options.get(ISSUE_SUMMARY) != null) {
            jiraIssue.setSummary(options.get(ISSUE_SUMMARY).toString());
        }
        if (options.get(ISSUE_ASSIGNEE) != null) {
            jiraIssue.setAssignee(options.get(ISSUE_ASSIGNEE).toString());
        }
        String description = ConnectorOptions.extractOption(options, "description");
        if (description != null) {
            jiraIssue.setDescription(description);
        }
        if (options.get(ISSUE_COMPONENTS) != null) {
            String comps = options.get(ISSUE_COMPONENTS).toString();
            List<String> components = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(comps);
            jiraIssue.setComponents(components);
        }
        if (options.get(ISSUE_TYPE_ID) != null) {
            String issueTypeName = options.get(ISSUE_TYPE_ID).toString();
            try {
                int issueType = Integer.parseInt(issueTypeName);
                jiraIssue.setIssueTypeId((long) issueType);
            } catch (NumberFormatException e) {
                // the value is a type name
                jiraIssue.setIssueTypeName(issueTypeName);
            }
        }
        if (options.get(ISSUE_PRIORITY_ID) != null) {
            String issuePriorityName = options.get(ISSUE_PRIORITY_ID).toString();
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
        if (exchange.getIn().getBody() != null) {
            bodyIssue = (JiraIssue) exchange.getIn().getBody();
        } else {
            bodyIssue = new JiraIssue();
        }
        bodyIssue.replaceNullValues(jiraIssue);
        exchange.getIn().setHeader(ISSUE_KEY, bodyIssue.getIssueKey());
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
