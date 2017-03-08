/*
 * Copyright (C) 2017 Red Hat, Inc.
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.springframework.stereotype.Service;

@Service
public class GitHubService {

    private final RepositoryService repoService;

    public GitHubService() {
        // Maybe make the service name configurable ?
        GitHubClient client = new GitHubClient("ipaas-github-proxy");
        repoService = new RepositoryService(client);
    }

    /**
     * Ensure that a given repository name exists. If it does not exist,
     * it will be created as a public repository
     *
     * @param name name of the repository to create. This name should
     *             be already a valid repo name. Must not be null.
     */
    public void ensureRepository(String name) throws IOException {
        if (!hasRepo(name)) {
            createRepo(name);
        }
    }

    /**
     * Convert a given name to GitHub acceptable repo name.
     *
     * @param name to sanitize, must not be null
     * @return sanitized name.
     */
    public String sanitizeRepoName(String name) {
        String ret = name.length() > 100 ? name.substring(0,100) : name;
        ret = ret.replace(" ","-");
        Pattern VALID_CHAR = Pattern.compile("^[a-zA-Z0-9\\-]$");
        return Pattern.compile("").splitAsStream(ret)
                      .filter(s -> VALID_CHAR.matcher(s).matches())
                      .collect(Collectors.joining());
    }

    /**
     * Create or update file in a given repo on the fly.
     *
     * @param repo to update
     * @param files map of files with the keys being relative paths within the the repo
     *              and the values is the content in bytes.
     */
    public void createOrUpdate(String repo, Map<String,byte[]> files) {

    }

    // =====================================================================================

    private void createRepo(String name) throws IOException {
        Repository repo = new Repository();
        repo.setName(name);
        repoService.createRepository(repo);
    }


    private boolean hasRepo(String name) throws IOException {
        for (Repository repo : repoService.getRepositories()) {
            if (name.equals(repo.getName())) {
                return true;
            }
        }
        return false;
    }
}
