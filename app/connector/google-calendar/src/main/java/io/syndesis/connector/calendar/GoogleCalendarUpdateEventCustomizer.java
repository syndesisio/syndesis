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

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.google.calendar.internal.CalendarEventsApiMethod;
import org.apache.camel.component.google.calendar.internal.GoogleCalendarApiCollection;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.lang3.StringUtils;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.common.base.Splitter;

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
        description = (String)options.get("description");
        summary = (String)options.get("summary");
        calendarId = (String)options.get("calendarId");
        eventId = (String)options.get("eventId");
        attendees = (String)options.get("attendees");
        startDate = (String)options.get("startDate");
        endDate = (String)options.get("endDate");
        startTime = (String)options.get("startTime");
        endTime = (String)options.get("endTime");
        location = (String)options.get("location");
        options.put("apiName", GoogleCalendarApiCollection.getCollection().getApiName(CalendarEventsApiMethod.class).getName());
        options.put("methodName", "update");
    }

    @SuppressWarnings("PMD.NPathComplexity")
    private void beforeProducer(Exchange exchange) throws MessagingException, IOException, ParseException {

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
            if (ObjectHelper.isNotEmpty(event.getStartDate()) && ObjectHelper.isNotEmpty(event.getStartTime())) {
                startDate = event.getStartDate();
                startTime = event.getStartTime();
            }
            if (ObjectHelper.isNotEmpty(event.getEndDate()) && ObjectHelper.isNotEmpty(event.getEndTime())) {
                endDate = event.getEndDate();
                endTime = event.getEndTime();
            }
            if (ObjectHelper.isNotEmpty(event.getLocation())) {
                location = event.getLocation();
            }
            if (ObjectHelper.isNotEmpty(event.getEventId())) {
                eventId = event.getEventId();
            }
        }

        in.setHeader("CamelGoogleCalendar.content", createGoogleEvent());
        in.setHeader("CamelGoogleCalendar.eventId", eventId);
        in.setHeader("CamelGoogleCalendar.calendarId", calendarId);
    }

    private void afterProducer(Exchange exchange) throws MessagingException, IOException, ParseException {

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
                model.setStartDate(dateFormat.format(new Date(event.getStart().getDateTime().getValue())));
                model.setStartTime(timeFormat.format(new Date(event.getStart().getDateTime().getValue())));
            }
            if (ObjectHelper.isNotEmpty(event.getEnd())) {
                model.setEndDate(dateFormat.format(new Date(event.getEnd().getDateTime().getValue())));
                model.setEndTime(timeFormat.format(new Date(event.getEnd().getDateTime().getValue())));
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

    private List<EventAttendee> getAttendeesList(String attendeesString) throws AddressException {
        List<String> list = Splitter.on(',').splitToList(attendeesString);
        List<EventAttendee> attendeesList = new ArrayList<EventAttendee>();
        for (String string : list) {
            EventAttendee attendee = new EventAttendee();
            attendee.setEmail(string);
            attendeesList.add(attendee);
        }
        return attendeesList;
    }

    private String getAttendeesString(List<EventAttendee> attendees) throws AddressException {
        String attendeesString;
        List<String> attendeesList = new ArrayList<>();
        for (EventAttendee eventAttendee : attendees) {
            attendeesList.add(eventAttendee.getEmail());
        }
        attendeesString = StringUtils.join(attendeesList, ',');
        return attendeesString;
    }

    private Event createGoogleEvent() throws AddressException, ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Event event = new Event();
        event.setSummary(summary);
        event.setDescription(description);
        event.setAttendees(getAttendeesList(attendees));
        String composedTime = startDate + " " + startTime;
        Date startDate = dateFormat.parse(composedTime);
        DateTime start = new DateTime(startDate);
        event.setStart(new EventDateTime().setDateTime(start));
        composedTime = endDate + " " + endTime;
        Date endDate = dateFormat.parse(composedTime);
        DateTime end = new DateTime(endDate);
        event.setEnd(new EventDateTime().setDateTime(end));
        event.setLocation(location);
        return event;
    }
}
