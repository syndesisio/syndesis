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
package io.syndesis.integration.runtime.logging;

import java.util.Arrays;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.language.simple.SimpleLanguage;
import org.apache.camel.util.ObjectHelper;

/**
 * Logger used to log body data in integration activity logging.
 */
@FunctionalInterface
public interface BodyLogger {
    String log(Exchange exchange);

    /**
     * Default implementation that logs body data. The default logger automatically logs arrays and lists
     * in readable format even if objects overwrite {@link Object#toString()}. This is required when grouped body aggregation
     * exchanges are logged because these exchanges do overwrite {@link Object#toString()}
     * in {@link org.apache.camel.processor.aggregate.AbstractListAggregationStrategy}
     *
     * Other body types are delegated to simple language "${body}" expression for logging the body as String.
     */
    class Default implements BodyLogger {
        @Override
        public String log(Exchange exchange) {
            if (ObjectHelper.isNotEmpty(exchange.getIn().getBody())) {
                if (exchange.getIn().getBody(List.class) != null) {
                    return Arrays.toString(exchange.getIn().getBody(List.class).toArray());
                } else if (exchange.getIn().getBody(Object[].class) != null) {
                    return Arrays.toString(exchange.getIn().getBody(Object[].class));
                }
            }

            return SimpleLanguage.expression("${body}")
                                 .evaluate(exchange, String.class);
        }
    }
}
