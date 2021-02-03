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
package io.syndesis.connector.slack;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.singletonMap;

public class SlackMetaDataRetrieval extends ComponentMetadataRetrieval {

    private static final Logger LOG = LoggerFactory.getLogger(SlackMetaDataRetrieval.class);

    @Override
    protected SyndesisMetadata adapt(final CamelContext context, final String componentId, final String actionId, final Map<String, Object> properties,
        final MetaDataExtension.MetaData metadata) {
        try {
            @SuppressWarnings("unchecked")
            final Set<String> channelNames = (Set<String>) metadata.getPayload();

            final List<PropertyPair> channels = channelNames.stream().map(c -> new PropertyPair(c, c)).collect(Collectors.toList());

            return SyndesisMetadata.of(singletonMap("channel", channels));
        } catch (final Exception e) {
            LOG.error("Unable to fetch Slack metadata", e);
            return SyndesisMetadata.EMPTY;
        }
    }

    @Override
    protected MetaDataExtension resolveMetaDataExtension(final CamelContext context, final Class<? extends MetaDataExtension> metaDataExtensionClass,
        final String componentId,
        final String actionId) {
        return new SlackMetaDataExtension(context);
    }

}
