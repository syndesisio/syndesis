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
package io.syndesis.connector.fhir.customizer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.RuntimeExchangeException;
import org.apache.camel.component.fhir.internal.FhirTransactionApiMethod;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FhirTransactionCustomizer implements ComponentProxyCustomizer {

    private FhirContext fhirContext;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        fhirContext = FhirCustomizerHelper.newFhirContext(options);

        options.put("apiName", FhirCustomizerHelper.getFhirApiName(FhirTransactionApiMethod.class));
        options.put("methodName", "withResources");

        component.setBeforeProducer(this::beforeProducer);
        component.setAfterProducer(this::afterProducer);
    }

    public void beforeProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        String body = in.getBody(String.class);

        if (body != null) {
            List<IBaseResource> resources = new ArrayList<>();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try {
                Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(body.getBytes("UTF-8")));
                Node transactionElement = doc.getFirstChild();
                NodeList childNodes = transactionElement.getChildNodes();
                IParser parser = fhirContext.newXmlParser();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node resourceNode = childNodes.item(i);
                    Document resourceDocument = toDocument(resourceNode, dbf);
                    String resourceXml = toXml(resourceDocument);
                    IBaseResource resource = parser.parseResource(resourceXml);
                    resources.add(resource);
                }
            } catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
                throw new RuntimeExchangeException("Cannot convert Transaction to a list of resources", exchange, e);
            }

            in.setHeader("CamelFhir.resources", resources);
        }
    }

    public void afterProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        @SuppressWarnings("unchecked")
        List<IBaseResource> body = in.getBody(List.class);

        StringBuilder transaction = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<Transaction xmlns=\"http://hl7.org/fhir\">");
        IParser parser = fhirContext.newXmlParser();
        for (IBaseResource resource: body) {
            String encodedResource = parser.encodeResourceToString(resource);
            transaction.append(encodedResource);
        }
        transaction.append("</Transaction>");

        in.setBody(transaction.toString());
    }

    private Document toDocument(Node resourceNode, DocumentBuilderFactory dbf) throws ParserConfigurationException {
        Document document = dbf.newDocumentBuilder().newDocument();
        Element elementNS = document.createElementNS("http://hl7.org/fhir", resourceNode.getNodeName());
        elementNS.setPrefix("tns");
        NodeList list = resourceNode.getChildNodes();
        for (int j = 0; j < list.getLength(); j++) {
            Node node = list.item(j);
            node = document.importNode(node, true);
            elementNS.appendChild(node);
        }
        document.appendChild(elementNS);
        return document;
    }

    private String toXml(Document document) throws TransformerException {
        StringWriter buf = new StringWriter();
        Transformer xform = TransformerFactory.newInstance().newTransformer();
        xform.transform(new DOMSource(document), new StreamResult(buf));
        return (buf.toString());
    }
}
