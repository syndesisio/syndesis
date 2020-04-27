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

package io.syndesis.test.container.dockerfile;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import io.syndesis.test.SyndesisTestEnvironment;
import org.testcontainers.images.builder.ImageFromDockerfile;

/**
 * Special on the fly Dockerfile builder that automatically adds project sources as directory of jar file to the image.
 * Also takes care on proper file permissions and user settings in the container.
 *
 * @author Christoph Deppisch
 */
public class SyndesisDockerfileBuilder extends ImageFromDockerfile {

    private static final String ROOT = "0";
    private static final String JBOSS = "jboss";
    private static final String EXTENSIONS_DEST = "/deployments/data/syndesis/loader/extensions";

    private String from;
    private String runCommand;
    private String projectSrc;
    private String projectDest;
    private String extensionsSrc = "extensions";
    private Path projectPath;
    private Path projectFatJar;
    private Map<String, String> envProperties = Collections.singletonMap("SYNDESIS_VERSION",
                                                                         SyndesisTestEnvironment.getSyndesisVersion());

    public SyndesisDockerfileBuilder(String dockerImageName, boolean deleteOnExit) {
        super(dockerImageName, deleteOnExit);
    }

    public SyndesisDockerfileBuilder build() {
        return (SyndesisDockerfileBuilder)
                withFileFromPath(projectSrc, getProjectPathOrFatJarPath())
                    .withFileFromPath(extensionsSrc, projectPath.resolve("extensions"))
                .withDockerfileFromBuilder(builder -> builder.from(from)
                    .env(envProperties)
                    .user(ROOT)
                    .copy(projectSrc, projectDest)
                    .copy(extensionsSrc, EXTENSIONS_DEST)
                    .run(fixGroupsCommand())
                    .run(fixPermissionsCommand())
                    .user(JBOSS)
                    .expose(SyndesisTestEnvironment.getDebugPort())
                    .cmd(runCommand)
                .build());
    }

    private Path getProjectPathOrFatJarPath() {
        return (projectFatJar != null ? projectFatJar : projectPath);
    }

    private String[] fixGroupsCommand() {
        return  new String [] { "chgrp", "-R", "0", projectDest };
    }

    private String[] fixPermissionsCommand() {
        return new String [] { "chmod", "-R", "g=u", projectDest };
    }

    public SyndesisDockerfileBuilder from(String image, String tag) {
        this.from = String.format("%s:%s", image, tag);
        return this;
    }

    public SyndesisDockerfileBuilder env(Map<String, String> envProperties) {
        this.envProperties = envProperties;
        return this;
    }

    public SyndesisDockerfileBuilder cmd(String runCommand) {
        this.runCommand = runCommand;
        return this;
    }

    public SyndesisDockerfileBuilder projectSource(String projectSrc) {
        this.projectSrc = projectSrc;
        return this;
    }

    public SyndesisDockerfileBuilder projectDestination(String projectDest) {
        this.projectDest = projectDest;
        return this;
    }

    public SyndesisDockerfileBuilder projectPath(Path projectPath) {
        this.projectPath = projectPath;
        return this;
    }

    public SyndesisDockerfileBuilder projectFatJar(Path projectFatJar) {
        this.projectFatJar = projectFatJar;
        return this;
    }

    public SyndesisDockerfileBuilder extensionsSource(String extensionsSrc) {
        this.extensionsSrc = extensionsSrc;
        return this;
    }
}
