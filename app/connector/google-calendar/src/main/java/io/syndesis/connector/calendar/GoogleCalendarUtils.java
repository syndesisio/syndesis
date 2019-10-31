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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.camel.util.ObjectHelper;

import com.google.api.services.calendar.model.EventAttendee;
import com.google.common.base.Splitter;

final class GoogleCalendarUtils {

    private GoogleCalendarUtils() {
        // utility class
    }

    static EventAttendee attendee(final String email) {
        final EventAttendee attendee = new EventAttendee();
        attendee.setEmail(email);

        return attendee;
    }

    static String formatAtendees(final List<EventAttendee> attendees) {
        if (attendees == null || attendees.isEmpty()) {
            return null;
        }

        return attendees.stream()
            .map(EventAttendee::getEmail)
            .collect(Collectors.joining(","));
    }

    static List<EventAttendee> parseAtendees(final String attendeesString) {
        if (ObjectHelper.isEmpty(attendeesString)) {
            return Collections.emptyList();
        }

        return Splitter.on(',').trimResults().splitToList(attendeesString)
            .stream()
            .map(GoogleCalendarUtils::attendee)
            .collect(Collectors.toList());
    }
}
