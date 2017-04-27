/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ipaas.github;

import java.io.IOException;
import java.util.Map;

public interface GitHubService {

    /**
     * Create or update project files at a given GitHub repo. This typically consists of several steps. This
     * method is not supposed to be transactional in the sense in case of an error some changes at GitHub might
     * still persists.
     *
     * @param repoName repo to either create if not existent or to update
     * @param commitMessage the message to use for committing files.
     * @param fileContents map of files to add or update. Key are pathes within the repo, values is the content to write
     * @param webHookUrl an optional Webhook URL. If non-null a webhook is created with this url as callback URL
     * @return the repositories clone URL
     * @throws IOException if interaction with GitHub fails.
     */
    String createOrUpdateProjectFiles(String repoName, String commitMessage, Map<String, byte[]> fileContents, String webHookUrl) throws IOException;

    /**
     * Get the current user connected with the GitHub API access
     *
     * @return name of the current user
     */
    String getApiUser() throws IOException;

    /**
     * @param repoName repo to either create if not existent or to update
     * @return the repositories clone URL or null if it does not exist
     * @throws IOException if interaction with GitHub fails.
     */
    public String getCloneURL(String repoName) throws IOException;

}
