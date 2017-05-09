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
package io.syndesis.github.backend;

import java.io.IOException;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.ContentsService;

import static org.eclipse.egit.github.core.client.IGitHubConstants.*;

/**
 * Extend ContentsService for creating and updating files
 */
public class ExtendedContentsService extends ContentsService {

    public ExtendedContentsService(GitHubClient client) {
        super(client);
    }

	public void createFile(IRepositoryIdProvider repository, String message, String path, byte[] content) throws IOException {
		StringBuilder uri = createContentsUri(repository, path);
		client.put(uri.toString(), new FileContent(path, message, content, null),null /* no response serialization */);
	}

	public void updateFile(IRepositoryIdProvider repository, String message, String path, String sha, byte[] content) throws IOException {
		StringBuilder uri = createContentsUri(repository, path);
		client.put(uri.toString(), new FileContent(path, message, content, sha),null /* no response serialization */);
    }

	private StringBuilder createContentsUri(IRepositoryIdProvider repository, String path) {
		String id = getId(repository);
		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_CONTENTS);
		if (path != null && path.length() > 0) {
			if (path.charAt(0) != '/')
				uri.append('/');
			uri.append(path);
		}
		return uri;
	}
}

