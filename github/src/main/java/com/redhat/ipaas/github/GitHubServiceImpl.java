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

import com.redhat.ipaas.github.backend.ExtendedContentsService;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

/**
 * @author roland
 * @since 08/03/2017
 */
@Service
@RequestScope
@ConditionalOnProperty(value = "github.enabled", matchIfMissing = true, havingValue = "true")
public class GitHubServiceImpl implements GitHubService {

    @Value("${github.service}")
    private String gitHubHost = "ipaas-github-proxy";

    private final RepositoryService repositoryService;
    private final ExtendedContentsService contentsService;

    public GitHubServiceImpl(RepositoryService repositoryService, ExtendedContentsService contentsService) {
        this.repositoryService = repositoryService;
        this.contentsService = contentsService;
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

    private Repository getRepository(String name) throws IOException {
        for (Repository repo : repositoryService.getRepositories()) {
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

    private boolean hasRepo(String name) throws IOException {
        return getRepository(name) != null;
    }

    private void createRepo(String name) throws IOException {
        Repository repo = new Repository();
        repo.setName(name);
        repositoryService.createRepository(repo);
    }

    private void createOrUpdate(String repo, String message, String path, byte[] content) throws IOException {
        String sha = getFileSha(repo, path);
        if (sha != null) {
            updateFile(repo, message, path, sha, content);
        } else {
            createFile(repo, message, path, content);
        }
    }

    private void createFile(String repo, String message, String path, byte[] content) throws IOException {
        Repository repository = getMandatoryRepository(repo, path);
        contentsService.createFile(repository, message, path, content);
    }

    private void updateFile(String repo, String message, String path, String sha, byte[] content) throws IOException {
        Repository repository = getMandatoryRepository(repo, path);
        contentsService.updateFile(repository, message, path, sha, content);
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

    private boolean isValidRepoChar(int c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= '0' && c <= '9') ||
               (c == '-');
    }
}
