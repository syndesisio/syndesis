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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.syndesis.common.util.IOStreams;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Predicate;
import org.apache.camel.language.bean.RuntimeBeanExpressionException;
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
        // Clone the exchange and set the JSON message converted to a Map /
        // List as in message.
        // The intention is that only this predicate acts on the converted
        // value, but the original in-message still continues to carry the
        // same format.
        // The predicated is supposed to be read only with respect to the
        // incoming message.
        final Exchange exchangeForProcessing = ExchangeHelper.createCopy(exchange, true);
        final Message payload = exchangeForProcessing.getIn();

        if (payload.getBody() instanceof List) {
            List<?> jsonBeans = payload.getBody(List.class);
            payload.setBody("[" + jsonBeans.stream().map(Object::toString).collect(Collectors.joining(",")) + "]");
        }

        try (InputStream stream = payload.getBody(InputStream.class)) {
            if (stream == null) {
                return predicate.matches(exchange);
            }

            ResetAfterCloseInputStream resetAfterCloseInputStream = new ResetAfterCloseInputStream(stream);
            if (exchange.getIn().getBody() instanceof InputStream) {
                exchange.getIn().setBody(resetAfterCloseInputStream);
            }

            // If it is a JSON document, suppose that this is a document which
            // needs to be parsed as JSON, therefore we set a map instead of the
            // string
            final JsonNode json = mapper.readTree(resetAfterCloseInputStream);

            if (json != null) {
                if (json.isArray()) {
                    payload.setBody(mapper.convertValue(json, List.class));
                    try {
                        return ognlPredicate.matches(exchangeForProcessing);
                    } catch (RuntimeBeanExpressionException e) {
                        if (Optional.ofNullable(e.getCause())
                                    .map(Object::getClass)
                                    .map(IndexOutOfBoundsException.class::equals)
                                    .orElse(false)) {
                            LOG.debug("Try to match array item out of bounds");
                            // we do not need to dump on the logs so log it at trace level.
                            LOG.trace("Try to match array item out of bounds ", e);
                            return false;
                        } else {
                            throw e;
                        }
                    }
                } else if (json.isObject()) {
                    payload.setBody(mapper.convertValue(json, Map.class));
                    return ognlPredicate.matches(exchangeForProcessing);
                }
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
        if (!(expression.startsWith("body.") || expression.startsWith("body[")) || isCollectionPath(expression)) {
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

    private static boolean isCollectionPath(String expression) {
        String[] collectionPaths = new String[] { "size()" };

        return Stream.of(collectionPaths)
                .map(path -> "body." + path)
                .anyMatch(path -> path.equals(expression));
    }

    /**
     * Input stream being able to reset on close in order to ensure exchange processing can consume stream content
     * one more time.
     */
    private static class ResetAfterCloseInputStream extends InputStream {
        private final byte[] sourceBytes;
        private ByteArrayInputStream delegate;

        ResetAfterCloseInputStream(InputStream inputStream) throws IOException {
            sourceBytes = IOStreams.readBytes(inputStream);
            delegate = new ByteArrayInputStream(sourceBytes);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
            delegate = new ByteArrayInputStream(sourceBytes);
        }

        @Override
        public int read() {
            return delegate.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return delegate.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }
    }
}
