package io.syndesis.verifier.v1;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

import io.syndesis.verifier.v1.metadata.MetadataAdapter;
import io.syndesis.verifier.v1.metadata.PropertyPair;
import io.syndesis.verifier.v1.metadata.SyndesisMetadata;

import org.apache.camel.component.extension.MetaDataExtension.MetaData;

import static org.assertj.core.api.Assertions.assertThat;

public class PetstoreAdapter implements MetadataAdapter<ObjectSchema> {

    private final Map<String, List<PropertyPair>> adaptedProperties;

    private final Map<String, String> expectedPayload;

    private final ObjectSchema inputSchema;

    private final ObjectSchema outputSchema;

    public PetstoreAdapter(final Map<String, String> expectedPayload,
        final Map<String, List<PropertyPair>> adaptedProperties, final ObjectSchema inputSchema,
        final ObjectSchema outputSchema) {
        this.adaptedProperties = adaptedProperties;
        this.expectedPayload = expectedPayload;
        this.inputSchema = inputSchema;
        this.outputSchema = outputSchema;
    }

    @Override
    public SyndesisMetadata<ObjectSchema> adapt(final Map<String, Object> properties, final MetaData metadata) {
        @SuppressWarnings("unchecked")
        final Map<String, String> payload = metadata.getPayload(Map.class);

        assertThat(payload).isSameAs(expectedPayload);

        return new SyndesisMetadata<>(adaptedProperties, inputSchema, outputSchema);
    }

}