package io.syndesis.s3.copy;

import org.apache.camel.component.connector.DefaultConnectorComponent;

/**
 * Camel S3CopyObjectConnectorComponent connector
 */
public class S3CopyObjectConnectorComponent extends DefaultConnectorComponent {
    
    public S3CopyObjectConnectorComponent() {
        super("aws-s3-copy-object-connector", "io.syndesis.s3.copy.S3CopyObjectConnectorComponent");
    }

}
