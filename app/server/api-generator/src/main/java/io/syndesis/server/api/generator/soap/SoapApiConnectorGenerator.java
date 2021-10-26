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
package io.syndesis.server.api.generator.soap;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import io.syndesis.common.model.action.ActionsSummary;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.common.model.connection.ConnectorTemplate;
import io.syndesis.server.api.generator.ConnectorAndActionGenerator;
import io.syndesis.server.api.generator.soap.parser.ParserException;
import io.syndesis.server.api.generator.soap.parser.SoapApiModelParser;

import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.ADDRESS_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.PORTS_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.PORT_NAME_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.SERVICES_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.SERVICE_NAME_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.SOAP_VERSION_PROPERTY;
import static io.syndesis.server.api.generator.soap.SoapConnectorConstants.WSDL_URL_PROPERTY;

/**
 * Generates SOAP API Connector.
 */
public class SoapApiConnectorGenerator extends ConnectorAndActionGenerator {

    // thread local API model to avoid re-parsing the WSDL.
    private static final ThreadLocal<SoapApiModelInfo> LOCAL_MODEL_INFO = new ThreadLocal<>();

    public SoapApiConnectorGenerator(Connector baseConnector) {
        super(baseConnector);
    }

    @Override
    public Connector generate(ConnectorTemplate connectorTemplate, ConnectorSettings connectorSettings) {
        try {
            final SoapApiModelInfo modelInfo = getModelInfo(connectorSettings);
            return createConnector(connectorTemplate, connectorSettings, modelInfo);
        } finally {
            releaseModelInfo();
        }
    }

    private Connector createConnector(ConnectorTemplate connectorTemplate, ConnectorSettings connectorSettings,
                                      SoapApiModelInfo modelInfo) {

        final Definition definition = modelInfo.getModel().
                orElseThrow(() -> new IllegalArgumentException("Unable to parse WSDL, or missing SOAP Service and Port in specification"));

        // get service and port
        final Map<String, String> configuredProperties = connectorSettings.getConfiguredProperties();
        final QName serviceName = getService(modelInfo, configuredProperties)
                .orElseThrow(() -> new IllegalArgumentException("Missing property " + SERVICE_NAME_PROPERTY));
        final String portName = getPortName(modelInfo, configuredProperties)
                .orElseThrow(() -> new IllegalArgumentException("Missing property " + PORT_NAME_PROPERTY));

        // get SOAP Version from Service and Port
        final Service service = definition.getService(serviceName);
        final Port port = service.getPort(portName);
        final Binding binding = port.getBinding();
        double soapVersion = 1.1;
        for (Object element : binding.getExtensibilityElements()) {
            if (element instanceof SOAP12Binding) {
                soapVersion = 1.2;
                break;
            }
        }

        // add actions
        try {

            final Connector configuredConnector = configuredConnector(connectorTemplate, connectorSettings);

            final List<ConnectorAction> actions = SoapApiModelParser.parseActions(definition,
                    serviceName, new QName(serviceName.getNamespaceURI(), portName),
                    configuredConnector.getId().get());
            final ActionsSummary actionsSummary = SoapApiModelParser.parseActionsSummary(definition, serviceName, portName);

            final Connector.Builder builder = new Connector.Builder()
                    .createFrom(configuredConnector)
                    .putConfiguredProperty(SOAP_VERSION_PROPERTY, String.valueOf(soapVersion))
                    .addAllActions(actions)
                    .actionsSummary(actionsSummary);

            return builder.build();

        } catch (ParserException e) {
            throw new IllegalArgumentException("Error getting actions from WSDL: " + e.getMessage(), e);
        }
    }

    @Override
    public APISummary info(ConnectorTemplate connectorTemplate, ConnectorSettings connectorSettings) {
        try {
            final SoapApiModelInfo modelInfo = getModelInfo(connectorSettings);
            return getApiSummary(connectorTemplate, connectorSettings, modelInfo);
        } finally {
            releaseModelInfo();
        }
    }

    @Override
    public ConnectorAction generateAction(final Connector connector, final ConnectorAction action, final Map<String, String> parameters, final InputStream specificationStream) {
        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()
            .specification(specificationStream)
            .build();

        try {
            final SoapApiModelInfo modelInfo = getModelInfo(connectorSettings);

            // we already parsed this so this must be safe
            final Definition definition = modelInfo.getModel().get();

            final Map<String, String> configuredProperties = connector.getConfiguredProperties();

            final String serviceNameString = configuredProperties.get(SERVICE_NAME_PROPERTY);
            final QName serviceName = parseQNameFrom(serviceNameString);

            final String portNameString = configuredProperties.get(PORT_NAME_PROPERTY);
            final QName portName = parseQNameFrom(portNameString);

            return SoapApiModelParser.generateDataShapesFor(action, definition, serviceName, portName);
        } finally {
            releaseModelInfo();
        }
    }

    private static QName parseQNameFrom(final String stringRepresentation) {
        final String namespace;
        final String localPart;
        if (stringRepresentation.charAt(0) != '{') {
            namespace = XMLConstants.NULL_NS_URI;
            localPart = stringRepresentation;
        } else {
            final int idx = stringRepresentation.indexOf('}');
            namespace = stringRepresentation.substring(1, idx);
            localPart = stringRepresentation.substring(idx + 1);
        }

        return new QName(namespace, localPart);
    }

    @Override
    protected String determineConnectorDescription(ConnectorTemplate connectorTemplate,
                                                   ConnectorSettings connectorSettings) {

        final String defaultDescription = "Web Services Connector";
        final SoapApiModelInfo modelInfo = getModelInfo(connectorSettings);
        final Optional<Definition> definition = modelInfo.getModel();

        return definition.map(d -> {
            final Element docElement = d.getDocumentationElement();
            final QName qName = d.getQName() != null ? d.getQName() : modelInfo.getDefaultService().orElse(null);
            return docElement != null ? docElement.getTextContent().trim() :
                (qName != null ? defaultDescription + " for service " + qName : null);
        }).orElse(defaultDescription);
    }

    @Override
    protected String determineConnectorName(ConnectorTemplate connectorTemplate,
                                                   ConnectorSettings connectorSettings) {
        final SoapApiModelInfo modelInfo = getModelInfo(connectorSettings);
        return modelInfo.getModel()
            .map(d -> d.getQName() != null ? d.getQName().getLocalPart() :
                modelInfo.getDefaultService().map(QName::getLocalPart).orElse(null))
            .orElse(super.determineConnectorName(connectorTemplate, connectorSettings));
    }

    private APISummary getApiSummary(ConnectorTemplate connectorTemplate, ConnectorSettings connectorSettings,
                                     SoapApiModelInfo modelInfo) {

        if (!modelInfo.getModel().isPresent()) {
            return new APISummary.Builder()
                .errors(modelInfo.getErrors())
                .warnings(modelInfo.getWarnings())
                .build();
        }

        // create connector from template and connectorSettings, and add connector properties and parse warnings and errors
        final Connector connector = configuredConnector(connectorTemplate, connectorSettings);
        final APISummary.Builder summaryBuilder = APISummary.Builder.createFrom(connector)
                .putAllConfiguredProperties(connector.getConfiguredProperties())
                .warnings(modelInfo.getWarnings())
                .errors(modelInfo.getErrors());

        modelInfo.getModel().ifPresent(definition -> {

            // add list of services and ports
            // NOTE configured properties doesn't allow non-String values so we use a json string instead
            summaryBuilder.putConfiguredProperty(SERVICES_PROPERTY,
                    toListValue(modelInfo.getServices().stream()
                            .map(QName::toString)
                            .collect(Collectors.toList())));
            final Map<QName, List<String>> ports = modelInfo.getPorts();
            summaryBuilder.putConfiguredProperty(PORTS_PROPERTY, toMapValue(ports));

            // default or user selected service and port names
            modelInfo.getDefaultService().ifPresent(s -> {
                summaryBuilder.putConfiguredProperty(SERVICE_NAME_PROPERTY, s.toString());
            });

            modelInfo.getDefaultPort().ifPresent(p -> {
                    summaryBuilder.putConfiguredProperty(PORT_NAME_PROPERTY, p);
            });

            for (QName service : modelInfo.getServices()) {
                try {
                    for (String port : ports.get(service)) {
                        summaryBuilder.addActionsSummary(SoapApiModelParser.parseActionsSummary(definition, service, port));
                    }
                } catch (ParserException e) {
                    summaryBuilder.addError(e.toViolation());
                }
            }

            summaryBuilder.putConfiguredProperty(SoapConnectorConstants.ADRESSES_PROPERTY, addressesToMapValue(modelInfo.getAddresses()));
        });

        return summaryBuilder.build();
    }

    private Connector configuredConnector(ConnectorTemplate connectorTemplate, ConnectorSettings connectorSettings) {
        // copy all configuration properties, might have to change for supporting different WSDL versions
        final Connector.Builder builder = new Connector.Builder()
                .createFrom(baseConnectorFrom(connectorTemplate, connectorSettings));

        // add configuration properties
        final Map<String, ConfigurationProperty> connectorProperties = connectorTemplate.getConnectorProperties();
        builder.putAllProperties(connectorProperties);

        final SoapApiModelInfo modelInfo = getModelInfo(connectorSettings);

        // set configured properties
        final Map<String, String> configuredProperties = connectorSettings.getConfiguredProperties();
        final String wsdlURL = modelInfo.getWsdlURL().orElse(configuredProperties.get(WSDL_URL_PROPERTY));
        if (wsdlURL != null) {
            builder.putConfiguredProperty(WSDL_URL_PROPERTY, wsdlURL);
        }

        getService(modelInfo, configuredProperties).ifPresent(s -> {
            builder.putConfiguredProperty(SERVICE_NAME_PROPERTY, s.toString());

            getPortName(modelInfo, configuredProperties).ifPresent(p -> {
                builder.putConfiguredProperty(PORT_NAME_PROPERTY, p);

                // set address from selected port
                final String address = SoapApiModelParser.getAddress(modelInfo.getModel().get(), s, p);
                builder.putConfiguredProperty(ADDRESS_PROPERTY,
                    address);
                builder.putProperty(ADDRESS_PROPERTY,
                    new ConfigurationProperty.Builder().createFrom(connectorProperties.get(ADDRESS_PROPERTY))
                        .defaultValue(address)
                        .build());
            });
        });

        if (!connectorSettings.getConfiguredProperties().containsKey(ADDRESS_PROPERTY)) {
            // if present, set default address from WSDL
            modelInfo.getDefaultAddress().ifPresent(a -> {
                builder.putConfiguredProperty(ADDRESS_PROPERTY, a);
                builder.putProperty(ADDRESS_PROPERTY,
                        new ConfigurationProperty.Builder().createFrom(connectorProperties.get(ADDRESS_PROPERTY))
                        .defaultValue(a)
                        .build());
            });
        }

        return builder.build();
    }

    private static Optional<QName> getService(SoapApiModelInfo modelInfo, Map<String, String> configuredProperties) {
        return configuredProperties.containsKey(SERVICE_NAME_PROPERTY) ?
                Optional.of(QName.valueOf(configuredProperties.get(SERVICE_NAME_PROPERTY))) : modelInfo.getDefaultService();
    }

    private static Optional<String> getPortName(SoapApiModelInfo modelInfo, Map<String, String> configuredProperties) {
        return configuredProperties.containsKey(PORT_NAME_PROPERTY) ?
                Optional.ofNullable(configuredProperties.get(PORT_NAME_PROPERTY)) : modelInfo.getDefaultPort();
    }

    // get cached TLS model info, or create one the first time
    private static SoapApiModelInfo getModelInfo(ConnectorSettings connectorSettings) {
        // check TLS first
        if (LOCAL_MODEL_INFO.get() == null) {
            Optional<InputStream> maybeSpecificationStream = connectorSettings.getSpecification();
            if (!maybeSpecificationStream.isPresent()) {
                throw new IllegalArgumentException("Missing specification");
            }
            final Map<String, String> configuredProperties = connectorSettings.getConfiguredProperties();
            final String wsdlUrl = configuredProperties.get(WSDL_URL_PROPERTY);

            try (InputStream specification = maybeSpecificationStream.get()) {
                LOCAL_MODEL_INFO.set(SoapApiModelParser.parseSoapAPI(specification, wsdlUrl));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return LOCAL_MODEL_INFO.get();
    }

    // release cached TLS model info
    private static void releaseModelInfo() {
        LOCAL_MODEL_INFO.remove();
    }

    // return a json representation of map of ports
    private static String toMapValue(Map<QName, List<String>> ports) {
        return ports.entrySet().stream()
            .map(e -> String.format("\"%s\": %s", e.getKey(), toListValue(e.getValue())))
            .collect(Collectors.joining(",", "{", "}"));
    }

    private static String addressesToMapValue(Map<String, String> addresses) {
        return addresses.entrySet().stream()
            .map(e -> String.format("\"%s\": \"%s\"", e.getKey(), e.getValue()))
            .collect(Collectors.joining(",", "{", "}"));
    }

    // return a json represenation of list of strings
    private static String toListValue(List<String> stringList) {
        return stringList.stream()
            .map(s -> String.format("\"%s\"", s))
            .collect(Collectors.joining(",", "[", "]"));
    }

}
