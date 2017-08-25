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

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import io.syndesis.core.Tokens;
import io.syndesis.git.GitWorkflow;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryHook;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
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

    private static final LocalDate GITHUB_NOREPLY_EMAIL_CUTOFF = LocalDate.of(2017, 7, 18);

    private final RepositoryService repositoryService;
    private final UserService userService;
    private final GitWorkflow gitWorkflow;

    public GitHubServiceImpl(RepositoryService repositoryService, UserService userService, GitWorkflow gitWorkflow) {
        this.repositoryService = repositoryService;
        this.userService = userService;
        this.gitWorkflow = gitWorkflow;
    }

    @Override
    public String createOrUpdateProjectFiles(String repoName, User author, String commitMessage, Map<String, byte[]> fileContents, String webHookUrl) throws IOException {
        Repository repository = getRepository(repoName);
        if (repository == null) {
            // New Repo
            repository = createRepo(repoName);
            // Add files
            createOrUpdateFiles(repository, author, commitMessage, fileContents);
            // Set WebHook
            createWebHookAsBuildTrigger(repository, webHookUrl);
        } else {
            // Only create or update files
            createOrUpdateFiles(repository, author, commitMessage, fileContents);
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
    public User getApiUser() throws IOException {
        final User user = userService.getUser();
        // if the user did not elect to publicly display his e-mail address, e-mail will be null
        // https://developer.github.com/v3/users/#get-a-single-user
        // let's put a dummy e-mail address then, as it is needed for the commit
        if (user.getEmail() == null) {
            // users before 2017-07-18 have their no-reply e-mail addresses in the form
            // username@users.noreply.github.com, and after that date
            // id+username@users.noreply.github.com
            // https://help.github.com/articles/about-commit-email-addresses/
            final LocalDate createdAt = user.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (createdAt.isAfter(GITHUB_NOREPLY_EMAIL_CUTOFF)) {
                user.setEmail(user.getId() + "+" + user.getLogin() + "@users.noreply.github.com");
            } else {
                user.setEmail(user.getLogin() + "@users.noreply.github.com");
            }
        }

        return user;
    }

    // =====================================================================================

    protected Repository getRepository(String name) throws IOException {
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

    protected Repository createRepo(String name) throws IOException {
        Repository repo = new Repository();
        repo.setName(name);
        return repositoryService.createRepository(repo);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod") // PMD false positive
    private void createOrUpdateFiles(Repository repo, User author, String message, Map<String, byte[]> files) throws IOException {
        Repository repository = getRepository(repo.getName());
        if (repository == null) {
            createRepo(repo.getName());
            gitWorkflow.createFiles(repo.getHtmlUrl(), repo.getName(), author, message, files, new UsernamePasswordCredentialsProvider(Tokens.fetchProviderTokenFromKeycloak(Tokens.TokenProvider.GITHUB), "") );
        } else {
            gitWorkflow.updateFiles(repo.getHtmlUrl(), repo.getName(), author, message, files, new UsernamePasswordCredentialsProvider(Tokens.fetchProviderTokenFromKeycloak(Tokens.TokenProvider.GITHUB), "") );
        }
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
}
