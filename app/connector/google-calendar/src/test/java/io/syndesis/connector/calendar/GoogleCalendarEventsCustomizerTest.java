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

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.junit.jupiter.api.Test;

import com.google.api.services.calendar.model.Event;

import static org.assertj.core.api.Assertions.assertThat;

public class GoogleCalendarEventsCustomizerTest {

    @Test
    public void shouldConvertGoogleEventToConnectorEventModel() {
        Exchange exchange = new DefaultExchange((CamelContext) null);
        Message in = new DefaultMessage(null);
        in.setBody(new Event());
        exchange.setIn(in);

        GoogleCalendarEventsCustomizer.beforeConsumer(exchange);

        Object body = in.getBody();

        assertThat(body).isInstanceOf(GoogleCalendarEventModel.class);
    }
}
