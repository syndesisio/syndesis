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

import org.eclipse.egit.github.core.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * No-op implementation of a GitHubService
 */
@Service
@ConditionalOnProperty(value = "github.enabled", havingValue = "false")
public class GitHubServiceNoOp implements GitHubService {

    @Override
    public String createOrUpdateProjectFiles(GithubRequest request) {
        // Dummy value
        return "https://this.doesnt.exist/promise.git";
    }

    @Override
    public User getApiUser() {
        return new User().setLogin("noob").setName("Noob").setEmail("noob@noob");
    }

    @Override
    public String getCloneURL(String repoName) throws IOException {
        return null;
    }
}
