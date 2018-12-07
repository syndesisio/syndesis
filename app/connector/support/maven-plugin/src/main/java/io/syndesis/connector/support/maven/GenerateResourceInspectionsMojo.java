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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.xml.inspect.SchemaInspector;
import io.atlasmap.xml.inspect.XmlInspectionException;
import io.atlasmap.xml.v2.XmlDocument;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.function.Function;

@Mojo(
    name = "generate-resource-inspections",
    defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateResourceInspectionsMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}")
    public MavenProject project;

    @Parameter()
    private String resourceProcessorClass;

    @Parameter(required = true)
    private String inputDirectory;

    @Parameter(required = true)
    private String outputDirectory;

    @SuppressWarnings({"ReturnValueIgnored"})
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Function<File, File> resourceProcessor = newResourceProcessor();

        File inputDir = new File(inputDirectory);

        if (resourceProcessor != null && inputDir.exists() && inputDir.isDirectory()) {
            File[] files = inputDir.listFiles();
            for (File file : files) {
                resourceProcessor.apply(file);
            }
        }

        final ObjectMapper mapper = io.atlasmap.v2.Json.mapper();
        File[] files = inputDir.listFiles();
        for (File file: files) {
            if (file.getName().endsWith(".xsd")) {
                SchemaInspector inspector = new SchemaInspector();
                try {
                    inspector.inspect(file);
                } catch (XmlInspectionException e) {
                    throw new MojoExecutionException("Cannot inspect schema " + file.getName() + " due to " + e.getCause().getMessage(), e);
                }
                XmlDocument xmlDocument = inspector.getXmlDocument();
                String inspection;
                try {
                    inspection = mapper.writer((PrettyPrinter) null).writeValueAsString(xmlDocument);
                } catch (JsonProcessingException e) {
                    throw new MojoExecutionException("Cannot serialize inspected " + file.getName() + " to json due to " + e.getCause().getMessage(), e);
                }

                File outputDir = new File(outputDirectory);
                outputDir.mkdirs();

                File json = new File(outputDir, file.getName().replace(".xsd", ".json"));
                try (FileOutputStream jsonOut = new FileOutputStream(json)) {
                    IOUtils.write(inspection, jsonOut);
                    getLog().info("Generated " + json.getPath());
                } catch (IOException e) {
                    throw new MojoExecutionException("Cannot write inspected json " + json.getName() + " due to " + e.getCause().getMessage(), e);
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private Function<File, File> newResourceProcessor() throws MojoFailureException {
        Function<File, File> resourceProcessor = null;
        if (!StringUtils.isBlank(resourceProcessorClass)) {
            try {
                Class<?> aClass = getClassLoader(project).loadClass(resourceProcessorClass);
                resourceProcessor = (Function<File, File>) aClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new MojoFailureException("Failed to instantiate resourceProcessorClass: " + resourceProcessorClass, e);
            }
        }
        return resourceProcessor;
    }

    private ClassLoader getClassLoader(MavenProject project) {
        try {
            List<String> classpathElements = project.getCompileClasspathElements();
            classpathElements.add( project.getBuild().getOutputDirectory() );
            classpathElements.add( project.getBuild().getTestOutputDirectory() );
            URL urls[] = new URL[classpathElements.size()];
            for (int i = 0; i < classpathElements.size(); ++i) {
                urls[i] = new File(classpathElements.get(i)).toURL();
            }
            return new URLClassLoader(urls, this.getClass().getClassLoader());
        } catch ( Exception e ) {
            getLog().debug( "Couldn't get the classloader." );
            return this.getClass().getClassLoader();
        }
    }
}
