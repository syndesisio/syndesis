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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.xml.inspect.XmlInspectionException;
import io.atlasmap.xml.inspect.XmlSchemaInspector;
import io.atlasmap.xml.v2.XmlDocument;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Generates inspections for resources in the given inputDirectory and stores them in the outputDirectory.
 *
 * It deletes files for which inspections have been created.
 *
 * Currently it supports only xml schemas with .xsd extension.
 *
 * It is possible to provide resourcesProcessorClass, which points to a class implementing BiConsumer<Path, Path>.
 * It can implement any processing on resources before running inspections and accepts input and output directories.
 * When the class is provided within the same project, the plugin must be run no sooner than in the process-classes
 * phase so that the class is available on the classpath.
 */
@Mojo(
    name = "generate-resource-inspections",
    defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateResourceInspectionsMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}")
    public MavenProject project;

    @Parameter
    private String resourcesProcessorClass;

    @Parameter(required = true)
    private String inputDirectory;

    @Parameter
    private String outputDirectory;

    private Path outputDir;

    final ObjectMapper mapper = io.atlasmap.v2.Json.mapper();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Path inputDir = FileSystems.getDefault().getPath(inputDirectory);
        if (!Files.isDirectory(inputDir)) {
            throw new MojoExecutionException("inputDirectory " + inputDirectory + " must be an existing directory");
        }

        outputDir = FileSystems.getDefault().getPath(outputDirectory);
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed creating directory " + outputDir, e);
        }

        try (DirectoryStream<Path> files = Files.newDirectoryStream(inputDir, "*.xsd")) {
            for (Path file: files) {
                Path out = outputDir.resolve(file.getFileName());
                Files.copy(file, out);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed copying files from " + inputDir + " to " + outputDir, e);
        }

        BiConsumer<Path, Path> resourcesProcessor = newResourcesProcessor();
        if (resourcesProcessor != null) {
            getLog().info("Processing files with " + resourcesProcessorClass);
            resourcesProcessor.accept(inputDir, outputDir);
        } else {
            getLog().info("The resourcesProcessorClass parameter not specified");
        }

        try (DirectoryStream<Path> files = Files.newDirectoryStream(outputDir, "*.xsd")) {
            for(Path file: files) {
                generateInspection(file);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot generate inspections in " + outputDir, e);
        }

    }

    private void generateInspection(Path xsdFile) throws IOException {
        XmlSchemaInspector inspector = new XmlSchemaInspector();
        try {
            inspector.inspect(xsdFile.toFile());
        } catch (XmlInspectionException e) {
            throw new IOException("Cannot inspect schema " + xsdFile, e);
        }
        XmlDocument xmlDocument = inspector.getXmlDocument();
        String inspection;
        try {
            inspection = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(xmlDocument);
        } catch (JsonProcessingException e) {
            throw new IOException("Cannot serialize inspected " + xsdFile + " to json", e);
        }

        Files.delete(xsdFile);

        final Path xsdFileName = xsdFile.getFileName();
        if (xsdFileName == null) {
            throw new IllegalArgumentException("Given path doesn't point to a file: " + xsdFile);
        }

        final String jsonFileName = xsdFileName.toString().replace(".xsd", ".json");
        Path jsonFile = outputDir.resolve(jsonFileName);
        try (OutputStream jsonOut = Files.newOutputStream(jsonFile)) {
            IOUtils.write(inspection, jsonOut, StandardCharsets.UTF_8);
            getLog().info("Generated " + jsonFile);
        }
    }

    @SuppressWarnings("unchecked")
    private BiConsumer<Path, Path> newResourcesProcessor() throws MojoExecutionException {
        if (StringUtils.isBlank(resourcesProcessorClass)) {
            return null;
        }

        try {
            Class<?> aClass = getClassLoader(project).loadClass(resourcesProcessorClass);
            return (BiConsumer<Path, Path>) aClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to instantiate resourcesProcessorClass: " + resourcesProcessorClass, e);
        }
    }

    private ClassLoader getClassLoader(MavenProject project) throws DependencyResolutionRequiredException, MalformedURLException {
        List<String> classpathElements = project.getCompileClasspathElements();
        classpathElements.add( project.getBuild().getOutputDirectory() );
        classpathElements.add( project.getBuild().getTestOutputDirectory() );
        URL urls[] = new URL[classpathElements.size()];
        for (int i = 0; i < classpathElements.size(); ++i) {
            urls[i] = new File(classpathElements.get(i)).toURI().toURL();
        }
        return new URLClassLoader(urls, this.getClass().getClassLoader());
    }
}
