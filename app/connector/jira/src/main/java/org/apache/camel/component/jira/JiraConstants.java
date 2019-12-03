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
package org.apache.camel.component.jira;

public final class JiraConstants {

    public static final String JIRA = "jira";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String VERIFICATION_CODE = "verificationCode";
    public static final String JIRA_URL = "jiraUrl";
    public static final String PRIVATE_KEY = "privateKey";
    public static final String CONSUMER_KEY = "consumerKey";
    public static final String ISSUE_ASSIGNEE = "IssueAssignee";
    public static final String ISSUE_COMPONENTS = "IssueComponents";
    public static final String ISSUE_COMMENT = "IssueComment";
    public static final String ISSUE_KEY = "IssueKey";
    public static final String ISSUE_PRIORITY_ID = "IssuePriorityId";
    public static final String ISSUE_PRIORITY_NAME = "IssuePriorityName";
    public static final String ISSUE_PROJECT_KEY = "ProjectKey";
    public static final String ISSUE_SUMMARY = "IssueSummary";
    public static final String ISSUE_TRANSITION_ID = "IssueTransitionId";
    public static final String ISSUE_TYPE_ID = "IssueTypeId";
    public static final String ISSUE_TYPE_NAME = "IssueTypeName";
    public static final String ISSUE_WATCHERS_ADD = "IssueWatchersAdd";
    public static final String ISSUE_WATCHERS_REMOVE = "IssueWatchersRemove";
    public static final String JIRA_REST_CLIENT_FACTORY = "JiraRestClientFactory";

    private JiraConstants() {
        // prevent instantiation of utility class.
    }

}
