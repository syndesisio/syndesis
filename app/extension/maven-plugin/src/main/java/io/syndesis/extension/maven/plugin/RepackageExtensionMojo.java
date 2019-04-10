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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import io.syndesis.common.util.Json;
import io.syndesis.extension.converter.ExtensionConverter;
import io.syndesis.common.model.extension.Extension;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.model.io.ModelWriter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.collection.ArtifactsFilter;
import org.apache.maven.shared.utils.StringUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.MavenWorkingSession;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinates;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependencies;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependency;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenWorkingSessionContainer;
import org.springframework.boot.maven.Exclude;
import org.springframework.boot.maven.ExcludeFilter;

import io.syndesis.extension.maven.plugin.layout.ModuleLayoutFactory;

/**
 * Helper Maven plugin
 *
 * @author pantinor
 */
@SuppressWarnings({"PMD.ExcessiveImports"})
@Mojo(name = "repackage-extension", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class RepackageExtensionMojo extends SupportMojo {

    public static final String SYNDESIS_BOM = "io.syndesis.extension:extension-bom:pom:";
    public static final String NET_MINIDEV_JSON_SMART = "net.minidev:json-smart";
    public static final String NET_MINIDEV_ACCESSORS_SMART = "net.minidev:accessors-smart";
    public static final String ORG_OW2_ASM_ASM = "org.ow2.asm:asm";
    public static final String SYNDESIS_ANNOTATION_PROCESSOR = "io.syndesis.extension:extension-annotation-processor";

    @Parameter
    protected String blackListedBoms;

    @Parameter
    protected String blackListedGAVs;

    @Component
    private RepositorySystem repoSystem;

    @Parameter(readonly = true, defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession repoSession;

    @Parameter(readonly = true, defaultValue = "${project.remotePluginRepositories}")
    private List<RemoteRepository> remoteRepos;

    @Parameter(readonly = true, defaultValue = "${project.build.directory}/classes/META-INF/syndesis/syndesis-extension-definition.json")
    private String metadataDestination;

    protected List<String> bomsUsed = new ArrayList<>();

    protected Extension extensionDescriptor;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        loadExtensionDescriptor();
        super.execute();
    }

    @Override
    protected void writeAdditionalPrivateFields() throws NoSuchFieldException, IllegalAccessException {
        boolean filter = true;
        if (extensionDescriptor != null && Extension.Type.Libraries.equals(extensionDescriptor.getExtensionType())) {
            filter = false;
            this.includeSystemScope = true;
        }
        writeFieldViaReflection("layoutFactory", new ModuleLayoutFactory(filter));
    }

    @Override
    protected Collection<ArtifactsFilter> getAdditionalFilters() {
        Collection<MavenDependency> dependencies = new HashSet<>();

        if(StringUtils.isNotBlank(blackListedBoms)){
            addCustomBoms(dependencies);
        } else {
            addDefaultBOMs(dependencies);
        }

        if(StringUtils.isNotBlank(blackListedGAVs)){
            addCustomGAVs(dependencies);
        } else {
            addDefaultGAVs(dependencies);
        }

        if(getLog().isDebugEnabled()){
            getLog().debug("Filtering out dependencies from the following BOMs: " + bomsUsed);
            getLog().debug("Dependencies to be filtered out:");
            dependencies.stream().sorted(Comparator.comparing(MavenCoordinate::toCanonicalForm)).forEach(mavenDependency -> getLog().debug(mavenDependency.toCanonicalForm()));
        }
        List<ArtifactsFilter> filters = new ArrayList<>(dependencies.size());

        for(MavenCoordinate dep : dependencies){
            filters.add(newExcludeFilter(dep));
        }

        return filters;
    }

    protected void addDefaultGAVs(Collection<MavenDependency> dependencies) {
        String[] defaultGAVs = new String[]{NET_MINIDEV_JSON_SMART, NET_MINIDEV_ACCESSORS_SMART, ORG_OW2_ASM_ASM, SYNDESIS_ANNOTATION_PROCESSOR};
        for (String gav : defaultGAVs){
            dependencies.add(newMavenDependency(gav));
        }
    }

    protected MavenDependency newMavenDependency(String gav) {
        MavenCoordinate coordinate = MavenCoordinates.createCoordinate(gav.trim());
        return MavenDependencies.createDependency(coordinate, ScopeType.COMPILE, false );
    }

    protected ExcludeFilter newExcludeFilter(MavenCoordinate dep) {
        Exclude exclude = new Exclude();
        exclude.setGroupId(dep.getGroupId());
        exclude.setArtifactId(dep.getArtifactId());
        return new ExcludeFilter(exclude);
    }

    protected void addCustomBoms(Collection<MavenDependency> dependencies) {
        String[] boms = blackListedBoms.split(",", -1);
        try {
            for (String bom : boms) {
                String trimmed = bom.trim();
                dependencies.addAll(obtainBomDependencies(trimmed));
            }
        } catch (IOException | MojoExecutionException e) {
            throw new IllegalStateException(e);
        }
        bomsUsed = Arrays.asList(boms);
    }

    private String resolveBomVersion(String bom, String containedArtifact, String property) {
        String propertyVal = project.getProperties().getProperty(property);
        if (propertyVal == null) {
            propertyVal = getVersionFromDependencyManagement(containedArtifact);
        }
        if (propertyVal == null) {
            propertyVal = "RELEASE";
        }
        return bom + propertyVal;
    }

    private String getVersionFromDependencyManagement(String artifact) {
        String[] parts = artifact.split(":", -1);
        String groupId = parts.length > 0 ? parts[0] : "";
        String artifactId = parts.length > 1 ? parts[1] : "";
        for (Dependency dep : project.getDependencyManagement().getDependencies()) {
            if (groupId.equals(dep.getGroupId()) && artifactId.equals(dep.getArtifactId())) {
                return dep.getVersion();
            }
        }
        return null;
    }

    protected void addDefaultBOMs(Collection<MavenDependency> dependencies) {
        String[] defaultBoms = new String[]{
            resolveBomVersion(SYNDESIS_BOM, "io.syndesis:extension-api", "syndesis.version")
        };

        try {
            for(String bom : defaultBoms){
                dependencies.addAll(obtainBomDependencies(bom));
            }
            bomsUsed = Arrays.asList(defaultBoms);
        } catch (IOException | MojoExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    protected void addCustomGAVs(Collection<MavenDependency> dependencies) {
        String[] gavs = blackListedGAVs.split(",", -1);

        for (String gav : gavs) {
            dependencies.add(newMavenDependency(gav));
        }
    }

    protected Set<MavenDependency> obtainBomDependencies(String urlLocation) throws IOException, MojoExecutionException {
        Artifact artifact = downloadAndInstallArtifact(urlLocation).getArtifact();

        File tempPom = new File(outputDirectory, ".syndesis-extension-plugin-temp-pom");
        try (BufferedWriter out = Files.newBufferedWriter(tempPom.toPath(), StandardCharsets.UTF_8)) {

            Dependency bom = new Dependency();
            bom.setGroupId(artifact.getGroupId());
            bom.setArtifactId(artifact.getArtifactId());
            bom.setVersion(artifact.getVersion());
            bom.setType(artifact.getExtension());
            bom.setScope("import");

            Model bomModel = new Model();
            bomModel.setDependencyManagement(new DependencyManagement());
            bomModel.getDependencyManagement().addDependency(bom);
            bomModel.setRepositories(project.getRepositories());
            MavenProject bomProject = new MavenProject();
            bomProject.setModel(bomModel);
            bomProject.setModelVersion(project.getModelVersion());
            bomProject.setGroupId(project.getGroupId());
            bomProject.setArtifactId(project.getArtifactId() + "-temp-bom");
            bomProject.setVersion(project.getVersion());


            ModelWriter modelWriter = new DefaultModelWriter();
            modelWriter.write(out, Collections.emptyMap(), bomProject.getModel());

            MavenResolverSystem resolver = Maven.resolver();
            resolver.loadPomFromFile(tempPom).importCompileAndRuntimeDependencies();
            MavenWorkingSession session =((MavenWorkingSessionContainer)resolver).getMavenWorkingSession();

            return session.getDependencyManagement();
        } finally {
            if (!tempPom.delete()) {
                getLog().warn("Cannot delete file " + tempPom);
            }
        }
    }

    /*
     * @param artifact The artifact coordinates in the format
     *            {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}, must not be {@code null}.
     *
     */
    protected ArtifactResult downloadAndInstallArtifact(String artifact) throws MojoExecutionException {
        ArtifactResult result;

        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact( new DefaultArtifact( artifact ) );
        request.setRepositories( remoteRepos );

        getLog().info( "Resolving artifact " + artifact + " from " + remoteRepos );
        try {
            result = repoSystem.resolveArtifact( repoSession, request );
            return result;
        } catch ( ArtifactResolutionException e ) {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    protected void loadExtensionDescriptor() throws MojoExecutionException {
        if (metadataDestination == null) {
            return;
        }
        File metadata = new File(metadataDestination);
        if (!metadata.exists()) {
            return;
        }

        try {
            JsonNode tree = Json.reader().readTree(Files.newBufferedReader(metadata.toPath(), StandardCharsets.UTF_8));
            this.extensionDescriptor = ExtensionConverter.getDefault().toInternalExtension(tree);
        } catch (IOException e) {
            throw new MojoExecutionException("Error while loading the extension metadata", e);
        }
    }

}
