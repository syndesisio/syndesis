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

import com.redhat.ipaas.github.backend.ExtendedContentsService;
import com.redhat.ipaas.github.backend.KeycloakTokenAwareGitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

/**
 * Configured in spring.factories so that tis configuration is automatically picked
 * up when included in the classpath.
 *
 * Beans are created in request scope because tokens have to be set in the clients afresh for each request.
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties(GitHubProperties.class)
public class GitHubConfiguration {

    @Bean
    public RepositoryService repositoryService(GitHubProperties props) {
        return new RepositoryService(new KeycloakTokenAwareGitHubClient(props.getService()));
    }

    @Bean
    public ExtendedContentsService contentsService(GitHubProperties props) {
        return new ExtendedContentsService(new KeycloakTokenAwareGitHubClient(props.getService()));
    }

    @Bean
    public UserService userService(GitHubProperties props) {
        return new UserService(new KeycloakTokenAwareGitHubClient(props.getService()));
    }

}
