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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syndesis.core.SyndesisServerException;

public class GitWorkflow {

    private static final Logger log = LoggerFactory.getLogger(GitWorkflow.class);
    /**
     * Commits and pushes the files to the Git Repository
     * 
     * @param repo- an existing github repository
     * @param message- commit message
     * @param files- map of file paths along with their content
     * @param credentials- Git credentials, for example username/password, authToken, personal access token
     * @return RevCommit, the commit info
     */
    public RevCommit createFiles(String repoHtmlUrl, String repoName, String message, Map<String, byte[]> files,
            UsernamePasswordCredentialsProvider credentials) {

        try {
            // create temporary directory
            Path workingDir = Files.createTempDirectory(repoName);
            if (log.isDebugEnabled()) log.debug("Created temporary directory {}", workingDir.toString());

            // git init
            Git git = Git.init().setDirectory(workingDir.toFile()).call();
            writeFiles(workingDir, files);

            RemoteAddCommand remoteAddCommand = git.remoteAdd();
            remoteAddCommand.setName("origin");
            remoteAddCommand.setUri(new URIish(repoHtmlUrl));
            remoteAddCommand.call();

            RevCommit commit = commitAndPush(git, message, credentials);
            workingDir.toFile().delete();
            return commit;

        } catch (Exception e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }
    
    public RevCommit updateFiles(String repoHtmlUrl, String repoName, String message, Map<String, byte[]> files,
            UsernamePasswordCredentialsProvider credentials) {

        try {
            // create temporary directory
            Path workingDir = Files.createTempDirectory(repoName);
            if (log.isDebugEnabled()) log.debug("Created temporary directory {}", workingDir.toString());

            // git clone
            Git git = Git.cloneRepository().setDirectory(workingDir.toFile()).setURI(repoHtmlUrl).call();
            writeFiles(workingDir, files);

            RevCommit commit =  commitAndPush(git, message, credentials);
            
            // cleanup tmp dir
            workingDir.toFile().delete();
            
            return commit;
        } catch (Exception e) {
            throw SyndesisServerException.launderThrowable(e);
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
        for (String filePath : files.keySet()) {
            File file = new File(workingDir.toString() + File.separator + filePath);
            file.getParentFile().mkdirs();
            Files.write(file.toPath(), files.get(filePath));
        }
    }
        
    private RevCommit commitAndPush(Git git, String message, UsernamePasswordCredentialsProvider credentials) 
            throws NoFilepatternException, GitAPIException {

        // git add .
        git.add().addFilepattern(".").call();
        if (log.isDebugEnabled()) log.debug("git add all file");

        // git commit
        RevCommit commit = git.commit().setMessage(message).call();
        log.info("git commit id {}", commit.getId());

        // git push -f, not merging but simply forcing the push (for now)
        Iterable<PushResult> pushResult = git.push().setCredentialsProvider(credentials).setForce(true).call();
        if (!pushResult.iterator().next().getMessages().equals("")) 
            log.warn("git push messages: {}", pushResult.iterator().next().getMessages());

        return commit;

    }
}
