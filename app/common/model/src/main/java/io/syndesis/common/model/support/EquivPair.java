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
package io.syndesis.common.model.support;

import java.util.Objects;

final class EquivPair {
    final Object a;

    final Object b;

    private final String name;

    public static EquivPair create(Object a, Object b, String name) {
        return new EquivPair(a, b, name);
    }

    private EquivPair(Object a, Object b, String name) {
        this.a = a == null ? Equivalencer.NULL : a;
        this.b = b == null ? Equivalencer.NULL : b;
        this.name = name;
    }

    public boolean isEqual() {
        return Objects.equals(a, b);
    }

    public String name() {
        return this.name;
    }
}
