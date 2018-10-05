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
        Event event;
        event = exchange.getIn().getBody(Event.class);
        if (event != null) {

            if (ObjectHelper.isNotEmpty(summary)) {
                event.setSummary(summary);
            }
            if (ObjectHelper.isNotEmpty(description)) {
                event.setDescription(description);
            }
            if (ObjectHelper.isNotEmpty(attendees)) {
                event.setAttendees(getAttendeesList(attendees));
            }
            if (ObjectHelper.isNotEmpty(startDate) && ObjectHelper.isNotEmpty(startTime)) {
                String composedTime = startDate + " " + startTime;
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date date = dateFormat.parse(composedTime);
                DateTime start = new DateTime(date);
                event.setStart(new EventDateTime().setDateTime(start));
            }
            if (ObjectHelper.isNotEmpty(endDate) && ObjectHelper.isNotEmpty(endTime)) {
                String composedTime = endDate + " " + endTime;
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date date = dateFormat.parse(composedTime);
                DateTime end = new DateTime(date);
                event.setEnd(new EventDateTime().setDateTime(end));
            }
            if (ObjectHelper.isNotEmpty(location)) {
                event.setLocation(location);
            }
            if (ObjectHelper.isEmpty(eventId)) {
                eventId = event.getId();
            }
        }

        in.setHeader("CamelGoogleCalendar.content", event);
        in.setHeader("CamelGoogleCalendar.eventId", eventId);
        in.setHeader("CamelGoogleCalendar.calendarId", calendarId);
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
}
