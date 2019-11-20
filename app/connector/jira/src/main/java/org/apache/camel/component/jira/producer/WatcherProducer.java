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
import java.util.Optional;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import org.apache.camel.Exchange;
import org.apache.camel.component.jira.JiraEndpoint;
import org.apache.camel.support.DefaultProducer;
import org.apache.camel.util.ObjectHelper;

import static org.apache.camel.component.jira.JiraConstants.ISSUE_KEY;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_WATCHERS_ADD;
import static org.apache.camel.component.jira.JiraConstants.ISSUE_WATCHERS_REMOVE;

public class WatcherProducer extends DefaultProducer {

    public WatcherProducer(JiraEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public void process(Exchange exchange) {
        String issueKey = exchange.getIn().getHeader(ISSUE_KEY, String.class);
        if (issueKey == null) {
            throw new IllegalArgumentException("Missing exchange input header named \'IssueKey\', it should specify the issue key to add/remove watchers to.");
        }

        List<?> watchersAdd = exchange.getIn().getHeader(ISSUE_WATCHERS_ADD, List.class);
        List<?> watchersRemove = exchange.getIn().getHeader(ISSUE_WATCHERS_REMOVE, List.class);
        JiraRestClient client = ((JiraEndpoint) getEndpoint()).getClient();
        boolean hasWatchersToAdd = ObjectHelper.isNotEmpty(watchersAdd);
        boolean hasWatchersToRemove = ObjectHelper.isNotEmpty(watchersRemove);
        if (hasWatchersToAdd || hasWatchersToRemove) {
            IssueRestClient issueClient = client.getIssueClient();
            Issue issue = issueClient.getIssue(issueKey).claim();
            if (hasWatchersToAdd) {
                for (Object watcher: watchersAdd) {
                    issueClient.addWatcher(issue.getWatchers().getSelf(), Optional.ofNullable(watcher).map(Object::toString).orElse(""));
                }
            }
            if (hasWatchersToRemove) {
                for (Object watcher: watchersRemove) {
                    issueClient.removeWatcher(issue.getWatchers().getSelf(), Optional.ofNullable(watcher).map(Object::toString).orElse(""));
                }
            }
        }
    }
}
