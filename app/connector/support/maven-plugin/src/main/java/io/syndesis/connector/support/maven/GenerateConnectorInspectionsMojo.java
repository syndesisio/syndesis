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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.PrettyPrinter;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.v2.CollectionType;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.util.Json;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.StringUtils;

@Mojo(
    name = "generate-connector-inspections",
    defaultPhase = LifecyclePhase.PROCESS_CLASSES,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateConnectorInspectionsMojo extends AbstractMojo {
    private static final String CONNECTORS_META_PATH = "META-INF/syndesis/connector/";

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(readonly = true, defaultValue = "${project.build.directory}/classes")
    private String output;

    @Parameter(defaultValue = "${project.build.directory}/classes/META-INF/syndesis")
    private File inspectionsOutputDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!inspectionsOutputDir.exists() && !inspectionsOutputDir.mkdirs()) {
            throw new MojoFailureException("Unable to create output directory at: " + inspectionsOutputDir);
        }

        try {
            File root = new File(output, CONNECTORS_META_PATH);
            if (root.exists()) {
                File[] files = root.listFiles((dir, name) -> name.endsWith(".json"));
                if (files == null) {
                    return;
                }

                for (File file: files) {
                    Connector connector = Json.reader().forType(Connector.class).readValue(file);
                    if (!connector.getId().isPresent()) {
                        continue;
                    }

                    List<ConnectorAction> actions = generateInspections(connector);
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

    private List<ConnectorAction> generateInspections(Connector connector) throws IOException, DependencyResolutionRequiredException, ClassNotFoundException {
        final List<ConnectorAction> actions = new ArrayList<>();

        for (ConnectorAction action : connector.getActions()) {
            Optional<DataShape> inputDataShape = action.getDescriptor().getInputDataShape();
            Optional<DataShape> outputDataShape = action.getDescriptor().getOutputDataShape();

            if (inputDataShape.isPresent()) {
                inputDataShape = Optional.of(generateInspections(connector, inputDataShape.get()));
            }
            if (outputDataShape.isPresent()) {
                outputDataShape = Optional.of(generateInspections(connector, outputDataShape.get()));
            }

            if (inputDataShape.isPresent() || outputDataShape.isPresent()) {
                ConnectorDescriptor descriptor = new ConnectorDescriptor.Builder()
                    .createFrom(action.getDescriptor())
                    .inputDataShape(inputDataShape)
                    .outputDataShape(outputDataShape)
                    .build();

                action = new ConnectorAction.Builder()
                    .createFrom(action)
                    .descriptor(descriptor)
                    .build();
            }

            actions.add(action);
        }

        return actions;
    }

    private DataShape generateInspections(Connector connector, DataShape shape) throws IOException, DependencyResolutionRequiredException, ClassNotFoundException {
        if (DataShapeKinds.JAVA != shape.getKind() || StringUtils.isEmpty(shape.getType())) {
            return shape;
        }

        getLog().info("Generating inspections for connector: " + connector.getId().get() + ", and type: " + shape.getType());

        final List<String> elements = project.getRuntimeClasspathElements();
        final URL[] classpath = new URL[elements.size()];

        for (int i = 0; i < elements.size(); i++) {
            classpath[i] = new File(elements.get(i)).toURI().toURL();
        }

        return generateInspections(classpath, shape);
    }

    public static DataShape generateInspections(URL[] classpath, DataShape shape) throws IOException, ClassNotFoundException {
        JavaClass c = inspect(classpath, shape);

        List<DataShape> variants = new ArrayList<>(shape.getVariants().size());
        for (DataShape s : shape.getVariants()) {
            variants.add(generateInspections(classpath, s));
        }

        String inspection = io.atlasmap.v2.Json.mapper().writer((PrettyPrinter) null).writeValueAsString(c);
        return new DataShape.Builder()
            .createFrom(shape)
            .specification(inspection)
            .variants(variants)
            .compress()
            .build();
    }

    public static JavaClass inspect(URL[] classpath, DataShape shape) throws IOException, ClassNotFoundException {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();

        try (URLClassLoader loader = new URLClassLoader(classpath, tccl)) {
            ClassInspectionService inspector = new ClassInspectionService();
            inspector.setConversionService(DefaultAtlasConversionService.getInstance());

            Class<?> clazz = loader.loadClass(shape.getType());
            CollectionType collectionClazz = shape.getCollectionType().map(CollectionType::fromValue).orElse(CollectionType.NONE);
            String collectionClazzName = shape.getCollectionClassName().orElse(null);

            return inspector.inspectClass(
                loader,
                clazz,
                collectionClazz,
                collectionClazzName
            );
        }
    }
}
