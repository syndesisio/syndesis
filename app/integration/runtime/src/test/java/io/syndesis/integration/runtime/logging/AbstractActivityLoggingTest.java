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

package io.syndesis.integration.runtime.logging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.syndesis.common.util.Json;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.integration.runtime.IntegrationTestSupport;
import io.syndesis.integration.runtime.util.JsonSupport;
import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
@SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes"})
public abstract class AbstractActivityLoggingTest extends IntegrationTestSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityLoggingTest.class);

    protected CamelContext context;
    protected ActivityTracker activityTracker;
    protected ArrayList<ActivityEvent> activityEvents;

    @Before
    public void before() throws Exception {
        activityEvents = new ArrayList<>();

        activityTracker = items -> {
            try {
                String json = JsonSupport.toJsonObject(items);
                LOGGER.debug(json);
                ActivityEvent event = Json.reader().forType(ActivityEvent.class).readValue(json);

                activityEvents.add(event);
            } catch (IOException e) {
                LOGGER.warn("Errors during activity tracking", e);
            }
        };

        context = new DefaultCamelContext();
        context.setUuidGenerator(KeyGenerator::createKey);
        context.addLogListener(new IntegrationLoggingListener(activityTracker));
        context.addInterceptStrategy(new ActivityTrackingInterceptStrategy(activityTracker));
        context.addRoutes(createTestRoutes());

        context.start();
    }

    @After
    public void after() throws Exception {
        context.stop();
    }

    protected abstract RoutesBuilder createTestRoutes();

    // **************
    // Helpers
    // **************

    List<String> findExchangesLogged() {
        return activityEvents.stream().map(x -> x.exchange).distinct().collect(Collectors.toList());
    }

    ActivityEvent findActivityEvent(Predicate<ActivityEvent> filter) {
        return getActivityEventStream(filter).findFirst().orElseThrow(NoSuchElementException::new);
    }

    List<ActivityEvent> findActivityEvents(Predicate<ActivityEvent> filter) {
        return getActivityEventStream(filter).collect(Collectors.toList());
    }

    private Stream<ActivityEvent> getActivityEventStream(Predicate<ActivityEvent> filter) {
        return activityEvents.stream()
                .filter(filter);
    }

    public static class ActivityEvent {
        public String exchange;
        public String status;
        public String step;
        public String duration;
        public String failed;
        public String failure;
        public String message;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ActivityEvent)) return false;

            ActivityEvent that = (ActivityEvent) o;

            if (!exchange.equals(that.exchange)) return false;
            if (status != null ? !status.equals(that.status) : that.status != null) return false;
            if (step != null ? !step.equals(that.step) : that.step != null) return false;
            if (duration != null ? !duration.equals(that.duration) : that.duration != null) return false;
            if (failed != null ? !failed.equals(that.failed) : that.failed != null) return false;
            if (failure != null ? !failure.equals(that.failure) : that.failure != null) return false;
            return message != null ? message.equals(that.message) : that.message == null;
        }

        @Override
        public int hashCode() {
            int result = exchange.hashCode();
            result = 31 * result + (status != null ? status.hashCode() : 0);
            result = 31 * result + (step != null ? step.hashCode() : 0);
            result = 31 * result + (duration != null ? duration.hashCode() : 0);
            result = 31 * result + (failed != null ? failed.hashCode() : 0);
            result = 31 * result + (failure != null ? failure.hashCode() : 0);
            result = 31 * result + (message != null ? message.hashCode() : 0);
            return result;
        }
    }
}
