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
package io.syndesis.common.model.api;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connector;

import static org.assertj.core.api.Assertions.assertThat;

public class APISummaryTest {

    @Test
    public void testConnectorSummary() {
        APISummary summary = APISummary.Builder.createFrom(
            new Connector.Builder().addActions(
                new ConnectorAction.Builder().addTags("1", "2", "3").build(),
                new ConnectorAction.Builder().addTags("2", "3", "4").build(),
                new ConnectorAction.Builder().addTag("2").build(),
                new ConnectorAction.Builder().build()
            ).build()
        ).build();

        assertThat(summary.getActionsSummary().getTotalActions()).isEqualTo(4);
        Map<String, Integer> actionCountByTags = summary.getActionsSummary().getActionCountByTags();
        assertThat(actionCountByTags).containsEntry("1", 1);
        assertThat(actionCountByTags).containsEntry("2", 3);
        assertThat(actionCountByTags).containsEntry("3", 2);
        assertThat(actionCountByTags).containsEntry("4", 1);
    }

}
