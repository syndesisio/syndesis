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
package io.syndesis.common.util;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

public final class Optionals {
    private Optionals() {
    }

    /**
     * Returns the first optional with value or Optional::empty.
     */
    @SafeVarargs
    public static <T> Optional<T> first(Optional<T>... optionals) {
        for (int i = 0; i < optionals.length; i++) {
            if (optionals[i].isPresent()) {
                return optionals[i];
            }
        }
        return Optional.empty();
    }

    /**
     * Returns true if none of the options is present.
     */
    @SafeVarargs
    public static <T> boolean none(Optional<T>... optionals) {
        for (int i = 0; i < optionals.length; i++) {
            if (optionals[i].isPresent()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts a Optional into a Stream in order to use it in a Stream#flatMap.
     */
    public static <T> Stream<T> asStream(Optional<T> optional) {
        return optional.map(Collections::singleton).orElse(Collections.emptySet()).stream();
    }
}
