/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.syndesis.test.container.s2i;

import java.nio.file.Path;

import io.syndesis.test.SyndesisTestEnvironment;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.dockerfile.statement.MultiArgsStatement;

/**
 * Syndesis S2i container that performs assemble step on a give project
 * directory. The project sources are assembled to a runnable project fat jar
 * using fabric8 S2i assemble script.
 * The container uses the Syndesis S2i image as base. This image already holds
 * all required Syndesis libraries and artifacts with the given version.
 */
public class SyndesisS2iAssemblyContainer extends GenericContainer<SyndesisS2iAssemblyContainer> {

    static final String S2I_ASSEMBLE_SCRIPT = "/usr/local/s2i/assemble";
    private static final String SRC_DIR = "/tmp/src";

    public SyndesisS2iAssemblyContainer(final String integrationName, final Path projectDir, final String imageTag) {
        super(new ImageFromDockerfile(integrationName + "-s2i", true)
            .withFileFromPath(SRC_DIR, projectDir)
            .withDockerfileFromBuilder(builder -> builder.from(String.format("syndesis/syndesis-s2i:%s", imageTag))
                .withStatement(new MultiArgsStatement("ADD", SRC_DIR, SRC_DIR))
                .user("0")
                .run("chown", "-R", "1000", SRC_DIR)
                .user("1000")
                .cmd(S2I_ASSEMBLE_SCRIPT)
                .build()));

        final WaitStrategy onLogDone = new LogMessageWaitStrategy()
            .withRegEx(".*\\.\\.\\. done.*\\s")
            .withStartupTimeout(SyndesisTestEnvironment.getContainerStartupTimeout());

        setWaitStrategy(onLogDone);
    }
}
