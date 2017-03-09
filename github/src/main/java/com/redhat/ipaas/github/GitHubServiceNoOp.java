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
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * No-op implementation of a GitHubService
 */
@Service
@ConditionalOnProperty(value = "github.enabled", havingValue = "false")
public class GitHubServiceNoOp implements GitHubService {

    @Override
    public boolean createRepositoryIfMissing(String name) throws IOException {
        // Intentional empty
        return false;
    }

    @Override
    public String sanitizeRepoName(String name) {
        // No mangling
        return name;
    }

    @Override
    public void createOrUpdate(String repo, String message, Map<String, byte[]> files) {
        // intentional empty
    }

    @Override
    public boolean buildTriggerAsWebHook(String repoName, String bcName, String secret) throws IOException {
        return false;
    }
}
