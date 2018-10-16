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
package io.syndesis.server.api.generator.swagger.util;

import java.util.Objects;

public final class OperationDescription {

    public final String description;

    public final String name;

    private final int hashCode;

    public OperationDescription(final String name, final String description) {
        this.name = Objects.requireNonNull(name, "operation name");
        this.description = Objects.requireNonNull(description, "operation description");
        this.hashCode = 31 * name.hashCode() + 7 * description.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof OperationDescription)) {
            return false;
        }

        final OperationDescription other = (OperationDescription) obj;

        return Objects.equals(name, other.name) && Objects.equals(description, other.description);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "OperationDescription: " + name + ", description: " + description;
    }
}
