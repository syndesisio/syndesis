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

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
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
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.v2.CollectionType;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ActionDescriptor;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.Names;
import io.syndesis.extension.converter.BinaryExtensionAnalyzer;
import io.syndesis.extension.converter.ExtensionConverter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.model.Dependency;
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
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinates;


/**
 * Helper Maven plugin
 *
 * @author pantinor
 */
@Mojo(name = "generate-metadata",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresProject = true,
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
@SuppressWarnings({ "PMD.GodClass", "PMD.TooManyFields", "PMD.TooManyMethods" })
public class GenerateMetadataMojo extends AbstractMojo {
    public enum InspectionMode {
        RESOURCE,
        SPECIFICATION,
        RESOURCE_AND_SPECIFICATION
    }

    @Component
    private ArtifactHandlerManager artifactHandlerManager;

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(readonly = true, defaultValue = "mapper/v1/java-inspections")
    private String inspectionsResourceDir;

    @Parameter(readonly = true, defaultValue = "${project.build.directory}/classes/META-INF/syndesis")
    private File syndesisMetadataSourceDir;

    @Parameter(readonly = true, defaultValue = "${project.build.directory}/classes/META-INF/syndesis/syndesis-extension-definition.json")
    private String metadataDestination;

    @Parameter
    private String extensionId;

    @Parameter
    private String version;

    @Parameter
    private String name;

    @Parameter
    private String description;

    @Parameter
    private String icon;

    @Parameter
    private String tags;

    @Parameter(defaultValue = "RESOURCE_AND_SPECIFICATION")
    @SuppressWarnings("PMD.ImmutableField")
    private InspectionMode inspectionMode = InspectionMode.RESOURCE_AND_SPECIFICATION;

    /**
     * Partial Extension JSON descriptor to augment
     */
    @Parameter(readonly = true)
    private String source;

    @Parameter(defaultValue = "false")
    private Boolean listAllArtifacts;

    protected Extension.Builder extensionBuilder = new Extension.Builder();
    protected Map<String, Action> actions = new HashMap<>();


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        tryImportingPartialJSON();
        processAnnotations();
        overrideConfigFromMavenPlugin();
        includeDependencies();
        addIcon();
        generateAtlasMapInspections();
        detectExtensionType();

        Extension extension = extensionBuilder.actions(actions.values()).build();

        saveExtensionMetaData(extension);
    }

    protected void processAnnotations() throws MojoExecutionException {
        String directory = project.getModel().getBuild().getDirectory();
        Path dir = Paths.get(directory, "generated-sources", "annotations");
        if (Files.exists(dir)) {
            getLog().info("Looking in for annotated classes in: " + dir);
            final ObjectMapper mapper = new ObjectMapper();
            final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.json");
            try (Stream<Path> matches = Files.find(dir, Integer.MAX_VALUE, (path, attr) -> matcher.matches(path)).sorted()) {
                final List<Path> paths = matches.collect(Collectors.toList());

                for (Path path: paths) {
                    try {
                        getLog().info("Loading annotations properties from: " + path);

                        JsonNode root = mapper.readTree(path.toFile());

                        assignProperties(root);
                    } catch (IOException e) {
                        getLog().error("Error reading file " + path);
                    }
                }
            } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
                throw new MojoExecutionException("Error checking annotations.", e);
            }
        } else {
            getLog().debug("Path " + dir + " does not exists");
        }
    }

    @SuppressWarnings({"PMD.PrematureDeclaration", "PMD.SignatureDeclareThrowsException"})
    protected void assignProperties(JsonNode root) throws Exception {

        final String actionId = root.get("id").asText();
        final String actionName = root.get("name").asText();
        final String actionKind = root.get("kind").asText();
        final String actionEntry = root.get("entrypoint").asText();

        if (StringUtils.isEmpty(actionId)) {
            getLog().warn("Unable to define action, reason: 'id' is not set (" + root + ")");
            return;
        }

        StepAction.Builder actionBuilder = new StepAction.Builder().id(actionId);
        if (actions.containsKey(actionId)) {
            // Create action from existing action if available in the partial json
            actionBuilder = actionBuilder.createFrom(actions.get(actionId));
        }

        if (StringUtils.isEmpty(actionName)) {
            getLog().warn("Unable to define action, reason: 'name' is not set (" + root + ")");
            return;
        }
        if (StringUtils.isEmpty(actionKind)) {
            getLog().warn("Unable to define action, reason: 'kind' is not set (" + root + ")");
            return;
        }
        if (StringUtils.isEmpty(actionEntry)) {
            getLog().warn("Unable to define action, reason: 'entrypoint' is not set (" + root + ")");
            return;
        }

        ArrayNode tags = (ArrayNode)root.get("tags");
        if(tags != null){
            for(JsonNode tag: tags) {
                actionBuilder.addTag(tag.asText());
            }
        }

        actions.put(
            actionId,
            actionBuilder
                .name(actionName)
                .description(Optional.ofNullable(root.get("description")).map(JsonNode::asText).orElse(""))
                .descriptor(
                    new StepDescriptor.Builder()
                        .kind(StepAction.Kind.valueOf(actionKind))
                        .entrypoint(actionEntry)
                        .resource(Optional.ofNullable(root.get("resource")).map(JsonNode::asText).orElse(""))
                        .inputDataShape(buildDataShape(root.get("inputDataShape")))
                        .outputDataShape(buildDataShape(root.get("outputDataShape")))
                        .propertyDefinitionSteps(createPropertiesDefinitionSteps(root))
                        .build())
                .build()
        );
    }

    @SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.NPathComplexity"})
    protected Optional<DataShape> buildDataShape(JsonNode root) {
        if (root == null) {
            return Optional.empty();
        }

        DataShape.Builder builder = new DataShape.Builder();
        String kind = Optional.ofNullable(root.get("kind")).map(JsonNode::asText).orElse(DataShapeKinds.NONE.toString());
        String type = Optional.ofNullable(root.get("type")).map(JsonNode::asText).orElse("");
        String name = Optional.ofNullable(root.get("name")).map(JsonNode::asText).orElse("");
        String desc = Optional.ofNullable(root.get("description")).map(JsonNode::asText).orElse("");
        String spec = Optional.ofNullable(root.get("specification")).map(JsonNode::asText).orElse("");

        if (StringUtils.isNotEmpty(type)) {
            int separator = type.indexOf(':');

            if (separator != -1) {
                kind = type.substring(0, separator);
                type = type.substring(separator + 1);
            }
        }

        if (StringUtils.isNotEmpty(kind)) {
            builder.kind(DataShapeKinds.fromString(kind));
        }
        if (StringUtils.isNotEmpty(type)) {
            builder.type(type);
        }
        if (StringUtils.isNotEmpty(name)) {
            builder.name(name);
        }
        if (StringUtils.isNotEmpty(desc)) {
            builder.description(desc);
        }
        if (StringUtils.isNotEmpty(spec)) {
            builder.specification(spec);
        }

        JsonNode meta = root.get("metadata");
        if (meta != null) {
            for (JsonNode node : meta) {
                JsonNode key = node.get("key");
                JsonNode val = node.get("value");

                if (key != null && val != null) {
                    builder.putMetadata(key.asText(), val.asText());
                }
            }
        }

        JsonNode variants = root.get("variants");
        if (variants != null) {
            for (JsonNode node : variants) {
                buildDataShape(node).ifPresent(builder::addVariant);
            }
        }

        return Optional.of(builder.build());
    }

    protected List<ActionDescriptor.ActionDescriptorStep> createPropertiesDefinitionSteps(JsonNode root) {

        ArrayNode properties = (ArrayNode) root.get("properties");
        if (properties != null) {
            ActionDescriptor.ActionDescriptorStep.Builder actionBuilder = new ActionDescriptor.ActionDescriptorStep.Builder();
            actionBuilder.name("extension-properties");
            actionBuilder.description("extension-properties");

            for (JsonNode node: properties) {
                ConfigurationProperty.Builder confBuilder = new ConfigurationProperty.Builder();

                Optional.ofNullable(node.get("description")).ifPresent(n -> confBuilder.description(n.textValue()));
                Optional.ofNullable(node.get("displayName")).ifPresent(n -> confBuilder.displayName(n.textValue()));
                Optional.ofNullable(node.get("componentProperty")).ifPresent(n -> confBuilder.componentProperty(n.booleanValue()));
                Optional.ofNullable(node.get("defaultValue")).ifPresent(n -> confBuilder.defaultValue(n.textValue()));
                Optional.ofNullable(node.get("deprecated")).ifPresent(n -> confBuilder.deprecated(n.booleanValue()));
                Optional.ofNullable(node.get("group")).ifPresent(n -> confBuilder.group(n.textValue()));
                Optional.ofNullable(node.get("javaType")).ifPresent(n -> confBuilder.javaType(n.textValue()));
                Optional.ofNullable(node.get("kind")).ifPresent(n -> confBuilder.kind(n.textValue()));
                Optional.ofNullable(node.get("label")).ifPresent(n -> confBuilder.label(n.textValue()));
                Optional.ofNullable(node.get("required")).ifPresent(n -> confBuilder.required(n.booleanValue()));
                Optional.ofNullable(node.get("secret")).ifPresent(n -> confBuilder.secret(n.booleanValue()));
                Optional.ofNullable(node.get("raw")).ifPresent(n -> confBuilder.raw(n.booleanValue()));
                Optional.ofNullable(node.get("type")).ifPresent(n -> confBuilder.type(n.textValue()));

                ArrayNode tagArray = (ArrayNode)node.get("tags");
                if (tagArray != null) {
                    for (JsonNode tagNode : tagArray) {
                        confBuilder.addTag(tagNode.asText().trim());
                    }
                }

                ArrayNode enumArray = (ArrayNode)node.get("enums");
                if (enumArray != null) {
                    for (JsonNode enumNode : enumArray) {
                        if (enumNode.has("value") && enumNode.has("label")) {
                            confBuilder.addEnum(
                                new ConfigurationProperty.PropertyValue.Builder()
                                    .value(enumNode.get("value").textValue())
                                    .label(enumNode.get("label").textValue())
                                    .build()
                            );
                        }
                    }
                }

                Optional.ofNullable(node.get("name")).ifPresent(n ->actionBuilder.putProperty(n.textValue(), confBuilder.build()));
            }

            return Collections.singletonList(actionBuilder.build());
        }

        return Collections.emptyList();
    }

    /**
     * Loads a partial metadata json file, if configured at Maven Plugin level.
     * @throws MojoExecutionException
     */
    protected void tryImportingPartialJSON() throws MojoExecutionException {
        File template;

        if(StringUtils.isNotEmpty(source)){
            template = new File(source);
        } else {
            template = new File(metadataDestination);
        }

        if (template.exists()) {
            try {
                JsonNode tree = Json.reader().readTree(Files.newBufferedReader(template.toPath(), StandardCharsets.UTF_8));
                Extension extension = ExtensionConverter.getDefault().toInternalExtension(tree);
                getLog().info("Loaded base partial metadata configuration file: " + source);

                actions.clear();

                for (Action action: extension.getActions()) {
                    action.getId().ifPresent(id -> actions.put(id, action));
                }

                extensionBuilder = extensionBuilder.createFrom(extension);
                extensionBuilder.actions(Collections.emptySet());
            } catch (IOException e) {
                throw new MojoExecutionException("Invalid input json: " + source, e );
            }
        }
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
    protected void overrideConfigFromMavenPlugin() {
        getLog().info("Looking for configuration to override at Maven Plugin configuration level. ");

        Extension fragment = extensionBuilder.build();

        if (StringUtils.isNotEmpty(extensionId)) {
            extensionBuilder.extensionId(extensionId);
        } else if (StringUtils.isEmpty(fragment.getExtensionId())) {
            extensionBuilder.extensionId(project.getGroupId() + ":" + project.getArtifactId());
        }

        if (StringUtils.isNotEmpty(version)){
            extensionBuilder.version(version);
        } else if (StringUtils.isEmpty(fragment.getVersion())) {
            extensionBuilder.version(project.getVersion());
        }

        if (StringUtils.isNotEmpty(name)) {
            extensionBuilder.name(name);
        } else if (StringUtils.isEmpty(fragment.getName())) {
            extensionBuilder.name(project.getName());
        }

        if (StringUtils.isNotEmpty(description)) {
            extensionBuilder.description(description);
        } else if (StringUtils.isEmpty(fragment.getDescription())) {
            extensionBuilder.description(project.getDescription());
        }

        if (StringUtils.isNotEmpty(icon)) {
            extensionBuilder.icon(icon);
        }

        if (StringUtils.isNotEmpty(tags)){
            String[] split = tags.split(",");
            extensionBuilder.addAllTags(Arrays.asList(split));
        }
    }

    protected void includeDependencies() {
        List<Artifact> artifacts;

        if (Boolean.TRUE.equals(listAllArtifacts)) {
            artifacts = project.getArtifacts().stream()
                .filter(artifact -> StringUtils.equals(artifact.getScope(), DefaultArtifact.SCOPE_PROVIDED))
                .collect(Collectors.toList());
        } else {
            artifacts = this.project.getDependencies().stream()
                .filter(dependency -> StringUtils.equals(dependency.getScope(), DefaultArtifact.SCOPE_PROVIDED))
                .map(this::toArtifact)
                .collect(Collectors.toList());
        }

        Set<String> bomVersionlessArtifacts = artifacts.stream()
            .map(this::versionlessKey)
            .collect(Collectors.toSet());

        List<io.syndesis.common.model.Dependency> jsonDependencies = extensionBuilder.build().getDependencies();

        List<Artifact> jsonFilteredArtifacts = jsonDependencies.stream()
            .filter(io.syndesis.common.model.Dependency::isMaven)
            .map(io.syndesis.common.model.Dependency::getId)
            .map(this::artifactFromId)
            .filter(a -> !bomVersionlessArtifacts.contains(versionlessKey(a)))
            .collect(Collectors.toList());

        List<io.syndesis.common.model.Dependency> mavenDependencies = Stream.concat(jsonFilteredArtifacts.stream(), artifacts.stream())
            .map(Artifact::getId)
            .sorted()
            .map(io.syndesis.common.model.Dependency::maven)
            .collect(Collectors.toList());

        extensionBuilder.dependencies(Stream.concat(mavenDependencies.stream(), jsonDependencies.stream().filter(d -> !d.isMaven()))
            .collect(Collectors.toList()));
    }

    protected String versionlessKey(Artifact artifact) {
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" +
            Optional.ofNullable(artifact.getType()).orElse("jar") + ":" +
            Optional.ofNullable(artifact.getClassifier()).orElse("");
    }

    protected Artifact artifactFromId(String id) {
        MavenCoordinate c = MavenCoordinates.createCoordinate(id);

        return new DefaultArtifact(c.getGroupId(), c.getArtifactId(), c.getVersion(), "compile", c.getType() != null ? c.getType().getId() : "jar", c.getClassifier(), null);
    }

    protected void saveExtensionMetaData(Extension jsonObject) throws MojoExecutionException {
        File targetFile = new File(metadataDestination);
        if (!targetFile.getParentFile().exists() &&
            !targetFile.getParentFile().mkdirs()) {
            throw new MojoExecutionException("Cannot create directory " + targetFile.getParentFile());
        }
        try {
            JsonNode tree = ExtensionConverter.getDefault().toPublicExtension(jsonObject);
            ObjectWriter writer = Json.writer();
            writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValue(targetFile, tree);
            getLog().info("Created file " + targetFile.getAbsolutePath());
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot write to file: " + metadataDestination, e);
        }
    }

    @Nullable
    protected Boolean getBooleanPropertyValue(Properties props, int prg, String name) {
        String val = getPropertyValue(props, prg, name);
        if (val == null) {
            return null;
        }
        return "true".equalsIgnoreCase(val);
    }

    protected String getPropertyValue(Properties props, int prg, String name) {
        return props.getProperty("property[" + prg + "]." + name);
    }

    protected String getPropertyValue(Properties props, int prg1, String root2, int prg2, String name) {
        return props.getProperty("property[" + prg1 + "]." + root2 + "[" + prg2 + "]." + name);
    }

    // ****************************************
    // Inspections
    // ****************************************

    /**
     * Generate atlasmap inspections, no matter if they come from annotations or they are written directly into source json
     */
    private void generateAtlasMapInspections() throws MojoExecutionException {
        try {
            Map<String, Action> processedActions = new TreeMap<>();
            for (Map.Entry<String, Action> actionEntry : actions.entrySet()) {
                Optional<DataShape> input = generateInspections(actionEntry.getKey(), actionEntry.getValue().getInputDataShape());
                Optional<DataShape> output = generateInspections(actionEntry.getKey(), actionEntry.getValue().getOutputDataShape());

                Action newAction;
                if (Action.TYPE_CONNECTOR.equals(actionEntry.getValue().getActionType())) {
                    newAction = new ConnectorAction.Builder()
                            .createFrom((ConnectorAction) actionEntry.getValue())
                            .descriptor(new ConnectorDescriptor.Builder()
                                    .createFrom((ConnectorDescriptor) actionEntry.getValue().getDescriptor())
                                    .inputDataShape(input)
                                    .outputDataShape(output)
                                    .build())
                            .build();
                } else if (Action.TYPE_STEP.equals(actionEntry.getValue().getActionType())) {
                    newAction = new StepAction.Builder()
                            .createFrom((StepAction) actionEntry.getValue())
                            .descriptor(new StepDescriptor.Builder()
                                    .createFrom((StepDescriptor) actionEntry.getValue().getDescriptor())
                                    .inputDataShape(input)
                                    .outputDataShape(output)
                                    .build())
                            .build();
                } else {
                    throw new IllegalArgumentException("Unsupported action type: " + actionEntry.getValue().getActionType());
                }

                processedActions.put(actionEntry.getKey(), newAction);
            }
            this.actions = processedActions;
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception ex) {
            throw new MojoExecutionException("Error processing atlasmap inspections", ex);
        }
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    protected Optional<DataShape> generateInspections(String actionId, Optional<DataShape> dataShape) throws Exception {
        if (dataShape.isPresent()) {

            // don't compute inspections if already set
            String spec = dataShape.get().getSpecification();
            if (StringUtils.isNotEmpty(spec)) {
                return dataShape;
            }

            Optional<String> specs = generateInspections(actionId, dataShape.get().getKind(), dataShape.get().getType());
            if (specs.isPresent()) {
                String inspection = specs.get();
                DataShape.Builder builder = new DataShape.Builder()
                                                    .createFrom(dataShape.get())
                                                    .specification(inspection);

                List<DataShape> inspectedShapes = new ArrayList<>();
                for (DataShape variant : dataShape.get().getVariants()) {
                    inspectedShapes.add(generateInspections(actionId, Optional.of(variant)).orElse(variant));
                }
                builder.variants(inspectedShapes);

                return Optional.of(builder.compress().build());
            }

            return dataShape;
        }

        return Optional.empty();
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private Optional<String> generateInspections(String actionId, DataShapeKinds kind, String type) throws Exception {
        Optional<String> specification = Optional.empty();

        if (DataShapeKinds.JAVA == kind) {
            final String name = Names.sanitize(actionId);

            File outputFile = new File(syndesisMetadataSourceDir, String.format("%s/%s/%s.json", inspectionsResourceDir, name, type));
            if (!outputFile.getParentFile().exists() && outputFile.getParentFile().mkdirs()) {
                getLog().debug("Directory " + outputFile.getParentFile() + " created");
            }

            getLog().info("Generating inspection for action: " + actionId + " (" + name + "), and type: " + type);

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
                final JavaClass c = classInspectionService.inspectClass(loader, clazz, CollectionType.NONE, null);
                final ObjectMapper mapper = io.atlasmap.v2.Json.mapper();

                if (inspectionMode == InspectionMode.SPECIFICATION || inspectionMode == InspectionMode.RESOURCE_AND_SPECIFICATION) {
                    specification = Optional.of(mapper.writeValueAsString(c));
                    getLog().info("Specification for type: " + type + " created");
                }

                if (inspectionMode == InspectionMode.RESOURCE || inspectionMode == InspectionMode.RESOURCE_AND_SPECIFICATION) {
                    mapper.writeValue(outputFile, c);
                    getLog().info("Created: " + outputFile);
                }
            }
        }

        return specification;
    }

    // ****************************************
    // Extension Type
    // ****************************************

    private void detectExtensionType() throws MojoFailureException {
        // An extension can be of type Steps or Connectors, but not both.
        long steps = actions.values().stream().filter(StepAction.class::isInstance).count();
        long connectors = actions.values().stream().filter(ConnectorAction.class::isInstance).count();

        if (steps == 0 && connectors == 0) {
            extensionBuilder.extensionType(Extension.Type.Libraries);
        } else if (steps > 0 && connectors == 0) {
            extensionBuilder.extensionType(Extension.Type.Steps);
        } else if (steps == 0 && connectors > 0) {
            extensionBuilder.extensionType(Extension.Type.Connectors);
        } else {
            throw new MojoFailureException("Extension contains " + steps + " steps and " + connectors + " connectors. Mixed extensions are not allowed, you should use only one type of actions (or none).");
        }
    }

    // ****************************************
    // Icon
    // ****************************************

    private void addIcon() throws MojoFailureException {
        // Add a default icon if a icon.png or icon.svg file is found
        if (extensionBuilder.build().getIcon() == null) {
            for (String iconFileName : BinaryExtensionAnalyzer.getDefault().getAllowedIconFileNames()) {
                File iconFile = new File(syndesisMetadataSourceDir, iconFileName);
                if (iconFile.exists()) {
                    extensionBuilder.icon("extension:" + iconFileName);
                    break;
                }
            }
        }
    }

    // ****************************************
    // Helpers
    // ****************************************

    protected Artifact toArtifact(Dependency dependency) {
        final ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler(dependency.getType());
        return new DefaultArtifact(dependency.getGroupId(),
            dependency.getArtifactId(),
            dependency.getVersion(),
            dependency.getScope(),
            dependency.getType(),
            dependency.getClassifier(),
            artifactHandler);
    }
}
