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

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Priority;

/**
 * @author Christoph Deppisch
 */
final class IssueProducerHelper {

    private IssueProducerHelper() {
        // prevent instantiation of utility class.
    }

    static Long getIssueTypeIdByName(JiraRestClient client, String issueTypeName) {
        if (issueTypeName == null) {
            return null;
        }

        // search for issueTypeId from an issueTypeName
        Iterable<IssueType> issueTypes = client.getMetadataClient().getIssueTypes().claim();
        for (IssueType type: issueTypes) {
            if (issueTypeName.equals(type.getName())) {
                return type.getId();
            }
        }

        return null;
    }

    static Long getPriorityIdByName(JiraRestClient client, String priorityName) {
        if (priorityName == null) {
            return null;
        }

        // search for priorityId from an priorityName
        Iterable<Priority> priorities = client.getMetadataClient().getPriorities().claim();
        for (Priority pri: priorities) {
            if (priorityName.equals(pri.getName())) {
                return pri.getId();
            }
        }

        return null;
    }
}
