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
package io.syndesis.server.api.generator.util;

import java.util.Comparator;
import java.util.stream.Collectors;

import io.syndesis.common.model.action.ConnectorAction;

import static org.apache.commons.lang3.StringUtils.trimToNull;

public final class ActionComparator implements Comparator<ConnectorAction> {

    public static final Comparator<ConnectorAction> INSTANCE = new ActionComparator();

    private static final Comparator<String> BASE_COMPARATOR = Comparator.nullsLast(String::compareTo);

    private ActionComparator() {
        // use INSTANCE field
    }

    @Override
    public int compare(final ConnectorAction left, final ConnectorAction right) {
        final String leftTags = allTags(left);
        final String rightTags = allTags(right);

        final int base = BASE_COMPARATOR.compare(leftTags, rightTags);
        if (base != 0) {
            return base;
        }

        final String leftName = name(left);
        final String rightName = name(right);

        return BASE_COMPARATOR.compare(leftName, rightName);
    }

    private static String allTags(final ConnectorAction left) {
        return trimToNull(left.getTags().stream().collect(Collectors.joining()));
    }

    private static String name(final ConnectorAction action) {
        return trimToNull(action.getName());
    }
}
