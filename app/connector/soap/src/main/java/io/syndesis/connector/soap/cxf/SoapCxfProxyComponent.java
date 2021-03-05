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
package io.syndesis.connector.soap.cxf;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Producer;
import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.camel.component.cxf.CxfEndpointConfigurer;
import org.apache.camel.processor.CatchProcessor;
import org.apache.camel.processor.Pipeline;
import org.apache.camel.processor.TryProcessor;
import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.binding.soap.SoapVersionFactory;

import io.syndesis.connector.soap.cxf.payload.Soap11FaultSoapPayloadConverter;
import io.syndesis.connector.soap.cxf.payload.Soap12FaultSoapPayloadConverter;
import io.syndesis.integration.component.proxy.ComponentDefinition;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyEndpoint;
import io.syndesis.integration.component.proxy.ComponentProxyProducer;

public final class SoapCxfProxyComponent extends ComponentProxyComponent {

    private CxfEndpointConfigurer endpointConfigurer;
    private boolean exceptionMessageCauseEnabled;
    private SoapVersion soapVersion;

    public SoapCxfProxyComponent(final String componentId, final String componentScheme) {
        super(componentId, componentScheme);
    }

    public void setCxfEndpointConfigurer(CxfEndpointConfigurer endpointConfigurer) {
        this.endpointConfigurer = endpointConfigurer;
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) {
        return super.createEndpoint(uri, remaining, parameters);
    }

    @Override
    protected void configureDelegateEndpoint(ComponentDefinition definition, Endpoint endpoint, Map<String, Object> options) {
        super.configureDelegateEndpoint(definition, endpoint, options);
        ((CxfEndpoint)((ComponentProxyEndpoint)endpoint).getEndpoint()).setCxfEndpointConfigurer(endpointConfigurer);
    }

    @Override
    protected void enrichOptions(Map<String, Object> options) {
        // read and remove soap version property
        this.soapVersion = Soap11.getInstance();
        final Object value = options.remove(ComponentProperties.SOAP_VERSION);
        if (value != null) {
            final double versionNumber = Double.parseDouble(value.toString());
            final Iterator<SoapVersion> versions = SoapVersionFactory.getInstance().getVersions();

            while (versions.hasNext()) {
                final SoapVersion version = versions.next();
                if (version.getVersion() == versionNumber) {
                    soapVersion = version;
                    break;
                }
            }
        }

        // read and remove exceptionMessageCauseEnabled
        final Object causeEnabled = options.remove(ComponentProperties.EXCEPTION_MESSAGE_CAUSE_ENABLED);
        if (causeEnabled != null) {
            exceptionMessageCauseEnabled = Boolean.TRUE.equals(Boolean.parseBoolean(causeEnabled.toString()));
        }
    }

    @Override
    protected Endpoint createDelegateEndpoint(ComponentDefinition definition, String scheme,
                                              Map<String, String> options) {

        final Endpoint delegateEndpoint = super.createDelegateEndpoint(definition, scheme, options);

        // wrap the delegate in a proxy that decorates the CXF producer
        final boolean isSoap11 = Soap11.getInstance().equals(soapVersion);
        return new ComponentProxyEndpoint(delegateEndpoint.getEndpointUri(), this, delegateEndpoint) {

            @Override
            public Producer createProducer() throws Exception {
                final CamelContext context = getCamelContext();

                // replace with a try-catch pipeline to handle SOAP faults
                return new ComponentProxyProducer(this, Pipeline.newInstance(context,
                    new TryProcessor(super.createProducer(),
                        Collections.singletonList(new CatchProcessor(Collections.singletonList(SoapFault.class),
                            (isSoap11 ? new Soap11FaultSoapPayloadConverter(exceptionMessageCauseEnabled) :
                                new Soap12FaultSoapPayloadConverter(exceptionMessageCauseEnabled)), null, null)), null)));
            }
        };
    }
}
