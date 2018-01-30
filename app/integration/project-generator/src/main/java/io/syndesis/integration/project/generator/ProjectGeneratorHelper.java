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
package io.syndesis.integration.project.generator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import com.github.mustachejava.Mustache;
import io.syndesis.integration.project.generator.mvn.MavenGav;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProjectGeneratorHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectGeneratorHelper.class);

    private ProjectGeneratorHelper() {
    }

    public static void addTarEntry(TarArchiveOutputStream tos, String path, byte[] content) throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(path);
        entry.setSize(content.length);
        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
    }

    public static byte[] generate(Object scope, Mustache template) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        template.execute(new OutputStreamWriter(bos, StandardCharsets.UTF_8), scope).flush();
        return bos.toByteArray();
    }

    public static boolean filterDefaultDependencies(MavenGav gav) {
        boolean answer = true;

        if ("org.springframework.boot".equals(gav.getGroupId())) {
            if ("spring-boot-starter-web".equals(gav.getArtifactId())) {
                answer = false;
            } if ("spring-boot-starter-undertow".equals(gav.getArtifactId())) {
                answer = false;
            } else if ("spring-boot-starter-actuator".equals(gav.getArtifactId())) {
                answer = false;
            }
        }

        if ("org.apache.camel".equals(gav.getGroupId()) && "camel-spring-boot-starter".equals(gav.getArtifactId())) {
            answer = false;
        }

        if ("io.syndesis".equals(gav.getGroupId()) && "integration-runtime".equals(gav.getArtifactId())) {
            answer = false;
        }

        if (!answer) {
            LOGGER.debug("Dependency: {} filtered", gav);
        }

        return answer;
    }
}
