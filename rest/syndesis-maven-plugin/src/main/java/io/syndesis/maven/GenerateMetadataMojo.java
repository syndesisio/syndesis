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
package io.syndesis.maven;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.core.Json;
import io.syndesis.model.techextension.TechExtension;
import io.syndesis.model.techextension.TechExtensionAction;
import io.syndesis.model.techextension.TechExtensionDataShape;
import io.syndesis.model.techextension.TechExtensionDescriptor;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.StringUtils;


/**
 * Helper Maven plugin
 *
 * @author pantinor
 */
@Mojo(name = "generate-metadata", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateMetadataMojo extends AbstractMojo {
    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter
    private String groupId;

    @Parameter
    private String artifactId;

    @Parameter
    private String version;

    @Parameter(defaultValue = "${project.name}")
    private String name;

    @Parameter(defaultValue = "${project.description}")
    private String description;

    @Parameter
    private String icon;

    @Parameter
    private String tags;

    @Parameter(readonly = true, defaultValue = "${project.build.directory}/classes/META-INF/syndesis/extension-definition.json")
    private String metadataDestination;

    /**
     * Partial Extension JSON descriptor to augment
     */
    @Parameter(readonly = true)
    private String source;

    @Parameter(defaultValue = "false")
    private Boolean listAllArtifacts;

    @Component
    private ArtifactFactory artifactFactory;

    protected final ObjectMapper objectMapper = Json.mapper();
    protected TechExtension.Builder techExtensionBuilder = new TechExtension.Builder();

    @Override
    public void execute() throws MojoExecutionException {
        tryImportingPartialJSON();
        processAnnotations();
        overrideConfigFromMavenPlugin();
        includeDependencies();

        saveExtensionMetaData(techExtensionBuilder.build());
    }

    protected void processAnnotations() throws MojoExecutionException {
        String directory = project.getModel().getBuild().getDirectory();
        Path dir = Paths.get(directory, "generated-sources", "annotations");
        if (Files.exists(dir)) {
            getLog().info("Looking in for annotated classes in: " + dir);
            try {
                Files.find(dir, Integer.MAX_VALUE, (path, basicFileAttributes) -> String.valueOf(path).endsWith(".properties")).forEach(path -> {
                    Properties p = new Properties();
                    try (Reader reader = new FileReader(path.toFile())) {
                        getLog().info("Loading annotations properties from: " + path);
                        p.load(reader);
                        assignProperties(p);
                    } catch (IOException e) {
                        getLog().error("Error reading file " + path);
                    }
                });
            } catch (IOException e) {
                throw new MojoExecutionException("Error checking annotations.", e);
            }
        } else {
            getLog().debug("Path " + dir + " does not exists");
        }
    }

    protected void assignProperties(Properties p) {
        if (StringUtils.isEmpty(p.getProperty("id"))) {
            getLog().warn("Unable to define action, reason: 'id' is not set (properties: " + p + ")");
            return;
        }
        if (StringUtils.isEmpty(p.getProperty("name"))) {
            getLog().warn("Unable to define action, reason: 'name' is not set (properties: " + p + ")");
            return;
        }
        if (StringUtils.isEmpty(p.getProperty("kind"))) {
            getLog().warn("Unable to define action, reason: 'kind' is not set (properties: " + p + ")");
            return;
        }
        if (StringUtils.isEmpty(p.getProperty("entrypoint"))) {
            getLog().warn("Unable to define action, reason: 'entrypoint' is not set (properties: " + p + ")");
            return;
        }

        TechExtensionAction.Builder actionBuilder = new TechExtensionAction.Builder();
        actionBuilder.id(p.getProperty("id"));
        actionBuilder.name(p.getProperty("name"));
        // fixed value
        actionBuilder.actionType("extension");
        actionBuilder.description(p.getProperty("description"));
        String tags = p.getProperty("tags");
        if(tags != null && !"".equals(tags.trim())){
            String[] strings = tags.trim().split(",");
            actionBuilder.tags(Arrays.asList(strings));
        }

        TechExtensionDescriptor.Builder descriptorBuilder = new TechExtensionDescriptor.Builder();
        descriptorBuilder.kind(p.getProperty("kind"));
        descriptorBuilder.entrypoint(p.getProperty("entrypoint"));

        TechExtensionDataShape.Builder inputBuilder = new TechExtensionDataShape.Builder();
        inputBuilder.kind(p.getProperty("inputDataShape"));
        descriptorBuilder.inputDataShape(inputBuilder.build());

        TechExtensionDataShape.Builder outputBuilder = new TechExtensionDataShape.Builder();
        outputBuilder.kind(p.getProperty("outputDataShape"));
        descriptorBuilder.outputDataShape(outputBuilder.build());

        actionBuilder.descriptor(descriptorBuilder.build());


        techExtensionBuilder.addAction(actionBuilder.build());
    }

    /**
     * Loads a partial metadata json file, if configured at Maven Plugin level.
     * @throws MojoExecutionException
     */
    protected void tryImportingPartialJSON() throws MojoExecutionException {
        if(StringUtils.isNotEmpty(source)){
            TechExtension techExtension ;
            try {
                techExtension = objectMapper.readValue(new File(source), TechExtension.class);
                getLog().info("Loaded base partial metadata configuration file: " + source);
            } catch (IOException e) {
                throw new MojoExecutionException("Invalid input json: " + source, e );
            }
            techExtensionBuilder = techExtensionBuilder.createFrom(techExtension);
        } else {
            File targetFile = new File(metadataDestination);
            if (targetFile.exists()) {
                TechExtension techExtension;
                try {
                    techExtension = objectMapper.readValue(targetFile, TechExtension.class);
                    getLog().info("Loaded base partial metadata configuration file: " + targetFile.getAbsolutePath());
                } catch (IOException e) {
                    throw new MojoExecutionException("Invalid input json: " + targetFile.getAbsolutePath(), e );
                }
                techExtensionBuilder = techExtensionBuilder.createFrom(techExtension);
            }
        }
    }

    protected void overrideConfigFromMavenPlugin() {
        getLog().info("Looking for configuration to override at Maven Plugin configuration level. ");
        if(StringUtils.isBlank(groupId)){
            groupId = project.getGroupId();
        }
        if(StringUtils.isBlank(artifactId)){
            artifactId = project.getArtifactId();
        }
        techExtensionBuilder.extensionId(groupId + ":" + artifactId);

        if(StringUtils.isBlank(version)){
            version = project.getVersion();
        }
        techExtensionBuilder.version(version);

        if(StringUtils.isNotEmpty(name)) {
            techExtensionBuilder.name(name);
        }

        if(StringUtils.isNotEmpty(description)) {
            techExtensionBuilder.description(description);
        }

        if(StringUtils.isNotEmpty(icon)) {
            techExtensionBuilder.icon(icon);
        }

        if(StringUtils.isNotEmpty(tags)){
            String[] split = tags.split(",");
            techExtensionBuilder.tags(Arrays.asList(split));
        }
    }

    protected void includeDependencies() {
        Stream<String> artifacts;

        if (Boolean.TRUE.equals(listAllArtifacts)) {
            artifacts = project.getArtifacts().stream()
                .filter(artifact -> StringUtils.equals(artifact.getScope(), DefaultArtifact.SCOPE_PROVIDED))
                .map(Artifact::getId);
        } else {
            artifacts = this.project.getDependencies().stream()
                .filter(dependency -> StringUtils.equals(dependency.getScope(), DefaultArtifact.SCOPE_PROVIDED))
                .map(dependency -> toArtifact(dependency).getId());
        }

        artifacts.sorted().forEachOrdered(techExtensionBuilder::addDependency);
    }

    protected void saveExtensionMetaData(TechExtension jsonObject) throws MojoExecutionException {
        File targetFile = new File(metadataDestination);
        if (!targetFile.getParentFile().exists() &&
            !targetFile.getParentFile().mkdirs()) {
            throw new MojoExecutionException("Cannot create directory " + targetFile.getParentFile());
        }
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(targetFile, jsonObject);
            getLog().info("Created file " + targetFile.getAbsolutePath());
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot write to file: " + metadataDestination, e);
        }
    }

    protected Artifact toArtifact(Dependency dependency) {
        return artifactFactory.createArtifact(
            dependency.getGroupId(),
            dependency.getArtifactId(),
            dependency.getVersion(),
            dependency.getScope(),
            dependency.getType()
        );
    }
}
