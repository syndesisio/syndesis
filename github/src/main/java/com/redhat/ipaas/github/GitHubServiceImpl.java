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

import javax.annotation.PostConstruct;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * @author roland
 * @since 08/03/2017
 */
@Service
@ConditionalOnProperty(value = "github.enabled", matchIfMissing = true, havingValue = "true")
public class GitHubServiceImpl implements GitHubService {

    private RepositoryService repoService;

    @Value("github.service")
    private String gitHubHost;

    @PostConstruct
    public void init() {
        // Maybe make the service name configurable ?
        GitHubClient client = new GitHubClient(gitHubHost != null ? gitHubHost : "ipaas-github-proxy");
        repoService = new RepositoryService(client);
    }


    @Override
    public void ensureRepository(String name) throws IOException {
        if (!hasRepo(name)) {
            createRepo(name);
        }
    }

    @Override
    public String sanitizeRepoName(String name) {
        String ret = name.length() > 100 ? name.substring(0,100) : name;
        return ret.replace(" ","-")
                  .toLowerCase()
                  .chars()
                  .filter(this::isValidRepoChar)
                  .collect(StringBuilder::new,
                           StringBuilder::appendCodePoint,
                           StringBuilder::append)
                  .toString();
    }

    @Override
    public void createOrUpdate(String repo, Map<String, byte[]> files) {
        // TODO: Still to implement
    }

    // =====================================================================================

    private boolean isValidRepoChar(int c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= '0' && c <= '9') ||
               c == '-';
    }

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
