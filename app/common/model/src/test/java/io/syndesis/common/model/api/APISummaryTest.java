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

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connector;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Map;

public class APISummaryTest {

    @Test
    public void testConnectorSummary() {
        APISummary summary = new APISummary.Builder().createFrom(
            new Connector.Builder().addActions(
                new ConnectorAction.Builder().addTags("1", "2", "3").build(),
                new ConnectorAction.Builder().addTags("2", "3", "4").build(),
                new ConnectorAction.Builder().addTag("2").build(),
                new ConnectorAction.Builder().build()
            ).build()
        ).build();

        Assertions.assertThat(summary.getActionsSummary().getTotalActions()).isEqualTo(4);
        Map<String, Integer> actionCountByTags = summary.getActionsSummary().getActionCountByTags();
        Assertions.assertThat(actionCountByTags).containsEntry("1", 1);
        Assertions.assertThat(actionCountByTags).containsEntry("2", 3);
        Assertions.assertThat(actionCountByTags).containsEntry("3", 2);
        Assertions.assertThat(actionCountByTags).containsEntry("4", 1);
    }

}
