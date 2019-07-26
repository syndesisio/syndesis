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

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.MetaDataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.syndesis.connector.support.util.ConnectorOptions;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class KnativeMetaDataExtension extends AbstractMetaDataExtension {
    private static final Logger LOG = LoggerFactory.getLogger(KnativeMetaDataExtension.class);

    public static final String TYPE_CHANNEL = "channel";
    public static final String TYPE_ENDPOINT = "endpoint";

    KnativeMetaDataExtension(CamelContext context) {
        super(context);
    }

    @Override
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public Optional<MetaData> meta(Map<String, Object> parameters) {
        String name = ConnectorOptions.extractOption(parameters, "name");
        if (name == null) {
            // lookup resources on Kubernetes
            String type = ConnectorOptions.extractOption(parameters, "type");
            if (type == null) {
                type = TYPE_CHANNEL;
            }
            LOG.debug("Retrieving Knative resources of type {}", type);

            Set<String> channelNames = new TreeSet<>();
            switch (type) {
                case TYPE_CHANNEL:
                    channelNames.addAll(KnativeMetaDataSupport.listChannels());
                    break;
                case TYPE_ENDPOINT:
                    channelNames.addAll(KnativeMetaDataSupport.listServices());
                    break;
                default:
                    throw new RuntimeException("Unsupported Knative type: " + type);
            }

            return Optional.of(
                MetaDataBuilder.on(getCamelContext())
                    .withAttribute(MetaData.CONTENT_TYPE, "text/plain")
                    .withAttribute(MetaData.JAVA_TYPE, String.class)
                    .withPayload(channelNames)
                    .build()
            );
        } else {
            // Just return the chosen resource
            return Optional.of(
                MetaDataBuilder.on(getCamelContext())
                    .withAttribute(MetaData.CONTENT_TYPE, "text/plain")
                    .withAttribute(MetaData.JAVA_TYPE, String.class)
                    .withPayload(Collections.singleton(name))
                    .build()
            );
        }
    }
}
