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
package io.syndesis.connector.support.verifier.api;

import java.util.Objects;

public final class PropertyPair {

    private final String displayValue;

    private final String value;

    public PropertyPair(final String value) {
        this(value, value);
    }

    public PropertyPair(final String value, final String displayValue) {
        this.value = value;
        this.displayValue = displayValue;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof PropertyPair)) {
            return false;
        }

        final PropertyPair other = (PropertyPair) obj;

        return Objects.equals(other.displayValue, displayValue) && Objects.equals(other.value, value);
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(value) + 31 * Objects.hashCode(displayValue);
    }

    @Override
    public String toString() {
        return value + "=" + displayValue;
    }
}
