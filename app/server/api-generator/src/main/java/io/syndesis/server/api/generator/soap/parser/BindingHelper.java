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
package io.syndesis.server.api.generator.soap.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.binding.soap.model.SoapBodyInfo;
import org.apache.cxf.binding.soap.model.SoapHeaderInfo;
import org.apache.cxf.binding.soap.model.SoapOperationInfo;
import org.apache.cxf.common.xmlschema.SchemaCollection;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAnnotated;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaSerializer;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static javax.jws.soap.SOAPBinding.Use.ENCODED;

/**
 * Helper class for working with {@link SOAPBinding} && {@link javax.wsdl.extensions.soap12.SOAP12Binding}.
 */
public class BindingHelper {

    private static final String SCHEMA_SET_XML =
        "<d:SchemaSet xmlns:d=\"http://atlasmap.io/xml/schemaset/v2\">" +
            "<d:AdditionalSchemas/>" +
        "</d:SchemaSet>";

    private final BindingMessageInfo bindingMessageInfo;
    private final SchemaCollection schemaCollection;
    private final BindingOperationInfo bindingOperation;
    private final Style style;

    private final List<MessagePartInfo> bodyParts;

    private final boolean hasHeaders;
    private final List<MessagePartInfo> headerParts;
    private final SoapVersion soapVersion;
    private final DocumentBuilder documentBuilder;

    BindingHelper(BindingMessageInfo bindingMessageInfo) throws ParserException, ParserConfigurationException {

        this.bindingMessageInfo = bindingMessageInfo;
        this.bindingOperation = bindingMessageInfo.getBindingOperation();
        this.schemaCollection = bindingMessageInfo.getBindingOperation().getBinding().getService().getXmlSchemaCollection();

        SoapOperationInfo soapOperationInfo = bindingOperation.getExtensor(SoapOperationInfo.class);
        SoapBindingInfo soapBindingInfo = (SoapBindingInfo) bindingOperation.getBinding();

        soapVersion = soapBindingInfo.getSoapVersion();

        // get binding style
        if (soapOperationInfo.getStyle() != null) {
            style = Style.valueOf(soapOperationInfo.getStyle().toUpperCase(Locale.US));
        } else if (soapBindingInfo.getStyle() != null) {
            style = Style.valueOf(soapBindingInfo.getStyle().toUpperCase(Locale.US));
        } else {
            style = Style.DOCUMENT;
        }

        // get body binding
        SoapBodyInfo soapBodyInfo = bindingMessageInfo.getExtensor(SoapBodyInfo.class);
        List<SoapHeaderInfo> soapHeaders = bindingMessageInfo.getExtensors(SoapHeaderInfo.class);
        bodyParts = soapBodyInfo.getParts();

        // get any headers as MessagePartInfos
        hasHeaders = soapHeaders != null && !soapHeaders.isEmpty();
        headerParts = hasHeaders ?
            soapHeaders.stream().map(SoapHeaderInfo::getPart).collect(Collectors.toList()) : null;

        // get required body use
        Use use = Use.valueOf(soapBodyInfo.getUse().toUpperCase(Locale.US));
        if (ENCODED.equals(use)) {
            // TODO could we add support for RPC/encoded messages by setting schema type to any??
            throw new ParserException("Messages with use='encoded' are not supported");
        }

        // Document builder for create schemaset
        this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    // get specification from BindingMessageInfo
    public String getSpecification() throws ParserException {

        // add a SOAP schema to hold wrapper elements, to be removed by soap connector
        final SchemaCollection targetSchemas = new SchemaCollection();
        final XmlSchema soapSchema = targetSchemas.newXmlSchemaInCollection(soapVersion.getEnvelope().getNamespaceURI());
        soapSchema.setElementFormDefault(XmlSchemaForm.QUALIFIED);
        soapSchema.setAttributeFormDefault(XmlSchemaForm.QUALIFIED);

        // extract elements/types from source schema using an XmlSchemaExtractor
        final XmlSchemaExtractor schemaExtractor = new XmlSchemaExtractor(targetSchemas, schemaCollection);

        // TODO also handle faults for output message, which requires an enhancement in Syndesis

        if (style == Style.RPC) {
            final OperationInfo operationInfo = bindingOperation.getOperationInfo();
            final QName operationName = operationInfo.getName();
            final QName wrapperElement = bindingMessageInfo.getMessageInfo().getType() == MessageInfo.Type.INPUT ?
                operationName : new QName(operationName.getNamespaceURI(), operationName.getLocalPart() + "Response");

            createRpcEnvelope(schemaExtractor, wrapperElement);
        } else {
            final List<XmlSchemaElement> bodyElements = getPartElements(schemaExtractor, soapSchema, bodyParts);

            // if topLevel is true, root element was already added to generated schema as top level element
            final List<XmlSchemaElement> headerElements = hasHeaders ? getPartElements(schemaExtractor, soapSchema,
                headerParts) : null;
            createDocumentEnvelope(schemaExtractor, headerElements, bodyElements);
        }

        return getSpecificationString(schemaExtractor);
    }

    private void createRpcEnvelope(XmlSchemaExtractor schemaExtractor, QName operationWrapper) throws ParserException {

        // create soap envelope
        final XmlSchema soapSchema = schemaExtractor.getTargetSchemas().getSchemaByTargetNamespace(soapVersion.getNamespace());
        final List<XmlSchemaSequenceMember> soapEnvelope = getSoapEnvelope(soapSchema);

        if (headerParts != null) {
            // soap header
            final List<XmlSchemaSequenceMember> soapHeader = getXmlSchemaElement(soapSchema, soapEnvelope,
                soapVersion.getHeader());
            // add header elements
            soapHeader.addAll(getPartElements(schemaExtractor, soapSchema, headerParts));
        }

        // soap body
        final List<XmlSchemaSequenceMember> soapBody = getXmlSchemaElement(soapSchema, soapEnvelope, soapVersion.getBody());

        // top-level operation wrapper element
        final String namespaceURI = operationWrapper.getNamespaceURI();
        final XmlSchema operationSchema = schemaExtractor.getOrCreateTargetSchema(namespaceURI);
        final List<XmlSchemaSequenceMember> bodySequence = getXmlSchemaElement(operationSchema, soapSchema,
            soapBody, operationWrapper.getLocalPart(), true);

        // add bodyParts to wrapper element
        bodySequence.addAll(getPartElements(schemaExtractor, operationSchema, bodyParts));
    }

    private void createDocumentEnvelope(XmlSchemaExtractor schemaExtractor, List<XmlSchemaElement> headerElements,
                                        List<XmlSchemaElement> bodyElements) {
        // envelope element
        final XmlSchema soapSchema = schemaExtractor.getTargetSchemas().getSchemaByTargetNamespace(soapVersion.getNamespace());
        final List<XmlSchemaSequenceMember> envelope = getSoapEnvelope(soapSchema);

        // check if there is a header included
        if (headerElements != null) {
            final List<XmlSchemaSequenceMember> headers = getXmlSchemaElement(soapSchema, envelope, soapVersion.getHeader());
            headers.addAll(headerElements);
        }

        // add body wrapper
        final List<XmlSchemaSequenceMember> bodySequence = getXmlSchemaElement(soapSchema, envelope, soapVersion.getBody());
        bodySequence.addAll(bodyElements);
    }

    private String getSpecificationString(XmlSchemaExtractor schemaExtractor) throws ParserException {
        try {
            // copy source elements and types to target schema
            schemaExtractor.copyObjects();
            final SchemaCollection targetSchemas = schemaExtractor.getTargetSchemas();

            // serialize schemas as AtlasMap schemaset
            // schemaset is described at https://docs.atlasmap.io/#importing-xml-files-into-atlasmap
            final Document schemaSet = this.documentBuilder.parse(new InputSource(new StringReader(SCHEMA_SET_XML)));
            final Node additionalSchemas = schemaSet.getElementsByTagName("d:AdditionalSchemas").item(0);

            // insert schemas into schemaset
            final XmlSchema[] xmlSchemas = targetSchemas.getXmlSchemaCollection().getXmlSchemas();
            for (XmlSchema schema : xmlSchemas) {
                final String targetNamespace = schema.getTargetNamespace();

                // no need to add XSD and XML schema
                if (!targetNamespace.equals(XMLConstants.XML_NS_URI) &&
                    !targetNamespace.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {

                    // get the schema DOM document
                    final Document schemaDocument = schema.getSchemaDocument();
                    // remove targetnamespace declaration for no namespace schema
                    if (targetNamespace.isEmpty()) {
                        // remove invalid xmlns:tns="" attribute or it breaks StaxUtils.toString below
                        schemaDocument.getDocumentElement().removeAttribute("xmlns:tns");
                    }

                    // import the schema into schemaset
                    final Node documentElement = schemaSet.importNode(schemaDocument.getDocumentElement(), true);

                    if (targetNamespace.equals(soapVersion.getNamespace())) {
                        // add soap envelope under schemaset as first child
                        schemaSet.getDocumentElement().insertBefore(documentElement, additionalSchemas);
                    } else {
                        // add the schema under 'AdditionalSchemas'
                        additionalSchemas.appendChild(documentElement);
                    }
                }
            }

            // write schemaset as string
            return StaxUtils.toString(schemaSet);

        } catch (XmlSchemaSerializer.XmlSchemaSerializerException | ParserException | SAXException | IOException e) {
            throw new ParserException(String.format("Error parsing %s for operation %s: %s",
                bindingMessageInfo.getMessageInfo().getType(),
                bindingMessageInfo.getBindingOperation().getOperationInfo().getName(),
                e.getMessage())
                , e);
        }
    }

    private List<XmlSchemaElement> getPartElements(XmlSchemaExtractor schemaExtractor, XmlSchema parentSchema,
                                                   List<MessagePartInfo> bodyParts) throws ParserException {

        final List<XmlSchemaElement> bodyElements = new ArrayList<>();
        for (MessagePartInfo part : bodyParts) {

            final XmlSchemaAnnotated annotated = part.getXmlSchema() != null ? part.getXmlSchema() :
                schemaCollection.getTypeByQName(part.getTypeQName());

            final QName name = part.getConcreteName();
            final XmlSchemaElement element;
            if (annotated instanceof XmlSchemaElement) {
                // extract element
                element = schemaExtractor.extract((XmlSchemaElement) annotated);
            } else if (annotated instanceof XmlSchemaType) {
                // extract type as top level element with 'type' attribute
                element = schemaExtractor.extract(name, (XmlSchemaType) annotated);
            } else {
                // probably an xsd:* type or RPC accessor, create an element with part's typename
                final XmlSchema targetSchema = schemaExtractor.getOrCreateTargetSchema(name.getNamespaceURI());

                element = new XmlSchemaElement(targetSchema, true);
                element.setName(name.getLocalPart());
                element.setSchemaTypeName(part.getTypeQName());
            }

            bodyElements.add(element);
        }

        // always return element refs
        return getElementRefs(parentSchema, bodyElements);
    }

    private static List<XmlSchemaElement> getElementRefs(XmlSchema soapSchema, List<XmlSchemaElement> bodyElements) {
        return bodyElements.stream()
            .map(e -> {
                final XmlSchemaElement refElement = new XmlSchemaElement(soapSchema, false);
                refElement.getRef().setTargetQName(e.getQName());
                return refElement;
            })
            .collect(Collectors.toList());
    }

    // create top-level Envelope element in SOAP schema
    private List<XmlSchemaSequenceMember> getSoapEnvelope(XmlSchema soapSchema) {
        return getXmlSchemaElement(soapSchema, null, null,
            soapVersion.getEnvelope().getLocalPart(), true);
    }

    // create local element in schema under parent sequence
    private static List<XmlSchemaSequenceMember> getXmlSchemaElement(XmlSchema schema,
                                                              List<XmlSchemaSequenceMember> parentSequence,
                                                              QName name) {
        return getXmlSchemaElement(schema, schema, parentSequence, name.getLocalPart(), false);
    }

    // create element in schema (local or topLevel), add element/ref under parent sequence in parentSchema
    private static List<XmlSchemaSequenceMember> getXmlSchemaElement(XmlSchema schema, XmlSchema parentSchema,
                                                              List<XmlSchemaSequenceMember> parentSequence,
                                                              String name, boolean topLevel) {
        // element
        final XmlSchemaElement element = new XmlSchemaElement(schema, topLevel);
        element.setName(name);

        // complex type
        final XmlSchemaComplexType complexType = new XmlSchemaComplexType(schema, false);
        element.setType(complexType);

        // sequence
        final XmlSchemaSequence sequence = new XmlSchemaSequence();
        complexType.setParticle(sequence);

        // need to add new element to a parent sequence?
        if(parentSequence != null) {
            final XmlSchemaElement child;
            if (!topLevel && schema.equals(parentSchema)) {
                // local element in parent schema, add as direct child to sequence
                child = element;
            } else {
                // add as ref in parent schema
                XmlSchemaElement refElement = new XmlSchemaElement(parentSchema, false);
                refElement.getRef().setTargetQName(element.getQName());
                child = refElement;
            }

            // add element or ref to parent sequence
            parentSequence.add(child);
        }

        return sequence.getItems();
    }

}
