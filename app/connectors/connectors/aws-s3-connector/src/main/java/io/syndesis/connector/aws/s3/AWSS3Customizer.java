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
package io.syndesis.connector.aws.s3;

import java.util.Map;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.util.ObjectHelper;

public class AWSS3Customizer implements CamelContextAware, ComponentProxyCustomizer {
    private CamelContext camelContext;

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        final String accessKey = (String)options.get("accessKey");
        final String secretKey = (String)options.get("secretKey");

        try {
            if (accessKey != null) {
                options.put("accessKey", "RAW(" + camelContext.resolvePropertyPlaceholders(accessKey) + ")");
            }
            if (secretKey != null) {
                options.put("secretKey", "RAW(" + camelContext.resolvePropertyPlaceholders(secretKey) + ")");
            }
        } catch (Exception e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }
    }
}
