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
package io.syndesis.connector.mongo.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FilterUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterUtil.class);
    private static final Pattern PARAM_PATTERN = Pattern.compile(":#([^ \\p{Punct}]+)");

    private FilterUtil() {

    }

    /**
     * Extract any <strong>:#variable</strong> parameter format
     * @param filter the filter expression, ie {"test": ":#var1"}
     * @return a list of parameters expected by the filter
     */
    public static List<String> extractParameters(String filter) {
        List<String> result = new ArrayList<>();
        Matcher matcher = PARAM_PATTERN.matcher(filter);
        while (matcher.find()) {
            String param = matcher.group();
            result.add(param.substring(2));
        }
        return result;
    }

    /**
     * Check whether the filter contains any parameter or is a static one
     */
    public static boolean hasAnyParameter(String filter) {
        return filter != null && filter.contains(":#");
    }

    /**
     * Merge the parameters found in the filter with the values found in the JSON message
     * @param filter an expression containing :#variable parameters
     * @param jsonMessage a message formatted as JSON
     */
    public static String merge(String filter, String jsonMessage) {
        if (filter == null){
            LOGGER.debug("Skipping merge as filter was null");
            return null;
        }
        LOGGER.debug("Filter configured by user {}", filter);
        LOGGER.debug("Input body {}", jsonMessage);
        String mergedFilter = filter;
        // Substitute the variables found
        List<String> parameters = FilterUtil.extractParameters(mergedFilter);
        for (String param : parameters) {
            Object inputValue = Document.parse(jsonMessage).get(param);
            if (inputValue == null) {
                throw new IllegalArgumentException(
                    String.format(
                        "Missing expected parameter \"%s\" in the input source \"%s\"",
                        param,
                        jsonMessage
                    )
                );
            }
            mergedFilter = mergedFilter.replaceAll(String.format(":#%s", param), inputValue.toString());
        }
        int anyVariablePending = mergedFilter.indexOf(":#");
        if (anyVariablePending >= 0) {
            // There is still some parameter unmapped!
            throw new IllegalArgumentException(
                String.format(
                    "Your input source must provide all variables expected by the filter \"%s\". Error at position %d.",
                    mergedFilter,
                    anyVariablePending
                )
            );
        }
        LOGGER.debug("Merged body with filter as {}", mergedFilter);
        return mergedFilter;
    }
}
