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

import io.atlasmap.maven.GenerateInspectionsMojo;
import io.syndesis.dao.init.ModelData;
import io.syndesis.dao.init.ReadApiClientData;
import io.syndesis.model.Kind;
import io.syndesis.model.connection.Action;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.DataShape;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@Mojo(name = "generate-mapper-inspections", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateMapperInspectionsMojo extends AbstractMojo {

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Component
    private RepositorySystem system;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepos;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/atlasmap")
    private File outputDir;

    @Parameter(defaultValue = "static/mapper/v1/java-inspections")
    private String resourceDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            Resource resource = new Resource();
            resource.setDirectory(outputDir.getCanonicalPath());
            project.addResource(resource);

            HashSet<File> generated = new HashSet<>();

            ReadApiClientData reader = new ReadApiClientData();
            List<ModelData<?>> modelList = reader.readDataFromFile("io/syndesis/dao/deployment.json");
            for (ModelData<?> model : modelList) {
                if (model.getKind() == Kind.Connector) {
                    Connector connector = (Connector) model.getData();

                    for (Action action : connector.getActions()) {

                        process(generated, connector, action, action.getInputDataShape());
                        process(generated, connector, action, action.getOutputDataShape());

                    }

                }
            }
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    private void process(HashSet<File> generated, Connector connector, Action action, DataShape shape) throws MojoFailureException, MojoExecutionException {
        if (shape == null) {
            return;
        }
        if (!connector.getId().isPresent()) {
            return;
        }
        if (!"java".equals(shape.getKind())) {
            return;
        }

        getLog().info("Generating for connector: " + connector.getId().get() + ", and type: " + shape.getType());
        File outputFile = new File(outputDir, resourceDir + "/" + connector.getId().get() + "/" + shape.getType() + ".json");
        if (generated.contains(outputFile)) {
            return;
        }

        if( outputFile.getParentFile().mkdirs() ) {
            getLog().debug("Created dir: "+outputFile.getParentFile());
        }

        GenerateInspectionsMojo generateInspectionsMojo = new GenerateInspectionsMojo();
        generateInspectionsMojo.setLog(getLog());
        generateInspectionsMojo.setPluginContext(getPluginContext());
        generateInspectionsMojo.setSystem(system);
        generateInspectionsMojo.setRemoteRepos(remoteRepos);
        generateInspectionsMojo.setRepoSession(repoSession);
        generateInspectionsMojo.setGav(action.getCamelConnectorGAV());
        generateInspectionsMojo.setClassName(shape.getType());
        generateInspectionsMojo.setOutputFile(outputFile);
        generateInspectionsMojo.execute();
        generated.add(outputFile);
    }

}
