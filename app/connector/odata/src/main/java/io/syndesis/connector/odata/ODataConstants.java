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

import io.syndesis.common.util.StringConstants;

@SuppressWarnings("PMD.ConstantsInInterface")
public interface ODataConstants extends StringConstants {

    String SERVICE_URI = "serviceUri";

    String API_NAME = "apiName";

    String METHOD_NAME = "methodName";

    String KEY_PREDICATE = "keyPredicate";

    String QUERY_PARAMS = "queryParams";

    String BASIC_PASSWORD = "basicPassword";

    String BASIC_USER_NAME = "basicUserName";

    String SKIP_CERT_CHECK = "skipCertificateCheck";

    String CLIENT_CERTIFICATE = "clientCertificate";

    String CONSUMER = "consumer";

    String INITIAL_DELAY = "initialDelay";

    String DELAY = "delay";

    String BACKOFF_IDLE_THRESHOLD = "backoffIdleThreshold";

    String BACKOFF_MULTIPLIER = "backoffMultiplier";

    String FILTER_ALREADY_SEEN = "filterAlreadySeen";

    String METADATA_ENDPOINT = "/$metadata";

    String ENDPOINT_HTTP_HEADERS = "endpointHttpHeaders";
}
