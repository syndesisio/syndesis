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
package io.syndesis.connector.calendar.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.api.services.calendar.model.EventAttendee;
import com.google.common.base.Splitter;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.lang3.StringUtils;

public final class GoogleCalendarUtils {

    private GoogleCalendarUtils() {
    }

    public static List<EventAttendee> getAttendeesList(String attendeesString) {
        List<EventAttendee> attendeesList = new ArrayList<>();
        if (ObjectHelper.isNotEmpty(attendeesString)) {
            List<String> list = Splitter.on(',').trimResults().splitToList(attendeesString);
            for (String string : list) {
                EventAttendee attendee = new EventAttendee();
                attendee.setEmail(string);
                attendeesList.add(attendee);
            }
        }
        return attendeesList;
    }

    public static String getAttendeesString(List<EventAttendee> attendees) {
        String attendeesString;
        List<String> attendeesList = new ArrayList<>();
        for (EventAttendee eventAttendee : attendees) {
            attendeesList.add(eventAttendee.getEmail());
        }
        attendeesString = StringUtils.join(attendeesList, ',');
        return attendeesString;
    }
}
