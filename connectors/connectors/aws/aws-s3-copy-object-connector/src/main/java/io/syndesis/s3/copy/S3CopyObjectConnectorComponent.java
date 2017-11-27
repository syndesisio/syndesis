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
        super("aws-s3-copy-object-connector", componentSchema, "io.syndesis.s3.copy.S3CopyObjectConnectorComponent");
    }
}
