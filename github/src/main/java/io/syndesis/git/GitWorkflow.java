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
package io.syndesis.git;

import io.syndesis.core.SyndesisServerException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
@ConditionalOnProperty(value = "git.enabled", matchIfMissing = true, havingValue = "true")
public class GitWorkflow {

    private static final Logger log = LoggerFactory.getLogger(GitWorkflow.class);

    private final GitProperties gitProperties;

    public GitWorkflow(GitProperties gitProperties) {
        this.gitProperties = gitProperties;
    }

    /**
     * Creates a new remote git repository and does the initial commit&push of all the project files
     * the files to it.
     *
     * @param remoteGitRepoHttpUrl- the HTML (not ssh) url to a git repository
     * @param repoName              - the name of the git repository
     * @param message-              commit message
     * @param files-                map of file paths along with their content
     * @param credentials-          Git credentials, for example username/password, authToken, personal access token
     * @return RevCommit, the commit info
     */
    public RevCommit createFiles(String remoteGitRepoHttpUrl, String repoName, String message, Map<String, byte[]> files,
                                 UsernamePasswordCredentialsProvider credentials) {

        try {
            // create temporary directory
            Path workingDir = Files.createTempDirectory(Paths.get(gitProperties.getLocalGitRepoPath()), repoName);
            if (log.isDebugEnabled()) {
                log.debug("Created temporary directory {}", workingDir.toString());
            }

            // git init
            Git git = Git.init().setDirectory(workingDir.toFile()).call();
            writeFiles(workingDir, files);

            RemoteAddCommand remoteAddCommand = git.remoteAdd();
            remoteAddCommand.setName("origin");
            remoteAddCommand.setUri(new URIish(remoteGitRepoHttpUrl));
            remoteAddCommand.call();

            RevCommit commit = commitAndPush(git, message, credentials);
            removeWorkingDir(workingDir);
            return commit;

        } catch (Exception e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    /**
     * Updates an existing git repository with the current version of project files.
     *
     * @param remoteGitRepoHttpUrl- the HTML (not ssh) url to a git repository
     * @param repoName              - the name of the git repository
     * @param message-              commit message
     * @param files-                map of file paths along with their content
     * @param credentials-          Git credentials, for example username/password, authToken, personal access token
     * @return RevCommit, the commit info
     */
    public RevCommit updateFiles(String remoteGitRepoHttpUrl, String repoName, String message, Map<String, byte[]> files,
                                 UsernamePasswordCredentialsProvider credentials) {

        try {
            // create temporary directory
            Path workingDir = Files.createTempDirectory(Paths.get(gitProperties.getLocalGitRepoPath()), repoName);
            if (log.isDebugEnabled()) {
                log.debug("Created temporary directory {}", workingDir.toString());
            }

            // git clone
            Git git = Git.cloneRepository().setDirectory(workingDir.toFile()).setURI(remoteGitRepoHttpUrl).call();
            writeFiles(workingDir, files);

            RevCommit commit = commitAndPush(git, message, credentials);
            removeWorkingDir(workingDir);

            return commit;
        } catch (Exception e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    private void removeWorkingDir(Path workingDir) throws IOException {
        // cleanup tmp dir
        if (!FileSystemUtils.deleteRecursively(workingDir.toFile())) {
            log.warn("Could not delete temporary directory {}", workingDir);
        }
    }

    /**
     * Write files to the file system
     *
     * @param workingDir
     * @param files
     * @throws IOException
     */
    private void writeFiles(Path workingDir, Map<String, byte[]> files) throws IOException {
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            File file = new File(workingDir.toString(), entry.getKey());
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                throw new IOException("Cannot create directory " + file.getParentFile());
            }
            Files.write(file.toPath(), entry.getValue());
        }
    }

    private RevCommit commitAndPush(Git git, String message, UsernamePasswordCredentialsProvider credentials)
        throws GitAPIException {

        // git add .
        git.add().addFilepattern(".").call();
        if (log.isDebugEnabled()) {
            log.debug("git add all file");
        }

        // git commit
        RevCommit commit = git.commit().setMessage(message).call();
        log.info("git commit id {}", commit.getId());

        // git push -f, not merging but simply forcing the push (for now)
        Iterable<PushResult> pushResult = git.push().setCredentialsProvider(credentials).setForce(true).call();
        if (!pushResult.iterator().next().getMessages().equals("")) {
            log.warn("git push messages: {}", pushResult.iterator().next().getMessages());
        }

        return commit;

    }
}
