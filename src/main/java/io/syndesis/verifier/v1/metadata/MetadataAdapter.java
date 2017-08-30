package io.syndesis.verifier.v1.metadata;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;

/**
 * Converting metadata to applicable properties is specific to each connector,
 * this adapter converts Camel {@link MetaDataExtension.MetaData} to a Map keyed
 * by action property name with a list of {@link PropertyPair} values that are
 * applicable to for that property. {@link #apply(Map, MetaData)} method will
 * receive all properties that client specified and the retrieved
 * {@link MetaDataExtension.MetaData} from the appropriate Camel
 * {@link MetaDataExtension}.
 */
@FunctionalInterface
public interface MetadataAdapter extends BiFunction<Map<String, Object>, MetaData, Map<String, List<PropertyPair>>> {
    // inherits apply(MetaData) from Function
}