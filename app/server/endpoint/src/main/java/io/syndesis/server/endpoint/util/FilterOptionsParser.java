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
package io.syndesis.server.endpoint.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilterOptionsParser {

    private static final Pattern VALID_QUERY_PATTERN = Pattern.compile("(?<property>\\w+)(?:(?<operation>(?:\\s*=\\s*))(?<value>\\p{Print}+))?");

    private FilterOptionsParser() {
    }

    public static List<Filter> fromString(String query) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyList();
        }

        return Stream.of(query.split(","))
            .filter(StringUtils::isNotBlank)
            .flatMap(q -> {
                Matcher m = VALID_QUERY_PATTERN.matcher(q);
                if (!m.matches()) {
                    return Stream.empty();
                }
                return Stream.of(
                    new Filter(m.group("property"), m.group("operation"), m.group("value"))
                );
            }).collect(Collectors.toList());
    }

    public static class Filter {
        private final String property;

        private final Optional<String> operation;

        private final Optional<String> value;

        Filter(String property, String operation, String value) {
            this.property = property;
            this.operation = Optional.ofNullable(operation);
            this.value = Optional.ofNullable(value);
        }

        public String getProperty() {
            return property;
        }

        public Optional<String> getOperation() {
            return operation;
        }

        public Optional<String> getValue() {
            return value;
        }
    }

}
