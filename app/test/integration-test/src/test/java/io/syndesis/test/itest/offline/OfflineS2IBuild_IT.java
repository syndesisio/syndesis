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
package io.syndesis.test.itest.offline;

import java.nio.file.Path;

import io.syndesis.common.model.integration.Integration;
import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.s2i.SyndesisS2iAssemblyContainer;
import io.syndesis.test.integration.project.Project;
import io.syndesis.test.integration.project.ProjectBuilder;
import io.syndesis.test.integration.project.SpringBootProjectBuilder;

import org.assertj.core.description.Description;
import org.junit.Test;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.LogUtils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;

import static org.assertj.core.api.Assertions.assertThat;

public class OfflineS2IBuild_IT {

    final Integration integration = new Integration.Builder()
        .name("offline-integration")
        .build();

    @Test
    public void s2iBuildShouldBeOffline() {
        final ProjectBuilder builder = new SpringBootProjectBuilder("offline-project", SyndesisTestEnvironment.getSyndesisVersion());

        final Project project = builder.build(() -> integration);

        try (SyndesisS2iAssemblyContainer s2i = createContainerForProjectIn(project.getProjectPath())) {
            s2i.start();
            final InspectContainerResponse containerInfo = s2i.getContainerInfo();

            final String containerId = containerInfo.getId();

            @SuppressWarnings("resource") // global docker client, we musn't close it
            final DockerClient docker = s2i.getDockerClient(); 
            try (InspectContainerCmd inspectCmd = docker.inspectContainerCmd(containerId)) {

                // for some reason containerInfo.getState().getExitCode() always
                // returns 0 so we run the `docker inspect` command instead
                final ContainerState state = inspectCmd.exec().getState();
                assertThat(state.getExitCodeLong())
                    .describedAs(new Description() {
                        @Override
                        public String value() {
                            return "When running without network the S2I build should complete without issues, "
                                + "but the exit code of the assembly script is non zero. Output:\n"
                                + LogUtils.getOutput(docker, containerId);
                        }
                    })
                    .isEqualTo(0);
            }
        }
    }

    private static SyndesisS2iAssemblyContainer createContainerForProjectIn(final Path project) {
        final WaitStrategy finished = new LogMessageWaitStrategy()
            .withRegEx("(?:.*\\.\\.\\. done.*\\s)|(?:Aborting due to error code .* for Maven build.*)")
            .withStartupTimeout(SyndesisTestEnvironment.getContainerStartupTimeout());

        @SuppressWarnings("resource") // we're creating the resource here and
                                      // should not return it closed
        final SyndesisS2iAssemblyContainer s2i = new SyndesisS2iAssemblyContainer("offline-integration", project,
            SyndesisTestEnvironment.getSyndesisImageTag())
                .withNetworkMode("none");
        s2i.setWaitStrategy(finished);

        return s2i;
    }

}
