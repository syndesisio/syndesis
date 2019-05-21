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
package io.syndesis.connector.knative.meta;

import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KnativeMetaDataRetrieval extends ComponentMetadataRetrieval {

    private static final String ACTION_ID_PREFIX_CHANNEL = "io.syndesis:knative-channel";
    private static final String ACTION_ID_PREFIX_ENDPOINT = "io.syndesis:knative-endpoint";

    /**
     * TODO: use local extension, remove when switching to camel 2.22.x
     */
    @Override
    protected MetaDataExtension resolveMetaDataExtension(CamelContext context, Class<? extends MetaDataExtension> metaDataExtensionClass, String componentId, String actionId) {
        return new KnativeMetaDataExtension(context);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SyndesisMetadata adapt(CamelContext context, String componentId, String actionId, Map<String, Object> properties, MetaDataExtension.MetaData metadata) {
        try {
            Set<String> channelNames = (Set<String>) metadata.getPayload();

            List<PropertyPair> channelResult = new ArrayList<>();
            channelNames.stream().forEach(
                t -> channelResult.add(new PropertyPair(t, t))
            );

            return SyndesisMetadata.of(
                Collections.singletonMap("name", channelResult)
            );
        } catch (Exception e) {
            return SyndesisMetadata.EMPTY;
        }
    }


    @Override
    protected Map<String, Object> prepareProperties(CamelContext context, String componentId, String actionId, Map<String, Object> properties) {
        Map<String, Object> props = new HashMap<>(super.prepareProperties(context, componentId, actionId, properties));
        props.computeIfAbsent("type", s -> typeFromActionId(actionId));
        return props;
    }

    protected String typeFromActionId(String actionId) {
        if (actionId.startsWith(ACTION_ID_PREFIX_CHANNEL)) {
            return KnativeMetaDataExtension.TYPE_CHANNEL;
        } else if (actionId.startsWith(ACTION_ID_PREFIX_ENDPOINT)) {
            return KnativeMetaDataExtension.TYPE_ENDPOINT;
        }
        throw new IllegalArgumentException("Unknown type for actionId " + actionId);
    }

}
