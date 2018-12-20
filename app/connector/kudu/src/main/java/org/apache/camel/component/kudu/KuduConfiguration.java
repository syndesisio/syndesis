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

import org.apache.camel.component.kudu.internal.KuduApiName;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriParams;
import org.apache.camel.spi.UriPath;

/**
 * Component configuration for Kudu component.
 */
@UriParams
public class KuduConfiguration {

    @UriPath
    @Metadata(required = "true")
    private KuduApiName apiName;

    @UriPath
    @Metadata(required = "true")
    private String methodName;

    private String kuduMasters = System.getProperty("kuduMasters", "quickstart.cloudera:7051");

    /**
     * What sub operation to use for the selected operation
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    /**
     * What kind of operation to perform
     */
    public void setApiName(KuduApiName apiName) {
        this.apiName = apiName;
    }

    public KuduApiName getApiName() {
        return apiName;
    }

    /**
     * Server list to connect
     */
    public void setkuduMasters(String kuduMasters) {
        this.kuduMasters = kuduMasters;
    }

    public String getKuduMasters() {
        return kuduMasters;
    }
}
