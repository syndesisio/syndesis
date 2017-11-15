package io.syndesis.s3.get;

import org.apache.camel.component.connector.DefaultConnectorComponent;

/**
 * Camel S3GetObjectConnectorComponent connector
 */
public class S3GetObjectConnectorComponent extends DefaultConnectorComponent {
    
    public S3GetObjectConnectorComponent() {
        super("aws-s3-get-object-connector", "io.syndesis.s3.get.S3GetObjectConnectorComponent");
    }

}
