package io.syndesis.verifier.v1.metadata;

import java.util.Map;

import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;

/**
 * Converting metadata from Camel components to applicable properties or
 * generating ObjectSchema from Metadata is specific to each connector. This
 * adapter bridges Camel {@link MetaDataExtension} Component specific
 * implementations to common Syndesis data model.
 */
public interface MetadataAdapter<T> {

    /**
     * Converts Camel {@link MetaDataExtension.MetaData} to
     * {@link SyndesisMetadata}. Method will receive all properties that client
     * specified and the retrieved {@link MetaDataExtension.MetaData} from the
     * appropriate Camel {@link MetaDataExtension}.
     *
     * @param properties properties specified on the endpoint
     * @param metadata the retrieved metadata
     * @return Syndesis styled metadata
     */
    SyndesisMetadata<T> adapt(Map<String, Object> properties, MetaData metadata);

}