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
package io.syndesis.common.model.validation.integration;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.validation.TargetWithDomain;

public class IntegrationWithDomain extends TargetWithDomain<Integration> implements Integration {

    private static final long serialVersionUID = 1L;

    public IntegrationWithDomain(Integration target, Collection<Integration> domain) {
        super(target, domain);
    }

    @Override
    public Optional<String> getDescription() {
        return getTarget().getDescription();
    }

    @Override
    public Map<String, ConfigurationProperty> getProperties() {
        return getTarget().getProperties();
    }

    @Override
    public SortedSet<String> getTags() {
        return getTarget().getTags();
    }

    @Override
    public String getName() {
        return getTarget().getName();
    }

    @Override
    public Optional<String> getExposure() {
        return getTarget().getExposure();
    }
}
