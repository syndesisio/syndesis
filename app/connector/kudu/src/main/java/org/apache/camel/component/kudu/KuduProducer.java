/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.component.kudu;

import org.apache.camel.util.component.AbstractApiProducer;
import org.apache.camel.component.kudu.internal.KuduApiName;
import org.apache.camel.component.kudu.internal.KuduPropertiesHelper;

/**
 * The Kudu producer.
 */
public class KuduProducer extends AbstractApiProducer<KuduApiName, KuduConfiguration> {

    public KuduProducer(KuduEndpoint endpoint) {
        super(endpoint, KuduPropertiesHelper.getHelper());
    }
}
