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
package io.syndesis.integration.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import io.syndesis.model.integration.IntegrationDeployment;

public interface IntegrationProjectGenerator {

    /**
     * Generate the project files in form of tar input stream
     *
     * @param deployment the deployment
     * @return an {@link InputStream} which holds a tar archive and which can be directly used for
     * an S2I build
     * @throws IOException if generating fails
     */
    InputStream generate(IntegrationDeployment deployment) throws IOException;

    Properties generateApplicationProperties(IntegrationDeployment deployment);

    byte[] generatePom(IntegrationDeployment deployment) throws IOException;
}
