/*
 * Copyright (C) 2017 Red Hat, Inc.
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

import org.junit.Test;

import static org.junit.Assert.*;

public class GitHubServiceImplTest {



    @Test
    public void sanitizeRepoName() throws Exception {
        GitHubService service = new GitHubServiceImpl();

        String data[] = {
            "bla", "bla",

            "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789",
            "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",

            "how-are-you", "how are you?",

            "yet-sth--with--spaceS", "yet sth  with !#ä spaceS",
        };

        for (int i = 0; i < data.length; i +=2) {
            assertEquals(data[i], service.sanitizeRepoName(data[i+1]));
        }
    }

}