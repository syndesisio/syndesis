package io.syndesis.rest.metrics.prometheus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

/**
 * Catch all for utility methods.
 *
 * @author dhirajsb
 */
public final class Utils {

    private static final ObjectReader OBJECT_READER;

    static {
        ObjectMapper objectMapper = new ObjectMapper()
            .registerModules(new Jdk8Module())
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
            .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
            .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE)
            .setDateFormat(new ISO8601DateFormat());
        OBJECT_READER = objectMapper.reader();
    }

    private Utils() {
    }

    public static ObjectReader getObjectReader() {
        return OBJECT_READER;
    }
}
