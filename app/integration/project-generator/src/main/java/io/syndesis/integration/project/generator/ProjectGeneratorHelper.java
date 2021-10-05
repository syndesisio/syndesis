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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.integration.project.generator.mvn.MavenGav;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProjectGeneratorHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectGeneratorHelper.class);

    private ProjectGeneratorHelper() {
    }

    public static void addResource(TarArchiveOutputStream tos, String destination, String resource) throws IOException {
        final URL url = ProjectGeneratorHelper.class.getResource(resource);
        final byte[] bytes = IOUtils.toByteArray(url);

        addTarEntry(tos, destination, bytes);
    }

    public static void addTarEntry(TarArchiveOutputStream tos, String path, byte[] content) throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(path);
        entry.setSize(content.length);
        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
    }

    public static byte[] generate(Object scope, Mustache template) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
            Writer templateWriter = template.execute(writer, scope)) {

            templateWriter.flush();

            return bos.toByteArray();
        }
    }

    public static boolean filterDefaultDependencies(MavenGav gav) {
        if (!shouldIncludeDependency(gav)) {
            LOGGER.debug("Dependency: {} filtered", gav);
            return false;
        }

        return true;
    }

    private static boolean shouldIncludeDependency(MavenGav gav) {
        final String groupId = gav.getGroupId();
        final String artifactId = gav.getArtifactId();

        final boolean isSpringBoot = "org.springframework.boot".equals(groupId)
            && ("spring-boot-starter-web".equals(artifactId)
                || "spring-boot-starter-undertow".equals(artifactId)
                || "spring-boot-starter-actuator".equals(artifactId));
        if (isSpringBoot) {
            return false;
        }

        final boolean isApacheSpringBoot = "org.apache.camel".equals(groupId) && "camel-spring-boot-starter".equals(artifactId);
        if (isApacheSpringBoot) {
            return false;
        }

        final boolean isSyndesisIntegrationRuntime = "io.syndesis".equals(groupId) && "integration-runtime".equals(artifactId);
        if (isSyndesisIntegrationRuntime) {
            return false;
        }

        return !"org.apache.camel.k".equals(groupId);
    }

    public static Mustache compile(MustacheFactory mustacheFactory, ProjectGeneratorConfiguration generatorProperties, String template, String name) throws IOException {
        String overridePath = generatorProperties.getTemplates().getOverridePath();
        URL resource = null;

        if (!StringUtils.isEmpty(overridePath)) {
            resource = ProjectGeneratorHelper.class.getResource("templates/" + overridePath + "/" + template);
        }
        if (resource == null) {
            resource = ProjectGeneratorHelper.class.getResource("templates/" + template);
        }
        if (resource == null) {
            throw new IllegalArgumentException(
                String.format("Unable to find te required template (overridePath=%s, template=%s)"
                    , overridePath
                    , template
                )
            );
        }

        try (InputStream stream = resource.openStream()) {
            return mustacheFactory.compile(new InputStreamReader(stream, StandardCharsets.UTF_8), name);
        }
    }

    public static String mandatoryDecrypt(IntegrationResourceManager manager, String propertyKey, String propertyVal) {
        String answer = propertyKey;

        if (propertyVal != null) {
            answer = manager.decrypt(propertyVal);
            if (answer == null) {
                throw new IllegalArgumentException("Unable to decrypt key:" + propertyKey);
            }
        }

        return answer;
    }
}
