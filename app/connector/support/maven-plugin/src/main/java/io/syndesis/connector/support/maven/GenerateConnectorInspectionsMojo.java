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
package io.syndesis.connector.support.maven;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.v2.JavaClass;
import io.syndesis.common.util.Json;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connector;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
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

import java.io.File;
import java.io.IOException;
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
    private ArtifactFactory artifactFactory;

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(readonly = true, defaultValue = "${project.build.directory}/classes")
    private String output;

    @Parameter(defaultValue = "mapper/v1/java-inspections")
    private String inspectionsResourceDir;

    @Parameter(defaultValue = "${project.build.directory}/classes/META-INF/syndesis")
    private File inspectionsOutputDir;

    @Parameter(defaultValue = "RESOURCE_AND_SPECIFICATION")
    @SuppressWarnings("PMD.ImmutableField")
    private InspectionMode inspectionMode = InspectionMode.RESOURCE_AND_SPECIFICATION;

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (!inspectionsOutputDir.exists() && !inspectionsOutputDir.mkdirs()) {
		throw new MojoFailureException("Unable to create output directory at: " + inspectionsOutputDir);
	    }

            File root = new File(output, CONNECTORS_META_PATH);
            if (root.exists()) {
                File[] files = root.listFiles((dir, name) -> name.endsWith(".json"));
                if (files == null) {
                    return;
                }

                for (File file: files) {
                    final Connector connector = Json.reader().forType(Connector.class).readValue(file);
                    final List<ConnectorAction> actions = new ArrayList<>();

                    for (ConnectorAction action : connector.getActions()) {
                        Optional<DataShape> inputDataShape = generateInspections(connector, action.getDescriptor().getInputDataShape());
                        Optional<DataShape> outputDataShape = generateInspections(connector, action.getDescriptor().getOutputDataShape());

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
                        Json.writer().writeValue(
                            file,
                            new Connector.Builder()
                                .createFrom(connector)
                                .actions(actions)
                                .build()
                        );
                    }
                }
            }

        } catch (ClassNotFoundException | DependencyResolutionRequiredException | IOException e) {
            throw new MojoExecutionException("Unable to generate inspections", e);
        }
    }

    // ****************************************
    // Inspections
    // ****************************************

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private Optional<DataShape> generateInspections(Connector connector, Optional<DataShape> shape) throws IOException, DependencyResolutionRequiredException, ClassNotFoundException {
        if (!shape.isPresent() || !connector.getId().isPresent()) {
            return Optional.empty();
        }

        final DataShape given = shape.get();
        DataShape ret = given;

        if (DataShapeKinds.JAVA == given.getKind() && StringUtils.isNotEmpty(given.getType())) {
            final String type = given.getType();


            File outputFile = new File(inspectionsOutputDir, String.format("%s/%s/%s.json", inspectionsResourceDir, connector.getId().get(), type));
            if (!outputFile.getParentFile().exists() && outputFile.getParentFile().mkdirs()) {
                getLog().debug("Directory created:" + outputFile.getParentFile());
            }

            getLog().info("Generating for connector: " + connector.getId().get() + ", and type: " + type);

            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            final List<String> elements = project.getRuntimeClasspathElements();
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
                    getLog().info("Specification for type: " + type + " created");
                    ret = new DataShape.Builder()
                                       .createFrom(given)
                                       .specification(mapper.writer((PrettyPrinter) null).writeValueAsString(c))
                                       .build();

                }

                if (inspectionMode == InspectionMode.RESOURCE || inspectionMode == InspectionMode.RESOURCE_AND_SPECIFICATION) {
                    mapper.writeValue(outputFile, c);
                    getLog().info("Created: " + outputFile);
                }
            }
        }

        return Optional.of(ret);
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
