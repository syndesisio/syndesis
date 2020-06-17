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

import java.util.Map;
import javax.xml.stream.XMLStreamWriter;

import org.apache.camel.RuntimeCamelException;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.staxutils.StaxUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Soap11FaultSoapPayloadConverter extends AbstractFaultSoapPayloadConverter {

    public Soap11FaultSoapPayloadConverter(boolean exceptionMessageCauseEnabled) {
        super(exceptionMessageCauseEnabled);
    }

    @Override
    protected void handleFault(XMLStreamWriter writer, SoapFault fault, SoapVersion soapVersion) {
        try {
            Map<String, String> namespaces = fault.getNamespaces();
            for (Map.Entry<String, String> e : namespaces.entrySet()) {
                writer.writeNamespace(e.getKey(), e.getValue());
            }

            final String soapNamespace = soapVersion.getNamespace();
            writer.writeStartElement(SOAP_PREFIX, "Fault", soapNamespace);

            writer.writeStartElement("faultcode");

            String codeString = fault.getCodeString(getFaultCodePrefix(writer, fault.getFaultCode()), SOAP_PREFIX);

            writer.writeCharacters(codeString);
            writer.writeEndElement();

            writer.writeStartElement("faultstring");
            String lang = fault.getLang();
            if (!StringUtils.isEmpty(lang)) {
                writer.writeAttribute("xml", "http://www.w3.org/XML/1998/namespace", "lang", lang);
            }
            writer.writeCharacters(getFaultMessage(fault));
            writer.writeEndElement();
            prepareStackTrace(soapNamespace, fault);

            if (fault.getRole() != null) {
                writer.writeStartElement("faultactor");
                writer.writeCharacters(fault.getRole());
                writer.writeEndElement();
            }

            if (fault.hasDetails()) {
                Element detail = fault.getDetail();
                writer.writeStartElement("detail");

                Node node = detail.getFirstChild();
                while (node != null) {
                    StaxUtils.writeNode(node, writer, true);
                    node = node.getNextSibling();
                }

                // Details
                writer.writeEndElement();
            }

            // Fault
            writer.writeEndElement();
        } catch (Exception xe) {
            throw new RuntimeCamelException("XML write exception: " + xe.getMessage(), xe);
        }
    }
}
