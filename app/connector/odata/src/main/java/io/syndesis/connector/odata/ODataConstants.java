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
package io.syndesis.connector.odata;

import java.util.Locale;
import org.apache.camel.component.olingo4.internal.Olingo4Constants;
import io.syndesis.common.util.StringConstants;

@SuppressWarnings("PMD.ConstantsInInterface")
public interface ODataConstants extends Olingo4Constants, StringConstants {

    // prefix for parameters when passed as exchange header properties
    String OLINGO4_PROPERTY_PREFIX = PROPERTY_PREFIX;

    String SERVICE_URI = "serviceUri";

    String METHOD_NAME = "methodName";

    String RESOURCE_PATH = "resourcePath";

    String DATA = "data";

    String KEY_PREDICATE = "keyPredicate";

    String QUERY_PARAMS = "queryParams";

    String CONNECTOR_DIRECTION = "connectorDirection";

    String BASIC_PASSWORD = "basicPassword";

    String BASIC_USER_NAME = "basicUserName";

    String SERVER_CERTIFICATE = "serverCertificate";

    String CONSUMER = "consumer";

    String INITIAL_DELAY = "initialDelay";

    String DELAY = "delay";

    String SPLIT_RESULT = "splitResult";

    String BACKOFF_IDLE_THRESHOLD = "backoffIdleThreshold";

    String BACKOFF_MULTIPLIER = "backoffMultiplier";

    String FILTER_ALREADY_SEEN = "filterAlreadySeen";

    String METADATA_ENDPOINT = "/$metadata";

    String ENDPOINT_HTTP_HEADERS = "endpointHttpHeaders";

    String RESULT_COUNT = "ResultCount";

    String FROM = "from";

    String TO = "to";

    enum Methods {
        READ,
        DELETE,
        CREATE,
        PATCH;

        public static Methods getValueOf(String name) {
            for (Methods method : Methods.values()) {
                if (method.name().equalsIgnoreCase(name)) {
                    return method;
                }
            }

            return Methods.READ;
        }

        public static Methods methodForAction(String connectorId) {
            if (connectorId == null) {
                return null;
            }

            for (Methods method : Methods.values()) {
                String identifier = method.actionIdentifierRoot();
                if (connectorId.contains(identifier)) {
                    return method;
                }
            }

            return null;
        }

        public String actionIdentifierRoot() {
            return "odata" + HYPHEN + id() + HYPHEN + "connector";
        }

        public String id() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }
}
