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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.ipaas.github.backend.ExtendedContentsService;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.RepositoryHook;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
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

    private final RepositoryService repositoryService;
    private final ExtendedContentsService contentsService;

    @Value("${github.webhook.callbackBaseUrl}")
    private String openShiftBaseUrl;

    @Value("${github.webhook.namespace}")
    private String namespace;

    @Value("${github.webhook.enabled}")
    private boolean webhookEnabled;

    public GitHubServiceImpl(RepositoryService repositoryService, ExtendedContentsService contentsService) {
        this.repositoryService = repositoryService;
        this.contentsService = contentsService;
    }

    @Override
    public boolean createRepositoryIfMissing(String name) throws IOException {
        if (!hasRepo(name)) {
            createRepo(name);
            return true;
        } else {
            return false;
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
    public void createOrUpdate(String repoName, String message, Map<String, byte[]> files) throws IOException {
        Repository repo = getMandatoryRepository(repoName);
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            createOrUpdate(repo, message, entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean buildTriggerAsWebHook(String repoName, String bcName, String secret) throws IOException {
        if (webhookEnabled && openShiftBaseUrl.length() > 0) {
            Repository repo = getMandatoryRepository(repoName);
            RepositoryHook hook = prepareRepositoryHookRequest(bcName, secret);
            repositoryService.createHook(repo, hook);
            return true;
        }
        return false;
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

    private Repository getMandatoryRepository(String repo) throws IOException {
        Repository repository = getRepository(repo);
        if (repository == null) {
            throw new IOException("No repo " + repo + " exists");
        }
        return repository;
    }

    private boolean hasRepo(String name) throws IOException {
        return getRepository(name) != null;
    }

    private RepositoryHook prepareRepositoryHookRequest(String bcName, String secret) {
        RepositoryHook hook = new RepositoryHook();
        Map<String, String> config = new HashMap<>();
        String openShiftUrl = String.format(
            "%s/oapi1/v1/namespaces/%s/buildconfigs/%s/webhooks/%s/github",openShiftBaseUrl,namespace,bcName,secret);
        config.put("url", openShiftUrl);
        config.put("content_type", "json");
        config.put("secret", "secret");
        hook.setConfig(config);
        hook.setName("web");
        hook.setActive(true);
        return hook;
    }

    private void createRepo(String name) throws IOException {
        Repository repo = new Repository();
        repo.setName(name);
        repositoryService.createRepository(repo);
    }

    private void createOrUpdate(Repository repo, String message, String path, byte[] content) throws IOException {
        String sha = getFileSha(repo, path);
        if (sha != null) {
            contentsService.updateFile(repo, message, path, sha, content);
        } else {
            contentsService.createFile(repo, message, path, content);
        }
    }

    private String getFileSha(Repository repository, String path) throws IOException {
        try {
            List<RepositoryContents> contents = contentsService.getContents(repository, path);
            if (contents ==  null) {
                return null;
            }
            if (contents.size() > 1) {
                throw new IllegalArgumentException("Given path " + path + " doesn't specify a file");
            }
            return contents.get(0).getSha();
        } catch (RequestException e) {
            if (e.getStatus() != HttpStatus.NOT_FOUND.value()) {
                throw e;
            }
            return null;
        }
    }

    private boolean isValidRepoChar(int c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= '0' && c <= '9') ||
               (c == '-');
    }
}
