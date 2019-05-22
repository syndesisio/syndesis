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

import java.util.Arrays;
import java.util.List;

import io.syndesis.common.util.Resources;
import io.syndesis.integration.runtime.logging.ActivityTracker;
import io.syndesis.integration.runtime.logging.IntegrationActivityTrackingPolicy;
import io.syndesis.integration.runtime.logging.IntegrationActivityTrackingPolicyFactory;
import io.syndesis.integration.runtime.logging.FlowActivityTrackingPolicy;
import io.syndesis.integration.runtime.logging.FlowActivityTrackingPolicyFactory;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.LogDefinition;
import org.apache.camel.model.PipelineDefinition;
import org.apache.camel.model.ProcessDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.model.SetHeaderDefinition;
import org.apache.camel.model.SplitDefinition;
import org.apache.camel.model.ToDefinition;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class IntegrationRouteBuilderTest extends IntegrationTestSupport {

    private ActivityTracker tracker = new ActivityTracker.SysOut();
    private List<ActivityTrackingPolicyFactory> policyFactories = Arrays.asList(new IntegrationActivityTrackingPolicyFactory(tracker),
                                                                                new FlowActivityTrackingPolicyFactory(tracker));

    @Test
    public void testIntegrationRouteBuilder() throws Exception {
        String configurationLocation = "classpath:syndesis/integration/integration.json";

        IntegrationRouteBuilder routeBuilder = new IntegrationRouteBuilder(configurationLocation, Resources.loadServices(IntegrationStepHandler.class), policyFactories);

        // initialize routes
        routeBuilder.configure();

        // Dump routes as XML for troubleshooting
        dumpRoutes(new DefaultCamelContext(), routeBuilder.getRouteCollection());

        RoutesDefinition routes = routeBuilder.getRouteCollection();

        assertThat(routes.getRoutes()).hasSize(1);

        RouteDefinition route = routes.getRoutes().get(0);

        assertThat(route.getRoutePolicies()).hasSize(1);

        assertThat(route.getInputs()).hasSize(1);
        assertThat(route.getInputs().get(0)).hasFieldOrPropertyWithValue("uri", "direct:expression");
        assertThat(route.getOutputs()).hasSize(2);
        assertThat(getOutput(route, 0)).isInstanceOf(PipelineDefinition.class);
        assertThat(getOutput(route, 0).getOutputs()).hasSize(2);
        assertThat(getOutput(route, 0).getOutputs().get(0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(route, 0).getOutputs().get(1)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(route, 1)).isInstanceOf(SplitDefinition.class);
        assertThat(getOutput(route, 1).getOutputs()).hasSize(3);
        assertThat(getOutput(route, 1, 0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(route, 1, 1)).isInstanceOf(ProcessDefinition.class);
        assertThat(getOutput(route, 1, 2)).isInstanceOf(PipelineDefinition.class);
        assertThat(getOutput(route, 1, 2).getOutputs()).hasSize(3);
        assertThat(getOutput(route, 1, 2, 0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(route, 1, 2, 1)).isInstanceOf(ToDefinition.class);
        assertThat(getOutput(route, 1, 2, 1)).hasFieldOrPropertyWithValue("uri", "mock:expression");
        assertThat(getOutput(route, 1, 2, 2)).isInstanceOf(ProcessDefinition.class);
    }

    @Test
    public void testMultiFlowIntegrationRouteBuilder() throws Exception {
        String configurationLocation = "classpath:syndesis/integration/multi-flow-integration.json";

        IntegrationRouteBuilder routeBuilder = new IntegrationRouteBuilder(configurationLocation, Resources.loadServices(IntegrationStepHandler.class), policyFactories);

        // initialize routes
        routeBuilder.configure();

        // Dump routes as XML for troubleshooting
        dumpRoutes(new DefaultCamelContext(), routeBuilder.getRouteCollection());

        RoutesDefinition routes = routeBuilder.getRouteCollection();

        assertThat(routes.getRoutes()).hasSize(3);

        RouteDefinition primaryRoute = routes.getRoutes().get(0);

        assertThat(primaryRoute.getRoutePolicies()).hasSize(1);
        assertThat(primaryRoute.getRoutePolicies().get(0)).isInstanceOf(IntegrationActivityTrackingPolicy.class);

        assertThat(primaryRoute.getInputs()).hasSize(1);
        assertThat(primaryRoute.getInputs().get(0)).hasFieldOrPropertyWithValue("uri", "direct:expression");
        assertThat(primaryRoute.getOutputs()).hasSize(2);
        assertThat(getOutput(primaryRoute, 0)).isInstanceOf(PipelineDefinition.class);
        assertThat(getOutput(primaryRoute, 0).getOutputs()).hasSize(2);
        assertThat(getOutput(primaryRoute, 0).getOutputs().get(0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(primaryRoute, 0).getOutputs().get(1)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(primaryRoute, 1)).isInstanceOf(SplitDefinition.class);
        assertThat(getOutput(primaryRoute, 1).getOutputs()).hasSize(5);
        assertThat(getOutput(primaryRoute, 1, 0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(primaryRoute, 1, 1)).isInstanceOf(ProcessDefinition.class);
        assertThat(getOutput(primaryRoute, 1, 2)).isInstanceOf(PipelineDefinition.class);
        assertThat(getOutput(primaryRoute, 1, 2).getOutputs()).hasSize(2);
        assertThat(getOutput(primaryRoute, 1, 2, 0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(primaryRoute, 1, 2, 1)).isInstanceOf(LogDefinition.class);
        assertThat(getOutput(primaryRoute, 1, 2, 1)).hasFieldOrPropertyWithValue("message", "Body: [${bean:bodyLogger}] Before");
        assertThat(getOutput(primaryRoute, 1, 3)).isInstanceOf(PipelineDefinition.class);
        assertThat(getOutput(primaryRoute, 1, 3).getOutputs()).hasSize(3);
        assertThat(getOutput(primaryRoute, 1, 3, 0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(primaryRoute, 1, 3, 1)).isInstanceOf(ChoiceDefinition.class);
        assertThat(getOutput(primaryRoute, 1, 3, 2)).isInstanceOf(ProcessDefinition.class);
        assertThat(getOutput(primaryRoute, 1, 4)).isInstanceOf(PipelineDefinition.class);
        assertThat(getOutput(primaryRoute, 1, 4).getOutputs()).hasSize(2);
        assertThat(getOutput(primaryRoute, 1, 4, 0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(primaryRoute, 1, 4, 1)).isInstanceOf(LogDefinition.class);
        assertThat(getOutput(primaryRoute, 1, 4, 1)).hasFieldOrPropertyWithValue("message", "Body: [${bean:bodyLogger}] Finished");

        RouteDefinition conditionalRoute = routes.getRoutes().get(1);

        assertThat(conditionalRoute.getRoutePolicies()).hasSize(1);
        assertThat(conditionalRoute.getRoutePolicies().get(0)).isInstanceOf(FlowActivityTrackingPolicy.class);

        assertThat(conditionalRoute.getInputs()).hasSize(1);
        assertThat(conditionalRoute.getInputs().get(0)).hasFieldOrPropertyWithValue("uri", "direct");
        assertThat(conditionalRoute.getOutputs()).hasSize(5);
        assertThat(getOutput(conditionalRoute, 0)).isInstanceOf(PipelineDefinition.class);
        assertThat(getOutput(conditionalRoute, 0).getOutputs()).hasSize(2);
        assertThat(getOutput(conditionalRoute, 0).getOutputs().get(0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(conditionalRoute, 0).getOutputs().get(1)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(conditionalRoute, 1)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(conditionalRoute, 2)).isInstanceOf(ProcessDefinition.class);
        assertThat(getOutput(conditionalRoute, 3)).isInstanceOf(PipelineDefinition.class);
        assertThat(getOutput(conditionalRoute, 3).getOutputs()).hasSize(2);
        assertThat(getOutput(conditionalRoute, 3, 0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(conditionalRoute, 3, 1)).isInstanceOf(LogDefinition.class);
        assertThat(getOutput(conditionalRoute, 3, 1)).hasFieldOrPropertyWithValue("message", "Body: [${bean:bodyLogger}] Found <Play>");
        assertThat(getOutput(conditionalRoute, 4)).isInstanceOf(PipelineDefinition.class);
        assertThat(getOutput(conditionalRoute, 4).getOutputs()).hasSize(3);
        assertThat(getOutput(conditionalRoute, 4, 0)).isInstanceOf(SetHeaderDefinition.class);
        assertThat(getOutput(conditionalRoute, 4, 1)).isInstanceOf(ToDefinition.class);
        assertThat(getOutput(conditionalRoute, 4, 1)).hasFieldOrPropertyWithValue("uri", "bean:io.syndesis.connector.flow.NoOpBean?method=process");
        assertThat(getOutput(conditionalRoute, 4, 2)).isInstanceOf(ProcessDefinition.class);
    }
}
