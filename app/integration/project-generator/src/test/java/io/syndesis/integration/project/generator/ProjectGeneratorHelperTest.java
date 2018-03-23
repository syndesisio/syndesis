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
package io.syndesis.integration.project.generator;

import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Scheduler;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectGeneratorHelperTest {

    @Test
    public void testSanitizeConnectors() {
        TestResourceManager resourceManager = new TestResourceManager();
        resourceManager.put(TestConstants.TIMER_CONNECTOR.getId().get(), TestConstants.TIMER_CONNECTOR);

        Integration source = resourceManager.newIntegration(
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(new Connection.Builder()
                    .id("timer-connection")
                    .connectorId(TestConstants.TIMER_CONNECTOR.getId().get())
                    .build())
                .putConfiguredProperty("period", "5000")
                .action(TestConstants.PERIODIC_TIMER_ACTION)
                .build()
        );

        assertThat(source.getSteps().get(0).getConnection().isPresent()).isTrue();
        assertThat(source.getSteps().get(0).getConnection().get().getConnector().isPresent()).isFalse();

        Integration sanitized = ProjectGeneratorHelper.sanitize(source, resourceManager);

        assertThat(sanitized.getSteps().get(0).getConnection().isPresent()).isTrue();
        assertThat(sanitized.getSteps().get(0).getConnection().get().getConnector().isPresent()).isTrue();
        assertThat(sanitized.getSteps().get(0).getConnection().get().getConnector().get()).isEqualTo(TestConstants.TIMER_CONNECTOR);
    }

    @Test
    public void testSanitizeScheduler() {
        TestResourceManager resourceManager = new TestResourceManager();

        Integration source = resourceManager.newIntegration(
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(new Connection.Builder()
                    .id("timer-connection")
                    .connector(TestConstants.HTTP_CONNECTOR)
                    .build())
                .putConfiguredProperty("schedulerType", "timer")
                .putConfiguredProperty("schedulerExpression", "1s")
                .action(TestConstants.HTTP_GET_ACTION)
                .build()
        );

        assertThat(source.getScheduler().isPresent()).isFalse();

        Integration sanitized = ProjectGeneratorHelper.sanitize(source, resourceManager);

        assertThat(sanitized.getScheduler().isPresent()).isTrue();
        assertThat(sanitized.getScheduler().get()).hasFieldOrPropertyWithValue("type", Scheduler.Type.timer);
        assertThat(sanitized.getScheduler().get()).hasFieldOrPropertyWithValue("expression", "1s");
        assertThat(sanitized.getSteps().get(0).getStepKind()).isEqualTo(StepKind.endpoint);
        assertThat(sanitized.getSteps().get(0).getConfiguredProperties()).doesNotContainKey("scheduler-type");
        assertThat(sanitized.getSteps().get(0).getConfiguredProperties()).doesNotContainKey("scheduler-expression");
    }

    @Test
    public void testSanitizeDefaultScheduler() {
        TestResourceManager resourceManager = new TestResourceManager();

        Integration source = resourceManager.newIntegration(
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(new Connection.Builder()
                    .id("timer-connection")
                    .connector(TestConstants.HTTP_CONNECTOR)
                    .build())
                .putConfiguredProperty("schedulerExpression", "1s")
                .action(TestConstants.HTTP_GET_ACTION)
                .build()
        );

        assertThat(source.getScheduler().isPresent()).isFalse();

        Integration sanitized = ProjectGeneratorHelper.sanitize(source, resourceManager);

        assertThat(sanitized.getScheduler().isPresent()).isTrue();
        assertThat(sanitized.getScheduler().get()).hasFieldOrPropertyWithValue("type", Scheduler.Type.timer);
        assertThat(sanitized.getScheduler().get()).hasFieldOrPropertyWithValue("expression", "1s");
        assertThat(sanitized.getSteps().get(0).getStepKind()).isEqualTo(StepKind.endpoint);
        assertThat(sanitized.getSteps().get(0).getConfiguredProperties()).doesNotContainKey("scheduler-type");
        assertThat(sanitized.getSteps().get(0).getConfiguredProperties()).doesNotContainKey("scheduler-expression");
    }
}
