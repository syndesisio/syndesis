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
package io.syndesis.server.endpoint.v1.handler.exception;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ErrorMap {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorMap.class);

    private ErrorMap() {
        // utility class
    }

    /**
     * Performs best effort to parse the rawMsg. If all parsers fail it returns the raw message.
     *
     * @param rawMsg
     * @return the underlying error message.
     */
    public static String from(String rawMsg) {
        if (rawMsg.matches("^\\s*\\<.*")) {
            return parseWith(rawMsg, new XmlMapper());
        }
        if (rawMsg.matches("^\\s*\\{.*")) {
            return parseWith(rawMsg, new ObjectMapper());
        }
        return rawMsg;
    }

    /**
     * Tries to parse the rawMsg assuming it is JSON formatted.
     * defaults to the rawMsg if parsing fails.
     * @param rawMsg
     * @return ErrorMap
     */
    static String parseWith(String rawMsg, ObjectMapper mapper) {
        try {
            final JsonNode jsonNode = mapper.readTree(rawMsg);
            final Optional<String> error = determineError(jsonNode);
            return error.orElse(rawMsg);
        } catch (IOException e) {
            LOG.debug("Swallowing {}", e.getMessage(), e);
        }
        return rawMsg;
    }

    static Optional<String> determineError(final JsonNode node) {
        return Optional.of(tryLookingUp(node, "errors", "message")
            .orElseGet(() -> tryLookingUp(node, "error", "message")
                .orElseGet(() -> tryLookingUp(node, "error")
                    .orElseGet(() -> tryLookingUp(node, "message")
                        .orElse(null)))));
    }

    static Optional<String> tryLookingUp(final JsonNode node, final String... pathElements) {
        JsonNode current = node;
        for (String pathElement : pathElements) {
            current = current.get(pathElement);

            if (current != null && current.isArray() && current.iterator().hasNext()) {
                current = current.iterator().next();
            }

            if (current == null) {
                return Optional.empty();
            }
        }

        if (current.isObject()) {
            return Optional.of(current.toString());
        }

        return Optional.of(current.asText());
    }

}
