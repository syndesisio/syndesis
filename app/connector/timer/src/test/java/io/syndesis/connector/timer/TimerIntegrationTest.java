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
package io.syndesis.connector.timer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.Resources;
import io.syndesis.integration.component.proxy.ComponentProxyEndpoint;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.quartz2.QuartzEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ModelCamelContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class TimerIntegrationTest {

    private static final String DESIRED_CRON_EXPRESSION = "0 0/1 * * * ?";

    private static final Connector TIMER = loadConnector();

    private final ConnectorAction action;

    public TimerIntegrationTest(final ConnectorAction action) {
        this.action = action;
    }

    @Test
    public void shouldSupportCronExpressions() throws Exception {
        final RouteBuilder builder = createRouteBuilder();

        final CamelContext context = builder.getContext();
        builder.addRoutesToCamelContext(context);

        try {
            context.start();
            final ComponentProxyEndpoint proxyEndpoint = (ComponentProxyEndpoint) context.getRoutes().get(0).getEndpoint();
            final QuartzEndpoint quartzEndpoint = (QuartzEndpoint) proxyEndpoint.getEndpoint();
            assertThat(quartzEndpoint.getCron()).isEqualTo(DESIRED_CRON_EXPRESSION);
        } finally {
            context.stop();
        }
    }

    RouteBuilder createRouteBuilder() {
        return new IntegrationRouteBuilder("", Resources.loadServices(IntegrationStepHandler.class)) {
            @Override
            protected ModelCamelContext createContainer() {
                final DefaultCamelContext leanContext = new DefaultCamelContext();
                leanContext.disableJMX();

                return leanContext;
            }

            @Override
            protected Integration loadIntegration() {
                return createTimerIntegration(action);
            }
        };
    }

    @Parameters
    public static Iterable<ConnectorAction> cases() {
        return Arrays.asList(cronAction("0+0/1+*+*+*+?"), cronAction("0 0/1 * * * ?"));
    }

    static Integration createTimerIntegration(final ConnectorAction action) {
        return new Integration.Builder()
            .addFlow(new Flow.Builder()
                .addStep(new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .connection(new Connection.Builder()
                        .connector(TIMER)
                        .build())
                    .action(action)
                    .build())
                .addStep(new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(new ConnectorAction.Builder()
                        .id("io.syndesis:log-action")
                        .descriptor(new ConnectorDescriptor.Builder()
                            .componentScheme("log")
                            .putConfiguredProperty("loggerName", "loggerName")
                            .build())
                        .build())
                    .build())
                .build())
            .build();
    }

    static ConnectorAction cronAction(final String expression) {
        final ConnectorAction cronTimer = TIMER.findActionById("io.syndesis:timer-chron").get();
        return new ConnectorAction.Builder().createFrom(cronTimer)
            .descriptor(new ConnectorDescriptor.Builder().createFrom(cronTimer.getDescriptor())
                .putConfiguredProperty("cron", expression)
                .build())
            .build();
    }

    static Connector loadConnector() {
        try (InputStream json = TimerIntegrationTest.class.getResourceAsStream("/META-INF/syndesis/connector/timer.json")) {
            return Json.readFromStream(json, Connector.class);
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }
}
