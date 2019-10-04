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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.extension.converter.ExtensionConverter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.artifact.filter.collection.ArtifactsFilter;
import org.apache.maven.shared.utils.StringUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.graph.transformer.NoopDependencyGraphTransformer;
import org.eclipse.aether.util.graph.visitor.PostorderNodeListGenerator;
import org.springframework.boot.maven.Exclude;
import org.springframework.boot.maven.ExcludeFilter;

import com.fasterxml.jackson.databind.JsonNode;

@Mojo(name = "repackage-extension", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class RepackageExtensionMojo extends SupportMojo {

    private static final String[] DEPENDENCIES_ADDED_BY_DEFAULT = new String[] {"io.syndesis.extension:extension-annotation-processor:X"};

    private static final String SYNDESIS_EXTENSION_BOM = "io.syndesis.extension:extension-bom:pom:";

    @Parameter(readonly = true, defaultValue = "${project.build.directory}/classes/META-INF/syndesis/syndesis-extension-definition.json")
    private String metadataDestination;

    @Parameter(readonly = true, defaultValue = "${project.remotePluginRepositories}")
    private List<RemoteRepository> remoteRepos;

    @Component
    private RepositorySystem repository;

    @Parameter(readonly = true, defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession session;

    @Parameter
    protected String blackListedBoms;

    @Parameter
    protected String blackListedGAVs;

    protected List<String> bomsUsed = new ArrayList<>();

    protected Extension extensionDescriptor;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        loadExtensionDescriptor();
        super.execute();
    }

    private void addCustomBoms(final Collection<Dependency> dependencies) {
        final String[] boms = blackListedBoms.split(",", -1);
        for (final String bom : boms) {
            final String trimmed = bom.trim();
            dependencies.addAll(obtainBomDependencies(trimmed));
        }

        bomsUsed = Arrays.asList(boms);
    }

    private void addCustomDependencies(final Collection<Dependency> dependencies) {
        final String[] gavs = blackListedGAVs.split(",", -1);

        for (final String gav : gavs) {
            dependencies.add(newDependency(gav));
        }
    }

    private void addDefaultBOMs(final Collection<Dependency> dependencies) {
        final String[] defaultBoms = new String[] {
            resolveBomVersion(SYNDESIS_EXTENSION_BOM, "io.syndesis:extension-api", "syndesis.version")};

        for (final String bom : defaultBoms) {
            dependencies.addAll(obtainBomDependencies(bom));
        }

        bomsUsed = Arrays.asList(defaultBoms);
    }

    /**
     * @param artifact The artifact coordinates in the format {@code
     * <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}, must not
     *            be {@code null}.
     *
     */
    private ArtifactResult downloadAndInstallArtifact(final String artifact) {
        final ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(new DefaultArtifact(artifact));
        request.setRepositories(remoteRepos);

        getLog().info("Resolving artifact " + artifact + " from " + remoteRepos);
        try {
            return repository.resolveArtifact(session, request);
        } catch (final ArtifactResolutionException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private String getVersionFromDependencyManagement(final String artifact) {
        final String[] parts = artifact.split(":", -1);
        final String groupId = parts.length > 0 ? parts[0] : "";
        final String artifactId = parts.length > 1 ? parts[1] : "";
        for (final org.apache.maven.model.Dependency dep : project.getDependencyManagement().getDependencies()) {
            if (groupId.equals(dep.getGroupId()) && artifactId.equals(dep.getArtifactId())) {
                return dep.getVersion();
            }
        }
        return null;
    }

    private void loadExtensionDescriptor() throws MojoExecutionException {
        if (metadataDestination == null) {
            return;
        }
        final File metadata = new File(metadataDestination);
        if (!metadata.exists()) {
            return;
        }

        try {
            final JsonNode tree = JsonUtils.reader()
                .readTree(Files.newBufferedReader(metadata.toPath(), StandardCharsets.UTF_8));
            extensionDescriptor = ExtensionConverter.getDefault().toInternalExtension(tree);
        } catch (final IOException e) {
            throw new MojoExecutionException("Error while loading the extension metadata", e);
        }
    }

    private List<Dependency> obtainBomDependencies(final String urlLocation) {
        final Artifact artifact = downloadAndInstallArtifact(urlLocation).getArtifact();

        final Dependency dependency = new Dependency(artifact, JavaScopes.RUNTIME);

        final List<RemoteRepository> remoteRepositories = project.getRepositories().stream()
            .map(r -> new RemoteRepository.Builder(r.getId(), r.getLayout(), r.getUrl()).build())
            .collect(Collectors.toList());

        CollectResult result;
        try {
            final ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest(artifact, remoteRepositories, null);
            final ArtifactDescriptorResult descriptor = repository.readArtifactDescriptor(session, descriptorRequest);

            final List<Dependency> dependencies = Stream.concat(
                descriptor.getDependencies().stream(),
                descriptor.getManagedDependencies().stream())
                .collect(Collectors.toList());

            final DefaultRepositorySystemSession sessionToUse = new DefaultRepositorySystemSession(session);
            sessionToUse.setDependencyGraphTransformer(new NoopDependencyGraphTransformer());

            final CollectRequest request = new CollectRequest(dependency, dependencies, remoteRepositories);
            result = repository.collectDependencies(sessionToUse, request);
        } catch (final DependencyCollectionException | ArtifactDescriptorException e) {
            throw new IllegalStateException("Unabele to obtain BOM dependencies for: " + urlLocation, e);
        }

        final DependencyNode root = result.getRoot();

        final PostorderNodeListGenerator visitor = new PostorderNodeListGenerator();
        root.accept(visitor);

        return visitor.getDependencies(true);
    }

    private String resolveBomVersion(final String bom, final String containedArtifact, final String property) {
        String propertyVal = project.getProperties().getProperty(property);
        if (propertyVal == null) {
            propertyVal = getVersionFromDependencyManagement(containedArtifact);
        }
        if (propertyVal == null) {
            propertyVal = "RELEASE";
        }
        return bom + propertyVal;
    }

    @Override
    protected Collection<ArtifactsFilter> getAdditionalFilters() {
        final Collection<Dependency> dependencies = new HashSet<>();

        if (StringUtils.isNotBlank(blackListedBoms)) {
            addCustomBoms(dependencies);
        } else {
            addDefaultBOMs(dependencies);
        }

        if (StringUtils.isNotBlank(blackListedGAVs)) {
            addCustomDependencies(dependencies);
        } else {
            addDefaultDependencies(dependencies);
        }

        if (getLog().isDebugEnabled()) {
            getLog().debug("Filtering out dependencies from the following BOMs: " + bomsUsed);
            getLog().debug("Dependencies to be filtered out:");
            dependencies.stream().map(d -> d.getArtifact()).sorted(Comparator.comparing(Artifact::toString))
                .forEach(a -> getLog().debug(a.toString()));
        }
        final List<ArtifactsFilter> filters = new ArrayList<>(dependencies.size());

        dependencies.forEach(d -> filters.add(newExcludeFilter(d)));

        return filters;
    }

    @Override
    protected void writeAdditionalPrivateFields() throws NoSuchFieldException, IllegalAccessException {
        boolean filter = true;
        if (extensionDescriptor != null && Extension.Type.Libraries.equals(extensionDescriptor.getExtensionType())) {
            filter = false;
            includeSystemScope = true;
        }
        writeFieldViaReflection("layoutFactory", new ModuleLayoutFactory(filter));
    }

    private static void addDefaultDependencies(final Collection<Dependency> dependencies) {
        for (final String gav : DEPENDENCIES_ADDED_BY_DEFAULT) {
            dependencies.add(newDependency(gav));
        }
    }

    private static Dependency newDependency(final String gav) {
        return new Dependency(new DefaultArtifact(gav), null);
    }

    private static ExcludeFilter newExcludeFilter(final Dependency dependency) {
        final Artifact artifact = dependency.getArtifact();

        final Exclude exclude = new Exclude();
        exclude.setGroupId(artifact.getGroupId());
        exclude.setArtifactId(artifact.getArtifactId());

        return new ExcludeFilter(exclude);
    }

}
