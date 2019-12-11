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

    public boolean equalTo(String type) {
        return name.equals(type) || Arrays.asList(aliases.split(",")).contains(type);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
