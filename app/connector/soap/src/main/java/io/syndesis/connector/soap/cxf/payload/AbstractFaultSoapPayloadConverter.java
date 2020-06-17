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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Message;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.cxf.CxfPayload;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.staxutils.StaxUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AbstractFaultSoapPayloadConverter extends ResponseSoapPayloadConverter {

    private final boolean exceptionMessageCauseEnabled;

    protected AbstractFaultSoapPayloadConverter(boolean exceptionMessageCauseEnabled) {
        this.exceptionMessageCauseEnabled = exceptionMessageCauseEnabled;
    }

    @Override
    public void process(Exchange exchange) {
        final Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
        if (exception instanceof SoapFault) {

            SoapFault soapFault = (SoapFault) exception;
            final Message in = exchange.getIn();

            // get SOAP QNames from CxfPayload headers
            final SoapMessage soapMessage = in.getHeader("CamelCxfMessage", SoapMessage.class);
            final SoapVersion soapVersion = soapMessage.getVersion();

            try {

                // get CxfPayload body
                final CxfPayload<?> cxfPayload = in.getMandatoryBody(CxfPayload.class);

                final OutputStream outputStream = newOutputStream(in, cxfPayload);
                final XMLStreamWriter writer = newXmlStreamWriter(outputStream);

                handleFault(writer, soapFault, soapVersion);
                writer.writeEndDocument();

                final InputStream inputStream = getInputStream(outputStream, writer);

                // set the input stream as the Camel message body
                in.setBody(inputStream);

            } catch (InvalidPayloadException | XMLStreamException | IOException e) {
                throw new RuntimeCamelException("Error parsing CXF Payload: " + e.getMessage(), e);
            }
        }
    }

    protected abstract void handleFault(XMLStreamWriter writer, SoapFault soapFault, SoapVersion soapVersion);

    protected static String getFaultCodePrefix(XMLStreamWriter writer, QName faultCode) throws XMLStreamException {
        String codeNs = faultCode.getNamespaceURI();
        String prefix = null;
        if (codeNs.length() > 0) {
            prefix = faultCode.getPrefix();
            if (!StringUtils.isEmpty(prefix)) {
                String boundNS = writer.getNamespaceContext().getNamespaceURI(prefix);
                if (StringUtils.isEmpty(boundNS)) {
                    writer.writeNamespace(prefix, codeNs);
                } else if (!codeNs.equals(boundNS)) {
                    prefix = null;
                }
            }
            if (StringUtils.isEmpty(prefix)) {
                prefix = StaxUtils.getUniquePrefix(writer, codeNs, true);
            }
        }
        return prefix;
    }

    protected String getFaultMessage(SoapFault fault) {
        if (fault.getMessage() != null) {
            if (exceptionMessageCauseEnabled && fault.getCause() != null
                && fault.getCause().getMessage() != null && !fault.getMessage().equals(fault.getCause().getMessage())) {
                return fault.getMessage() + " Caused by: " + fault.getCause().getMessage();
            }
            return fault.getMessage();
        } else if (exceptionMessageCauseEnabled && fault.getCause() != null) {
            if (fault.getCause().getMessage() != null) {
                return fault.getCause().getMessage();
            }
            return fault.getCause().toString();
        } else {
            return "Fault occurred while processing.";
        }
    }

    protected void prepareStackTrace(String soapNamespace, SoapFault fault) {
        if (exceptionMessageCauseEnabled && fault.getCause() != null) {
            StringBuilder sb = new StringBuilder();
            Throwable throwable = fault.getCause();
            sb.append("Caused by: ")
                .append(throwable.getClass().getCanonicalName())
                .append(": ")
                .append(throwable.getMessage())
                .append('\n')
                .append(org.apache.cxf.message.Message.EXCEPTION_CAUSE_SUFFIX);
            while (throwable != null) {
                for (StackTraceElement ste : throwable.getStackTrace()) {
                    sb.append(ste.getClassName())
                        .append('!')
                        .append(ste.getMethodName())
                        .append('!')
                        .append(ste.getFileName())
                        .append('!')
                        .append(ste.getLineNumber())
                        .append(org.apache.cxf.message.Message.EXCEPTION_CAUSE_SUFFIX);
                }
                throwable = throwable.getCause();
                if (throwable != null) {
                    sb.append("Caused by: ")
                        .append(throwable.getClass().getCanonicalName())
                        .append(" : ")
                        .append(throwable.getMessage())
                        .append(org.apache.cxf.message.Message.EXCEPTION_CAUSE_SUFFIX);
                }
            }
            Element detail = fault.getDetail();
            if (detail == null) {
                Document doc = DOMUtils.getEmptyDocument();
                Element stackTrace = doc.createElementNS(
                    Fault.STACKTRACE_NAMESPACE, Fault.STACKTRACE);
                stackTrace.setTextContent(sb.toString());
                detail = doc.createElementNS(
                    soapNamespace, "detail");
                fault.setDetail(detail);
                detail.appendChild(stackTrace);
            } else {
                Element stackTrace =
                    detail.getOwnerDocument().createElementNS(Fault.STACKTRACE_NAMESPACE,
                        Fault.STACKTRACE);
                stackTrace.setTextContent(sb.toString());
                detail.appendChild(stackTrace);
            }
        }
    }

}
