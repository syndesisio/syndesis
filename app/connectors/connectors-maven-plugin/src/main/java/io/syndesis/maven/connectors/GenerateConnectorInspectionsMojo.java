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
package io.syndesis.maven.connectors;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.atlasmap.maven.GenerateInspectionsMojo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.StringUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.immutables.value.Value;

@Mojo(
    name = "generate-connector-inspections",
    defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateConnectorInspectionsMojo extends AbstractMojo {
    private static final String CONNECTORS_META_PATH = "META-INF/syndesis/connector/";

    @Component
    private RepositorySystem system;

    @Component
    private ArtifactFactory artifactFactory;

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
    private RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepos;

    @Parameter(readonly = true, defaultValue = "${project.build.directory}/classes")
    private String output;

    @Parameter(defaultValue = "mapper/v1/java-inspections")
    private String inspectionsResourceDir;

    @Parameter(defaultValue = "${project.build.directory}/classes/META-INF/syndesis")
    private File inspectionsOutputDir;

    private Set<String> inspections = new HashSet<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            inspectionsOutputDir.mkdirs();

            File root = new File(output, CONNECTORS_META_PATH);
            if (root.exists()) {
                File[] files = root.listFiles((dir, name) -> name.endsWith(".json"));
                if (files == null) {
                    return;
                }
                for (File file: files) {
                    Connector connector = new ObjectMapper()
                        .registerModules(new Jdk8Module())
                        .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                        .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                        .readValue(file, Connector.class);

                    for (ConnectorAction action : connector.getActions()) {
                        generateInspections(connector, action.getDescriptor().getInputDataShape());
                        generateInspections(connector, action.getDescriptor().getOutputDataShape());
                    }
                }
            }

        } catch (Exception e) {
            throw new MojoExecutionException("", e);
        }
    }

    // ****************************************
    // Inspections
    // ****************************************

    private void generateInspections(Connector connector, Optional<DataShape> shape) throws Exception {
        if (!shape.isPresent()) {
            return;
        }

        if (StringUtils.equals("java", shape.get().getKind()) && StringUtils.isNotEmpty(shape.get().getType())) {
            final String kind = shape.get().getKind();
            final String type = shape.get().getType();

            if (inspections.contains(type)) {
                return;
            }

            getLog().info("Generating for connector: " + connector.getId() + ", and type: " + type);

            File outputFile = new File(inspectionsOutputDir, String.format("%s/%s/%s.json", inspectionsResourceDir, connector.getId(), type));
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            List<String> artifacts = project.getDependencies().stream()
                .map(this::toArtifact)
                .map(Artifact::getId)
                .collect(Collectors.toList());

            GenerateInspectionsMojo mojo = new GenerateInspectionsMojo();
            mojo.setLog(getLog());
            mojo.setPluginContext(getPluginContext());
            mojo.setSystem(system);
            mojo.setRemoteRepos(remoteRepos);
            mojo.setRepoSession(repoSession);
            mojo.setClassName(type);
            mojo.setArtifacts(artifacts);
            mojo.setOutputFile(outputFile);
            mojo.execute();

            inspections.add(type);
        }
    }

    protected Artifact toArtifact(org.apache.maven.model.Dependency dependency) {
        return artifactFactory.createArtifact(
            dependency.getGroupId(),
            dependency.getArtifactId(),
            dependency.getVersion(),
            dependency.getScope(),
            dependency.getType()
        );
    }

    // ****************************************
    // Model
    // ****************************************

    @Value.Immutable
    @JsonDeserialize(builder = Dependency.Builder.class)
    public interface Dependency {
        String getType();
        String getId();

        class Builder extends ImmutableDependency.Builder {
        }
    }

    public interface WithDependencies {
        @Value.Default
        default List<Dependency> getDependencies() {
            return Collections.emptyList();
        }
    }

    @Value.Immutable
    @JsonDeserialize(builder = DataShape.Builder.class)
    public interface DataShape {
        @Value.Default
        default String getKind() {
            return "";
        }
        @Value.Default
        default String getType() {
            return "";
        }

        class Builder extends ImmutableDataShape.Builder {
        }
    }

    @Value.Immutable
    @JsonDeserialize(builder = ConnectorDescriptor.Builder.class)
    public interface ConnectorDescriptor {
        Optional<DataShape> getInputDataShape();
        Optional<DataShape> getOutputDataShape();

        class Builder extends ImmutableConnectorDescriptor.Builder {
        }
    }

    @Value.Immutable
    @JsonDeserialize(builder = ConnectorAction.Builder.class)
    @SuppressWarnings("immutables")
    public interface ConnectorAction extends WithDependencies {
        ConnectorDescriptor getDescriptor();

        class Builder extends ImmutableConnectorAction.Builder {
        }
    }

    @Value.Immutable
    @JsonDeserialize(builder = Connector.Builder.class)
    @SuppressWarnings("immutables")
    public interface Connector extends WithDependencies {
        String getId();

        @Value.Default
        default List<ConnectorAction> getActions() {
            return Collections.emptyList();
        }

        class Builder extends ImmutableConnector.Builder {
        }
    }
}
