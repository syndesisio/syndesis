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
package kudu.component;

import kudu.component.internal.KuduApiName;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriParam;
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

    @UriParam
    private String kuduMasters = System.getProperty("kuduMasters", "quickstart.cloudera:7051");


    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public KuduApiName getApiName() {
        return apiName;
    }

    public void setApiName(KuduApiName apiName) {
        this.apiName = apiName;
    }

    public void setkuduMasters(String kuduMasters) {
        this.kuduMasters = kuduMasters;
    }

    public String getKuduMasters() {
        return kuduMasters;
    }

    public void setKuduMasters(String kuduMasters) {
        this.kuduMasters = kuduMasters;
    }


}
