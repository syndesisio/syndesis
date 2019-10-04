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
package io.syndesis.extension.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.v2.CollectionType;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.common.util.Names;
import io.syndesis.extension.converter.BinaryExtensionAnalyzer;
import io.syndesis.extension.converter.ExtensionConverter;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactType;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.util.artifact.JavaScopes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;

import static io.syndesis.extension.maven.plugin.DataShapeUtils.buildDataShape;
import static io.syndesis.extension.maven.plugin.MavenUtils.createPropertiesDefinitionSteps;
import static io.syndesis.extension.maven.plugin.MavenUtils.versionlessKey;
import static io.syndesis.extension.maven.plugin.Utils.isEmpty;
import static io.syndesis.extension.maven.plugin.Utils.isNotEmpty;
import static io.syndesis.extension.maven.plugin.Utils.nvl;

@Mojo(name = "generate-metadata", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresProject = true, threadSafe = true,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
@SuppressWarnings("PMD.TooManyFields")
public class GenerateMetadataMojo extends AbstractMojo {
    @Parameter
    private String description;

    @Parameter
    private String extensionId;

    @Parameter
    private String icon;

    @Parameter(defaultValue = "RESOURCE_AND_SPECIFICATION")
    private InspectionMode inspectionMode;

    @Parameter(readonly = true, defaultValue = "mapper/v1/java-inspections")
    private String inspectionsResourceDir;

    @Parameter(defaultValue = "false")
    private Boolean listAllArtifacts;

    @Parameter(readonly = true, defaultValue = "${project.build.directory}/classes/META-INF/syndesis/syndesis-extension-definition.json")
    private String metadataDestination;

    @Parameter
    private String name;

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(readonly = true, defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession session;

    /**
     * Partial Extension JSON descriptor to augment
     */
    @Parameter(readonly = true)
    private String source;

    @Parameter(readonly = true, defaultValue = "${project.build.directory}/classes/META-INF/syndesis")
    private File syndesisMetadataSourceDir;

    @Parameter
    private String tags;

    @Parameter
    private String version;

    protected Map<String, Action> actions = new HashMap<>();

    protected Extension.Builder extensionBuilder = new Extension.Builder();

    public enum InspectionMode {
        RESOURCE, RESOURCE_AND_SPECIFICATION, SPECIFICATION
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        tryImportingPartialJSON();
        processAnnotations();
        overrideConfigFromMavenPlugin();
        includeDependencies();
        addIcon();
        generateAtlasMapInspections();
        detectExtensionType();

        final Extension extension = extensionBuilder.actions(actions.values()).build();

        saveExtensionMetaData(extension);
    }

    private void addIcon() {
        // Add a default icon if a icon.png or icon.svg file is found
        if (extensionBuilder.build().getIcon() == null) {
            for (final String iconFileName : BinaryExtensionAnalyzer.getDefault().getAllowedIconFileNames()) {
                final File iconFile = new File(syndesisMetadataSourceDir, iconFileName);
                if (iconFile.exists()) {
                    extensionBuilder.icon("extension:" + iconFileName);
                    break;
                }
            }
        }
    }

    private void assignProperties(final JsonNode root) {

        final String actionId = root.get("id").asText();

        if (isEmpty(actionId)) {
            getLog().warn("Unable to define action, reason: 'id' is not set (" + root + ")");
            return;
        }

        StepAction.Builder actionBuilder = new StepAction.Builder().id(actionId);
        if (actions.containsKey(actionId)) {
            // Create action from existing action if available in the partial
            // json
            actionBuilder = actionBuilder.createFrom(actions.get(actionId));
        }

        final String actionName = root.get("name").asText();
        if (isEmpty(actionName)) {
            getLog().warn("Unable to define action, reason: 'name' is not set (" + root + ")");
            return;
        }

        final String actionKind = root.get("kind").asText();
        if (isEmpty(actionKind)) {
            getLog().warn("Unable to define action, reason: 'kind' is not set (" + root + ")");
            return;
        }

        final String actionEntry = root.get("entrypoint").asText();
        if (isEmpty(actionEntry)) {
            getLog().warn("Unable to define action, reason: 'entrypoint' is not set (" + root + ")");
            return;
        }

        final ArrayNode tags = (ArrayNode) root.get("tags");
        if (tags != null) {
            for (final JsonNode tag : tags) {
                actionBuilder.addTag(tag.asText());
            }
        }

        actions.put(actionId,
            actionBuilder.name(actionName)
                .description(Optional.ofNullable(root.get("description")).map(JsonNode::asText).orElse(""))
                .descriptor(new StepDescriptor.Builder().kind(StepAction.Kind.valueOf(actionKind))
                    .entrypoint(actionEntry)
                    .resource(Optional.ofNullable(root.get("resource")).map(JsonNode::asText).orElse(""))
                    .inputDataShape(buildDataShape(root.get("inputDataShape")))
                    .outputDataShape(buildDataShape(root.get("outputDataShape")))
                    .propertyDefinitionSteps(createPropertiesDefinitionSteps(root)).build())
                .build());
    }

    private void detectExtensionType() throws MojoFailureException {
        // An extension can be of type Steps or Connectors, but not both.
        final long steps = actions.values().stream().filter(StepAction.class::isInstance).count();
        final long connectors = actions.values().stream().filter(ConnectorAction.class::isInstance).count();

        if (steps == 0 && connectors == 0) {
            extensionBuilder.extensionType(Extension.Type.Libraries);
        } else if (steps > 0 && connectors == 0) {
            extensionBuilder.extensionType(Extension.Type.Steps);
        } else if (steps == 0 && connectors > 0) {
            extensionBuilder.extensionType(Extension.Type.Connectors);
        } else {
            throw new MojoFailureException("Extension contains " + steps + " steps and " + connectors
                + " connectors. Mixed extensions are not allowed, you should use only one type of actions (or none).");
        }
    }

    /**
     * Generate atlasmap inspections, no matter if they come from annotations or
     * they are written directly into source json
     *
     * @throws MojoExecutionException
     */
    private void generateAtlasMapInspections() throws MojoExecutionException {
        final Map<String, Action> processedActions = new TreeMap<>();
        for (final Map.Entry<String, Action> actionEntry : actions.entrySet()) {
            final Optional<DataShape> input = generateInspections(actionEntry.getKey(),
                actionEntry.getValue().getInputDataShape());
            final Optional<DataShape> output = generateInspections(actionEntry.getKey(),
                actionEntry.getValue().getOutputDataShape());

            Action newAction;
            if (Action.TYPE_CONNECTOR.equals(actionEntry.getValue().getActionType())) {
                newAction = new ConnectorAction.Builder().createFrom((ConnectorAction) actionEntry.getValue())
                    .descriptor(new ConnectorDescriptor.Builder()
                        .createFrom((ConnectorDescriptor) actionEntry.getValue().getDescriptor())
                        .inputDataShape(input).outputDataShape(output).build())
                    .build();
            } else if (Action.TYPE_STEP.equals(actionEntry.getValue().getActionType())) {
                newAction = new StepAction.Builder().createFrom((StepAction) actionEntry.getValue())
                    .descriptor(new StepDescriptor.Builder()
                        .createFrom((StepDescriptor) actionEntry.getValue().getDescriptor())
                        .inputDataShape(input).outputDataShape(output).build())
                    .build();
            } else {
                throw new IllegalArgumentException(
                    "Unsupported action type: " + actionEntry.getValue().getActionType());
            }

            processedActions.put(actionEntry.getKey(), newAction);
        }
        actions = processedActions;
    }

    private Optional<String> generateInspections(final String actionId, final DataShapeKinds kind, final String type) throws MojoExecutionException {
        Optional<String> specification = Optional.empty();

        if (DataShapeKinds.JAVA == kind) {
            final String name = Names.sanitize(actionId);

            final File outputFile = new File(syndesisMetadataSourceDir,
                String.format("%s/%s/%s.json", inspectionsResourceDir, name, type));
            if (!outputFile.getParentFile().exists() && outputFile.getParentFile().mkdirs()) {
                getLog().debug("Directory " + outputFile.getParentFile() + " created");
            }

            getLog().info("Generating inspection for action: " + actionId + " (" + name + "), and type: " + type);

            List<String> elements;
            try {
                elements = project.getCompileClasspathElements();
            } catch (final DependencyResolutionRequiredException e) {
                throw new MojoExecutionException("Unable to get dependencies needed for compiling", e);
            }
            final URL[] classpath = new URL[elements.size()];

            for (int i = 0; i < elements.size(); i++) {
                final File tmp = new File(elements.get(i));
                try {
                    classpath[i] = tmp.toURI().toURL();
                } catch (final MalformedURLException e) {
                    throw new IllegalStateException(e);
                }

                getLog().debug("Add element to classpath: " + classpath[i]);
            }

            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            try (URLClassLoader loader = new URLClassLoader(classpath, tccl)) {
                final ClassInspectionService classInspectionService = new ClassInspectionService();
                classInspectionService.setConversionService(DefaultAtlasConversionService.getInstance());

                final Class<?> clazz = loader.loadClass(type);
                final JavaClass c = classInspectionService.inspectClass(loader, clazz, CollectionType.NONE, null);
                final ObjectMapper mapper = io.atlasmap.v2.Json.mapper();

                if (inspectionMode == InspectionMode.SPECIFICATION
                    || inspectionMode == InspectionMode.RESOURCE_AND_SPECIFICATION) {
                    specification = Optional.of(mapper.writeValueAsString(c));
                    getLog().info("Specification for type: " + type + " created");
                }

                if (inspectionMode == InspectionMode.RESOURCE
                    || inspectionMode == InspectionMode.RESOURCE_AND_SPECIFICATION) {
                    mapper.writeValue(outputFile, c);
                    getLog().info("Created: " + outputFile);
                }
            } catch (final IOException | ClassNotFoundException e) {
                throw new MojoExecutionException("Unable to generate class inspections", e);
            }
        }

        return specification;
    }

    private Optional<DataShape> generateInspections(final String actionId, final Optional<DataShape> dataShape) throws MojoExecutionException {
        if (dataShape.isPresent()) {

            // don't compute inspections if already set
            final String spec = dataShape.get().getSpecification();
            if (isNotEmpty(spec)) {
                return dataShape;
            }

            final Optional<String> specs = generateInspections(actionId, dataShape.get().getKind(),
                dataShape.get().getType());
            if (specs.isPresent()) {
                final String inspection = specs.get();
                final DataShape.Builder builder = new DataShape.Builder().createFrom(dataShape.get())
                    .specification(inspection);

                final List<DataShape> inspectedShapes = new ArrayList<>();
                for (final DataShape variant : dataShape.get().getVariants()) {
                    inspectedShapes.add(generateInspections(actionId, Optional.of(variant)).orElse(variant));
                }
                builder.variants(inspectedShapes);

                return Optional.of(builder.compress().build());
            }

            return dataShape;
        }

        return Optional.empty();
    }

    private void includeDependencies() {
        List<Artifact> artifacts;

        if (Boolean.TRUE.equals(listAllArtifacts)) {
            artifacts = project.getArtifacts().stream()
                .filter(artifact -> JavaScopes.PROVIDED.equals(artifact.getScope())).map(this::toArtifact)
                .collect(Collectors.toList());
        } else {
            artifacts = project.getDependencies().stream()
                .filter(dependency -> JavaScopes.PROVIDED.equals(dependency.getScope())).map(this::toArtifact)
                .collect(Collectors.toList());
        }

        final Set<String> bomVersionlessArtifacts = artifacts.stream().map(MavenUtils::versionlessKey)
            .collect(Collectors.toSet());

        final List<io.syndesis.common.model.Dependency> jsonDependencies = extensionBuilder.build().getDependencies();

        final List<Artifact> jsonFilteredArtifacts = jsonDependencies.stream()
            .filter(io.syndesis.common.model.Dependency::isMaven).map(io.syndesis.common.model.Dependency::getId)
            .map(MavenUtils::artifactFromId).filter(a -> !bomVersionlessArtifacts.contains(versionlessKey(a)))
            .collect(Collectors.toList());

        final List<io.syndesis.common.model.Dependency> mavenDependencies = Stream
            .concat(jsonFilteredArtifacts.stream(), artifacts.stream()).map(MavenUtils::toGav).sorted()
            .map(io.syndesis.common.model.Dependency::maven).collect(Collectors.toList());

        extensionBuilder.dependencies(
            Stream.concat(mavenDependencies.stream(), jsonDependencies.stream().filter(d -> !d.isMaven()))
                .collect(Collectors.toList()));
    }

    private void overrideConfigFromMavenPlugin() {
        getLog().info("Looking for configuration to override at Maven Plugin configuration level. ");

        final Extension fragment = extensionBuilder.build();

        extensionBuilder.extensionId(nvl(extensionId, fragment.getExtensionId(), project.getGroupId() + ":" + project.getArtifactId()));
        extensionBuilder.version(nvl(version, fragment.getVersion(), project.getVersion()));
        extensionBuilder.name(nvl(name, fragment.getName(), project.getName()));
        extensionBuilder.description(nvl(description, fragment.getDescription(), project.getDescription()));

        if (isNotEmpty(icon)) {
            extensionBuilder.icon(icon);
        }

        if (isNotEmpty(tags)) {
            final String[] split = tags.split(",");
            extensionBuilder.addAllTags(Arrays.asList(split));
        }
    }

    private void processAnnotations() throws MojoExecutionException {
        final String directory = project.getModel().getBuild().getDirectory();
        final Path dir = Paths.get(directory, "generated-sources", "annotations");
        if (Files.exists(dir)) {
            getLog().info("Looking in for annotated classes in: " + dir);
            final ObjectMapper mapper = new ObjectMapper();
            final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.json");
            try (Stream<Path> matches = Files.find(dir, Integer.MAX_VALUE, (path, attr) -> matcher.matches(path))
                .sorted()) {
                final List<Path> paths = matches.collect(Collectors.toList());

                for (final Path path : paths) {
                    try {
                        getLog().info("Loading annotations properties from: " + path);

                        final JsonNode root = mapper.readTree(path.toFile());

                        assignProperties(root);
                    } catch (final IOException e) {
                        getLog().error("Error reading file " + path, e);
                    }
                }
            } catch (final Exception e) {
                throw new MojoExecutionException("Error checking annotations.", e);
            }
        } else {
            getLog().debug("Path " + dir + " does not exists");
        }
    }

    private void saveExtensionMetaData(final Extension jsonObject) throws MojoExecutionException {
        final File targetFile = new File(metadataDestination);
        if (!targetFile.getParentFile().exists() && !targetFile.getParentFile().mkdirs()) {
            throw new MojoExecutionException("Cannot create directory " + targetFile.getParentFile());
        }
        try {
            final JsonNode tree = ExtensionConverter.getDefault().toPublicExtension(jsonObject);
            final ObjectWriter writer = JsonUtils.writer();
            writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValue(targetFile, tree);
            getLog().info("Created file " + targetFile.getAbsolutePath());
        } catch (final IOException e) {
            throw new MojoExecutionException("Cannot write to file: " + metadataDestination, e);
        }
    }

    private Artifact toArtifact(final Dependency dependency) {
        final ArtifactTypeRegistry registry = session.getArtifactTypeRegistry();
        final ArtifactType type = registry.get(dependency.getType());

        return new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getClassifier(),
            type.getExtension(), dependency.getVersion(), type);
    }

    private Artifact toArtifact(final org.apache.maven.artifact.Artifact artifact) {
        final ArtifactTypeRegistry registry = session.getArtifactTypeRegistry();
        final ArtifactType type = registry.get(artifact.getType());

        return new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(),
            type.getExtension(), artifact.getVersion(), type);
    }

    /**
     * Loads a partial metadata json file, if configured at Maven Plugin level.
     *
     * @throws MojoExecutionException
     */
    private void tryImportingPartialJSON() throws MojoExecutionException {
        File template;

        if (isNotEmpty(source)) {
            template = new File(source);
        } else {
            template = new File(metadataDestination);
        }

        if (template.exists()) {
            try (Reader reader = Files.newBufferedReader(template.toPath(), StandardCharsets.UTF_8)) {
                final JsonNode tree = JsonUtils.reader()
                    .readTree(reader);

                final Extension extension = ExtensionConverter.getDefault().toInternalExtension(tree);
                getLog().info("Loaded base partial metadata configuration file: " + source);

                actions.clear();

                for (final Action action : extension.getActions()) {
                    action.getId().ifPresent(id -> actions.put(id, action));
                }

                extensionBuilder = extensionBuilder.createFrom(extension);
                extensionBuilder.actions(Collections.emptySet());
            } catch (final IOException e) {
                throw new MojoExecutionException("Invalid input json: " + source, e);
            }
        }
    }

}
