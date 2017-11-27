package io.syndesis.s3.polling;

import org.apache.camel.component.connector.DefaultConnectorComponent;

/**
 * Camel S3GetObjectConnectorComponent connector
 */
public class S3PollingBucketConnectorComponent extends DefaultConnectorComponent {

    public S3PollingBucketConnectorComponent() {
        this(null);
    }

    public S3PollingBucketConnectorComponent(String componentSchema) {
        super("polling-bucket-connector", componentSchema, "io.syndesis.s3.polling.S3PollingBucketConnectorComponent");
    }
}
