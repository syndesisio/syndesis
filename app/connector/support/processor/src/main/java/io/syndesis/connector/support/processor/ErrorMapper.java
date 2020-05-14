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
import java.util.Set;

import io.syndesis.common.util.json.JsonUtils;

import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import io.syndesis.common.util.CamelCase;
import io.syndesis.common.util.ErrorCategory;
import io.syndesis.common.util.SyndesisConnectorException;

public final class ErrorMapper {

    private static final TypeReference<Map<String, Integer>> STRING_MAP_TYPE = new TypeReference<Map<String, Integer>>() {
        // type token used when deserializing generics
    };

    private static final Logger LOG = LoggerFactory.getLogger(ErrorMapper.class);

    private ErrorMapper() {
        // utility class
    }

    public static Map<String, Integer> jsonToMap(String property) {

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

    public static ErrorStatusInfo mapError(final Exception exception, final Map<String, Integer> httpResponseCodeMappings,
            final Integer defaultResponseCode) {

        SyndesisConnectorException sce;
        if (isOrCausedBySyndesisConnectorException(exception)) {
            sce = extract(exception);
        } else {
            sce = fromRuntimeException(exception, httpResponseCodeMappings.keySet());
        }
        if (sce == null) { //catch all server error
            sce = SyndesisConnectorException.from(exception);
        }
        Integer responseCode = httpResponseCodeMappings.get(sce.getCategory()) != null ?
                httpResponseCodeMappings.get(sce.getCategory()): defaultResponseCode;
        return new ErrorStatusInfo(responseCode, sce.getCategory(), sce.getMessage(), deriveErrorName(sce));
    }

    private static String deriveErrorName(final Throwable t) {
        String className = (t.getCause() != null ? t.getCause().getClass() : t.getClass()).getSimpleName();
        return CamelCase.toUnderscore(className).replace("Exception","Error").toLowerCase(Locale.US);
    }

    private static SyndesisConnectorException fromRuntimeException(Throwable exception, Set<String> integrationCategories) {
        String category = ErrorCategory.getCategory(exception, integrationCategories);
        return SyndesisConnectorException.wrap(category, exception);
    }

    private static boolean isOrCausedBySyndesisConnectorException(Throwable exception) {
        if (exception instanceof SyndesisConnectorException) {
            return true;
        } else {
            return (exception.getCause() != null && exception.getCause() instanceof SyndesisConnectorException);
        }
    }

    private static SyndesisConnectorException extract(Throwable exception) {
        if (exception instanceof SyndesisConnectorException) {
            return (SyndesisConnectorException) exception;
        } else  {
            return (SyndesisConnectorException) exception.getCause();
        }
    }

}