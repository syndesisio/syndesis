/**
 * Copyright (C) 2016 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.github;

import io.syndesis.git.GitWorkflow;
import org.assertj.core.api.Assertions;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class GitHubServiceITCase {

    // Create a token using:
    //
    // curl https://api.github.com/authorizations --user "<USER_NAME>" --data '{"scopes":["repo", "delete_repo"],"note":"Syndesis-GitHubServiceITCase", "client_id":"<CLIENT_ID>", "client_secret":"<CLIENT_SECRET>" }'

    private static String authToken = null;

    // test project directory
    private static String PROJECT_DIR = "/sample-github-project";
    private static String REPO_NAME = "syndesis-itcase";

    private static GitHubClient client = null;
    private static GitHubService githubService = null;

    @BeforeClass
    public static void before() throws IOException {

        authToken = System.getProperties().getProperty("github.oauth.token");

        Assertions.assertThat(authToken).isNotNull().isNotBlank();

        // Setup credentials for GitHub client (Token should include "repo" and "delete_repo" scopes)
        client = new GitHubClient();
        client.setOAuth2Token(authToken);

        // Now that we have a client we create one of our GitHubService
        githubService = new GitHubServiceImpl(new RepositoryService(client), new UserService(client));

    }

    @Test
    public void testGetApiUser() throws IOException {
        // Test that the token is usable by getting the username
        String apiUser = githubService.getApiUser();
        Assertions.assertThat(apiUser).isNotNull().isNotBlank();
    }

    // Requires repo and delete_repo scope
    @Test
    public void testCreateNewRepository() throws IOException {
        String testRepo = REPO_NAME + "-create-new";
        Repository repository = ((GitHubServiceImpl) githubService).getRepository(testRepo);
        if (repository != null) {
            String apiUser = githubService.getApiUser();
            client.delete("/repos/" + apiUser + "/" + testRepo);
            repository = ((GitHubServiceImpl) githubService).getRepository(testRepo);
        }
        Assert.assertNull(repository); // repository should not exist on GitHub

        // Create Repository
        repository = ((GitHubServiceImpl) githubService).createRepo(testRepo);
        Assertions.assertThat(repository).isNotNull();
        Assertions.assertThat(repository.getName()).isEqualTo(testRepo);
        System.out.println("Successfully created repository " + repository.getName());

        String apiUser = githubService.getApiUser();
        client.delete("/repos/" + apiUser + "/" + testRepo);
    }

    // Requires repo scope
    @Test
    public void testProjectCommit()
        throws IOException, IllegalStateException, GitAPIException, URISyntaxException {
        URL url = this.getClass().getResource(PROJECT_DIR);
        System.out.println("Reading sample project from " + url);
        //Read from classpath sample-github-project into map
        Map<String, byte[]> files = new HashMap<String, byte[]>();
        Files.find(Paths.get(url.getPath()), Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())
            .forEach(filePath -> {
                if (!filePath.startsWith(".git")) {
                    byte[] content;
                    try {
                        content = Files.readAllBytes(filePath);
                        String file = filePath.toString()
                            .substring(filePath.toString().indexOf(PROJECT_DIR) + PROJECT_DIR.length() + 1);
                        files.put(file, content);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

        GitWorkflow wf = new GitWorkflow();
        Repository repository = ((GitHubServiceImpl) githubService).getRepository(REPO_NAME);
        RevCommit commit = null;
        if (repository == null) {
            repository = ((GitHubServiceImpl) githubService).createRepo(REPO_NAME);
            commit = wf.createFiles(repository.getHtmlUrl(), repository.getName(), "my itcase initial message", files,
                new UsernamePasswordCredentialsProvider(authToken, ""));

        } else {
            commit = wf.updateFiles(repository.getHtmlUrl(), repository.getName(), "my itcase update message", files,
                new UsernamePasswordCredentialsProvider(authToken, ""));
        }
        Path workingDir = Files.createTempDirectory(repository.getName());
        Git git = Git.cloneRepository().setDirectory(workingDir.toFile()).setURI(repository.getHtmlUrl()).call();
        RevCommit lastCommit = git.log().setMaxCount(1).call().iterator().next();
        Assertions.assertThat(lastCommit.getId()).isEqualTo(commit.getId());
    }

}
