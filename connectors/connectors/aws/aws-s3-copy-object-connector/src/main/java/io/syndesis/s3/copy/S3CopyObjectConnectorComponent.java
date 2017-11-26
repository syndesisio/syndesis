package io.syndesis.s3.copy;

import org.apache.camel.component.connector.DefaultConnectorComponent;

/**
 * Camel S3CopyObjectConnectorComponent connector
 */
public class S3CopyObjectConnectorComponent extends DefaultConnectorComponent {

    public S3CopyObjectConnectorComponent() {
        this(null);
    }

    public S3CopyObjectConnectorComponent(String componentSchema) {
        super("copy-object-connector", componentSchema, "io.syndesis.s3.copy.S3CopyObjectConnectorComponent");
    }
}
