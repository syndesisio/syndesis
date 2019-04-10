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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.syndesis.common.model.action.ConnectorAction;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ActionComparatorTest {

    @Test
    public void shouldOrderActionsBasedOnTags() {
        final ConnectorAction a = new ConnectorAction.Builder().addTag("a").build();
        final ConnectorAction b = new ConnectorAction.Builder().addTag("b").build();
        final ConnectorAction c = new ConnectorAction.Builder().addTag("c").build();
        final ConnectorAction noTags = new ConnectorAction.Builder().build();

        final List<ConnectorAction> actions = new ArrayList<>(Arrays.asList(c, noTags, a, b));

        Collections.shuffle(actions);

        actions.sort(ActionComparator.INSTANCE);

        assertThat(actions).containsExactly(a, b, c, noTags);
    }

    @Test
    public void shouldOrderActionsBasedOnTagsAndName() {
        final ConnectorAction a = new ConnectorAction.Builder().name("a").addTag("a").build();
        final ConnectorAction b = new ConnectorAction.Builder().name("b").addTag("b").build();
        final ConnectorAction c = new ConnectorAction.Builder().name("c").addTag("b").build();
        final ConnectorAction noTagsA = new ConnectorAction.Builder().name("a").build();
        final ConnectorAction noTagsB = new ConnectorAction.Builder().name("b").build();
        final ConnectorAction noTagsNoName = new ConnectorAction.Builder().build();

        final List<ConnectorAction> actions = new ArrayList<>(Arrays.asList(c, noTagsA, a, noTagsB, b, noTagsNoName));

        Collections.shuffle(actions);

        actions.sort(ActionComparator.INSTANCE);

        assertThat(actions).containsExactly(a, b, c, noTagsA, noTagsB, noTagsNoName);
    }
}
