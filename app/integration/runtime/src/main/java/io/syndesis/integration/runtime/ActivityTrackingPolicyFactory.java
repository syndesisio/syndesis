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
package io.syndesis.integration.runtime;

import io.syndesis.common.model.integration.Flow;
import org.apache.camel.spi.RoutePolicy;

public interface ActivityTrackingPolicyFactory {

    /**
     * Factory method creating new route policy instance.
     * @param flowId
     * @return
     */
    RoutePolicy createRoutePolicy(String flowId);

    /**
     * Checks whether this factory produces the given route policy.
     * @param routePolicy the policy to evaluate.
     * @return true if given route policy is produced by this factory.
     */
    boolean isInstance(RoutePolicy routePolicy);

    /**
     * Evaluates if this policy factory applies to the given flow.
     * @param flow given flow that this policy should apply to.
     * @return boolean flag marking that this policy is applicable or not.
     */
    default boolean appliesTo(Flow flow) {
        return true;
    }
}
