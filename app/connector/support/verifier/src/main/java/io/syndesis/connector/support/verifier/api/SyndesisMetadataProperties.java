package io.syndesis.connector.support.verifier.api;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SyndesisMetadataProperties {

    public static final SyndesisMetadataProperties EMPTY = new SyndesisMetadataProperties(Collections.emptyMap());

    /**
     * A Map keyed by action property name with a list of {@link PropertyPair}
     * values that are applicable to for that property.
     */
    protected final Map<String, List<PropertyPair>> properties;

    public SyndesisMetadataProperties(Map<String, List<PropertyPair>> properties) {
        this.properties = properties;

        if (properties != null && !properties.isEmpty()) {
            for (final List<PropertyPair> propertyPairs : properties.values()) {
                Collections.sort(propertyPairs, Comparator.comparing(PropertyPair::getDisplayValue));
            }
        }
    }

    public Map<String, List<PropertyPair>> getProperties() {
        return properties;
    }
}
