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

package io.syndesis.test.integration.project;

import java.io.IOException;
import java.nio.file.Path;

import io.syndesis.test.integration.source.IntegrationSource;

/**
 * @author Christoph Deppisch
 */
@FunctionalInterface
public interface ProjectBuilder {

    /**
     * Builds the integration project sources and provides the path to that project dir.
     * @param integrationSource
     * @return
     */
    Path build(IntegrationSource integrationSource);

    /**
     * @param source
     * @param integrationFile
     * @throws IOException
     */
    default void customizeIntegrationFile(IntegrationSource source, Path integrationFile) throws IOException {
        // subclasses can add integration file customizations
    };

}
