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
package io.syndesis.connector.sql.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.syndesis.common.util.SyndesisConnectorException;

public class Params {
    public final SyndesisConnectorException error;

    public final List<Map<String, String[]>> expectedResults = new ArrayList<>();

    public final Map<String, Object> parameters = new HashMap<>();

    public final String query;

    private Params(final String query, final Map<String, Object> parameters, final List<Map<String, String[]>> expectedResults,
        final SyndesisConnectorException error) {
        this(query, error);
        this.parameters.putAll(parameters);
        this.expectedResults.addAll(expectedResults);
    }

    private Params(final String query, final SyndesisConnectorException error) {
        this.query = query;
        this.error = error;
    }

    public Params withError(final String category, final String message) {
        return new Params(query, parameters, Collections.emptyList(), new SyndesisConnectorException(category, message));
    }

    public Params withParameter(final String name, final Object value) {
        return new Params(query, put(parameters, name, value), expectedResults, error);
    }

    public Params withResultColumnValues(final String name, final String... values) {
        return new Params(query, parameters, put(expectedResults, name, values), error);
    }

    public static Params query(final String query) {
        return new Params(query, null);
    }

    private static List<Map<String, String[]>> put(final List<Map<String, String[]>> existing, final String name, final String[] values) {
        final List<Map<String, String[]>> expectedResults = new ArrayList<>(existing);
        expectedResults.add(Collections.singletonMap(name, values));

        return expectedResults;
    }

    private static Map<String, Object> put(final Map<String, Object> existing, final String name, final Object value) {
        final Map<String, Object> parameters = new HashMap<>(existing);
        parameters.put(name, value);

        return parameters;
    }
}