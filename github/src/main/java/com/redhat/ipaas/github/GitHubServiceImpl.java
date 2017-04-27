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
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author roland
 * @since 08/03/2017
 */
@Service
@ConditionalOnProperty(value = "github.enabled", matchIfMissing = true, havingValue = "true")
public class GitHubServiceImpl implements GitHubService {

    private final RepositoryService repositoryService;
    private final ExtendedContentsService contentsService;
    private final UserService userService;

    public GitHubServiceImpl(RepositoryService repositoryService, ExtendedContentsService contentsService, UserService userService) {
        this.repositoryService = repositoryService;
        this.contentsService = contentsService;
        this.userService = userService;
    }

    @Override
    public String createOrUpdateProjectFiles(String repoName, String commitMessage, Map<String, byte[]> fileContents, String webHookUrl) throws IOException {
        Repository repository = getRepository(repoName);
        if (repository == null) {
            // New Repo
            repository = createRepo(repoName);
            // Add files
            createOrUpdateFiles(repository, commitMessage, fileContents);
            // Set WebHook
            createWebHookAsBuildTrigger(repository, webHookUrl);
        } else {
            // Only create or update files
            createOrUpdateFiles(repository, commitMessage, fileContents);
        }
        return repository.getCloneUrl();
    }

    @Override
    public String getCloneURL(String repoName) throws IOException {
        Repository repository = getRepository(repoName);
        if( repository == null ) {
            return null;
        }
        return repository.getCloneUrl();
    }

    @Override
    public String getApiUser() throws IOException {
        return userService.getUser().getLogin();
    }

    // =====================================================================================

    private Repository getRepository(String name) throws IOException {
        User user = userService.getUser();
        try {
            return repositoryService.getRepository(user.getLogin(), name);
        } catch (RequestException e) {
            if (e.getStatus() != HttpStatus.NOT_FOUND.value()) {
                throw e;
            }
            return null;
        }
    }

    private Repository createRepo(String name) throws IOException {
        Repository repo = new Repository();
        repo.setName(name);
        return repositoryService.createRepository(repo);
    }

    private void createOrUpdateFiles(Repository repo, String message, Map<String, byte[]> files) throws IOException {
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            // Wait a bit to let GitHub catch up
            // See http://stackoverflow.com/questions/19576601/github-api-issue-with-file-upload for details
            sleep(1000L);
            createOrUpdateFile(repo, message, entry.getKey(), entry.getValue());
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {}
    }

    private void createWebHookAsBuildTrigger(Repository repository, String url) throws IOException {
        if (url != null && url.length() > 0) {
            RepositoryHook hook = prepareRepositoryHookRequest(url);
            repositoryService.createHook(repository, hook);
        }
    }

    private RepositoryHook prepareRepositoryHookRequest(String url) {
        RepositoryHook hook = new RepositoryHook();
        Map<String, String> config = new HashMap<>();
        config.put("url", url);
        config.put("content_type", "json");
        hook.setConfig(config);
        hook.setName("web");
        hook.setActive(true);
        return hook;
    }

    private void createOrUpdateFile(Repository repo, String message, String path, byte[] content) throws IOException {
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
}
