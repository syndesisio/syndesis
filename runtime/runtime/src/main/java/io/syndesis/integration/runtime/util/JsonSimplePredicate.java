/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Objects;
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

/**
 * Predicate which tries to convert a JSON message to a map first before
 * applying
 */
public class JsonSimplePredicate implements Predicate {

    private final Language language;
    private final String expression;

    public JsonSimplePredicate(String expression, CamelContext context) {
        this.language = context.resolveLanguage("simple");
        this.expression = expression;
        Objects.requireNonNull(language, "The language 'simple' could not be resolved!");
    }

    @Override
    public boolean matches(Exchange exchange) {
        Object msgBody = exchange.getIn().getBody();

        // TODO: Maybe check for content-type, too ?
        // String contentType = exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class);
        // if ("application/json".equals(contentType)) { ... }
        // ???
        if (!(msgBody instanceof String)) {
            return language.createPredicate(expression).matches(exchange);
        }

        Exchange exchangeToCheck = exchange;
        // If it is a json document , suppose that this is a document which needs to be parsed as JSON
        // Therefor we set a map instead of the string
        Map jsonDocument = jsonStringAsMap((String) msgBody, exchange);
        if (jsonDocument != null) {
            // Clone the exchange and set the JSON message converted to a Map / List as in message.
            // The intention is that only this predicate acts on the converted value,
            // but the original in-message still continues to carry the same format
            // The predicated is supposed to be read only with respect to the incoming messaeg.
            exchangeToCheck = ExchangeHelper.createCopy(exchange, true);
            exchangeToCheck.getIn().setBody(jsonDocument);
        }
        return language.createPredicate(convertSimpleToOGNLForMaps(expression)).matches(exchangeToCheck);
    }

    private String convertSimpleToOGNLForMaps(String expression) {
        String[] expressionParts = expression.split("\\s+",2);
        Matcher matcher = Pattern.compile("\\$\\{\\s*body\\.(.*?)\\s*}").matcher(expressionParts[0]);
        if (!matcher.matches()) {
            // Nothing we can convert;
            return expression;
        }
        String path = matcher.group(1);
        StringBuilder ognlExpr = new StringBuilder("${body");
        for (String part : path.split("\\.")) {
            ognlExpr.append("[").append(part).append("]");
        }
        ognlExpr.append("}");
        if (expressionParts.length > 1) {
            ognlExpr.append(" ").append(expressionParts[1]);
        }
        return ognlExpr.toString();
    }

    private Map jsonStringAsMap(String body, Exchange exchange) {
        ObjectMapper mapper = resolveObjectMapper(exchange.getContext().getRegistry());

        // convert JSON string to Map
        try {
            return mapper.readValue(body, new TypeReference<Map<String, Object>>(){});
        } catch (IOException e) {
            // ignore because we are attempting to convert, but its not a JSON document
        }
        return null;
    }

    private ObjectMapper resolveObjectMapper(Registry registry) {
        Set<ObjectMapper> mappers = registry.findByType(ObjectMapper.class);
        if (mappers.size() == 1) {
            return mappers.iterator().next();
        }
        return new ObjectMapper();
    }
}
