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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.apache.maven.shared.artifact.filter.collection.ArtifactsFilter;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;
import org.springframework.boot.maven.RepackageMojo;

/**
 * Base Mojo Support class to provide helper methods from the specialized Mojos
 */
abstract class SupportMojo extends RepackageMojo {
    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    protected String finalName;
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    protected File outputDirectory;
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        writePrivateFields();
        super.execute();
    }

    /**
     * Fields marked as required in the delegated Mojo. Annotation based
     * initialization is not triggered so we have to trigger it here, and
     * forward the values.
     */
    private void writePrivateFields() throws MojoFailureException {
        try {

            writeFieldViaReflection("project", project);
            writeFieldViaReflection("outputDirectory", outputDirectory);
            writeFieldViaReflection("finalName", finalName);

            writeAdditionalPrivateFields();

        } catch (final NoSuchFieldException e) {
            getLog().error(
                "Field is no longer present in class org.springframework.boot.maven.RepackageIntegrationMojo");
            throw new MojoFailureException("Problem with the wrapped Spring Boot Plugin", e);
        } catch (final IllegalAccessException e) {
            getLog().error("Error while invoking Spring Boot Plugin Classes");
            throw new MojoFailureException("Problem with the wrapped Spring Boot Plugin", e);
        }

    }

    @Override
    protected final Set<Artifact> filterDependencies(final Set<Artifact> dependencies, final FilterArtifacts filters)
        throws MojoExecutionException {
        try {
            final Set<Artifact> filtered = new LinkedHashSet<>(dependencies);

            final Collection<ArtifactsFilter> filtersToUse = filters.getFilters();
            filtersToUse.addAll(getAdditionalFilters());

            filtered.retainAll(filters.filter(dependencies));
            return filtered;
        } catch (final ArtifactFilterException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
    }

    /**
     * Defines the default set of artifacts to exclude from the repackaged
     * module
     *
     * @return
     */
    protected Collection<ArtifactsFilter> getAdditionalFilters() {
        return new ArrayList<>();
    }

    /**
     * Optional method used to specify the set of fields you want to write.
     * Often uses `writeFieldViaReflection` to do its job.
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    protected abstract void writeAdditionalPrivateFields() throws NoSuchFieldException, IllegalAccessException;

    protected final void writeFieldViaReflection(final String fieldName, final Object value)
        throws NoSuchFieldException, IllegalAccessException {
        final Field field = obtainAccessibleFieldInAncestors(fieldName);
        field.set(this, value);
    }

    /**
     * Utility method to find a reference to a Field in the Superclass hiearchy
     * to be set via reflection.
     *
     * @param fieldName
     * @return
     * @throws NoSuchFieldException
     */
    private static Field obtainAccessibleFieldInAncestors(final String fieldName) throws NoSuchFieldException {
        Class<?> clazz = SupportMojo.class.getSuperclass();
        while (clazz != null) {
            for (final Field field : clazz.getDeclaredFields()) {
                if (fieldName.equals(field.getName())) {
                    field.setAccessible(true);
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new NoSuchFieldException("fieldName: " + fieldName + " not found in current class hierarchy");
    }

}
