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

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.integration.IntegrationDeployment;

final class IntegrationIdFilter
    implements Function<ListResult<IntegrationDeployment>, ListResult<IntegrationDeployment>> {

    final String integrationId;

    IntegrationIdFilter(final String integrationId) {
        this.integrationId = Objects.requireNonNull(integrationId, "integrationId");
    }

    @Override
    public ListResult<IntegrationDeployment> apply(final ListResult<IntegrationDeployment> list) {
        if (integrationId == null) {
            return list;
        }

        final List<IntegrationDeployment> filtered = list.getItems().stream()
            .filter(i -> i.getSpec().idEquals(integrationId)).collect(Collectors.toList());

        return ListResult.of(filtered);
    }
}
