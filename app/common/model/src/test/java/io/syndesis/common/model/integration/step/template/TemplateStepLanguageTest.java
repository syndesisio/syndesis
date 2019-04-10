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
package io.syndesis.common.model.integration.step.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage;

public class TemplateStepLanguageTest {

    /**
     * Tests an integration containing a template step is correctly updated
     * by the {@link TemplateStepLanguage#updateIntegration(Integration)}
     * with the correct dependency given a language property
     */
    @Test
    public void testIntegrationUpdate() {
        for(TemplateStepLanguage stepLanguage : TemplateStepLanguage.values()) {
            String template = "{{text}}";
            String language = stepLanguage.toString();

            Integration integration = new Integration.Builder()
                .id("test-integration")
                .name("Test Integration")
                .description("This is a test integration!")
                .addFlow(new Flow.Builder()
                     .addStep(new Step.Builder()
                              .id("Step-1")
                              .stepKind(StepKind.endpoint)
                              .build())
                     .addStep(new Step.Builder()
                              .id("templating")
                              .stepKind(StepKind.template)
                              .putConfiguredProperty("template", template)
                              .putConfiguredProperty("language", language)
                              .build())
                     .addStep(new Step.Builder()
                              .id("mock-endpoint")
                              .stepKind(StepKind.endpoint)
                              .action(new ConnectorAction.Builder()
                                      .descriptor(new ConnectorDescriptor.Builder()
                                                  .componentScheme("mock")
                                                  .putConfiguredProperty("name", "result")
                                                  .build())
                                      .build())
                              .build())
                     .build())
                .build();

            Integration updated = TemplateStepLanguage.updateIntegration(integration);

            assertNotEquals(integration, updated);

            assertEquals(1, updated.getFlows().size());
            Flow updatedFlow = updated.getFlows().iterator().next();

            assertEquals(3, updatedFlow.getSteps().size());
            for (Step step : updatedFlow.getSteps()) {
                assertTrue(step.getStepKind() == StepKind.endpoint || step.getStepKind() == StepKind.template);
                if (step.getStepKind() == StepKind.template) {
                    assertEquals(1, step.getDependencies().size());
                    Dependency dependency = step.getDependencies().iterator().next();
                    assertEquals(stepLanguage.mavenDependency(), dependency.getId());
                }
            }
        }
    }

    /**
     * An integration without a language specifed defaults to mustache
     */
    @Test
    public void testDefaultIntegrationUpdate() {
        TemplateStepLanguage stepLanguage = TemplateStepLanguage.MUSTACHE;
        String template = "{{text}}";

        Integration integration = new Integration.Builder()
            .id("test-integration")
            .name("Test Integration")
            .description("This is a test integration!")
            .addFlow(new Flow.Builder()
                     .addStep(new Step.Builder()
                              .id("Step-1")
                              .stepKind(StepKind.endpoint)
                              .build())
                     .addStep(new Step.Builder()
                              .id("templating")
                              .stepKind(StepKind.template)
                              .putConfiguredProperty("template", template)
                              .build())
                     .addStep(new Step.Builder()
                              .id("mock-endpoint")
                              .stepKind(StepKind.endpoint)
                              .action(new ConnectorAction.Builder()
                                      .descriptor(new ConnectorDescriptor.Builder()
                                                  .componentScheme("mock")
                                                  .putConfiguredProperty("name", "result")
                                                  .build())
                                      .build())
                              .build())
                     .build())
            .build();

        Integration updated = TemplateStepLanguage.updateIntegration(integration);

        assertNotEquals(integration, updated);

        assertEquals(1, updated.getFlows().size());
        Flow updatedFlow = updated.getFlows().iterator().next();

        assertEquals(3, updatedFlow.getSteps().size());
        for (Step step : updatedFlow.getSteps()) {
            assertTrue(step.getStepKind() == StepKind.endpoint || step.getStepKind() == StepKind.template);
            if (step.getStepKind() == StepKind.template) {
                assertEquals(1, step.getDependencies().size());
                Dependency dependency = step.getDependencies().iterator().next();
                assertEquals(stepLanguage.mavenDependency(), dependency.getId());
            }
        }
    }
}
