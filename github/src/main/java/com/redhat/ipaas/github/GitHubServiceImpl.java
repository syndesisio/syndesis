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
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.redhat.ipaas.github.extended.ExtendedContentsService;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
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
    private ExtendedContentsService contentsService;

    @Value("github.service")
    private String gitHubHost;

    @PostConstruct
    public void init() {
        GitHubClient client = new GitHubClient(gitHubHost != null ? gitHubHost : "ipaas-github-proxy");
        repoService = new RepositoryService(client);
        contentsService = new ExtendedContentsService(client);
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
    public void createOrUpdate(String repo, String message, Map<String, byte[]> files) throws IOException {
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            createOrUpdate(repo, message, entry.getKey(), entry.getValue());
        }
    }

    // =====================================================================================

    private boolean isValidRepoChar(int c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= '0' && c <= '9') ||
               (c == '-');
    }

    private void createRepo(String name) throws IOException {
        Repository repo = new Repository();
        repo.setName(name);
        repoService.createRepository(repo);
    }

    private boolean hasRepo(String name) throws IOException {
        return getRepository(name) != null;
    }

    private Repository getRepository(String name) throws IOException {
        for (Repository repo : repoService.getRepositories()) {
            if (name.equals(repo.getName())) {
                return repo;
            }
        }
        return null;
    }

    private Repository getMandatoryRepository(String repo, String path) throws IOException {
        Repository repository = getRepository(repo);
        if (repository == null) {
            throw new IOException("No repo " + repo + " exists for looking up file " + path);
        }
        return repository;
    }

    private void createOrUpdate(String repo, String message, String path, byte[] content) throws IOException {
        String sha = getFileSha(repo, path);
        if (sha != null) {
            updateFile(repo, message, path, sha, content);
        } else {
            createFile(repo, message, path, content);
        }
    }

    private String getFileSha(String repo, String path) throws IOException {
        Repository repository = getMandatoryRepository(repo, path);
        List<RepositoryContents> contents = contentsService.getContents(repository, path);
        if (contents ==  null) {
            return null;
        }
        if (contents.size() > 1) {
            throw new IllegalArgumentException("Given path " + path + " doesn't specify a file");
        }
        return contents.get(0).getSha();
    }

    private void createFile(String repo, String message, String path, byte[] content) throws IOException {
        Repository repository = getMandatoryRepository(repo, path);
        contentsService.createFile(repository, message, path, content);
    }

    private void updateFile(String repo, String message, String path, String sha, byte[] content) throws IOException {
        Repository repository = getMandatoryRepository(repo, path);
        contentsService.updateFile(repository, message, path, sha, content);
    }
}
