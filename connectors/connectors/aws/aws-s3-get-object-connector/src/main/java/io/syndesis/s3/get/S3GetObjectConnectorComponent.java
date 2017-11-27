package io.syndesis.s3.get;

import org.apache.camel.component.connector.DefaultConnectorComponent;

/**
 * Camel S3GetObjectConnectorComponent connector
 */
public class S3GetObjectConnectorComponent extends DefaultConnectorComponent {

    public S3GetObjectConnectorComponent() {
        this(null);
    }

    public S3GetObjectConnectorComponent(String componentSchema) {
        super("get-object-connector", componentSchema, "io.syndesis.s3.get.S3GetObjectConnectorComponent");
    }
}
