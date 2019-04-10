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
package io.syndesis.server.metrics.prometheus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.UriBuilder;

import org.immutables.value.Value;

/**
 * Prometheus HTTP Query
 */
@Value.Immutable
@SuppressWarnings("immutables")
public interface HttpQuery {

    class Builder extends ImmutableHttpQuery.Builder {
        // make ImmutableHttpQuery.Builder accessible

        public Builder addLabelValues(String value, String label) {
            addLabelValues(LabelValue.Builder.of(value, label));
            return this;
        }
    }

    @Value.Immutable
    interface LabelValue {

        @SuppressWarnings("PMD.UseUtilityClass")
        class Builder extends ImmutableLabelValue.Builder {
            public static HttpQuery.LabelValue of(final String value, final String label) {
                return new Builder().label(label).value(value).build();
            }
        }

        default void appendTo(StringBuilder builder) {
            builder.append(getLabel()).append("=\"").append(getValue()).append('"');
        }

        String getLabel();

        String getValue();
    }

    String getHost();

    Optional<String> getFunction();

    Optional<String> getAggregationOperator();

    List<String> getAggregationOperatorParameters();

    String getMetric();

    List<LabelValue> getLabelValues();

    Optional<String> getRange();

    List<String> getWithoutLabels();

    List<String> getByLabels();

    @SuppressWarnings({ "PMD.NPathComplexity", "OrphanedFormatString" })
    default UriBuilder getUriBuilder() {
        StringBuilder queryExpression = new StringBuilder();

        // is there an aggregation operator?
        boolean closeAggregation = false;
        final String aggregationOperator = getAggregationOperator().orElse(null);
        if (aggregationOperator != null && !aggregationOperator.isEmpty()) {
            queryExpression.append(aggregationOperator).append('(');
            closeAggregation = true;
        }

        // are there aggregation operator parameters?
        if (closeAggregation) {
            final List<String> params = getAggregationOperatorParameters();
            if (params != null && !params.isEmpty()) {
                for (String param : params) {
                    queryExpression.append(param);
                    queryExpression.append(',');
                }
            }
        }

        // is there a query function?
        final String function = getFunction().orElse(null);
        boolean closeFunction = false;
        if (function != null && !function.isEmpty()) {
            queryExpression.append(function).append('(');
            closeFunction = true;
        }

        queryExpression.append(getMetric());
        if (!getLabelValues().isEmpty()) {
            queryExpression.append("%7B");
            boolean first = true;
            for (LabelValue label : getLabelValues()) {
                if (first) {
                    first = false;
                } else {
                    queryExpression.append(',');
                }
                label.appendTo(queryExpression);
            }
            queryExpression.append("%7D");
        }

        // is there a range?
        final String range = getRange().orElse(null);
        if (range != null && !range.isEmpty()) {
            queryExpression.append('[').append(range).append(']');
        }

        // close function?
        if (closeFunction) {
            queryExpression.append(')');
        }

        // close aggregation operator?
        if (closeAggregation) {

            queryExpression.append(')');

            final String withoutLabels = getWithoutLabels().stream().collect(Collectors.joining(","));
            if (!withoutLabels.isEmpty()) {
                queryExpression.append(" without (").append(withoutLabels).append(')');
            }
            final String byLabels = getByLabels().stream().collect(Collectors.joining(","));
            if (!byLabels.isEmpty()) {
                queryExpression.append(" by (").append(byLabels).append(')');
            }
        }

        return UriBuilder.fromPath(String.format("http://%s/api/v1/query", getHost()))
            .queryParam("query", queryExpression.toString());
    }

}
