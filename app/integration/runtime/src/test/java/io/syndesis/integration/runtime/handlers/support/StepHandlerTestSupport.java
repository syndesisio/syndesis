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
package io.syndesis.integration.runtime.handlers.support;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import javax.xml.bind.JAXBException;

import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.model.filter.ExpressionFilterStep;
import io.syndesis.model.filter.RuleFilterStep;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.IntegrationDeploymentSpec;
import io.syndesis.model.integration.SimpleStep;
import io.syndesis.model.integration.Step;
import org.apache.camel.CamelContext;
import org.apache.camel.model.ModelHelper;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepHandlerTestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(StepHandlerTestSupport.class);

    protected void dumpRoutes(CamelContext context) {
        for (RouteDefinition definition: context.getRouteDefinitions()) {
            try {
                LOGGER.info("Route {}: \n{}", definition.getId(), ModelHelper.dumpModelAsXml(context, definition));
            } catch (JAXBException e) {
                LOGGER.warn("", e);
            }
        }
    }

    protected static IntegrationRouteBuilder newIntegrationRouteBuilder(Step... steps) {
        return new IntegrationRouteBuilder("", Collections.emptyList()) {
            @Override
            protected IntegrationDeployment loadDeployment() throws IOException {
                return newIntegrationDeployment(steps);
            }
        };
    }

    protected static IntegrationDeployment newIntegrationDeployment(Step... steps) {
        for (int i = 0; i < steps.length; i++) {
            if (steps[i] instanceof SimpleStep) {
                steps[i] = new SimpleStep.Builder().createFrom(steps[i]).putMetadata(Step.METADATA_STEP_INDEX, Integer.toString(i + 1)).build();
            } else if (steps[i] instanceof ExpressionFilterStep) {
                steps[i] = new ExpressionFilterStep.Builder().createFrom(steps[i]).putMetadata(Step.METADATA_STEP_INDEX, Integer.toString(i + 1)).build();
            } else if (steps[i] instanceof RuleFilterStep) {
                steps[i] = new RuleFilterStep.Builder().createFrom(steps[i]).putMetadata(Step.METADATA_STEP_INDEX, Integer.toString(i + 1)).build();
            }
        }
        return new IntegrationDeployment.Builder()
            .integrationId("test-integration")
            .name("Test Integration")
            .spec(new IntegrationDeploymentSpec.Builder()
                .description("This is a test integration!")
                .steps(Arrays.asList(steps))
                .build())
            .build();
    }
}
