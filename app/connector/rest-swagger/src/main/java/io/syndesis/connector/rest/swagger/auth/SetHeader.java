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
package io.syndesis.connector.rest.swagger.auth;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class SetHeader implements Processor {

    final String headerName;

    final String headerValue;

    public SetHeader(final String headerName, final String headerValue) {
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    @Override
    public void process(final Exchange exchange) throws Exception {
        final Message in = exchange.getIn();
        in.setHeader(headerName, headerValue);
    }

}
