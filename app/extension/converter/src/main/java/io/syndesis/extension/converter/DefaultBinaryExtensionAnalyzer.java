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
package io.syndesis.extension.converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.util.json.JsonUtils;

/**
 * Tools to analyze binary extensions.
 */
class DefaultBinaryExtensionAnalyzer implements BinaryExtensionAnalyzer {

    private static final String SYNDESIS_ROOT = "META-INF/syndesis/";
    private static final String MANIFEST_LOCATION = SYNDESIS_ROOT + "syndesis-extension-definition.json";

    private final Map<String, String> allowedIconPaths;

    DefaultBinaryExtensionAnalyzer() {
        this.allowedIconPaths = new TreeMap<>();
        this.allowedIconPaths.put("icon.png", "image/png");
        this.allowedIconPaths.put("icon.svg", "image/svg+xml");
    }

    @Override
    public Extension getExtension(InputStream binaryExtension) {
        try {
            return doGetExtension(binaryExtension);
        } catch (IOException ex) {
            throw SyndesisServerException.launderThrowable("Cannot read from binary extension file", ex);
        }
    }

    @Override
    public Optional<InputStream> getIcon(InputStream binaryExtension, String path) {
        if (!allowedIconPaths.containsKey(path)) {
            try {
                binaryExtension.close();
            } catch (IOException ignored) {
                // ignore
            }
            throw new IllegalArgumentException("The specified path for the icon (" + path + ") is not allowed. Only " + allowedIconPaths.keySet() + " are allowed.");
        }

        try {
            final InputStream iconStream = readPath(binaryExtension, SYNDESIS_ROOT + path);
            if (iconStream == null) {
                return Optional.empty();
            }

            // iconStream is a stream to the entry bytes within compressed binaryExtension, closing it
            // would close the binaryExtension stream, and that's up to the caller
            return Optional.of(iconStream);
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable("Cannot read from binary extension file", e);
        }
    }

    @Override
    public Set<String> getAllowedIconFileNames() {
        return Collections.unmodifiableSet(allowedIconPaths.keySet());
    }

    @Override
    public String getIconMediaType(String path) {
        if (!allowedIconPaths.containsKey(path)) {
            throw new IllegalArgumentException("The path (" + path + ") is not a valid icon location");
        }
        return allowedIconPaths.get(path);
    }

    private static Extension doGetExtension(InputStream binaryExtension) throws IOException {
        // manifestStream is a stream to the entry bytes within compressed binaryExtension, closing it
        // would close the binaryExtension stream, and that's up to the caller
        InputStream manifestStream = readPath(binaryExtension, MANIFEST_LOCATION);
        if (manifestStream == null) {
            throw new IllegalArgumentException("Cannot find manifest file (" + MANIFEST_LOCATION + ") inside JAR");
        }

        JsonNode tree = JsonUtils.reader().readTree(manifestStream);
        Extension extension = ExtensionConverter.getDefault().toInternalExtension(tree);
        if (extension == null) {
            throw new IllegalArgumentException("Cannot extract Extension from manifest file (" + MANIFEST_LOCATION + ") inside JAR");
        }
        return extension;
    }

    private static InputStream readPath(InputStream binaryExtension, String path) throws IOException {
        JarInputStream jar = new JarInputStream(binaryExtension);
        for (JarEntry entry = jar.getNextJarEntry(); entry != null; entry = jar.getNextJarEntry()) {
            if (path.equals(entry.getName())) {
                return jar;
            }
        }
        return null;
    }

}
