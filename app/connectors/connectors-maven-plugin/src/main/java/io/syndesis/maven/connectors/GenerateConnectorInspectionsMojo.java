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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.v2.JavaClass;
import io.syndesis.core.Json;
import io.syndesis.model.DataShape;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.connection.Connector;
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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mojo(
    name = "generate-connector-inspections",
    defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateConnectorInspectionsMojo extends AbstractMojo {
    private static final String CONNECTORS_META_PATH = "META-INF/syndesis/connector/";

    public enum InspectionMode {
        RESOURCE,
        SPECIFICATION,
        RESOURCE_AND_SPECIFICATION
    }

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

    @Parameter(defaultValue = "RESOURCE_AND_SPECIFICATION")
    private InspectionMode inspectionMode = InspectionMode.RESOURCE_AND_SPECIFICATION;

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
                    final Connector connector = Json.mapper().readValue(file, Connector.class);
                    final List<ConnectorAction> actions = new ArrayList<>();

                    for (ConnectorAction action : connector.getActions()) {
                        Optional<DataShape> inputDataShape = generateInspections(file, connector, action.getDescriptor().getInputDataShape());
                        Optional<DataShape> outputDataShape = generateInspections(file, connector, action.getDescriptor().getOutputDataShape());

                        if (inspectionMode == InspectionMode.SPECIFICATION || inspectionMode == InspectionMode.RESOURCE_AND_SPECIFICATION) {
                            if (inputDataShape.isPresent() || outputDataShape.isPresent()) {
                                ConnectorDescriptor.Builder descriptorBuilder = new ConnectorDescriptor.Builder().createFrom(action.getDescriptor());

                                inputDataShape.ifPresent(descriptorBuilder::inputDataShape);
                                outputDataShape.ifPresent(descriptorBuilder::outputDataShape);
                                
                                actions.add(
                                    new ConnectorAction.Builder()
                                        .createFrom(action)
                                        .descriptor(descriptorBuilder.build())
                                        .build()
                                );
                            } else {
                                actions.add(action);
                            }
                        }
                    }

                    if (!actions.isEmpty()) {
                        Json.mapper().writeValue(
                            file,
                            new Connector.Builder()
                                .createFrom(connector)
                                .actions(actions)
                                .build()
                        );
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

    private Optional<DataShape> generateInspections(File file, Connector connector, Optional<DataShape> shape) throws Exception {
        if (!shape.isPresent() || !connector.getId().isPresent()) {
            return Optional.empty();
        }

        if (StringUtils.equals("java", shape.get().getKind()) && StringUtils.isNotEmpty(shape.get().getType())) {
            final String kind = shape.get().getKind();
            final String type = shape.get().getType();


            File outputFile = new File(inspectionsOutputDir, String.format("%s/%s/%s.json", inspectionsResourceDir, connector.getId().get(), type));
            if (!outputFile.getParentFile().exists()) {
                if (outputFile.getParentFile().mkdirs()) {
                    getLog().debug("Directory created:" + outputFile.getParentFile());
                }
            }

            getLog().info("Generating for connector: " + connector.getId().get() + ", and type: " + type);

            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            final List<String> elements = project.getCompileClasspathElements();
            final URL[] classpath = new URL[elements.size()];

            for (int i = 0; i < elements.size(); i++) {
                classpath[i] = new File(elements.get(i)).toURI().toURL();

                getLog().debug("Add element to classpath: " + classpath[i]);
            }

            try (URLClassLoader loader = new URLClassLoader(classpath, tccl)) {
                ClassInspectionService classInspectionService = new ClassInspectionService();
                classInspectionService.setConversionService(DefaultAtlasConversionService.getInstance());

                final Class<?> clazz = loader.loadClass(type);
                final JavaClass c = classInspectionService.inspectClass(loader, clazz);
                final ObjectMapper mapper = io.atlasmap.v2.Json.mapper();

                if (inspectionMode == InspectionMode.SPECIFICATION || inspectionMode == InspectionMode.RESOURCE_AND_SPECIFICATION) {
                    shape = Optional.of(
                        new DataShape.Builder()
                            .createFrom(shape.get())
                            .specification(mapper.writeValueAsString(c))
                            .build()
                    );

                    getLog().info("Specification for type: " + type + " created");
                }

                if (inspectionMode == InspectionMode.RESOURCE || inspectionMode == InspectionMode.RESOURCE_AND_SPECIFICATION) {
                    mapper.writeValue(outputFile, c);
                    getLog().info("Created: " + outputFile);
                }
            }
        }

        return shape;
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
}
