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
package io.syndesis.connector.calendar;

import java.util.Map;

import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.google.calendar.internal.CalendarEventsApiMethod;
import org.apache.camel.component.google.calendar.internal.GoogleCalendarApiCollection;

import com.google.api.services.calendar.model.Event;

public class GoogleCalendarSendEventCustomizer implements ComponentProxyCustomizer {

    private GoogleCalendarEventModel defaults;
    private String calendarId;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setApiMethod(options);
        component.setBeforeProducer(this::beforeProducer);
        component.setAfterProducer(GoogleCalendarSendEventCustomizer::afterProducer);
    }

    private void setApiMethod(Map<String, Object> options) {
        defaults = new GoogleCalendarEventModel();
        defaults.setDescription(ConnectorOptions.extractOption(options, "description"));
        defaults.setTitle(ConnectorOptions.extractOption(options, "summary"));
        defaults.setAttendees(ConnectorOptions.extractOption(options, "attendees"));
        defaults.setStartDate(ConnectorOptions.extractOption(options, "startDate"));
        defaults.setStartTime(ConnectorOptions.extractOption(options, "startTime"));
        defaults.setEndDate(ConnectorOptions.extractOption(options, "endDate"));
        defaults.setEndTime(ConnectorOptions.extractOption(options, "endTime"));
        defaults.setLocation(ConnectorOptions.extractOption(options, "location"));

        calendarId = ConnectorOptions.extractOption(options, "calendarId");

        options.put("apiName", GoogleCalendarApiCollection.getCollection().getApiName(CalendarEventsApiMethod.class).getName());
        options.put("methodName", "insert");
    }

    private void beforeProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        final GoogleCalendarEventModel event = exchange.getIn().getBody(GoogleCalendarEventModel.class);
        final GoogleCalendarEventModel eventWithDefaults = event.applyDefaultsFrom(defaults);

        in.setHeader("CamelGoogleCalendar.content", eventWithDefaults.asEvent());
        in.setHeader("CamelGoogleCalendar.calendarId", calendarId);
    }

    private static void afterProducer(Exchange exchange) {

        final Message in = exchange.getIn();
        final Event event = exchange.getIn().getBody(Event.class);
        final GoogleCalendarEventModel model = GoogleCalendarEventModel.newFrom(event);

        in.setBody(model);
    }

}
