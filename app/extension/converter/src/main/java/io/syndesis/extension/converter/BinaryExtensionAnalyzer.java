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

import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

import io.syndesis.common.model.extension.Extension;

/**
 * Tools to analyze binary extensions.
 */
public interface BinaryExtensionAnalyzer {

    static BinaryExtensionAnalyzer getDefault() {
        return new DefaultBinaryExtensionAnalyzer();
    }

    /**
     * Analyze a binary extension to obtain the embedded {@link Extension} object.
     *
     * @param binaryExtension the binary stream of the extension
     * @return the embedded {@code Extension} object
     */
    Extension getExtension(InputStream binaryExtension);

    /**
     * Get the icon file from the binary extension.
     *
     * @param binaryExtension
     * @param path the icon path, it must be an allowed icon path
     * @return the icon of the extension or an empty option
     */
    Optional<InputStream> getIcon(InputStream binaryExtension, String path);

    /**
     * Returns a list of allowed paths for custom icons
     * @return the allowed paths
     */
    Set<String> getAllowedIconFileNames();

    /**
     * Returns the media type for the icon.
     * @return the media type
     */
    String getIconMediaType(String path);

}
