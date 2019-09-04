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
package io.syndesis.connector.apiprovider;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import io.syndesis.common.util.Json;
import io.syndesis.common.util.SyndesisConnectorException;

public class ErrorMapper {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorMapper.class);

    public static Map<String, String> jsonToMap(String property) {

        try {
            if (ObjectHelper.isEmpty(property)) {
                return Collections.emptyMap();
            }
            return Json.reader().forType(new TypeReference<Map<String, String>>(){}).readValue(property);
        } catch (IOException e) {
            LOG.warn(String.format("Failed to read error code mapping property %s: %s", property, e.getMessage()), e);
            return Collections.emptyMap();
        }
    }

    public static ErrorStatusInfo mapError(final Exception exception, final Map<String, String> errorResponseCodeMappings,
            final Integer responseCode) {
        Integer errorResponseCode = responseCode;
        SyndesisConnectorException sce = SyndesisConnectorException.from(exception);
        for (Map.Entry<String, String> mapping : errorResponseCodeMappings.entrySet()) {
            if (mapping.getKey().matches(sce.getCategory())) {
                errorResponseCode = Integer.valueOf(mapping.getValue());
                break;
            }
        }
        return new ErrorStatusInfo(errorResponseCode, sce.getCategory(), sce.getMessage());
    }
}
