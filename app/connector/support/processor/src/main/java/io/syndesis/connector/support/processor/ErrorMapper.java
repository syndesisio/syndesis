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
package io.syndesis.connector.support.processor;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import io.syndesis.common.util.json.JsonUtils;

import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import io.syndesis.common.util.SyndesisConnectorException;

public final class ErrorMapper {

    private static final TypeReference<Map<String, String>> STRING_MAP_TYPE = new TypeReference<Map<String, String>>() {
        // type token used when deserializing generics
    };

    private static final Logger LOG = LoggerFactory.getLogger(ErrorMapper.class);

    private ErrorMapper() {
        // utility class
    }

    public static Map<String, String> jsonToMap(String property) {

        try {
            if (ObjectHelper.isEmpty(property)) {
                return Collections.emptyMap();
            }
            return JsonUtils.reader().forType(STRING_MAP_TYPE).readValue(property);
        } catch (IOException e) {
            LOG.warn(String.format("Failed to read error code mapping property %s: %s", property, e.getMessage()), e);
            return Collections.emptyMap();
        }
    }

    public static ErrorStatusInfo mapError(final Exception exception, final Map<String, String> errorResponseCodeMappings,
            final Integer responseCode) {
        Integer errorResponseCode = responseCode;
        final ErrorStatusInfo info;
        if (matchByException(exception, errorResponseCodeMappings)) {
            info = new ErrorStatusInfo(
                    Integer.valueOf(errorResponseCodeMappings.get(exception.getClass().getName())),
                    categoryFromClassName(exception.getClass().getSimpleName()),
                    getMessage(exception));
        } else {
            SyndesisConnectorException sce = SyndesisConnectorException.from(exception);
            if (matchByCategory(sce, errorResponseCodeMappings)) {
                info = new ErrorStatusInfo(
                        Integer.valueOf(errorResponseCodeMappings.get(sce.getCategory())),
                        sce.getCategory(),
                        sce.getMessage());
            } else {
                info = new ErrorStatusInfo(errorResponseCode, sce.getCategory(), sce.getMessage());
            }
        }
        return info;
    }

    private static boolean matchByException(final Exception exception, final Map<String, String> errorResponseCodeMappings) {
        return errorResponseCodeMappings.containsKey(exception.getClass().getName());
    }

    private static boolean matchByCategory(final SyndesisConnectorException sce, final Map<String, String> errorResponseCodeMappings) {
        return errorResponseCodeMappings.containsKey(sce.getCategory());
    }

    private static String getMessage(final Exception exception) {
        if (exception.getCause()!=null && exception.getCause().getMessage()!=null) {
            return exception.getCause().getMessage();
        } else {
            return exception.getMessage();
        }
    }

    private static String categoryFromClassName(final String className) {
        return camelCaseToUnderscore(className).replace("Exception","Error").toUpperCase(Locale.US);
    }

    public static String camelCaseToUnderscore(final String text) {
        return text.replaceAll("([^_A-Z])([A-Z])", "$1_$2");
    }
}