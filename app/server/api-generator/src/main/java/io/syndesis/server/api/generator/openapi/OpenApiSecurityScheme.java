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

package io.syndesis.server.api.generator.openapi;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Supported security schemes.
 */
public enum OpenApiSecurityScheme {
    OAUTH2("oauth2"),
    BASIC("basic", "http"),
    API_KEY("apiKey");

    private final String name;
    private final String aliases;

    OpenApiSecurityScheme(String name, String... aliases) {
        this.name = name;
        this.aliases = String.join(",", aliases);
    }

    public static Set<String> namesAndAliases() {
        return Stream.concat(
            Arrays.stream(values()).map(OpenApiSecurityScheme::getName),
            Arrays.stream(values()).flatMap(scheme -> scheme.getAliases().stream()))
            .collect(Collectors.toSet());
    }

    public boolean equalTo(String type) {
        return name.equals(type) || getAliases().contains(type);
    }

    private List<String> getAliases() {
        return Arrays.asList(aliases.split(","));
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
