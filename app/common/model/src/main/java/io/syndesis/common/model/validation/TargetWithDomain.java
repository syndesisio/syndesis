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
package io.syndesis.common.model.validation;

import java.util.Collection;
import java.util.Optional;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithId;

/**
 * An object paired with a search domain allowing then to be passed
 * around as single objects.
 */
public abstract class TargetWithDomain<T extends WithId<T>> implements WithId<T> {

    private final T target;

    private final Collection<T> domain;

    protected TargetWithDomain(T target, Collection<T> domain) {
        this.target = target;
        this.domain = domain;
    }

    public T getTarget() {
        return target;
    }

    public Collection<T> getDomain() {
        return domain;
    }

    @Override
    public Optional<String> getId() {
        return target.getId();
    }

    @Override
    public Kind getKind() {
        return target.getKind();
    }

    @Override
    public T withId(String id) {
        return target.withId(id);
    }
}
