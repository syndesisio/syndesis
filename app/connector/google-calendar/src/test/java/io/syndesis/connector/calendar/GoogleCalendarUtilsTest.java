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
import java.util.Collections;

import org.junit.jupiter.api.Test;

import static io.syndesis.connector.calendar.GoogleCalendarUtils.attendee;

import static org.assertj.core.api.Assertions.assertThat;

public class GoogleCalendarUtilsTest {

    @Test
    public void shouldFormatEmptyAtendees() {
        assertThat(GoogleCalendarUtils.formatAtendees(Collections.emptyList())).isNull();
    }

    @Test
    public void shouldFormatNullAtendees() {
        assertThat(GoogleCalendarUtils.formatAtendees(null)).isNull();
    }

    @Test
    public void shouldFormatOneAtendee() {
        assertThat(GoogleCalendarUtils.formatAtendees(Collections.singletonList(attendee("a@mail.com")))).isEqualTo("a@mail.com");
    }

    @Test
    public void shouldFormatSeveralAtendees() {
        assertThat(GoogleCalendarUtils.formatAtendees(Arrays.asList(attendee("a@mail.com"), attendee("b@mail.com"), attendee("c@mail.com"))))
            .isEqualTo("a@mail.com,b@mail.com,c@mail.com");
    }

    @Test
    public void shouldParseBlankAtendees() {
        assertThat(GoogleCalendarUtils.parseAtendees(" ")).isEmpty();
    }

    @Test
    public void shouldParseEmptyAtendees() {
        assertThat(GoogleCalendarUtils.parseAtendees("")).isEmpty();
    }

    @Test
    public void shouldParseNullAtendees() {
        assertThat(GoogleCalendarUtils.parseAtendees(null)).isEmpty();
    }

    @Test
    public void shouldParseOneAtendee() {
        assertThat(GoogleCalendarUtils.parseAtendees("a@mail.com")).containsExactly(attendee("a@mail.com"));
    }

    @Test
    public void shouldParseSeveralAtendee() {
        assertThat(GoogleCalendarUtils.parseAtendees("a@mail.com, b@mail.com,  c@mail.com "))
            .containsExactly(attendee("a@mail.com"), attendee("b@mail.com"), attendee("c@mail.com"));
    }
}
