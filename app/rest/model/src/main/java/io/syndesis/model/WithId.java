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
package io.syndesis.model;

import java.util.Optional;

public interface WithId<T extends WithId<T>> extends WithKind {

    Optional<String> getId();

    T withId(String id);

    default boolean idEquals(final String another) {
        final Optional<String> id = getId();
        if (id.isPresent()) {
            return id.get().equals(another);
        }

        return false;
    }
}
