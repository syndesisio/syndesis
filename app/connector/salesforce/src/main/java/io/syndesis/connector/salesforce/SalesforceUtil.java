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

import java.util.Map;
import org.apache.camel.component.salesforce.SalesforceEndpointConfig;
import io.syndesis.connector.support.util.ConnectorOptions;

public final class SalesforceUtil {
    private static final String TOPIC_PREFIX = "syndesis_";
    private static final int TOPIC_NAME_MAX_LENGTH = 25;

    private SalesforceUtil() {
    }

    public static String topicNameFor(final Map<String, String> options) {
        final String sObjectName = options.get(SalesforceEndpointConfig.SOBJECT_NAME);
        final String topicSuffix;
        if (ConnectorOptions.extractOptionAndMap(options, "notifyForOperationCreate", Boolean::valueOf, false)) {
            topicSuffix = "_c";
        } else if (ConnectorOptions.extractOptionAndMap(options, "notifyForOperationUpdate", Boolean::valueOf, false)) {
            topicSuffix = "_up";
        } else if (ConnectorOptions.extractOptionAndMap(options, "notifyForOperationDelete", Boolean::valueOf, false)) {
            topicSuffix = "_d";
        } else if (ConnectorOptions.extractOptionAndMap(options, "notifyForOperationUndelete", Boolean::valueOf, false)) {
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
