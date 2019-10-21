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
package org.apache.camel.component.jira.producer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.component.jira.JiraEndpoint;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ObjectHelper;

import static org.apache.camel.component.jira.JiraConstants.ISSUE_ASSIGNEE;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_COMPONENTS;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_PRIORITY_ID;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_PRIORITY_NAME;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_PROJECT_KEY;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_SUMMARY;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_TYPE_ID;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_TYPE_NAME;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_WATCHERS_ADD;

public class AddIssueProducer extends DefaultProducer {

    public AddIssueProducer(JiraEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    public void process(Exchange exchange) {
        // required fields
        String projectKey = exchange.getIn().getHeader(ISSUE_PROJECT_KEY, String.class);
        if (projectKey == null) {
            throw new IllegalArgumentException("A valid project key is required.");
        }

        String summary = exchange.getIn().getHeader(ISSUE_SUMMARY, String.class);
        if (summary == null) {
            throw new IllegalArgumentException("A summary field is required, actual value is null.");
        }

        JiraRestClient client = ((JiraEndpoint) getEndpoint()).getClient();
        String issueTypeName = exchange.getIn().getHeader(ISSUE_TYPE_NAME, String.class);
        Long issueTypeId = Optional.ofNullable(exchange.getIn().getHeader(ISSUE_TYPE_ID, Long.class))
                                   .orElseGet(() -> IssueProducerHelper.getIssueTypeIdByName(client, issueTypeName));
        if (issueTypeId == null) {
            throw new IllegalArgumentException("A valid issue type id is required, actual: id is null, name(" + issueTypeName + ")");
        }

        // optional fields
        String assigneeName = exchange.getIn().getHeader(ISSUE_ASSIGNEE, String.class);
        String priorityName = exchange.getIn().getHeader(ISSUE_PRIORITY_NAME, String.class);
        Long priorityId = Optional.ofNullable(exchange.getIn().getHeader(ISSUE_PRIORITY_ID, Long.class))
                                  .orElseGet(() -> IssueProducerHelper.getPriorityIdByName(client, priorityName));
        List<?> components = exchange.getIn().getHeader(ISSUE_COMPONENTS, List.class);
        List<?> watchers = exchange.getIn().getHeader(ISSUE_WATCHERS_ADD, List.class);

        IssueInputBuilder builder = new IssueInputBuilder(projectKey, issueTypeId);
        builder.setDescription(exchange.getIn().getBody(String.class));
        builder.setSummary(summary);
        if (ObjectHelper.isNotEmpty(components)) {
            builder.setComponentsNames(components.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toList()));
        }
        if (priorityId != null) {
            builder.setPriorityId(priorityId);
        }
        if (assigneeName != null) {
            builder.setAssigneeName(assigneeName);
        }

        IssueRestClient issueClient = client.getIssueClient();
        BasicIssue issueCreated = issueClient.createIssue(builder.build()).claim();
        Issue issue = issueClient.getIssue(issueCreated.getKey()).claim();
        addWatchers(issue, issueClient, watchers);

        // support InOut
        if (exchange.getPattern().isOutCapable()) {
            // copy the header of in message to the out message
            exchange.getOut().copyFrom(exchange.getIn());
            exchange.getOut().setBody(issue);
        } else {
            exchange.getIn().setBody(issue);
        }
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    private void addWatchers(Issue issue, IssueRestClient issueClient, List<?> watchers) {
        if (ObjectHelper.isNotEmpty(watchers) && issue.getWatchers() != null) {
            for (Object watcher: watchers) {
                issueClient.addWatcher(issue.getWatchers().getSelf(), Optional.ofNullable(watcher)
                        .map(Object::toString)
                        .orElse(""));
            }
        }
    }
}
