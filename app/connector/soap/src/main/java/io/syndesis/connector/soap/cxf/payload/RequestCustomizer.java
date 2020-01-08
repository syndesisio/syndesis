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
package io.syndesis.connector.soap.cxf.payload;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.binding.soap.SoapVersionFactory;

import io.syndesis.connector.soap.cxf.ComponentProperties;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import io.syndesis.integration.component.proxy.Processors;

public final class RequestCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(final ComponentProxyComponent component, final Map<String, Object> options) {

        final AtomicReference<SoapVersion> soapVersion = new AtomicReference<>(Soap11.getInstance());
        consumeOption(options, ComponentProperties.SOAP_VERSION, value -> {
            final double versionNumber = Double.parseDouble((String) value);
            final Iterator<SoapVersion> versions = SoapVersionFactory.getInstance().getVersions();

            while (versions.hasNext()) {
                final SoapVersion version = versions.next();
                if (version.getVersion() == versionNumber) {
                    soapVersion.set(version);
                    break;
                }
            }
        });

        Processors.addBeforeProducer(component, new RequestSoapPayloadConverter(soapVersion.get()));
    }

}
