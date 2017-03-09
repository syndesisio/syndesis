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
     * Ensure that a given repository name exists. If it does not exist,
     * it will be created as a public repository
     *
     * @param name name of the repository to create. This name should
     *             be already a valid repo name. Must not be null.
     * @return true if a repository has been created, false if it already exists
     */
    boolean createRepositoryIfMissing(String name) throws IOException;

    /**
     * Convert a given name to GitHub acceptable repo name.
     *
     * @param name to sanitize, must not be null
     * @return sanitized name.
     */
    String sanitizeRepoName(String name);

    /**
     * Create or update file in a given repo on the fly.
     *
     * @param repo to update
     * @param message commit message to use
     * @param files map of files with the keys being relative paths within the the repo
     *              and the values is the content in bytes.
     */
    void createOrUpdate(String repo, String message, Map<String, byte[]> files) throws IOException;

    /**
     * Create a GitHub web hook for triggering a build on an initial commit
     *
     * @param repoName repository name
     * @param bcName build config name
     * @param secret secret to use for the trigger
     *
     * @return true if a web hook has been created, false if setting webhooks is disabled.
     */
    boolean buildTriggerAsWebHook(String repoName, String bcName, String secret) throws IOException;
    }
