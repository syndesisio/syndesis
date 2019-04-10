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
package io.syndesis.common.model.validation.extension;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.validation.TargetWithDomain;

public class ExtensionWithDomain extends TargetWithDomain<Extension> implements Extension {

    public ExtensionWithDomain(Extension target, Collection<Extension> domain) {
        super(target, domain);
    }

    @Override
    public List<Action> getActions() {
        return getTarget().getActions();
    }

    @Override
    public String getName() {
        return getTarget().getName();
    }

    @Override
    public SortedSet<String> getTags() {
        return getTarget().getTags();
    }

    @Override
    public Map<String, ConfigurationProperty> getProperties() {
        return getTarget().getProperties();
    }

    @Override
    public String getVersion() {
        return getTarget().getVersion();
    }

    @Override
    public String getExtensionId() {
        return getTarget().getExtensionId();
    }

    @Override
    public String getSchemaVersion() {
        return getTarget().getSchemaVersion();
    }

    @Override
    public Optional<Status> getStatus() {
        return getTarget().getStatus();
    }

    @Override
    public String getIcon() {
        return getTarget().getIcon();
    }

    @Override
    public String getDescription() {
        return getTarget().getDescription();
    }

    @Override
    public int getUses() {
        return getTarget().getUses();
    }

    @Override
    public Optional<String> getUserId() {
        return getTarget().getUserId();
    }

    @Override
    public Optional<Date> getLastUpdated() {
        return getTarget().getLastUpdated();
    }

    @Override
    public Optional<Date> getCreatedDate() {
        return getTarget().getCreatedDate();
    }

    @Override
    public Type getExtensionType() {
        return getTarget().getExtensionType();
    }

}
