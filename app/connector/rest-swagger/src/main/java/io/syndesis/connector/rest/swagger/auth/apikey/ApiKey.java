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
package io.syndesis.connector.rest.swagger.auth.apikey;

import io.syndesis.connector.rest.swagger.Configuration;
import io.syndesis.connector.rest.swagger.SwaggerProxyComponent;
import io.syndesis.connector.rest.swagger.auth.SetHeader;
import io.syndesis.connector.rest.swagger.auth.SetHttpHeader;
import io.syndesis.integration.component.proxy.Processors;

public final class ApiKey {

    public enum Placement {
        header, query
    }

    private ApiKey() {
        // utility class
    }

    public static void setup(final SwaggerProxyComponent component, final Configuration configuration) {
        final String authParameterName = configuration.stringOption("authenticationParameterName");
        final String authParameterValue = configuration.stringOption("authenticationParameterValue");
        final String authParameterPlacement = configuration.stringOption("authenticationParameterPlacement");

        if ("header".equals(authParameterPlacement)) {
            Processors.addBeforeProducer(component, new SetHttpHeader(authParameterName, authParameterValue));
        } else if ("query".equals(authParameterPlacement)) {
            Processors.addBeforeProducer(component, new SetHeader(authParameterName, authParameterValue));
        }

    }

}
