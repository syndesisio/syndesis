package io.syndesis.verifier.v1.metadata;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.camel.component.extension.MetaDataExtension.MetaData;

@FunctionalInterface
public interface MetadataAdapter extends BiFunction<Map<String, Object>, MetaData, Map<String, List<PropertyPair>>> {
    // inherits apply(MetaData) from Function
}