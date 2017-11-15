package io.syndesis.s3.polling;

import org.apache.camel.component.connector.DefaultConnectorComponent;

/**
 * Camel S3GetObjectConnectorComponent connector
 */
public class S3PollingBucketConnectorComponent extends DefaultConnectorComponent {
    
    public S3PollingBucketConnectorComponent() {
        super("aws-s3-polling-bucket-connector", "io.syndesis.s3.polling.S3PollingBucketConnectorComponent");
    }

}
