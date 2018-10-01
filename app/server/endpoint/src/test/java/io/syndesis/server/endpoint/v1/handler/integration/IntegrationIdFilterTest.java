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
package io.syndesis.server.endpoint.v1.handler.integration;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;

import org.junit.Test;

import static java.util.Collections.emptyList;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationIdFilterTest {

    static final String ID = "wanted";

    final IntegrationIdFilter filter = new IntegrationIdFilter(ID);

    final IntegrationDeployment wanted = new IntegrationDeployment.Builder()
        .spec(new Integration.Builder().id(ID).build()).build();

    final IntegrationDeployment unwanted = new IntegrationDeployment.Builder()
        .spec(new Integration.Builder().id("unwanted").build()).build();

    @Test
    public void shouldFilterEmptyResults() {
        assertThat(filter.apply(ListResult.of(emptyList()))).isEmpty();
    }

    @Test
    public void shouldFilterOutTrivialWantedDeployments() {
        assertThat(filter.apply(ListResult.of(wanted))).containsOnly(wanted);
    }

    @Test
    public void shouldFilterOutWantedDeployments() {
        assertThat(filter.apply(ListResult.of(unwanted, wanted, unwanted))).containsOnly(wanted);
    }

    @Test
    public void shouldFilterOutWantedDeploymentsNotFinding() {
        assertThat(filter.apply(ListResult.of(unwanted, unwanted, unwanted))).isEmpty();
    }

}
