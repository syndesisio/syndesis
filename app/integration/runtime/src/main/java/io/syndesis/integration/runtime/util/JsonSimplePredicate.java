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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Predicate;
import org.apache.camel.spi.Language;
import org.apache.camel.spi.Registry;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Predicate which tries to convert a JSON message to a map first before
 * applying
 */
public final class JsonSimplePredicate implements Predicate {
    private static final Pattern SIMPLE_EXPRESSION = Pattern.compile("\\$\\{([^}]+)\\}");

    private static final MapLikeType GENERIC_TYPE = TypeFactory.defaultInstance().constructMapLikeType(HashMap.class, String.class,
        Object.class);

    private static final Logger LOG = LoggerFactory.getLogger(JsonSimplePredicate.class);

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModules(new Jdk8Module());

    private final ObjectMapper mapper;

    private final Predicate ognlPredicate;

    private final Predicate predicate;

    public JsonSimplePredicate(final String expression, final CamelContext context) {
        final Language language = ObjectHelper.notNull(context.resolveLanguage("simple"), "simple language");
        final String ognlExpression = convertSimpleToOGNLForMaps(expression);

        predicate = language.createPredicate(expression);
        ognlPredicate = language.createPredicate(ognlExpression);

        final Registry registry = context.getRegistry();
        final Set<ObjectMapper> mappers = registry.findByType(ObjectMapper.class);

        if (mappers.size() != 1) {
            mapper = MAPPER;
        } else {
            mapper = mappers.iterator().next();
        }
    }

    @Override
    public boolean matches(final Exchange exchange) {
        final Message payload = exchange.getIn();

        try (InputStream stream = payload.getBody(InputStream.class)) {
            if (stream == null) {
                return predicate.matches(exchange);
            }

            // If it is a JSON document, suppose that this is a document which
            // needs to be parsed as JSON, therefore we set a map instead of the
            // string
            final Map<String, Object> json = mapper.readValue(stream, GENERIC_TYPE);

            if (json != null) {
                // Clone the exchange and set the JSON message converted to a Map /
                // List as in message.
                // The intention is that only this predicate acts on the converted
                // value, but the original in-message still continues to carry the
                // same format.
                // The predicated is supposed to be read only with respect to the
                // incoming message.
                final Exchange exchangeForProcessing = ExchangeHelper.createCopy(exchange, true);
                exchangeForProcessing.getIn().setBody(json);

                return ognlPredicate.matches(exchangeForProcessing);
            }
        } catch (final JsonParseException e) {
            LOG.debug("Incoming message is not a json, try to match using simple language");
            // in case the body is not convertible to a map, the json converter
            // may throw an exception we do not need to dump on the logs so log
            // it at trace level.
            LOG.trace("Unable to parse incoming message body as JSON ", e);
        } catch (final IOException e) {
            LOG.warn("Unable to apply simple filter to the given payload");
            LOG.debug("Unable to parse incoming message body as JSON needed for simple filtering", e);
        }

        // if above fails
        return predicate.matches(exchange);
    }

    static String convertSimpleToOGNLForMaps(final String simple) {
        final Matcher matcher = SIMPLE_EXPRESSION.matcher(simple);

        final StringBuffer ognl = new StringBuffer(simple.length() + 5);
        while (matcher.find()) {
            final String expression = toOgnl(matcher);

            matcher.appendReplacement(ognl, "\\$\\{" + expression + "\\}");
        }

        matcher.appendTail(ognl);

        return ognl.toString();
    }

    static String toOgnl(final Matcher matcher) {
        final String expression = matcher.group(1);
        if (!(expression.startsWith("body.") || expression.startsWith("body["))) {
            return expression;
        }

        final StringBuilder ognl = new StringBuilder(expression.length() + 5);
        final char[] chars = expression.toCharArray();
        boolean start = true;
        for (final char ch : chars) {
            if (ch == '.' || ch == '[') {
                if (!start) {
                    ognl.append(']');
                }
                start = false;

                ognl.append('[');
            } else if (ch == '$') {
                ognl.append("\\$");
            } else if (ch != ']') {
                ognl.append(ch);
            }
        }

        if (!start) {
            ognl.append(']');
        }

        return ognl.toString();
    }

}
