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

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;

import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.StringHelper;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import static io.syndesis.connector.calendar.GoogleCalendarUtils.formatAtendees;

public class GoogleCalendarEventModel {

    private static final DateTimeFormatter LOCAL_DATE_TIME = new DateTimeFormatterBuilder()
        .appendValue(HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(MINUTE_OF_HOUR, 2)
        .appendLiteral(':')
        .appendValue(SECOND_OF_MINUTE, 2)
        .toFormatter();

    private String title;
    private String description;
    private String attendees;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private String location;
    private String eventId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAttendees() {
        return attendees;
    }

    public void setAttendees(String attendees) {
        this.attendees = attendees;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return "GoogleCalendarEventModel [title=" + title + ", description=" + description + ", attendees=" + attendees + ", startDate=" + startDate
            + ", startTime=" + startTime
            + ", endDate=" + endDate + ", endTime=" + endTime + ", location=" + location + ", eventId=" + eventId + "]";
    }

    public static GoogleCalendarEventModel newFrom(final Event event) {
        final GoogleCalendarEventModel model = new GoogleCalendarEventModel();

        if (event == null) {
            return model;
        }

        model.title = StringHelper.trimToNull(event.getSummary());

        model.description = StringHelper.trimToNull(event.getDescription());

        model.attendees = formatAtendees(event.getAttendees());

        model.setStart(event.getStart());

        model.setEnd(event.getEnd());

        model.location = StringHelper.trimToNull(event.getLocation());

        model.eventId = StringHelper.trimToNull(event.getId());

        return model;
    }

    void setStart(final EventDateTime start) {
        if (start == null) {
            startDate = null;
            startTime = null;
            return;
        }

        // this conversion looses timezone and converts literal without
        // taking into account any timezone changes, this is how it was
        // originally implemented, and the refactor kept the backward
        // compatibility; potential for wrong outcomes is not insignificant
        if (start.getDateTime() != null) {
            final String asRfc3339 = start.getDateTime().toStringRfc3339();
            final TemporalAccessor dateTime = DateTimeFormatter.ISO_DATE_TIME.parse(asRfc3339);

            startDate = DateTimeFormatter.ISO_LOCAL_DATE.format(dateTime);
            startTime = LOCAL_DATE_TIME.format(dateTime);
        } else {
            final String asRfc3339 = start.getDate().toStringRfc3339();
            final TemporalAccessor date = DateTimeFormatter.ISO_DATE.parse(asRfc3339);

            startDate = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
        }
    }

    void setEnd(final EventDateTime end) {
        if (end == null) {
            endDate = null;
            endTime = null;
            return;
        }

        // this conversion looses timezone and converts literal without
        // taking into account any timezone changes, this is how it was
        // originally implemented, and the refactor kept the backward
        // compatibility; potential for wrong outcomes is not insignificant
        if (end.getDateTime() != null) {
            final String asRfc3339 = end.getDateTime().toStringRfc3339();
            TemporalAccessor dateTime = DateTimeFormatter.ISO_DATE_TIME.parse(asRfc3339);

            endDate = DateTimeFormatter.ISO_LOCAL_DATE.format(dateTime);
            endTime = LOCAL_DATE_TIME.format(dateTime);
        } else {
            final String asRfc3339 = end.getDate().toStringRfc3339();
            final TemporalAccessor date = DateTimeFormatter.ISO_DATE.parse(asRfc3339);

            endDate = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
        }
    }

    GoogleCalendarEventModel applyDefaultsFrom(GoogleCalendarEventModel defaults) {
        if (defaults == null) {
            return this;
        }

        GoogleCalendarEventModel model = new GoogleCalendarEventModel();

        model.title = or(title, defaults.title);

        model.description = or(description, defaults.description);

        model.attendees = or(attendees, defaults.attendees);

        model.startDate = or(startDate, defaults.startDate);

        model.startTime = or(startTime, defaults.startTime);

        model.endDate = or(endDate, defaults.endDate);

        model.endTime = or(endTime, defaults.endTime);

        model.location = or(location, defaults.location);

        model.eventId = or(eventId, defaults.eventId);

        return model;
    }

    Event asEvent() {
        final Event event = new Event();
        event.setSummary(title);
        event.setDescription(description);
        event.setAttendees(GoogleCalendarUtils.parseAtendees(attendees));
        event.setStart(getStart());
        event.setEnd(getEnd());
        event.setLocation(location);
        event.setId(eventId);

        return event;
    }

    private EventDateTime getStart() {
        final EventDateTime start = new EventDateTime();

        if (startTime == null) {
            start.setDate(DateTime.parseRfc3339(startDate));
        } else {
            start.setDateTime(DateTime.parseRfc3339(startDate + "T" + startTime));
        }

        return start;
    }

    private EventDateTime getEnd() {
        final EventDateTime end = new EventDateTime();

        if (endTime == null) {
            end.setDate(DateTime.parseRfc3339(endDate));
        } else {
            end.setDateTime(DateTime.parseRfc3339(endDate + "T" + endTime));
        }

        return end;
    }

    private static String or(String first, String second) {
        if (ObjectHelper.isEmpty(first)) {
            return second;
        }

        return first;
    }
}
