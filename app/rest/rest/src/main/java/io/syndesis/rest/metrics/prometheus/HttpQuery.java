package io.syndesis.rest.metrics.prometheus;

import java.util.List;
import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.immutables.value.Value;

/**
 * Prometheus HTTP Query
 */
@Value.Immutable
@SuppressWarnings("immutables")
public interface HttpQuery {

    class Builder extends ImmutableHttpQuery.Builder {
        // make ImmutableHttpQuery.Builder accessible
    }

    @Value.Immutable
    @JsonDeserialize(builder = HttpQuery.LabelValue.Builder.class)
    interface LabelValue {

        @SuppressWarnings("PMD.UseUtilityClass")
        class Builder extends ImmutableLabelValue.Builder {
            public static HttpQuery.LabelValue of(final String value, final String label) {
                return new Builder().label(label).value(value).build();
            }
        }

        String getLabel();

        String getValue();
    }

    String getHost();

    String getMetric();

    List<LabelValue> getLabelValues();

    default UriBuilder getUriBuilder() {
        StringBuilder queryExpression = new StringBuilder();
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
                queryExpression.append(label.getLabel());
                queryExpression.append('=');
                queryExpression.append('"');
                queryExpression.append(label.getValue());
                queryExpression.append('"');
            }
            queryExpression.append("%7D");
        }

        return UriBuilder.fromPath(String.format("http://%s/api/v1/query", getHost()))
            .queryParam("query", queryExpression.toString());
    }

}
