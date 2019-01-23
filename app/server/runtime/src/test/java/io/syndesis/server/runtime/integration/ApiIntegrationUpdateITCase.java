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
package io.syndesis.server.runtime.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.server.runtime.BaseITCase;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiIntegrationUpdateITCase extends BaseITCase {

    private Integration existing;

    private final HttpHeaders multipart;

    public ApiIntegrationUpdateITCase() {
        multipart = new HttpHeaders();
        multipart.setContentType(MediaType.MULTIPART_FORM_DATA);
    }

    @Before
    public void generateApiIntegration() {
        final MultiValueMap<Object, Object> data = specification("/io/syndesis/server/runtime/test-swagger.json");

        final ResponseEntity<Integration> integrationResponse = post("/api/v1/apis/generator", data, Integration.class, tokenRule.validToken(), HttpStatus.OK,
            multipart);

        final Integration integration = integrationResponse.getBody();
        final Flow createTaskFlow = integration.findFlowById(integration.getId().get() + ":flows:create-task").get();
        final List<Step> createTaskFlowSteps = createTaskFlow.getSteps();
        final Step startStep = createTaskFlowSteps.get(0);
        final Step logStep = new Step.Builder().id("log-step").stepKind(StepKind.log).build();
        final Step endStep = createTaskFlowSteps.get(1);
        final Flow flowWithLog = createTaskFlow.builder().steps(Arrays.asList(startStep, logStep, endStep)).build();
        final Integration integrationWithOneImplementedFlow = integration.builder().flows(replace(integration.getFlows(), flowWithLog)).build();

        existing = dataManager.create(integrationWithOneImplementedFlow);
    }

    @Test
    public void shouldUpdateApiIntegration() {
        final String integrationId = existing.getId().get();
        put("/api/v1/integrations/" + integrationId + "/specification", specification("/io/syndesis/server/runtime/updated-test-swagger.json"), Void.class,
            tokenRule.validToken(), HttpStatus.NO_CONTENT, multipart);

        final Integration updated = dataManager.fetch(Integration.class, integrationId);

        // not present in updated-test-swagger.json
        assertThat(updated.findFlowById(integrationId + ":flows:fetch-task")).isNotPresent();

        // new operation added in updated-test-swagger.json
        assertThat(updated.findFlowById(integrationId + ":flows:count-tasks")).isPresent();

        // modified in updated-test-swagger.json
        assertThat(updated.findFlowById(integrationId + ":flows:create-task")).hasValueSatisfying(flow -> {
            final List<Step> steps = flow.getSteps();
            assertThat(steps).hasSize(3);
            assertThat(flow.findStepById("log-step")).isPresent();

            final Step firstStep = steps.get(0);
            final DataShape outputDataShape = firstStep.outputDataShape().get();
            final String outputSpecification = outputDataShape.getSpecification();
            assertThat(outputSpecification).contains("debug", "task");
        });
    }

    static Iterable<Flow> replace(final List<Flow> flows, final Flow replacement) {
        final ArrayList<Flow> ret = new ArrayList<>(flows.size());

        final String replacementId = replacement.getId().get();
        for (final Flow flow : flows) {
            if (flow.idEquals(replacementId)) {
                ret.add(replacement);
            } else {
                ret.add(flow);
            }
        }

        return ret;
    }

    static MultiValueMap<Object, Object> specification(final String path) {
        final LinkedMultiValueMap<Object, Object> data = new LinkedMultiValueMap<>(1);
        data.add("specification", new InputStreamResource(ApiIntegrationUpdateITCase.class.getResourceAsStream(path)));
        return data;
    }

}
