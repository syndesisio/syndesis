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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public final class StaticResourceClassInspector extends DataMapperBaseInspector<Void> {

    private static final String PREFIX = "classpath:/static/mapper/v1/java-inspections/**/";

    private static final String SUFFIX = ".json";

    private final PathMatchingResourcePatternResolver resolver;

    @Autowired
    public StaticResourceClassInspector(final ResourceLoader loader) {
        super(false);
        resolver = new PathMatchingResourcePatternResolver(loader);
    }

    @Override
    protected String fetchJsonFor(final String fullyQualifiedName, final Context<Void> context) throws IOException {
        final String path = PREFIX + fullyQualifiedName + SUFFIX;

        final Resource[] resources = resolver.getResources(path);

        if (resources.length == 0) {
            throw new FileNotFoundException(path);
        }

        // let's just take the first one, we don't have the connectorId here so
        // we can't narrow the choice
        final Resource resource = resources[0];

        try (InputStream in = resource.getInputStream()) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        }
    }

    @Override
    protected boolean internalSupports(final String kind, final String type, final String specification, final Optional<byte[]> exemplar) {
        return StringUtils.isEmpty(specification);
    }

}
