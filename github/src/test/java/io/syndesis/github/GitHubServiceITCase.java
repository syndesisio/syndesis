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

import io.fabric8.mockwebserver.DefaultMockServer;
import io.syndesis.git.GitProperties;
import io.syndesis.git.GitWorkflow;
import io.syndesis.github.backend.KeycloakProviderTokenAwareGitHubClient;
import org.assertj.core.api.Assertions;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GitHubServiceITCase {

    // Create a token using:
    //
    // curl https://api.github.com/authorizations --user "<USER_NAME>" --data '{"scopes":["repo", "delete_repo"],"note":"Syndesis-GitHubServiceITCase", "client_id":"<CLIENT_ID>", "client_secret":"<CLIENT_SECRET>" }'

    private static String authToken;

    // test project directory
    private static final String PROJECT_DIR = "/sample-github-project";
    private static final String REPO_NAME = "syndesis-itcase";

    private GitHubClient client;
    private GitHubServiceImpl githubService;
    private final GitWorkflow gitWorkflow = new GitWorkflow(new GitProperties());
    private DefaultMockServer webserver;
    @Rule
    public TestName testName = new TestName();

    @Before
    public void before() {

        authToken = System.getProperties().getProperty("github.oauth.token");

        Assume.assumeNotNull(authToken);
        Assume.assumeFalse("GitHub OAuth token needs to be specified in Java system property `github.oauth.token`", authToken.isEmpty());

        client = new KeycloakProviderTokenAwareGitHubClient();

        // Now that we have a client we create one of our GitHubService
        githubService = new GitHubServiceImpl(new RepositoryService(client), new UserService(client), gitWorkflow);

        webserver = new DefaultMockServer();
        webserver.start(1234);

        webserver.expect().get().withPath("/auth/realms/syndesis-it/broker/github/token").andReturn(
            200,
            "access_token=" + authToken + "&scope=public_repo%2Cuser%3Aemail&token_type=bearer"
        )
            .always();

        KeycloakAuthenticationToken keycloakAuthenticationToken = new KeycloakAuthenticationToken(
            new SimpleKeycloakAccount(
                new KeycloakPrincipal<RefreshableKeycloakSecurityContext>("testuser", null),
                null,
                new RefreshableKeycloakSecurityContext(
                    null,
                    null,
                    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6MTIzNC9hdXRoL3JlYWxtcy9zeW5kZXNpcy1pdCJ9.N0R7v55eiVXMzzhooYb1BStzyu_iP07wKNIO9z9dRMs",
                    null,
                    null,
                    null,
                    null
                )
            )
        );

        SecurityContextHolder.getContext().setAuthentication(keycloakAuthenticationToken);
    }

    @After
    public void after() {
        SecurityContextHolder.getContext().setAuthentication(null);

        if (webserver != null) {
            webserver.shutdown();
        }
    }

    @Test
    public void testGetApiUser() throws IOException {
        // Test that the token is usable by getting the username
        User apiUser = githubService.getApiUser();
        Assertions.assertThat(apiUser).isNotNull();
        Assertions.assertThat(apiUser.getLogin()).isNotNull().isNotBlank();
    }

    // Requires repo and delete_repo scope
    @Test
    public void testCreateNewRepository() throws IOException {
        String testRepo = REPO_NAME + "-create-new";
        Repository repository = githubService.getRepository(testRepo);
        if (repository != null) {
            User apiUser = githubService.getApiUser();
            client.delete("/repos/" + apiUser.getLogin() + "/" + testRepo);
            repository = githubService.getRepository(testRepo);
        }
        Assert.assertNull(repository); // repository should not exist on GitHub

        // Create Repository
        repository = githubService.createRepo(testRepo);
        Assertions.assertThat(repository).isNotNull();
        Assertions.assertThat(repository.getName()).isEqualTo(testRepo);
        System.out.println("Successfully created repository " + repository.getName());

        User apiUser = githubService.getApiUser();
        client.delete("/repos/" + apiUser.getLogin() + "/" + testRepo);
    }

    // Requires repo scope
    @Test
    public void testProjectCommit()
        throws IOException, IllegalStateException, GitAPIException, URISyntaxException {
        URL url = this.getClass().getResource(PROJECT_DIR);
        System.out.println("Reading sample project from " + url);
        //Read from classpath sample-github-project into map
        Map<String, byte[]> files = new HashMap<>();
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

        User user = githubService.getApiUser();
        String cloneURL = githubService.createOrUpdateProjectFiles(REPO_NAME, user,"my itcase initial message" + UUID.randomUUID().toString(), files, null);
        Assertions.assertThat(cloneURL).isNotNull().isNotBlank();

        File tmpDir = Files.createTempDirectory(testName.getMethodName()).toFile();
        tmpDir.deleteOnExit();
        Git clone = Git.cloneRepository().setDirectory(tmpDir).setURI(cloneURL).call();
        PersonIdent author = clone.log().call().iterator().next().getAuthorIdent();
        Assertions.assertThat(author).isNotNull();
        Assertions.assertThat(author.getName()).isNotNull().isNotBlank();
    }

}
