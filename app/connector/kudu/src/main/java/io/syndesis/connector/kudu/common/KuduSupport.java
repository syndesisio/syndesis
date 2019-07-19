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

package io.syndesis.connector.kudu.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.connector.support.util.ConnectorOptions;
import org.apache.kudu.client.KuduClient;
import org.apache.camel.util.ObjectHelper;
import java.util.HashMap;
import java.util.Map;

public final class KuduSupport {

    private KuduSupport() {
        // utility class
    }

    public static KuduClient createConnection(Map<String, Object> options) {

        String host = ConnectorOptions.extractOption(options, "host");
        String port = ConnectorOptions.extractOption(options, "port");
        if(ObjectHelper.isNotEmpty(host) && ObjectHelper.isNotEmpty(port)) {

            Long socketTimeout = ConnectorOptions.extractOptionAndMap(options, "socketTimeout", Long::valueOf, 3000L);
            Long operationTimeout = ConnectorOptions.extractOptionAndMap(options, "operationTimeout", Long::valueOf, 10000L);
            Long adminOperationTimeout = ConnectorOptions.extractOptionAndMap(options, "operationTimeout", Long::valueOf, 10000L);

            return new KuduClient.KuduClientBuilder(host + ":" + port)
                    .defaultSocketReadTimeoutMs(socketTimeout)
                    .defaultOperationTimeoutMs(operationTimeout)
                    .defaultAdminOperationTimeoutMs(adminOperationTimeout)
                    .build();
        } else {
            throw new SyndesisServerException("Port and Host parameter are mandatory");
        }
    }

    /**
     * Convenience method to convert a Camel Map output to a JSON Bean String.
     *
     * @param map
     * @return JSON bean String
     */
    public static String toJSONBean(final Map<String, Object> map) {
        ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> data = new HashMap<>(map.size());

        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (key.charAt(0) != '#') { // don't include Camel stats
                data.put(key, value);
            }
        }

        try {
            return mapper.writeValueAsString(data);
        } catch (final JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize to JSON", e);
        }
    }

}
