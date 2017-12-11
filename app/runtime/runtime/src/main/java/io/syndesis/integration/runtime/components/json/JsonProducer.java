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
package io.syndesis.integration.runtime.components.json;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;

/**
 */
public class JsonProducer extends DefaultProducer {
    public JsonProducer(JsonEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        JsonEndpoint endpoint = getEndpoint();
        endpoint.jsonMarshalIfRequired(exchange);
    }

    @Override
    public JsonEndpoint getEndpoint() {
        return (JsonEndpoint) super.getEndpoint();
    }
}

