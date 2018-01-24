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
package io.syndesis.integration.component.proxy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Endpoint;
import org.apache.camel.NoTypeConversionAvailableException;
import org.apache.camel.Processor;
import org.apache.camel.TypeConverter;
import org.apache.camel.catalog.CamelCatalog;
import org.apache.camel.catalog.DefaultCamelCatalog;
import org.apache.camel.component.extension.ComponentExtension;
import org.apache.camel.component.extension.ComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.URISupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentProxyComponent extends DefaultComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentProxyComponent.class);

    private final CamelCatalog catalog;
    private final String componentId;
    private final String componentScheme;
    private final Map<String, Object> options;
    private final ComponentDefinition definition;

    private Optional<String> componentSchemeAlias;

    private Processor beforeProducer;
    private Processor afterProducer;
    private Processor beforeConsumer;
    private Processor afterConsumer;

    public ComponentProxyComponent(String componentId, String componentScheme) {
        this.componentId = componentId;
        this.componentScheme = componentScheme;
        this.componentSchemeAlias = Optional.empty();
        this.options = new HashMap<>();
        this.catalog = new DefaultCamelCatalog(false);

        try {
            this.definition = ComponentDefinition.forScheme(catalog, componentScheme);
        } catch (IOException e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }

        registerExtension(this::getComponentVerifierExtension);
    }

    public void setOptions(Map<String, Object> options) {
        this.options.clear();
        this.options.putAll(options);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        // grab the regular query parameters
        Map<String, String> options = buildEndpointOptions(remaining, parameters);

        // create the uri of the base component
        String delegateUri = catalog.asEndpointUri(componentSchemeAlias.orElse(componentScheme), options, false);
        Endpoint delegate = getCamelContext().getEndpoint(delegateUri);

        LOGGER.info("Connector resolved: {} -> {}", URISupport.sanitizeUri(uri), URISupport.sanitizeUri(delegateUri));

        ComponentProxyEndpoint answer = new ComponentProxyEndpoint(uri, this, delegate);
        answer.setBeforeProducer(getBeforeProducer());
        answer.setAfterProducer(getAfterProducer());
        answer.setBeforeConsumer(getBeforeConsumer());
        answer.setAfterConsumer(getAfterConsumer());

        // clean-up parameters so that validation won't fail later on
        // in DefaultConnectorComponent.validateParameters()
        parameters.clear();

        return answer;
    }

    @Override
    protected void doStart() throws Exception {
        Optional<Component> component = createNewBaseComponent();
        if (component.isPresent()) {
            componentSchemeAlias = Optional.of(componentScheme + "-" + componentId);

            if (!catalog.findComponentNames().contains(componentSchemeAlias.get())) {
                catalog.addComponent(
                    componentSchemeAlias.get(),
                    definition.getComponent().getJavaType(),
                    catalog.componentJSonSchema(componentScheme)
                );
            }

            LOGGER.info("Register component: {} (type: {}) with scheme: {} and alias: {}",
                this.componentId,
                component.get().getClass().getName(),
                this.componentScheme,
                this.componentSchemeAlias.get()
            );

            // remove old component if present so
            getCamelContext().removeComponent(this.componentSchemeAlias.get());

            // ensure component is started and stopped when Camel shutdown
            getCamelContext().addService(component, true, true);
            getCamelContext().addComponent(this.componentSchemeAlias.get(), component.get());
        } else {
            componentSchemeAlias = Optional.empty();
        }

        LOGGER.debug("Starting connector: {}", componentId);
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        if (componentSchemeAlias.isPresent()) {
            LOGGER.debug("Stopping component: {}", componentSchemeAlias.get());
            getCamelContext().removeComponent(componentSchemeAlias.get());
        }

        LOGGER.debug("Stopping connector: {}", componentId);
        super.doStop();
    }

    public Processor getBeforeProducer() {
        return beforeProducer;
    }


    public void setBeforeProducer(Processor beforeProducer) {
        this.beforeProducer = beforeProducer;
    }

    public Processor getAfterProducer() {
        return afterProducer;
    }

    public void setAfterProducer(Processor afterProducer) {
        this.afterProducer = afterProducer;
    }

    public Processor getBeforeConsumer() {
        return beforeConsumer;
    }

    public void setBeforeConsumer(Processor beforeConsumer) {
        this.beforeConsumer = beforeConsumer;
    }

    public Processor getAfterConsumer() {
        return afterConsumer;
    }

    public void setAfterConsumer(Processor afterConsumer) {
        this.afterConsumer = afterConsumer;
    }

    public String getComponentId() {
        return componentId;
    }

    public String getComponentScheme() {
        return componentScheme;
    }

    // ***************************************
    // Helpers
    // ***************************************

    private <T> void doAddOption(Map<String, T> options, String name, T value) {
        LOGGER.trace("Adding option: {}={}", name, value);
        T val = options.put(name, value);
        if (val != null) {
            LOGGER.debug("Options {} overridden, old value was {}", name, val);
        }
    }

    /**
     * Create the endpoint instance which either happens with a new base component
     * which has been pre-configured for this connector or we fallback and use
     * the default component in the camel context
     */
    private Optional<Component> createNewBaseComponent() throws Exception {
        final String componentClass = definition.getComponent().getJavaType();
        final CamelContext context = getCamelContext();

        // configure component with extra options
        if (componentClass != null && !options.isEmpty()) {
            // Get the list of options from the connector catalog that
            // are configured to target the endpoint
            Collection<String> endpointOptions = definition.getEndpointProperties().keySet();

            // Check if any of the option applies to the component, if not
            // there's no need to create a dedicated component.
            Collection<Map.Entry<String, Object>> entries = options.entrySet().stream()
                .filter(e -> !endpointOptions.contains(e.getKey()))
                .collect(Collectors.toList());

            if (!entries.isEmpty()) {
                // create a new instance of this base component
                final Class<Component> type = context.getClassResolver().resolveClass(componentClass, Component.class);
                final Component component = context.getInjector().newInstance(type);

                component.setCamelContext(context);

                for (Map.Entry<String, Object> entry : entries) {
                    String key = entry.getKey();
                    Object val = entry.getValue();

                    LOGGER.debug("Using component option: {}={}", key, val);

                    if (val instanceof String) {
                        val = getCamelContext().resolvePropertyPlaceholders((String) val);
                    }

                    IntrospectionSupport.setProperty(context, component, key, val);
                }

                return Optional.of(component);
            }
        }

        return Optional.empty();
    }

    /**
     * Gather all options to use when building the delegate uri.
     */
    private Map<String, String> buildEndpointOptions(String remaining, Map<String, Object> parameters) throws URISyntaxException, NoTypeConversionAvailableException {
        final TypeConverter converter = getCamelContext().getTypeConverter();
        final Map<String, String> endpointOptions = new LinkedHashMap<>();

        // Extract options from options that are supposed to be set at the endpoint
        // level, those options can be overridden and extended using by the query
        // parameters.
        Collection<String> endpointProperties = definition.getEndpointProperties().keySet();
        for (String key : endpointProperties) {
            Object val = this.options.get(key);
            if (val != null) {
                doAddOption(endpointOptions, key, converter.mandatoryConvertTo(String.class, val));
            }
        }

        // options from query parameters
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (entry.getValue() != null) {
                doAddOption(endpointOptions, entry.getKey(), converter.mandatoryConvertTo(String.class, entry.getValue()));
            }
        }

        // add extra options from remaining (context-path)
        if (remaining != null) {
            String targetUri = componentScheme + ":" + remaining;
            Map<String, String> extra = catalog.endpointProperties(targetUri);
            if (extra != null && !extra.isEmpty()) {
                extra.forEach((key, value) -> doAddOption(endpointOptions, key, value));
            }
        }

        return endpointOptions;
    }

    // ***************************************
    // Extensions
    // ***************************************

    @Override
    public Collection<Class<? extends ComponentExtension>> getSupportedExtensions() {
        Set<Class<? extends ComponentExtension>> extensions = new HashSet<>();
        extensions.addAll(super.getSupportedExtensions());
        extensions.addAll(getCamelContext().getComponent(componentScheme, true, false).getSupportedExtensions())   ;

        return extensions;
    }

    @Override
    public <T extends ComponentExtension> Optional<T> getExtension(Class<T> extensionType) {
        // first try to grab extensions from component proxy
        Optional<T> extension = super.getExtension(extensionType);

        if (!extension.isPresent()) {
            // then try to grab it from component.
            extension = getCamelContext().getComponent(componentScheme, true, false).getExtension(extensionType);
        }

        return extension;
    }

    /**
     * Build a ComponentVerifierExtension using options bound to this component.
     */
    private ComponentVerifierExtension getComponentVerifierExtension() {
        try {
            //
            final Component component = getCamelContext().getComponent(componentScheme, true, false);
            final Optional<ComponentVerifierExtension> extension = component.getExtension(ComponentVerifierExtension.class);

            if (extension.isPresent()) {
                return (ComponentVerifierExtension.Scope scope, Map<String, Object> map) -> {
                    Map<String, Object> options;

                    try {
                        // A little nasty hack required as verifier uses Map<String, Object>
                        // to be compatible with all the methods in CamelContext whereas
                        // catalog deals with Map<String, String>
                        options = Map.class.cast(buildEndpointOptions(null, map));
                    } catch (URISyntaxException | NoTypeConversionAvailableException e) {
                        // If a failure is detected while reading the catalog, wrap it
                        // and stop the validation step.
                        return ResultBuilder.withStatusAndScope(ComponentVerifierExtension.Result.Status.OK, scope)
                            .error(ResultErrorBuilder.withException(e).build())
                            .build();
                    }

                    return extension.get().verify(scope, options);
                };
            } else {
                return (scope, map) -> {
                    return ResultBuilder.withStatusAndScope(ComponentVerifierExtension.Result.Status.UNSUPPORTED, scope)
                        .error(
                            ResultErrorBuilder.withCode(ComponentVerifierExtension.VerificationError.StandardCode.UNSUPPORTED)
                                .detail("camel_connector_id", componentId)
                                .detail("camel_component_scheme", componentScheme)
                                .detail("camel_component_scheme_alias", componentSchemeAlias)
                                .build())
                        .build();
                };
            }
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            return (scope, map) -> {
                return ResultBuilder.withStatusAndScope(ComponentVerifierExtension.Result.Status.OK, scope)
                    .error(ResultErrorBuilder.withException(e).build())
                    .build();
            };
        }
    }
}

