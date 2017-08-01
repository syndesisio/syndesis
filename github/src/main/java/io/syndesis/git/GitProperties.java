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
package io.syndesis.git;

import org.springframework.boot.context.properties.ConfigurationProperties;
import sun.security.action.GetPropertyAction;

import java.nio.file.Paths;
import java.security.AccessController;


/**
 * Git specific properties, set it in application.yml with a prefix "git."
 */
@ConfigurationProperties("git")
public class GitProperties {

    private String localGitRepoPath = Paths.get(AccessController.doPrivileged(new GetPropertyAction("java.io.tmpdir"))).toString();

    public String getLocalGitRepoPath() {
        return localGitRepoPath;
    }

    public void setLocalGitRepoPath(String localGitRepoPath) {
        this.localGitRepoPath = localGitRepoPath;
    }

}
