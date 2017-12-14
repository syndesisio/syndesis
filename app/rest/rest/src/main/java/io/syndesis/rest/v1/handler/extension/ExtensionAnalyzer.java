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
package io.syndesis.rest.v1.handler.extension;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.core.Json;
import io.syndesis.core.SyndesisServerException;
import io.syndesis.model.extension.Extension;
import org.springframework.stereotype.Component;

/**
 * Tools to analyze binary extensions.
 */
@Component
public class ExtensionAnalyzer {

    private static final String MANIFEST_LOCATION = "META-INF/syndesis/syndesis-extension-definition.json";
    private static final ObjectMapper MAPPER = Json.mapper();

    /**
     * Analyze a binary extension to obtain the embedded {@link Extension} object.
     *
     * @param binaryExtension the binary stream of the extension
     * @return the embedded {@code Extension} object
     */
    @Nonnull
    public Extension analyze(InputStream binaryExtension) {
        try {
            return getExtension(binaryExtension);
        } catch (IOException ex) {
            throw SyndesisServerException.launderThrowable("Cannot read from binary extension file", ex);
        }
    }

    private Extension getExtension(InputStream binaryExtension) throws IOException {
        InputStream entry = readManifest(binaryExtension);
        if (entry == null) {
            throw new IllegalArgumentException("Cannot find manifest file (" + MANIFEST_LOCATION + ") inside JAR");
        }

        Extension extension = MAPPER.readValue(entry, Extension.class);
        if (extension == null) {
            throw new IllegalArgumentException("Cannot extract Extension from manifest file (" + MANIFEST_LOCATION + ") inside JAR");
        }
        return extension;
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    private InputStream readManifest(InputStream binaryExtension) throws IOException {
        JarInputStream jar = new JarInputStream(binaryExtension);
        try {
            JarEntry entry;
            do {
                entry = jar.getNextJarEntry();
                if (entry != null && MANIFEST_LOCATION.equals(entry.getName())) {
                    return jar;
                }
            } while (entry != null);

            jar.close();
            return null;
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            try {
                jar.close();
            } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception ex2) {
                // ignore
            }
            throw SyndesisServerException.launderThrowable(e);
        }
    }

}
