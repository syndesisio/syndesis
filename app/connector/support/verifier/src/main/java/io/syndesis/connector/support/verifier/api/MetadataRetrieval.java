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
package io.syndesis.connector.support.verifier.api;

import java.util.Map;

import io.syndesis.common.util.SyndesisServerException;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;

/**
 * Converting metadata from Camel components to applicable properties or
 * generating ObjectSchema from Metadata is specific to each connector. This
 * adapter bridges Camel {@link MetaDataExtension} Component specific
 * implementations to common Syndesis data model.
 */
public interface MetadataRetrieval {

    /**
     * Converts Camel {@link MetaData} to {@link SyndesisMetadata}. Method will
     * receive all properties that client specified and the retrieved
     * {@link MetaData} from the appropriate Camel {@link MetaDataExtension}.
     */
    SyndesisMetadata fetch(CamelContext context, String componentId, String actionId, Map<String, Object> properties);

    default RuntimeException handle(final Exception e) {
        return new SyndesisServerException(e.getMessage() + ". Unable to fetch and process metadata", e);
    }
}
