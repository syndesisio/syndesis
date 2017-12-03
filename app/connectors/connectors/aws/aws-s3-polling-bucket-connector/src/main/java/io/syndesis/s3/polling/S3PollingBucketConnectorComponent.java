/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        super("aws-s3-polling-bucket-connector", componentSchema, "io.syndesis.s3.polling.S3PollingBucketConnectorComponent");
    }
}
