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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.Violation;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ActionsSummary;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.server.api.generator.soap.SoapApiModelInfo;

import org.apache.cxf.BusException;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.soap.model.SoapBodyInfo;
import org.apache.cxf.binding.soap.model.SoapHeaderInfo;
import org.apache.cxf.service.model.AbstractPropertiesHolder;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl11.SOAPBindingUtil;
import org.apache.cxf.wsdl11.WSDLManagerImpl;
import org.apache.cxf.wsdl11.WSDLRuntimeException;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.DATA_FORMAT_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.DEFAULT_OPERATION_NAMESPACE_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.DEFAULT_OPERATION_NAME_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.PAYLOAD_FORMAT;

/**
 * Parses SOAP WSDL specification.
 */
@SuppressWarnings("PMD.GodClass")
public final class SoapApiModelParser {

    private static final Logger LOG = LoggerFactory.getLogger(SoapApiModelParser.class);

    private SoapApiModelParser() {
        // utility
    }

    public static SoapApiModelInfo parseSoapAPI(final InputStream specification, final String wsdlURL) {
        SoapApiModelInfo.Builder builder = new SoapApiModelInfo.Builder();

        try {
            if (wsdlURL != null) {
                builder.wsdlURL(wsdlURL);
            }

            // get concise WSDL representation without extra spaces
            @SuppressWarnings("resource") // we're passing it on to the builder, we don't want to close it here
            final InputStream condensedSpecification = condenseWSDL(specification);
            builder.specification(condensedSpecification);

            // parse WSDL to get model Definition
            final InputSource inputSource = new InputSource(condensedSpecification);
            final Definition definition = getWsdlReader().readWSDL(new Locator(wsdlURL, inputSource));

            builder.model(definition);
            validateModel(definition, builder);

            // set default service, port and address if present
            final SoapApiModelInfo localBuild = builder.build();
            if (localBuild.getServices().size() >= 1) {

                final QName defaultService = localBuild.getServices().get(0);
                builder.defaultService(defaultService);

                final List<String> ports = localBuild.getPorts().get(defaultService);
                if (ports.size() == 1) {
                    final String defaultPort = ports.get(0);
                    builder.defaultPort(defaultPort);

                    // set default address
                    String locationURI = getAddress(definition, defaultService, defaultPort);
                    if (locationURI != null) {
                        builder.defaultAddress(locationURI);
                    }
                }
            }

            @SuppressWarnings("unchecked")
            final Map<QName, Service> services = definition.getServices();

            services.forEach((serviceName, service) -> {
                @SuppressWarnings("unchecked")
                final Map<String, Port> ports = service.getPorts();

                ports.keySet().forEach(portName -> {
                    builder.putAddresses(portName, getAddress(definition, serviceName, portName));
                });
            });

        } catch (IOException e) {
            addError(builder, "Error reading WSDL: " + e.getMessage(), e);
            return builderWithEmptyStream(builder);
        } catch (WSDLException | BusException e) {
            addError(builder, "Error parsing WSDL: " + e.getMessage(), e);
            return builderWithEmptyStream(builder);
        } catch (TransformerException e) {
            addError(builder, "Error parsing WSDL: " + messageFrom(e), e);
            return builderWithEmptyStream(builder);
        }

        return builder.build();
    }

    private static SoapApiModelInfo builderWithEmptyStream(SoapApiModelInfo.Builder builder) {
        final ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[0]);

        return builder.specification(emptyStream).build();
    }

    private static String messageFrom(final TransformerException e) {
        TransformerException last = e;
        while (last.getCause() instanceof TransformerException) {
            last = (TransformerException) e.getCause();
        }

        final Throwable lastCause = last.getCause();
        if (lastCause != null) {
            return lastCause.getMessage();
        }

        return last.getMessage();
    }

    public static String getAddress(Definition definition, QName service, String port) {
        String locationURI = null;
        @SuppressWarnings("unchecked")
        final List<ExtensibilityElement> extensibilityElements = definition.getService(service)
                .getPort(port).getExtensibilityElements();
        for (ExtensibilityElement a : extensibilityElements) {
            final SOAPAddress soapAddress = SOAPBindingUtil.getSoapAddress(a);
            if (soapAddress != null) {
                locationURI = soapAddress.getLocationURI();
                break;
            }
        }
        return locationURI;
    }

    public static ActionsSummary parseActionsSummary(Definition definition, QName serviceName, String portName) throws ParserException {

        ActionsSummary.Builder actionsBuilder = new ActionsSummary.Builder();
        final List<BindingOperation> bindingOperations = getBindingOperations(definition, serviceName, portName);
        final AtomicInteger totalActions = new AtomicInteger(0);
        final Map<String, Integer> operationMap = bindingOperations.stream()
                .peek(o -> totalActions.incrementAndGet())
                .collect(Collectors.toMap(BindingOperation::getName, o -> 1, (k, i) -> ++i));

        actionsBuilder.totalActions(totalActions.get());
        actionsBuilder.actionCountByTags(operationMap);

        return actionsBuilder.build();
    }

    public static List<ConnectorAction> parseActions(Definition definition, QName serviceName, QName portName,
                                                     String connectorId) throws ParserException {
        final Collection<BindingOperationInfo> bindingOperations = parseBindingOperations(definition, serviceName, portName);

        // operation id map for overloaded names
        final Map<String, Integer> idMap = new HashMap<>();

        return bindingOperations.stream().map(o -> parseConnectorAction(o, idMap, connectorId)).collect(Collectors.toList());
    }

    public static ConnectorAction generateDataShapesFor(final ConnectorAction action, final Definition definition, final QName serviceName, final QName portName) {
        final String actionId = action.getId().get();
        final BindingOperationInfo[] matchingBindingOperations = parseBindingOperations(definition, serviceName, portName).stream()
            .filter(op -> {
                final QName name = op.getName();
                final String operationName = ":" + name.getLocalPart();

                return actionId.endsWith(operationName) || actionId.replaceAll("_[0-9]+$", "").endsWith(operationName);
            }).toArray(BindingOperationInfo[]::new);

        // we're assuming that the order of services within the definition will be consistent
        // this might not be the case but we don't have much to else to work with, and the
        // hypothesis is that operation name collisions are very rare
        final BindingOperationInfo bindingOperationInfo;
        if (matchingBindingOperations.length == 0) {
            throw new IllegalStateException("Unable to find binding operation info matching action with ID: " + actionId);
        } else if (matchingBindingOperations.length > 1) {
            int idx = Integer.parseInt(actionId.replaceAll(".*_([0-9]+)$", "$1")) - 1; // they're 1-based

            bindingOperationInfo = matchingBindingOperations[idx]; // let it explode with ArrayIndexOutOfBounds if it must
        } else {
            bindingOperationInfo = matchingBindingOperations[0];
        }

        final DataShape input = generateDataShape(bindingOperationInfo, bindingOperationInfo.getInput(), Collections.emptyList(), MessageInfo.Type.INPUT);
        final DataShape output = generateDataShape(bindingOperationInfo, bindingOperationInfo.getOutput(), bindingOperationInfo.getFaults(), MessageInfo.Type.OUTPUT);

        return new ConnectorAction.Builder().createFrom(action)
            .descriptor(new ConnectorDescriptor.Builder().createFrom(action.getDescriptor())
                .inputDataShape(input)
                .outputDataShape(output)
                .build())
            .build();
    }

    private static Collection<BindingOperationInfo> parseBindingOperations(Definition definition, QName serviceName, QName portName) {
        // use CXF helper classes to parse actions
        final WSDLServiceBuilder serviceBuilder = new WSDLServiceBuilder(BusFactory.getDefaultBus());
        final ServiceInfo serviceInfo;
        try {
            final List<ServiceInfo> serviceInfos = serviceBuilder.buildServices(definition, serviceName, portName);
            serviceInfo = serviceInfos.get(0);
        } catch (WSDLRuntimeException e) {
            throw new IllegalArgumentException(String.format("Error parsing actions for %s: %s", serviceName, e.getMessage()), e);
        }

        final EndpointInfo endpoint = serviceInfo.getEndpoints().stream()
            .filter(e -> e.getName().equals(portName) || e.getName().getLocalPart().equals(portName.getLocalPart()))
            .findFirst()
            .get();
        final BindingInfo binding = endpoint.getBinding();

        return binding.getOperations();
    }

    private static WSDLReader getWsdlReader() throws BusException {
        WSDLManager wsdlManager = new WSDLManagerImpl();
        // this is similar to what WSDLManager does,
        // but we need to read and store WSDL specification, so we create the reader ourselves
        final WSDLFactory wsdlFactory = wsdlManager.getWSDLFactory();
        final WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        wsdlReader.setFeature("javax.wsdl.importDocuments", true);
        wsdlReader.setExtensionRegistry(wsdlManager.getExtensionRegistry());
        return wsdlReader;
    }

    private static ConnectorAction parseConnectorAction(final BindingOperationInfo bindingOperationInfo, final Map<String, Integer> idMap,
                                                        final String connectorId) {
        final OperationInfo operationInfo = bindingOperationInfo.getOperationInfo();
        final String description = getDescription(operationInfo,
                o -> "Invokes the operation " + getOperationName(bindingOperationInfo));
        final QName name = bindingOperationInfo.getName();

        final ConnectorAction.Builder builder = new ConnectorAction.Builder()
                .name(name.getLocalPart())
                .description(description)
                .id(getActionId(connectorId, name.getLocalPart(), idMap))
                .addTag("dynamic")
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId(connectorId)
                    .putConfiguredProperty(DEFAULT_OPERATION_NAME_PROPERTY, name.getLocalPart())
                    .putConfiguredProperty(DEFAULT_OPERATION_NAMESPACE_PROPERTY, name.getNamespaceURI())
                    .putConfiguredProperty(DATA_FORMAT_PROPERTY, PAYLOAD_FORMAT)
                    .build())
                .pattern(Action.Pattern.To);
        return builder.build();
    }

    private static <T extends AbstractPropertiesHolder> String getDescription(T described, Function<T, String> defaultDescription) {
        return described.getDocumentation() != null ? described.getDocumentation() : defaultDescription.apply(described);
    }

    private static DataShape generateDataShape(final BindingOperationInfo bindingOperationInfo, final BindingMessageInfo messageInfo,
        final Collection<BindingFaultInfo> faults, final MessageInfo.Type type) {

        // message is missing or doesn't have any headers, body parts, and
        // faults
        if (faults.isEmpty() && (messageInfo == null ||
            (messageInfo.getExtensor(SoapBodyInfo.class) == null && messageInfo.getExtensor(SoapHeaderInfo.class) == null))) {
            return new DataShape.Builder().kind(DataShapeKinds.NONE).build();
        }

        final String specification;
        try {
            final BindingHelper bindingHelper = new BindingHelper(bindingOperationInfo, messageInfo, faults, type, false);
            specification = bindingHelper.getSpecification();
        } catch (ParserConfigurationException | ParserException e) {
            throw new IllegalStateException("Error creating XML Document parser: " + e.getMessage(), e);
        }

        final String name = messageInfo != null ? messageInfo.getMessageInfo().getName().getLocalPart() : getOperationName(bindingOperationInfo) + "Response";

        final String description = messageInfo != null ? getMessageDescription(messageInfo)
            : String.format("Data output for operation %s", getOperationName(bindingOperationInfo));

        return new DataShape.Builder()
            .kind(DataShapeKinds.XML_SCHEMA)
            .name(name)
            .description(description)
            .specification(specification)
            .build();
    }

    private static String getMessageDescription(BindingMessageInfo bindingMessageInfo) {
        // TODO get description from referenced type or element since message most likely doesn't provide one
        return getDescription(bindingMessageInfo.getMessageInfo(),
            m -> String.format("Data %s for operation %s", m.getType().toString().toLowerCase(Locale.ENGLISH),
                getOperationName(bindingMessageInfo.getBindingOperation())));
    }

    private static String getOperationName(BindingOperationInfo bindingOperationInfo) {
        return bindingOperationInfo.getOperationInfo().getName().getLocalPart();
    }

    static String getActionId(String connectorId, String name, Map<String, Integer> idMap) {
        final String operationId;
        final int id = idMap.merge(name, 1, Integer::sum) - 1;
        if (id > 0) {
            operationId = name + "_" + id;
        } else {
            operationId = name;
        }
        return connectorId + ":" + operationId;
    }

    private static List<BindingOperation> getBindingOperations(Definition definition, QName serviceName,
                                                               String portName) throws ParserException {
        final Service service = getService(definition, serviceName);
        final Port port = getPort(service, portName);
        @SuppressWarnings("unchecked")
        final List<BindingOperation> result = port.getBinding().getBindingOperations();
        return result;
    }

    private static Service getService(Definition definition, QName serviceName) throws ParserException {
        final Service service = definition.getService(serviceName);
        if (service == null) {
            throw new ParserException("Missing Service " + serviceName, "serviceName");
        }
        return service;
    }

    private static Port getPort(Service service, String portName) throws ParserException {
        final Port port = service.getPort(portName);
        if (port == null) {
            throw new ParserException("Missing Port " + portName, "portName");
        }
        return port;
    }

    // Removes extra spaces from WSDL to reduce the amount of text stored in DB and remove unwanted spaces in documentation elements.
    private static InputStream condenseWSDL(final InputStream specification) throws TransformerException, IOException {
        try (InputStream in = specification; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final XMLReader reader;
            try {
                reader = XMLReaderFactory.createXMLReader();
                reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            } catch (SAXException e) {
                throw new TransformerException(e);
            }

            final InputSource inputSource = new InputSource(in);
            final Source source = new SAXSource(reader, inputSource);

            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer wsdlCondenser = transformerFactory.newTransformer(new StreamSource(new StringReader(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\">" +
                            "<xsl:strip-space elements=\"*\" />" +
                            "<xsl:template match=\"node()|@*\" name=\"identity\">" +
                                "<xsl:copy>" +
                                    "<xsl:apply-templates select=\"node()|@*\"/>" +
                                "</xsl:copy>" +
                            "</xsl:template>" +
                            "<xsl:template match=\"comment()\"/>" +
                            "<xsl:template match=\"text()\">" +
                                "<xsl:value-of select=\"normalize-space()\"/>" +
                            "</xsl:template>" +
                        "</xsl:stylesheet>")));

            wsdlCondenser.transform(source, new StreamResult(out));

            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    @SuppressWarnings("unchecked")
    private static void validateModel(Definition definition, SoapApiModelInfo.Builder builder) {
        final Map<QName, Service> services = definition.getServices();

        // check that WSDL has at least one Service
        if (services.isEmpty()) {
            addError(builder, "Missing Service in WSDL");
        } else {

            final List<QName> serviceNames = services.entrySet().stream()
                    .filter(e -> !e.getValue().getPorts().isEmpty()) // ignore Services without Ports
                    .map(Map.Entry::getKey)
                    .sorted(Comparator.comparing(QName::toString))
                    .collect(Collectors.toList());
            builder.services(serviceNames);

            // service MUST have a Port
            if (serviceNames.isEmpty()) {
                addError(builder, "Missing Service with Port in WSDL");
            } else {

                final Map<QName, List<String>> ports = services.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> ((Map<String, Port>) e.getValue().getPorts()).values().stream()
                                .filter(SoapApiModelParser::isaSoapPort)
                                .map(Port::getName)
                                .collect(Collectors.toList())));
                if (ports.isEmpty()) {
                    addError(builder, "Missing Port with SOAP Address in WSDL");
                } else {
                    builder.ports(ports);
                }
            }
        }
    }

    // port MUST have SOAP address and SOAP binding
    @SuppressWarnings("unchecked")
    private static boolean isaSoapPort(Port port) {
        return port.getExtensibilityElements().stream().anyMatch(SOAPBindingUtil::isSOAPAddress) &&
                SOAPBindingUtil.isSOAPBinding(port.getBinding());
    }

    private static void addError(SoapApiModelInfo.Builder builder, String message) {
        addError(builder, message, null);
    }

    private static void addError(SoapApiModelInfo.Builder builder, String message, Exception e) {
        if (e == null) {
            LOG.debug(message);
        } else {
            LOG.debug(message, e);
        }
        builder.addErrors(getError(message));
    }

    private static Violation getError(String message) {
        return new Violation.Builder().error("error").message(message).build();
    }
}
