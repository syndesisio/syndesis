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
package io.syndesis.server.controller.integration.noop;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.server.controller.StateChangeHandler;
import io.syndesis.server.controller.StateChangeHandlerProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "controllers.integration", havingValue = "noop")
public class NoopHandlerProvider implements StateChangeHandler, StateChangeHandlerProvider {

    @Override
    public Set<IntegrationDeploymentState> getTriggerStates() {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            IntegrationDeploymentState.Published,
            IntegrationDeploymentState.Unpublished)));
    }

    @Override
    public List<StateChangeHandler> getStatusChangeHandlers() {
        return Collections.singletonList(this);
    }
}
