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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.syndesis.maven.layouts.ModuleLayoutFactory;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.artifact.filter.collection.ArtifactsFilter;
import org.apache.maven.shared.utils.StringUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
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

/**
 * Helper Maven plugin
 *
 * @author pantinor
 */
@SuppressWarnings({"PMD.ExcessiveImports"})
@Mojo(name = "repackage-extension", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class RepackageExtensionMojo extends SupportMojo {

    public static final String SPRING_BOOT_BOM = "org.springframework.boot:spring-boot-dependencies:pom:";
    public static final String CAMEL_BOM = "org.apache.camel:camel-spring-boot-dependencies:pom:";
    public static final String SYNDESIS_INTEGRATION_RUNTIME_BOM = "io.syndesis:integration-runtime-bom:pom:";
    public static final String SYNDESIS_BOM = "io.syndesis:syndesis-rest-parent:pom:";
    public static final String NET_MINIDEV_JSON_SMART = "net.minidev:json-smart";
    public static final String NET_MINIDEV_ACCESSORS_SMART = "net.minidev:accessors-smart";
    public static final String ORG_OW2_ASM_ASM = "org.ow2.asm:asm";

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

    protected List<String> bomsUsed = new ArrayList<>();

    @Override
    protected void writeAdditionalPrivateFields() throws NoSuchFieldException, IllegalAccessException {
        writeFieldViaReflection("layoutFactory", new ModuleLayoutFactory());
    }

    @Override
    protected Collection getAdditionalFilters() {
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
        String[] defaultGAVs = new String[]{NET_MINIDEV_JSON_SMART, NET_MINIDEV_ACCESSORS_SMART, ORG_OW2_ASM_ASM};
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
        String[] boms = blackListedBoms.split(",");
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

    private String resolveBomVersion(String bom, String property) {
        return bom + project.getProperties().getProperty(property, "RELEASE");
    }

    protected void addDefaultBOMs(Collection<MavenDependency> dependencies) {
        String[] defaultBoms = new String[]{
            resolveBomVersion(SPRING_BOOT_BOM, "spring-boot.version"),
            resolveBomVersion(CAMEL_BOM, "camel.version"),
            resolveBomVersion(SYNDESIS_BOM, "syndesis.version"),
            resolveBomVersion(SYNDESIS_INTEGRATION_RUNTIME_BOM, "syndesis-integration-runtime.version")
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
        String[] gavs = blackListedGAVs.split(",");

        for (String gav : gavs) {
            dependencies.add(newMavenDependency(gav));
        }
    }


    protected Set<MavenDependency> obtainBomDependencies(String urlLocation) throws IOException, MojoExecutionException {
        ArtifactResult artifact = downloadAndInstallArtifact(urlLocation);
        File file = artifact.getArtifact().getFile();

        MavenResolverSystem resolver = Maven.resolver();
        resolver.loadPomFromFile(file).importCompileAndRuntimeDependencies();
        MavenWorkingSession session =((MavenWorkingSessionContainer)resolver).getMavenWorkingSession();

        return session.getDependencyManagement();
    }

    /*
     * @param artifact The artifact coordinates in the format
     *            {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}, must not be {@code null}.
     *            */
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

}
