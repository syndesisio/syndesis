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
package io.syndesis.server.endpoint.v1.handler.api;

import java.util.Arrays;
import java.util.List;

import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiGeneratorHelperTest {

    @Test
    public void shouldMaintainSameIntegrationPrefixForFlowIds() {
        final Integration existing = new Integration.Builder().id("existing").build();

        final List<Flow> flows = Arrays.asList(new Flow.Builder().id("generated:flows:operation-1").build(),
            new Flow.Builder().id("generated:flows:operation-2").build());

        final Iterable<Flow> sameIntegrationPrefixFlows = ApiGeneratorHelper.withSameIntegrationIdPrefix(existing, flows);

        assertThat(sameIntegrationPrefixFlows).allMatch(f -> f.getId().get().startsWith("existing:flows:operation-"));
    }
}
