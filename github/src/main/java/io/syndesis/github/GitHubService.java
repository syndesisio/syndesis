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
package io.syndesis.github;

import org.eclipse.egit.github.core.User;

import java.io.IOException;

public interface GitHubService {

    /**
     * Create or update project files at a given GitHub repo. This typically consists of several steps. This
     * method is not supposed to be transactional in the sense in case of an error some changes at GitHub might
     * still persists.
     *
     * @param request The container object that includes all create or update parameters
     * @return the repositories clone URL
     * @throws IOException if interaction with GitHub fails.
     */
    String createOrUpdateProjectFiles(GithubRequest request);

    /**
     * Get the current user connected with the GitHub API access
     *
     * @return the current user
     */
    User getApiUser() throws IOException;

    /**
     * @param repoName repo to either create if not existent or to update
     * @return the repositories clone URL or null if it does not exist
     * @throws IOException if interaction with GitHub fails.
     */
    String getCloneURL(String repoName) throws IOException;

}
