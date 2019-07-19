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

import static io.syndesis.connector.calendar.utils.GoogleCalendarUtils.getAttendeesList;
import static io.syndesis.connector.calendar.utils.GoogleCalendarUtils.getAttendeesString;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.google.calendar.internal.CalendarEventsApiMethod;
import org.apache.camel.component.google.calendar.internal.GoogleCalendarApiCollection;
import org.apache.camel.util.ObjectHelper;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

public class GoogleCalendarUpdateEventCustomizer implements ComponentProxyCustomizer {

    private String description;
    private String summary;
    private String attendees;
    private String calendarId;
    private String eventId;
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private String location;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setApiMethod(options);
        component.setBeforeProducer(this::beforeProducer);
        component.setAfterProducer(this::afterProducer);
    }

    private void setApiMethod(Map<String, Object> options) {
        description = ConnectorOptions.extractOption(options, "description");
        summary = ConnectorOptions.extractOption(options, "summary");
        calendarId = ConnectorOptions.extractOption(options, "calendarId");
        eventId = ConnectorOptions.extractOption(options, "eventId");
        attendees = ConnectorOptions.extractOption(options, "attendees");
        startDate = ConnectorOptions.extractOption(options, "startDate");
        endDate = ConnectorOptions.extractOption(options, "endDate");
        startTime = ConnectorOptions.extractOption(options, "startTime");
        endTime = ConnectorOptions.extractOption(options, "endTime");
        location = ConnectorOptions.extractOption(options, "location");
        options.put("apiName", GoogleCalendarApiCollection.getCollection().getApiName(CalendarEventsApiMethod.class).getName());
        options.put("methodName", "update");
    }

    @SuppressWarnings("PMD.NPathComplexity")
    private void beforeProducer(Exchange exchange) throws ParseException {

        final Message in = exchange.getIn();
        final GoogleCalendarEventModel event = exchange.getIn().getBody(GoogleCalendarEventModel.class);
        if (event != null) {

            if (ObjectHelper.isNotEmpty(event.getTitle())) {
                summary = event.getTitle();
            }
            if (ObjectHelper.isNotEmpty(event.getDescription())) {
                description = event.getDescription();
            }
            if (ObjectHelper.isNotEmpty(event.getAttendees())) {
                attendees = event.getAttendees();
            }
            if (ObjectHelper.isNotEmpty(event.getStartDate())) {
                startDate = event.getStartDate();
            }
            if (ObjectHelper.isNotEmpty(event.getStartTime())) {
                startTime = event.getStartTime();
            }
            if (ObjectHelper.isNotEmpty(event.getEndDate())) {
                endDate = event.getEndDate();
            }
            if (ObjectHelper.isNotEmpty(event.getEndTime())) {
                endTime = event.getEndTime();
            }
            if (ObjectHelper.isNotEmpty(event.getLocation())) {
                location = event.getLocation();
            }
            if (ObjectHelper.isNotEmpty(event.getEventId())) {
                eventId = event.getEventId();
            }
        }

        in.setHeader("CamelGoogleCalendar.content", createGoogleEvent(summary, description, attendees, startDate, startTime, endDate, endTime, location));
        in.setHeader("CamelGoogleCalendar.eventId", eventId);
        in.setHeader("CamelGoogleCalendar.calendarId", calendarId);
    }

    @SuppressWarnings("PMD.NPathComplexity")
    private void afterProducer(Exchange exchange) {

        final Message in = exchange.getIn();
        final Event event = exchange.getIn().getBody(Event.class);
        GoogleCalendarEventModel model = new GoogleCalendarEventModel();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        if (event != null) {

            if (ObjectHelper.isNotEmpty(event.getSummary())) {
                model.setTitle(event.getSummary());
            }
            if (ObjectHelper.isNotEmpty(event.getDescription())) {
                model.setDescription(event.getDescription());
            }
            if (ObjectHelper.isNotEmpty(event.getAttendees())) {
                model.setAttendees(getAttendeesString(event.getAttendees()));
            }
            if (ObjectHelper.isNotEmpty(event.getStart())) {
                if (event.getStart().getDateTime() != null) {
                    model.setStartDate(dateFormat.format(new Date(event.getStart().getDateTime().getValue())));
                    model.setStartTime(timeFormat.format(new Date(event.getStart().getDateTime().getValue())));
                } else {
                    model.setStartDate(dateFormat.format(new Date(event.getStart().getDate().getValue())));
                }
            }
            if (ObjectHelper.isNotEmpty(event.getEnd())) {
                if (event.getEnd().getDateTime() != null) {
                    model.setEndDate(dateFormat.format(new Date(event.getEnd().getDateTime().getValue())));
                    model.setEndTime(timeFormat.format(new Date(event.getEnd().getDateTime().getValue())));
                } else {
                    model.setEndDate(dateFormat.format(new Date(event.getEnd().getDate().getValue())));
                }
            }
            if (ObjectHelper.isNotEmpty(event.getLocation())) {
                model.setLocation(event.getLocation());
            }
            if (ObjectHelper.isNotEmpty(event.getId())) {
                model.setEventId(event.getId());
            }
        }

        in.setBody(model);
    }

    private Event createGoogleEvent(String summary, String description, String attendees, String startDate, String startTime, String endDate, String endTime, String location) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Event event = new Event();
        event.setSummary(summary);
        event.setDescription(description);
        event.setAttendees(getAttendeesList(attendees));
        String composedTime;
        if (ObjectHelper.isNotEmpty(startTime)) {
            composedTime = startDate + " " + startTime;
            Date startMidDate = dateFormat.parse(composedTime);
            DateTime start = new DateTime(startMidDate);
            event.setStart(new EventDateTime().setDateTime(start));
        } else {
            composedTime = startDate;
            DateTime startDateTime = new DateTime(composedTime);
            EventDateTime startEventDateTime = new EventDateTime().setDate(startDateTime);
            event.setStart(startEventDateTime);
        }
        if (ObjectHelper.isNotEmpty(endTime)) {
            composedTime = endDate + " " + endTime;
            Date endMidDate = dateFormat.parse(composedTime);
            DateTime end = new DateTime(endMidDate);
            event.setEnd(new EventDateTime().setDateTime(end));
        } else {
            composedTime = endDate;
            DateTime endDateTime = new DateTime(composedTime);
            EventDateTime endEventDateTime = new EventDateTime().setDate(endDateTime);
            event.setEnd(endEventDateTime);
        }
        event.setLocation(location);
        return event;
    }
}
