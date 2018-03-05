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
package io.syndesis.connector.salesforce;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.util.Json;
import org.apache.camel.CamelContext;
import org.apache.camel.component.salesforce.SalesforceEndpointConfig;
import org.apache.camel.util.ResourceHelper;

public final class SalesforceUtil {
    private final static String TOPIC_PREFIX = "syndesis_";
    private final static int TOPIC_NAME_MAX_LENGTH = 25;

    private SalesforceUtil() {
    }

    public static Connector mandatoryLookupConnector(CamelContext context, String id) {
        Connector connector;

        try (InputStream is = ResourceHelper.resolveMandatoryResourceAsInputStream(context, "META-INF/syndesis/connector/" + id + ".json")) {
            connector = Json.reader().forType(Connector.class).readValue(is);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        if (connector == null) {
            throw new IllegalStateException("Unable to lod connector for: " + id);
        }

        return connector;
    }

    public static final ConnectorAction mandatoryLookupAction(CamelContext context, Connector connector, String actionId) {
        for (ConnectorAction action : connector.getActions()) {
            if (action.getId().isPresent() && action.getId().get().equals(actionId)) {
                return action;
            }
        }

        throw new IllegalArgumentException("Unable to find action: " + actionId);
    }

    public static String topicNameFor(final Map<String, String> options) {
        final String sObjectName = options.get(SalesforceEndpointConfig.SOBJECT_NAME);
        final String topicSuffix;
        if (Boolean.valueOf(options.get("notifyForOperationCreate"))) {
            topicSuffix = "_c";
        } else if (Boolean.valueOf(options.get("notifyForOperationUpdate"))) {
            topicSuffix = "_up";
        } else if (Boolean.valueOf(options.get("notifyForOperationDelete"))) {
            topicSuffix = "_d";
        } else if (Boolean.valueOf(options.get("notifyForOperationUndelete"))) {
            topicSuffix = "_un";
        } else {
            topicSuffix = "_a";
        }

        String topicName = TOPIC_PREFIX + sObjectName + topicSuffix;
        if (topicName.length() > TOPIC_NAME_MAX_LENGTH) {
            int diffLength = topicName.length() - TOPIC_NAME_MAX_LENGTH;
            topicName = TOPIC_PREFIX + sObjectName.substring(0, Math.min(sObjectName.length(), sObjectName.length() - diffLength)) + topicSuffix;
        }
        return topicName;
    }
}
