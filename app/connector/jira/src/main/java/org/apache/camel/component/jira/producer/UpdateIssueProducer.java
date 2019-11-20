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
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.component.jira.JiraEndpoint;
import org.apache.camel.support.DefaultProducer;
import org.apache.camel.util.ObjectHelper;

import static org.apache.camel.component.jira.JiraConstants.ISSUE_ASSIGNEE;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_COMPONENTS;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_KEY;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_PRIORITY_ID;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_PRIORITY_NAME;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_SUMMARY;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_TYPE_ID;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_TYPE_NAME;

public class UpdateIssueProducer extends DefaultProducer {

    public UpdateIssueProducer(JiraEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public void process(Exchange exchange) {
        String issueKey = exchange.getIn().getHeader(ISSUE_KEY, String.class);
        if (issueKey == null) {
            throw new IllegalArgumentException("Missing exchange input header named \'IssueKey\', it should specify the issue key.");
        }

        JiraRestClient client = ((JiraEndpoint) getEndpoint()).getClient();
        String issueTypeName = exchange.getIn().getHeader(ISSUE_TYPE_NAME, String.class);
        Long issueTypeId = Optional.ofNullable(exchange.getIn().getHeader(ISSUE_TYPE_ID, Long.class))
                                   .orElseGet(() -> IssueProducerHelper.getIssueTypeIdByName(client, issueTypeName));
        String summary = exchange.getIn().getHeader(ISSUE_SUMMARY, String.class);
        String assigneeName = exchange.getIn().getHeader(ISSUE_ASSIGNEE, String.class);
        String priorityName = exchange.getIn().getHeader(ISSUE_PRIORITY_NAME, String.class);
        Long priorityId = Optional.ofNullable(exchange.getIn().getHeader(ISSUE_PRIORITY_ID, Long.class))
                                  .orElseGet(() -> IssueProducerHelper.getPriorityIdByName(client, priorityName));

        List<?> components = exchange.getIn().getHeader(ISSUE_COMPONENTS, List.class);

        IssueInputBuilder builder = new IssueInputBuilder();
        if (issueTypeId != null) {
            builder.setIssueTypeId(issueTypeId);
        }
        if (summary != null) {
            builder.setSummary(summary);
        }
        String description = exchange.getIn().getBody(String.class);
        if (description != null) {
            builder.setDescription(description);
        }
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
        issueClient.updateIssue(issueKey, builder.build()).claim();
    }
}
