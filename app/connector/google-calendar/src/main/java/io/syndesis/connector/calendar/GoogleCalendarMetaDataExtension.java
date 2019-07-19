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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.common.base.Splitter;
import io.syndesis.connector.support.util.ConnectorOptions;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.MetaDataBuilder;
import org.apache.camel.component.google.calendar.BatchGoogleCalendarClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleCalendarMetaDataExtension extends AbstractMetaDataExtension {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleCalendarMetaDataExtension.class);

    GoogleCalendarMetaDataExtension(CamelContext context) {
        super(context);
    }

    @Override
    public Optional<MetaData> meta(Map<String, Object> parameters) {

        String clientId = ConnectorOptions.extractOption(parameters, "clientId");
        String clientSecret = ConnectorOptions.extractOption(parameters, "clientSecret");
        String googleScopes = "https://www.googleapis.com/auth/calendar";
        String applicationName = ConnectorOptions.extractOption(parameters, "applicationName");
        String accessToken = ConnectorOptions.extractOption(parameters, "accessToken");
        String refreshToken = ConnectorOptions.extractOption(parameters, "refreshToken");

        if (clientId != null) {
            try {
                LOG.debug("Retrieving calendars for connection to google calendar");
                Calendar client = new BatchGoogleCalendarClientFactory().makeClient(clientId, clientSecret,
                         getScopes(googleScopes), applicationName, refreshToken, accessToken, null, null, "me");

                CalendarList calendars;
                calendars = client.calendarList().list().execute();

                Set<CalendarListEntry> setCalendars = new HashSet<CalendarListEntry>();
                if (calendars.getItems() != null) {
                   for (CalendarListEntry entry : calendars.getItems()) {
                       setCalendars.add(entry);
                   }
                }

                return Optional
                        .of(MetaDataBuilder.on(getCamelContext()).withAttribute(MetaData.CONTENT_TYPE, "text/plain")
                            .withAttribute(MetaData.JAVA_TYPE, String.class).withPayload(setCalendars).build());
            } catch (Exception e) {
                 throw new IllegalStateException("Get information about calendars has failed.", e);
            }
        } else {
            return Optional.empty();
        }
    }

    private List<String> getScopes(String scopesString) {
       return Splitter.on(',').splitToList(scopesString);
    }
}
