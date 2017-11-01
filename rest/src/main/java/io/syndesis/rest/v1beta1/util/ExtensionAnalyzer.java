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
package io.syndesis.rest.v1beta1.util;

import io.syndesis.core.SyndesisServerException;
import io.syndesis.model.extension.Extension;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * Tools to analyze binary extensions.
 *
 * TODO: determine the right place to put this component
 */
@Component
public class ExtensionAnalyzer {

    /**
     * Analyze a binary extension to obtain the embedded {@link Extension} object.
     *
     * TODO: implement it, this is a dummy stub
     *
     * @param binaryExtension the binary stream of the extension
     * @return the embedded {@code Extension} object
     */
    @Nonnull
    public Extension analyze(InputStream binaryExtension) {
        try {
            if (binaryExtension.read() < 0) {
                // TODO: remove it from the actual code. This simulates a read from the stream
                throw new IllegalArgumentException("Empty stream");
            }
        } catch (IOException ex) {
            throw SyndesisServerException.launderThrowable("Cannot read from binary extension file", ex);
        }

        return new Extension.Builder()
            .name("Dummy")
            .description("Dummy description")
            .build();
    }

}
