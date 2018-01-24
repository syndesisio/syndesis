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
package io.syndesis.integration.runtime.util;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.spi.Language;
import org.apache.camel.spi.Registry;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.ObjectHelper;

/**
 * Predicate which tries to convert a JSON message to a map first before
 * applying
 */
public class JsonSimplePredicate implements Predicate {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Predicate predicate;
    private final Predicate ognlPredicate;

    public JsonSimplePredicate(String expression, CamelContext context) {
        Language language = ObjectHelper.notNull(context.resolveLanguage("simple"), "simple language");
        String ognlExpression = convertSimpleToOGNLForMaps(expression);

        this.predicate = language.createPredicate(expression);
        this.ognlPredicate = language.createPredicate(ognlExpression);
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    @Override
    public boolean matches(Exchange exchange) {
        Object msgBody = exchange.getIn().getBody();
        Predicate predicate = this.predicate;

        // TODO: Maybe check for content-type, too ?
        // String contentType = exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class);
        // if ("application/json".equals(contentType)) { ... }
        // ???
        if (msgBody instanceof String) {
            // If it is a json document , suppose that this is a document which needs to be parsed as JSON
            // Therefor we set a map instead of the string
            Map<String, Object> jsonDocument = jsonStringAsMap((String) msgBody, exchange);
            if (jsonDocument != null) {
                // Clone the exchange and set the JSON message converted to a Map / List as in message.
                // The intention is that only this predicate acts on the converted value,
                // but the original in-message still continues to carry the same format
                // The predicated is supposed to be read only with respect to the incoming messaeg.
                exchange = ExchangeHelper.createCopy(exchange, true);
                exchange.getIn().setBody(jsonDocument);

                predicate = this.ognlPredicate;
            }
        }

        return predicate.matches(exchange);
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    public static String convertSimpleToOGNLForMaps(String input) {
        final String regexp = "(?<=\\$\\{body).*?(\\.[\\w]+)\\.?([^\\}]*?)";
        final Pattern pattern = Pattern.compile(regexp);

        Matcher m = pattern.matcher(input);
        while (m.find()) {
            input = input.substring(0, m.start(1)) + '[' + input.substring(m.start(1)+1, m.end(1)) + ']' + input.substring(m.end(1));
            m = pattern.matcher(input);
        }

        return input;
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    private static Map<String, Object> jsonStringAsMap(String body, Exchange exchange) {
        ObjectMapper mapper = resolveObjectMapper(exchange.getContext().getRegistry());

        // convert JSON string to Map
        try {
            return mapper.readValue(body, new TypeReference<Map<String, Object>>(){});
        } catch (IOException e) {
            // ignore because we are attempting to convert, but its not a JSON document
        }
        return null;
    }

    private static ObjectMapper resolveObjectMapper(Registry registry) {
        Set<ObjectMapper> mappers = registry.findByType(ObjectMapper.class);
        if (mappers.size() == 1) {
            return mappers.iterator().next();
        }

        return MAPPER;
    }
}
