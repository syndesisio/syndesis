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
import java.util.Collection;
import java.util.Collections;
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
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAnnotated;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaChoiceMember;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaSerializer;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static javax.jws.soap.SOAPBinding.Use.ENCODED;

/**
 * Helper class for working with {@link SOAPBinding} && {@link javax.wsdl.extensions.soap12.SOAP12Binding}.
 */
@SuppressWarnings("PMD.GodClass")
public class BindingHelper {

    private static final String SCHEMA_SET_XML =
        "<d:SchemaSet xmlns:d=\"http://atlasmap.io/xml/schemaset/v2\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\">" +
            "<d:AdditionalSchemas>" +
                "<xsd:schema targetNamespace=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
                    "<xsd:attribute name=\"lang\">" +
                        "<xsd:simpleType>" +
                            "<xsd:union memberTypes=\"xsd:language\">" +
                                "<xsd:simpleType>" +
                                    "<xsd:restriction base=\"xsd:string\">" +
                                        "<xsd:enumeration value=\"\"/>" +
                                    "</xsd:restriction>" +
                                "</xsd:simpleType>" +
                            "</xsd:union>" +
                        "</xsd:simpleType>" +
                    "</xsd:attribute>" +
                    "<xsd:attribute name=\"space\">" +
                        "<xsd:simpleType>" +
                            "<xsd:restriction base=\"xsd:NCName\">" +
                                "<xsd:enumeration value=\"default\"/>" +
                                "<xsd:enumeration value=\"preserve\"/>" +
                            "</xsd:restriction>" +
                        "</xsd:simpleType>" +
                    "</xsd:attribute>" +
                    "<xsd:attribute name=\"base\" type=\"xsd:anyURI\"/>" +
                    "<xsd:attribute name=\"id\" type=\"xsd:ID\"/>" +
                "</xsd:schema>" +
            "</d:AdditionalSchemas>" +
        "</d:SchemaSet>";

    private static final SchemaCollection SOAP_SCHEMAS;
    private static final XmlSchema SOAP_SCHEMA_1_1;
    private static final XmlSchema SOAP_SCHEMA_1_2;
    private static final XmlSchemaComplexType FAULT_1_1;
    private static final XmlSchemaComplexType FAULT_1_2;

    private static final String SOAP1_1_DETAIL = "detail";
    private static final String SOAP1_2_DETAIL = "Detail";

    static {
        // load SOAP 1.1 and SOAP 1.2 Fault types
        SOAP_SCHEMAS = new SchemaCollection();
        final XmlSchemaCollection schemaCollection = SOAP_SCHEMAS.getXmlSchemaCollection();
        // read xml-ns.xsd to avoid opening an http connection
        schemaCollection.read(new InputSource(BindingHelper.class.getResourceAsStream("/schema/xml-ns.xsd")));

        SOAP_SCHEMA_1_1 = schemaCollection.read(new InputSource(BindingHelper.class.getResourceAsStream("/schema/soap1_1.xsd")));
        FAULT_1_1 = (XmlSchemaComplexType) SOAP_SCHEMA_1_1.getTypeByName("Fault");
        // remove detail element
        XmlSchemaSequence faultSequence = (XmlSchemaSequence) FAULT_1_1.getParticle();
        faultSequence.getItems().removeIf(i -> SOAP1_1_DETAIL.equals(((XmlSchemaElement)i).getName()));

        SOAP_SCHEMA_1_2 = schemaCollection.read(new InputSource(BindingHelper.class.getResourceAsStream("/schema/soap1_2.xsd")));
        FAULT_1_2 = (XmlSchemaComplexType) SOAP_SCHEMA_1_2.getTypeByName("Fault");
        faultSequence = (XmlSchemaSequence) FAULT_1_2.getParticle();
        faultSequence.getItems().removeIf(i -> SOAP1_2_DETAIL.equals(((XmlSchemaElement)i).getName()));
    }

    // operation
    private final BindingOperationInfo bindingOperation;

    private final SchemaCollection schemaCollection;
    private final Style style;

    private final List<MessagePartInfo> bodyParts;

    private final boolean hasHeaders;
    private final List<MessagePartInfo> headerParts;

    private final List<MessagePartInfo> faultParts;

    private final SoapVersion soapVersion;
    private final DocumentBuilder documentBuilder;
    private final MessageInfo.Type type;
    private final boolean useXmlSchemaChoice;

    BindingHelper(BindingOperationInfo bindingOperationInfo, BindingMessageInfo bindingMessageInfo, Collection<BindingFaultInfo> faults, MessageInfo.Type type, boolean useXmlSchemaChoice) throws ParserException, ParserConfigurationException {

        this.bindingOperation = bindingOperationInfo;
        this.type = type;
        this.useXmlSchemaChoice = useXmlSchemaChoice;

        this.schemaCollection = bindingOperation.getBinding().getService().getXmlSchemaCollection();

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
        if (bindingMessageInfo != null) {
            SoapBodyInfo soapBodyInfo = bindingMessageInfo.getExtensor(SoapBodyInfo.class);
            List<SoapHeaderInfo> soapHeaders = bindingMessageInfo.getExtensors(SoapHeaderInfo.class);
            // TODO handle headerfaults
            //List<SoapHeaderFault> soapHeaderFaults = bindingMessageInfo.getExtensors(SoapHeaderFault.class);
            bodyParts = soapBodyInfo.getParts();

            // get any headers as MessagePartInfos
            hasHeaders = soapHeaders != null && !soapHeaders.isEmpty();
            headerParts = hasHeaders ?
                soapHeaders.stream().map(SoapHeaderInfo::getPart).collect(Collectors.toList()) : null;

            // get required body use
            Use use = Use.valueOf(soapBodyInfo.getUse().toUpperCase(Locale.US));
            if (ENCODED.equals(use)) {
                throw new ParserException("Messages with use='encoded' are not supported");
            }

        } else {
            bodyParts = Collections.emptyList();
            hasHeaders = false;
            headerParts = null;
        }

        // get fault parts
        faultParts = new ArrayList<>();
        for (BindingFaultInfo fault : faults) {
            final FaultInfo faultInfo = fault.getFaultInfo();
            faultParts.addAll(faultInfo.getMessageParts());
        }

        // Document builder for create schemaset
        this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    // get specification from BindingMessageInfo and or faults
    public String getSpecification() throws ParserException {

        // add a SOAP schema to hold wrapper elements, to be removed by soap connector
        final SchemaCollection targetSchemas = new SchemaCollection();
        final XmlSchema soapSchema = createSoapSchema(targetSchemas);

        // extract elements/types from source schema using an XmlSchemaExtractor
        final XmlSchemaExtractor schemaExtractor = new XmlSchemaExtractor(targetSchemas, schemaCollection);

        if (style == Style.RPC) {
            final OperationInfo operationInfo = bindingOperation.getOperationInfo();
            final QName operationName = operationInfo.getName();
            final QName wrapperElement = type == MessageInfo.Type.INPUT ?
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
        final List<? extends XmlSchemaObjectBase> envelope = createSoapEnvelope(soapSchema);

        if (headerParts != null) {
            // soap header
            final List<XmlSchemaSequenceMember> soapHeader = createXmlSchemaSequenceElement(soapSchema, envelope, soapVersion.getHeader());
            // add header elements
            soapHeader.addAll(getPartElements(schemaExtractor, soapSchema, headerParts));
        }

        // create operation schema
        final String namespaceURI = operationWrapper.getNamespaceURI();
        final XmlSchema operationSchema = schemaExtractor.getOrCreateTargetSchema(namespaceURI);

        // soap body
        final List<? extends XmlSchemaObjectBase> soapBody;
        final boolean useAlternativeToChoice = (type == MessageInfo.Type.OUTPUT && !this.useXmlSchemaChoice);
        if (type == MessageInfo.Type.INPUT || useAlternativeToChoice) {
            soapBody = createXmlSchemaSequenceElement(soapSchema, envelope, soapVersion.getBody());
        } else {
            soapBody = createXmlSchemaChoiceElement(soapSchema, envelope, soapVersion.getBody());
        }

        // top-level operation wrapper element
        final XmlSchemaSequence bodySequence = new XmlSchemaSequence();
        createXmlSchemaElement(operationSchema, soapSchema, soapBody,
            operationWrapper.getLocalPart(), bodySequence, true, useAlternativeToChoice ? 0 : 1);

        // add bodyParts to wrapper element
        bodySequence.getItems().addAll(getPartElements(schemaExtractor, operationSchema, bodyParts));

        if (type == MessageInfo.Type.OUTPUT) {
            // add faults to soapBody
            @SuppressWarnings("unchecked")
            List<? super XmlSchemaObjectBase> soapConsumerBody = (List<? super XmlSchemaObjectBase>) soapBody;
            addFaultsToSoapBody(schemaExtractor, soapSchema, soapConsumerBody);
        }
    }

    private void createDocumentEnvelope(XmlSchemaExtractor schemaExtractor, List<XmlSchemaElement> headerElements,
                                        List<XmlSchemaElement> bodyElements) throws ParserException {
        // envelope element
        final XmlSchema soapSchema = schemaExtractor.getTargetSchemas().getSchemaByTargetNamespace(soapVersion.getNamespace());
        final List<XmlSchemaSequenceMember> envelope = createSoapEnvelope(soapSchema);

        // check if there is a header included
        if (headerElements != null) {
            final List<XmlSchemaSequenceMember> headers = createXmlSchemaSequenceElement(soapSchema, envelope, soapVersion.getHeader());
            headers.addAll(headerElements);
        }

        // add body wrapper
        if (type == MessageInfo.Type.INPUT) {

            final List<XmlSchemaSequenceMember> sequenceBody = createXmlSchemaSequenceElement(soapSchema,
                envelope, soapVersion.getBody());
            sequenceBody.addAll(bodyElements);

        } else {

            final List<? extends XmlSchemaObjectBase> soapBody;
            if (this.useXmlSchemaChoice) {
                soapBody = createXmlSchemaChoiceElement(soapSchema, envelope, soapVersion.getBody());
            } else {
                soapBody = createXmlSchemaSequenceElement(soapSchema, envelope, soapVersion.getBody());
            }

            // add bodyElements under a sequence
            final XmlSchemaSequence bodyPartsSequence = new XmlSchemaSequence();
            bodyPartsSequence.setMinOccurs(useXmlSchemaChoice ? 1 : 0);
            bodyPartsSequence.getItems().addAll(bodyElements);

            @SuppressWarnings("unchecked")
            final List<? super XmlSchemaObjectBase> consumerSoapBody = (List<? super XmlSchemaObjectBase>) soapBody;
            consumerSoapBody.add(bodyPartsSequence);

            // add faults to soapBody
            addFaultsToSoapBody(schemaExtractor, soapSchema, consumerSoapBody);
        }
    }

    private void addFaultsToSoapBody(XmlSchemaExtractor schemaExtractor, XmlSchema soapSchema,
                                     List<? super XmlSchemaObjectBase> soapBody) throws ParserException {

        QName fault = soapVersion.getFault();
        XmlSchemaExtractor faultExtractor = new XmlSchemaExtractor(schemaExtractor.getTargetSchemas(), SOAP_SCHEMAS);
        final XmlSchemaComplexType faultSource;
        final String detailName;
        if (fault.equals(FAULT_1_1.getQName())) {
            faultSource = FAULT_1_1;
            detailName = SOAP1_1_DETAIL;
        } else {
            faultSource = FAULT_1_2;
            detailName = SOAP1_2_DETAIL;
        }

        // temporarily copy Fault element and type from original SOAP schema
        final XmlSchemaElement tempFault = faultExtractor.extract(fault, faultSource);
        faultExtractor.copyObjects();

        // get Fault type sequence
        final XmlSchemaComplexType tempFaultType = (XmlSchemaComplexType) soapSchema.getTypeByName("Fault");
        final XmlSchemaSequence faultSequence = (XmlSchemaSequence) tempFaultType.getParticle();

        // create new Fault element under Body choice
        XmlSchemaElement faultElement = new XmlSchemaElement(soapSchema, false);
        faultElement.setName(tempFault.getName());
        faultElement.setMinOccurs(useXmlSchemaChoice ? 1 : 0);
        final XmlSchemaComplexType faultType = new XmlSchemaComplexType(soapSchema, false);
        faultType.setParticle(faultSequence);
        faultElement.setType(faultType);
        soapBody.add(faultElement);

        // remove temp root Fault element and type
        soapSchema.getItems().removeIf(i -> i instanceof XmlSchemaNamed && "Fault".equals(((XmlSchemaNamed)i).getName()));

        final List<XmlSchemaSequenceMember> faultSequenceItems = faultSequence.getItems();

        final List<? extends XmlSchemaObjectBase> faultChoiceElements;
        if (useXmlSchemaChoice) {
            faultChoiceElements = createXmlSchemaChoiceElement(soapSchema,
                faultSequenceItems, new QName(soapSchema.getTargetNamespace(), detailName));
        } else {
            faultChoiceElements = createXmlSchemaSequenceElement(soapSchema,
                faultSequenceItems, new QName(soapSchema.getTargetNamespace(), detailName));
        }
        // set detail minOccurs to 0 and form to SOAP default
        faultSequenceItems.forEach(i -> {
            if (i instanceof XmlSchemaElement) {
                XmlSchemaElement element1 = (XmlSchemaElement) i;
                if (element1.getName().equals(detailName)) {
                    element1.setMinOccurs(0);
                    final XmlSchema realSoapSchema =
                        SOAP_SCHEMAS.getSchemaByTargetNamespace(soapSchema.getTargetNamespace());
                    element1.setForm(realSoapSchema.getElementFormDefault());
                }
            }
        });

        @SuppressWarnings("unchecked")
        final List<? super XmlSchemaElement> elementList = ((List<? super XmlSchemaElement>) faultChoiceElements);
        elementList.addAll(getPartElements(schemaExtractor, soapSchema, faultParts));
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
            throw new ParserException(String.format("Error parsing %s for operation %s: %s", type,
                bindingOperation.getName(), e.getMessage()) , e);
        }
    }

    protected XmlSchema createSoapSchema(SchemaCollection targetSchemas) {

        final XmlSchema soapSchema = targetSchemas.newXmlSchemaInCollection(soapVersion.getEnvelope().getNamespaceURI());
        soapSchema.setElementFormDefault(XmlSchemaForm.QUALIFIED);
        soapSchema.setAttributeFormDefault(XmlSchemaForm.QUALIFIED);

        return soapSchema;
    }

    private List<XmlSchemaElement> getPartElements(XmlSchemaExtractor schemaExtractor, XmlSchema parentSchema,
                                                   List<MessagePartInfo> bodyParts) throws ParserException {

        final List<XmlSchemaElement> bodyElements = new ArrayList<>();
        for (MessagePartInfo part : bodyParts) {

            final XmlSchemaAnnotated annotated = part.getXmlSchema() != null ? part.getXmlSchema() :
                schemaCollection.getTypeByQName(part.getTypeQName());

            final QName name = part.getName();
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
    private List<XmlSchemaSequenceMember> createSoapEnvelope(XmlSchema soapSchema) {
        XmlSchemaSequence particle = new XmlSchemaSequence();
        createXmlSchemaElement(soapSchema, null, null,
            soapVersion.getEnvelope().getLocalPart(), particle, true);
        return particle.getItems();
    }

    // create local element in schema under parent items
    private static <T> List<XmlSchemaSequenceMember> createXmlSchemaSequenceElement(XmlSchema schema,
                                                                                    List<T> parentItems,
                                                                                    QName name) {
        final XmlSchemaSequence particle = new XmlSchemaSequence();
        createXmlSchemaElement(schema, schema, parentItems, name.getLocalPart(), particle, false);
        return particle.getItems();
    }

    // create local element in schema under parent items
    private static <T> List<XmlSchemaChoiceMember> createXmlSchemaChoiceElement(XmlSchema schema,
                                                                                List<T> parentItems,
                                                                                QName name) {
        final XmlSchemaChoice particle = new XmlSchemaChoice();
        createXmlSchemaElement(schema, schema, parentItems, name.getLocalPart(), particle, false);
        return particle.getItems();
    }

    // create element in schema (local or topLevel) with provided particle,
    // add element/ref under parent items in parentSchema
    @SuppressWarnings("unchecked")
    private static <T> void createXmlSchemaElement(XmlSchema schema, XmlSchema parentSchema,
                                                   List<T> parentSequence, String name,
                                                   XmlSchemaParticle particle, boolean topLevel, long refMinOccurs) {

        // element
        final XmlSchemaElement element = new XmlSchemaElement(schema, topLevel);
        element.setName(name);

        // complex type
        final XmlSchemaComplexType complexType = new XmlSchemaComplexType(schema, false);
        complexType.setParticle(particle);
        element.setType(complexType);

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
                refElement.setMinOccurs(refMinOccurs);
                child = refElement;
            }

            // add element or ref to parent sequence
            parentSequence.add((T) child);
        }
    }

    // create element with default refMinOccurs set to 1
    private static <T> void createXmlSchemaElement(XmlSchema schema, XmlSchema parentSchema,
                                                   List<T> parentSequence, String name,
                                                   XmlSchemaParticle particle, boolean topLevel) {
        createXmlSchemaElement(schema, parentSchema, parentSequence, name, particle, topLevel, 1);
    }
}
