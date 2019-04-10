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
package io.syndesis.server.monitoring;

import io.syndesis.common.model.integration.IntegrationDeploymentState;

/**
 * Interface to register {@link StateHandler}s with {@link DeploymentStateMonitorController}.
 * @author dhirajsb
 */
public interface DeploymentStateMonitor {
    /**
     * Registers handler with controller.
     * @param state        trigger state.
     * @param stateHandler handler for state.
     */
    void register(IntegrationDeploymentState state, StateHandler stateHandler);
}
