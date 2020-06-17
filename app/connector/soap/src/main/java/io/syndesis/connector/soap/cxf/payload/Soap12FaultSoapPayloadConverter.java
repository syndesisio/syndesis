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

import java.util.Locale;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.apache.camel.RuntimeCamelException;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.staxutils.StaxUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Soap12FaultSoapPayloadConverter extends AbstractFaultSoapPayloadConverter {

    public Soap12FaultSoapPayloadConverter(boolean exceptionMessageCauseEnabled) {
        super(exceptionMessageCauseEnabled);
    }

    @Override
    protected void handleFault(XMLStreamWriter writer, SoapFault soapFault, SoapVersion soapVersion) {
        try {
            Map<String, String> namespaces = soapFault.getNamespaces();
            for (Map.Entry<String, String> e : namespaces.entrySet()) {
                writer.writeNamespace(e.getKey(), e.getValue());
            }

            String ns = soapVersion.getNamespace();
            writer.writeStartElement(SOAP_PREFIX, "Fault", ns);

            writer.writeStartElement(SOAP_PREFIX, "Code", ns);
            writer.writeStartElement(SOAP_PREFIX, "Value", ns);

            writer.writeCharacters(soapFault.getCodeString(getFaultCodePrefix(writer, soapFault.getFaultCode()), SOAP_PREFIX));
            writer.writeEndElement();

            if (soapFault.getSubCodes() != null) {
                int fscCount = 0;
                for (QName fsc : soapFault.getSubCodes()) {
                    writer.writeStartElement(SOAP_PREFIX, "Subcode", ns);
                    writer.writeStartElement(SOAP_PREFIX, "Value", ns);
                    writer.writeCharacters(getCodeString(getFaultCodePrefix(writer, fsc), SOAP_PREFIX, fsc));
                    writer.writeEndElement();
                    fscCount++;
                }
                while (fscCount > 0) {
                    writer.writeEndElement();
                    fscCount--;
                }
            }
            writer.writeEndElement();

            writer.writeStartElement(SOAP_PREFIX, "Reason", ns);
            writer.writeStartElement(SOAP_PREFIX, "Text", ns);
            String lang = soapFault.getLang();
            if (StringUtils.isEmpty(lang)) {
                lang = getLangCode();
            }
            writer.writeAttribute("xml", "http://www.w3.org/XML/1998/namespace", "lang", lang);
            writer.writeCharacters(getFaultMessage(soapFault));
            writer.writeEndElement();
            writer.writeEndElement();

            if (soapFault.getRole() != null) {
                writer.writeStartElement(SOAP_PREFIX, "Role", ns);
                writer.writeCharacters(soapFault.getRole());
                writer.writeEndElement();
            }

            prepareStackTrace(ns, soapFault);

            if (soapFault.hasDetails()) {
                Element detail = soapFault.getDetail();
                writer.writeStartElement(SOAP_PREFIX, "Detail", ns);

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
            throw new RuntimeCamelException("XML Write Exception: " + xe.getMessage(), xe);
        }
    }

    private static String getLangCode() {
        String code = Locale.getDefault().getLanguage();
        if (StringUtils.isEmpty(code)) {
            return "en";
        }
        return code;
    }

    private static String getCodeString(String prefix, String defaultPrefix, QName code) {
        String codePrefix;
        if (StringUtils.isEmpty(prefix)) {
            codePrefix = code.getPrefix();
            if (StringUtils.isEmpty(codePrefix)) {
                codePrefix = defaultPrefix;
            }
        } else {
            codePrefix = prefix;
        }

        return codePrefix + ":" + code.getLocalPart();
    }
}
