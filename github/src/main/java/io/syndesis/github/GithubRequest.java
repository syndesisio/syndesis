/*
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
import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;

@Value.Immutable(copy = false)
//We adding copy = false, to avoid issues with findbugs:
// https://github.com/immutables/immutables/issues/263
// http://immutables.github.io/immutable.html#copy-methods
public interface GithubRequest {

    String getRepoName();
    User getAuthor();
    String getCommitMessage();
    Map<String, byte[]> getFileContents();
    Optional<String> getWebHookUrl();

    class Builder extends ImmutableGithubRequest.Builder {

    }
}
