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

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.google.api.services.calendar.model.Event;

import static io.syndesis.connector.calendar.GoogleCalendarUtils.attendee;
import static io.syndesis.connector.calendar.TestHelper.date;
import static io.syndesis.connector.calendar.TestHelper.dateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class GoogleCalendarEventModelTest {

    @Test
    public void createsConnectorModelFromTrivialGoogleModel() {
        final Event googleModel = new Event();

        final GoogleCalendarEventModel eventModel = GoogleCalendarEventModel.newFrom(googleModel);

        assertThat(eventModel).isEqualToComparingFieldByField(new GoogleCalendarEventModel());
    }

    @Test
    public void createsConnectorModelFromGoogleModel() {
        final Event googleModel = new Event();
        googleModel.setSummary("summary");
        googleModel.setDescription("description");
        googleModel.setAttendees(Arrays.asList(attendee("a1@mail.com"), attendee("a2@mail.com")));
        googleModel.setStart(dateTime("2018-05-18T15:30:00+02:00"));
        googleModel.setEnd(dateTime("2018-05-18T16:30:00+02:00"));
        googleModel.setLocation("location");
        googleModel.setId("eventId");

        final GoogleCalendarEventModel eventModel = GoogleCalendarEventModel.newFrom(googleModel);

        final GoogleCalendarEventModel expected = new GoogleCalendarEventModel();
        expected.setTitle("summary");
        expected.setDescription("description");
        expected.setAttendees("a1@mail.com,a2@mail.com");
        expected.setStartDate("2018-05-18");
        expected.setStartTime("15:30:00");
        expected.setEndDate("2018-05-18");
        expected.setEndTime("16:30:00");
        expected.setLocation("location");
        expected.setEventId("eventId");

        assertThat(eventModel).isEqualToComparingFieldByField(expected);
        googleModel.setStart(dateTime("2018-05-18T15:30:00.000Z"));
        googleModel.setEnd(dateTime("2018-05-18T16:30:00.000Z"));
        assertThat(eventModel.asEvent()).isEqualTo(googleModel);
    }

    @Test
    public void shouldSetStartDate() {
        GoogleCalendarEventModel eventModel = new GoogleCalendarEventModel();

        eventModel.setStart(date("2000-01-01"));

        assertThat(eventModel.getStartDate()).isEqualTo("2000-01-01");
        assertThat(eventModel.getStartTime()).isNull();
    }

    @Test
    public void shouldSetStartDateTime() {
        GoogleCalendarEventModel eventModel = new GoogleCalendarEventModel();

        eventModel.setStart(dateTime("2018-05-18T15:30:00+02:00"));

        assertThat(eventModel.getStartDate()).isEqualTo("2018-05-18");
        assertThat(eventModel.getStartTime()).isEqualTo("15:30:00");
    }

    @Test
    public void shouldSetEndDate() {
        GoogleCalendarEventModel eventModel = new GoogleCalendarEventModel();

        eventModel.setEnd(date("2000-01-01"));

        assertThat(eventModel.getEndDate()).isEqualTo("2000-01-01");
        assertThat(eventModel.getEndTime()).isNull();
    }

    @Test
    public void shouldSetEndDateTime() {
        GoogleCalendarEventModel eventModel = new GoogleCalendarEventModel();

        eventModel.setEnd(dateTime("2018-05-18T15:30:00+02:00"));

        assertThat(eventModel.getEndDate()).isEqualTo("2018-05-18");
        assertThat(eventModel.getEndTime()).isEqualTo("15:30:00");
    }

    @Test
    public void newModelFromNullIsValid() {
        final GoogleCalendarEventModel eventModel = GoogleCalendarEventModel.newFrom(null);

        assertThat(eventModel).isEqualToComparingFieldByField(new GoogleCalendarEventModel());
    }
}
