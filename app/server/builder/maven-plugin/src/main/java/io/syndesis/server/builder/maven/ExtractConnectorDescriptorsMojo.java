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
package io.syndesis.server.builder.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.artifact.resolve.ArtifactResult;

/**
 * Simple maven plugin for creating a JSON file holding all connectors descriptors which are supported.
 *
 * @author roland
 * @since 23.09.17
 */
@Mojo(name = "extract-connector-descriptors", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class ExtractConnectorDescriptorsMojo extends AbstractMojo {

    @Component
    private ArtifactResolver artifactResolver;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
    private List<ArtifactRepository> remoteRepositories;

    @Parameter(defaultValue = "${project.build.outputDirectory}/META-INF/camel/camel-meta.json")
    private String target;

    @Override
    @SuppressWarnings({"PMD.EmptyCatchBlock", "PMD.CyclomaticComplexity"})
    public void execute() throws MojoExecutionException, MojoFailureException {

        ArrayNode root = new ArrayNode(JsonNodeFactory.instance);

        URLClassLoader classLoader = null;
        try {
            PluginDescriptor desc = (PluginDescriptor) getPluginContext().get("pluginDescriptor");
            List<Artifact> artifacts = desc.getArtifacts();
            ProjectBuildingRequest buildingRequest =
                new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
            buildingRequest.setRemoteRepositories(remoteRepositories);
            for (Artifact artifact : artifacts) {
                ArtifactResult result = artifactResolver.resolveArtifact(buildingRequest, artifact);
                File jar = result.getArtifact().getFile();
                classLoader = createClassLoader(jar);
                if (classLoader == null) {
                    throw new IOException("Can not create classloader for " + jar);
                }
                ObjectNode entry = new ObjectNode(JsonNodeFactory.instance);
                addConnectorMeta(entry, classLoader);
                addComponentMeta(entry, classLoader);
                if (entry.size() > 0) {
                    addGav(entry, artifact);
                    root.add(entry);
                }
            }
            if (root.size() > 0) {
                saveCamelMetaData(root);
            }
        } catch (ArtifactResolverException | IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            if (classLoader != null) {
                try {
                    classLoader.close();
                } catch (IOException ignored) {

                }
            }
        }
    }

    private URLClassLoader createClassLoader(File jar) throws MalformedURLException {
        return AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
            @Override
            public URLClassLoader run() {
                try {
                    return new URLClassLoader(new URL[]{jar.toURI().toURL()});
                } catch (MalformedURLException e) {
                    return null;
                }
            }
        });
    }

    private void addConnectorMeta(ObjectNode root, ClassLoader classLoader) {
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
        addOptionalNode(classLoader, node, "meta", "camel-connector.json");
        addOptionalSchemaAsString(classLoader, node, "schema", "camel-connector-schema.json");
        if (node.size() > 0) {
            root.set("connector", node);
        }
    }


    private void addComponentMeta(ObjectNode root, ClassLoader classLoader) {
        // is there any custom Camel components in this library?
        ObjectNode component = new ObjectNode(JsonNodeFactory.instance);

        ObjectNode componentMeta = getComponentMeta(classLoader);
        if (componentMeta != null) {
            component.set("meta", componentMeta);
        }
        addOptionalSchemaAsString(classLoader, component, "schema", "camel-component-schema.json");

        if (component.size() > 0) {
            root.set("component", component);
        }
    }

    private ObjectNode getComponentMeta(ClassLoader classLoader) {
        Properties properties = loadComponentProperties(classLoader);
        if (properties == null) {
            return null;
        }
        String components = (String) properties.get("components");
        if (components == null) {
            return null;
        }
        String[] part = components.split("\\s", -1);
        ObjectNode componentMeta = new ObjectNode(JsonNodeFactory.instance);
        for (String scheme : part) {
            // find the class name
            String javaType = extractComponentJavaType(classLoader, scheme);
            if (javaType == null) {
                continue;
            }
            String schemeMeta = loadComponentJSonSchema(classLoader, scheme, javaType);
            if (schemeMeta == null) {
                continue;
            }
            componentMeta.set(scheme, new TextNode(schemeMeta));
        }
        return componentMeta.size() > 0 ? componentMeta : null;
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private Properties loadComponentProperties(ClassLoader classLoader) {
        try {
            // load the component files using the recommended way by a component.properties file
            InputStream is = classLoader.getResourceAsStream("META-INF/services/org/apache/camel/component.properties");
            if (is != null) {
                Properties ret = new Properties();
                ret.load(is);
                return ret;
            }
        } catch (Exception e) {
            getLog().warn("WARN: Error loading META-INF/services/org/apache/camel/component.properties file due " + e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private String extractComponentJavaType(ClassLoader classLoader, String scheme) {
        try {
            InputStream is = classLoader.getResourceAsStream("META-INF/services/org/apache/camel/component/" + scheme);
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                return (String) props.get("class");
            }
        } catch (Exception e) {
            getLog().warn("WARN: Error loading META-INF/services/org/apache/camel/component/" + scheme + " file due " + e.getMessage());
        }
        return null;
    }

    // TODO: Should return an ObjectNode, but because of
    // Camels' JSonSchemaHelper being very picky on the format, we use it as plain string directly
    private String loadComponentJSonSchema(ClassLoader classLoader, String scheme, String javaType) {
        int pos = javaType.lastIndexOf('.');
        String path = javaType.substring(0, pos);
        path = path.replace('.', '/');
        path = new StringBuilder(path).append("/").append(scheme).append(".json").toString();
        return getOpaqueJsonString(classLoader, path);
    }


    private void saveCamelMetaData(ArrayNode root) throws MojoExecutionException, IOException {
        File targetFile = new File(target);
        if (!targetFile.getParentFile().exists() &&
            !targetFile.getParentFile().mkdirs()) {
            throw new MojoExecutionException("Cannot create directory " + targetFile.getParentFile());
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(target), root);
    }

    private void addOptionalSchemaAsString(ClassLoader cl, ObjectNode node, String key, String path) {
        // We can't use real JSON here as
        // https://github.com/apache/camel/blob/master/camel-core/src/main/java/org/apache/camel/runtimecatalog/JSonSchemaHelper.java#L44-L123
        // can parse proper JSON. We need to do it as an opaque sting
        String result = getOpaqueJsonString(cl, path);
        if (result != null) {
            node.set(key, new TextNode(result));
        }
    }

    private void addOptionalNode(ClassLoader cl, ObjectNode node, String key, String path) {
        JsonNode result = getJson(cl, path);
        if (result != null) {
            node.set(key, result);
        }
    }

    private void addGav(ObjectNode node, Artifact artifact) {
        TextNode gavNode = new TextNode(
            String.format("%s:%s:%s",
                          artifact.getGroupId(),
                          artifact.getArtifactId(),
                          artifact.getVersion()));
        node.set("gav", gavNode);
    }

    private JsonNode getJson(ClassLoader classLoader, String path) {
        try {
            InputStream is = classLoader.getResourceAsStream(path);
            if (is == null) {
                return null;
            }
            return new ObjectMapper().readTree(is);
        } catch (IOException e) {
            return null;
        }
    }

    private String getOpaqueJsonString(ClassLoader classLoader, String path) {
        try {
            InputStream is = classLoader.getResourceAsStream(path);
            if (is == null) {
                return null;
            }
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

}
