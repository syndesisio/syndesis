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

package io.syndesis.server.inspector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.syndesis.common.util.Json;
import io.syndesis.common.util.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
@Component
public class JsonInstanceInspector implements Inspector {

    private static final Logger LOG = LoggerFactory.getLogger(JsonInstanceInspector.class);

    private static final String ARRAY_CONTEXT = "[]";
    static final List<String> COLLECTION_PATHS = Collections.singletonList("size()");

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getPaths(String kind, String type, String specification, Optional<byte[]> exemplar) {
        if (specification == null) {
            return Collections.emptyList();
        }

        String context = null;
        final List<String> paths = new ArrayList<>();
        try {
            Map<String, Object> json;
            if (JsonUtils.isJsonArray(specification)) {
                List<Object> items = Json.reader().forType(List.class).readValue(specification);
                // add collection specific paths
                paths.addAll(COLLECTION_PATHS);
                context = ARRAY_CONTEXT;

                if (items.isEmpty()) {
                    return paths;
                }

                json = (Map<String, Object>) items.get(0);
            } else {
                json = Json.reader().forType(Map.class).readValue(specification);
            }

            fetchPaths(context, paths, json);
        } catch (final IOException e) {
            LOG.warn("Unable to parse the given JSON instance, increase log level to DEBUG to see the instance being parsed");
            LOG.debug(specification);
            LOG.trace("Unable to parse the given JSON instance", e);
        }

        return paths;
    }

    @SuppressWarnings("unchecked")
    private static void fetchPaths(final String context, final List<String> paths, final Object json) {
        if (json instanceof Map) {
            Map<String, Object> properties = (Map<String, Object>) json;

            if (properties.isEmpty() && context != null) {
                paths.add(context);
            }

            for (final Map.Entry<String, Object> entry : properties.entrySet()) {
                final String key = entry.getKey();
                if (context == null) {
                    fetchPaths(key, paths, entry.getValue());
                } else {
                    fetchPaths(context + "." + key, paths, entry.getValue());
                }
            }
        } else if (json instanceof List) {
            List<Object> items = (List) json;
            if (!items.isEmpty()) {
                if (context == null) {
                    fetchPaths(ARRAY_CONTEXT, paths, items.get(0));
                } else {
                    COLLECTION_PATHS.stream().map(path -> context + "." + path).forEach(paths::add);
                    fetchPaths(context + ARRAY_CONTEXT, paths, ((List) json).get(0));
                }
            } else {
                COLLECTION_PATHS.stream().map(path -> context + "." + path).forEach(paths::add);
                paths.add(context + ARRAY_CONTEXT);
            }
        } else {
            paths.add(context);
        }
    }

    @Override
    public boolean supports(String kind, String type, String specification, Optional<byte[]> optional) {
        return "json-instance".equals(kind) && !StringUtils.isEmpty(specification);

    }
}
