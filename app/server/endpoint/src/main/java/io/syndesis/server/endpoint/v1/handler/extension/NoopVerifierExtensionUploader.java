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
package io.syndesis.server.endpoint.v1.handler.extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.syndesis.common.model.extension.Extension;

@Component
@ConditionalOnProperty(value = "openshift.enabled", havingValue = "false", matchIfMissing=false)
public class NoopVerifierExtensionUploader implements VerifierExtensionUploader {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoopVerifierExtensionUploader.class);

    @Override
    public void uploadToVerifier(Extension extension) {
        LOGGER.info("Upload %4 extension to verifier", ExtensionActivator.getConnectorIdForExtension(extension));
    }

    @Override
    public void deleteFromVerifier(Extension extension) {
        LOGGER.info("Delete %4 extension from verifier", ExtensionActivator.getConnectorIdForExtension(extension));
    }
}
