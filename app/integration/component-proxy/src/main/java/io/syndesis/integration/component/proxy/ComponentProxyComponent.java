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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Endpoint;
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
import org.apache.camel.util.StringHelper;
import org.apache.camel.util.URISupport;
import org.apache.camel.util.function.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.GodClass")
public class ComponentProxyComponent extends DefaultComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentProxyComponent.class);

    private final CamelCatalog catalog;
    private final String componentId;
    private final String componentScheme;
    private final Map<String, Object> configuredOptions;
    private final Map<String, Object> remainingOptions;
    private final ComponentDefinition definition;

    private Optional<String> componentSchemeAlias;

    private Processor beforeProducer;
    private Processor afterProducer;
    private Processor beforeConsumer;
    private Processor afterConsumer;


    public ComponentProxyComponent(String componentId, String componentScheme) {
        this(componentId, componentScheme, (String)null, new DefaultCamelCatalog(false));
    }

    public ComponentProxyComponent(String componentId, String componentScheme, CamelCatalog catalog) {
        this(componentId, componentScheme, (String)null, catalog);
    }

    public ComponentProxyComponent(String componentId, String componentScheme, String componentClass) {
        this(componentId, componentScheme, componentClass, new DefaultCamelCatalog(false));
    }

    public ComponentProxyComponent(String componentId, String componentScheme, Class<?> componentClass) {
        this(componentId, componentScheme, componentClass.getName(), new DefaultCamelCatalog(false));
    }

    public ComponentProxyComponent(String componentId, String componentScheme, Class<?> componentClass, CamelCatalog catalog) {
        this(componentId, componentScheme, componentClass.getName(), catalog);
    }

    public ComponentProxyComponent(String componentId, String componentScheme, String componentClass, CamelCatalog catalog) {
        this.componentId = StringHelper.notEmpty(componentId, "componentId");
        this.componentScheme = StringHelper.notEmpty(componentScheme, "componentScheme");
        this.componentSchemeAlias = Optional.empty();
        this.configuredOptions = new HashMap<>();
        this.remainingOptions = new HashMap<>();
        this.catalog = ObjectHelper.notNull(catalog, "catalog");

        if (ObjectHelper.isNotEmpty(componentClass)) {
            this.catalog.addComponent(componentScheme, componentClass);
        }

        try {
            this.definition = ComponentDefinition.forScheme(catalog, componentScheme);
        } catch (IOException e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }

        registerExtension(this::getComponentVerifierExtension);
    }

    public void setOptions(Map<String, Object> options) {
        this.configuredOptions.clear();

        if (ObjectHelper.isNotEmpty(options)) {
            for (Map.Entry<String, Object> entry : options.entrySet()) {
                // Filter out null values
                if (entry.getValue() != null) {
                    this.configuredOptions.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * Allows the definition to be overridden if required by specific components
     *
     * @return definition
     */
    protected ComponentDefinition getDefinition() {
        return definition;
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        // merge parameters
        final Map<String, Object> options = new HashMap<>();
        doAddOptions(options, this.remainingOptions);
        doAddOptions(options, parameters);

        // create the uri of the base component, DO NOT log the computed delegate
        final Map<String, String> endpointOptions = buildEndpointOptions(remaining, options);
        final String endpointScheme = componentSchemeAlias.orElse(componentScheme);
        ComponentDefinition definition = getDefinition();
        final Endpoint delegate = createDelegateEndpoint(definition, endpointScheme, endpointOptions);

        LOGGER.info("Connector resolved: {} -> {}", URISupport.sanitizeUri(uri), URISupport.sanitizeUri(delegate.getEndpointUri()));

        // remove options already set on the endpoint
        options.keySet().removeIf(endpointOptions::containsKey);

        // Configure the delegated endpoint
        configureDelegateEndpoint(definition, delegate, options);

        final ComponentProxyEndpoint answer = new ComponentProxyEndpoint(uri, this, delegate);
        answer.setBeforeProducer(ObjectHelper.trySetCamelContext(getBeforeProducer(), getCamelContext()));
        answer.setAfterProducer(ObjectHelper.trySetCamelContext(getAfterProducer(), getCamelContext()));
        answer.setBeforeConsumer(ObjectHelper.trySetCamelContext(getBeforeConsumer(), getCamelContext()));
        answer.setAfterConsumer(ObjectHelper.trySetCamelContext(getAfterConsumer(), getCamelContext()));

        // clean-up parameters so that validation won't fail later on
        // in DefaultConnectorComponent.validateParameters()
        parameters.clear();

        // remove temporary options
        this.remainingOptions.clear();

        return answer;
    }

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    protected void doStart() throws Exception {
        this.remainingOptions.clear();
        this.remainingOptions.putAll(this.configuredOptions);

        enrichOptions(this.remainingOptions);

        ComponentDefinition definition = getDefinition();
        Optional<Component> component = createDelegateComponent(definition, this.remainingOptions);
        if (component.isPresent()) {

            // Configure the component, options should be removed once consumed
            configureDelegateComponent(definition, component.get(), this.remainingOptions);

            // Create a unique delegate component alias
            componentSchemeAlias = Optional.of(componentScheme + "-" + componentId);

            if (!catalog.findComponentNames().contains(componentSchemeAlias.get())) {
                // Create an alias for new scheme to the delegate component scheme
                // so catalog can be used to build uri
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

            // remove old component if present
            getCamelContext().removeComponent(this.componentSchemeAlias.get());

            if (!getCamelContext().hasService(component.get())) {
                // ensure component is started and stopped when Camel shutdown if
                // not already added
                getCamelContext().addService(component.get(), true, true);
            }

            getCamelContext().addComponent(this.componentSchemeAlias.get(), component.get());
        } else {
            componentSchemeAlias = Optional.empty();
        }

        LOGGER.debug("Starting connector: {}", componentId);
        super.doStart();
    }

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
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

    /**
     * Method used to enrich options before any delegating component/endpoint is created. This is useful
     * when some options need to be enforced.
     */
    protected void enrichOptions(Map<String, Object> options) {
        // no-op
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    protected Optional<Component> createDelegateComponent(ComponentDefinition definition, Map<String, Object> options) throws Exception {
        final String componentClass = definition.getComponent().getJavaType();

        // configure component with extra options
        if (componentClass != null && !options.isEmpty()) {
            // Get the list of options from the connector catalog that
            // are configured to target the endpoint
            final Collection<String> endpointOptions = definition.getEndpointProperties().keySet();

            // Check if any of the option applies to the component, if not
            // there's no need to create a dedicated component.
            boolean hasComponentOptions = options.keySet().stream().anyMatch(Predicates.negate(endpointOptions::contains));

            // Options set on a step are strings so if any of the options is
            // not a string, is should have been added by a customizer so try to
            // bind them to the component first.
            boolean hasPojoOptions = options.values().stream().anyMatch(Predicates.negate(String.class::isInstance));

            if (hasComponentOptions || hasPojoOptions) {
                final CamelContext context = getCamelContext();

                // create a new instance of this base component
                final Class<Component> type = context.getClassResolver().resolveClass(componentClass, Component.class);
                final Component component = context.getInjector().newInstance(type);

                component.setCamelContext(context);

                return Optional.of(component);
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    protected void configureDelegateComponent(ComponentDefinition definition, Component component, Map<String, Object> options) throws Exception {
        final CamelContext context = getCamelContext();
        final List<Map.Entry<String, Object>> entries = new ArrayList<>();

        // Get the list of options from the connector catalog that
        // are configured to target the endpoint
        final Collection<String> endpointOptions = definition.getEndpointProperties().keySet();

        // Check if any of the option applies to the component, if not
        // there's no need to create a dedicated component.
        options.entrySet().stream()
            .filter(e -> !endpointOptions.contains(e.getKey()))
            .forEach(entries::add);

        // Options set on a step are strings so if any of the options is
        // not a string, is should have been added by a customizer so try to
        // bind them to the component first.
        options.entrySet().stream()
            .filter(e -> e.getValue() != null)
            .filter(Predicates.negate(e -> e.getValue() instanceof String))
            .forEach(entries::add);

        if (!entries.isEmpty()) {
            component.setCamelContext(context);

            for (Map.Entry<String, Object> entry : entries) {
                String key = entry.getKey();
                Object val = entry.getValue();

                if (val instanceof String) {
                    val = getCamelContext().resolvePropertyPlaceholders((String) val);
                }

                if (IntrospectionSupport.setProperty(context, component, key, val)) {
                    options.remove(key);
                }
            }
        }
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    protected Endpoint createDelegateEndpoint(ComponentDefinition definition, String scheme, Map<String, String> options) throws Exception {
        // Build the delegate uri using the catalog
        final String uri = catalog.asEndpointUri(scheme, options, false);

        return getCamelContext().getEndpoint(uri);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    protected void configureDelegateEndpoint(ComponentDefinition definition, Endpoint endpoint, Map<String, Object> options) throws Exception {
        // no-op
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    protected Map<String, String> buildEndpointOptions(String remaining, Map<String, Object> options) throws Exception {
        final TypeConverter converter = getCamelContext().getTypeConverter();
        final Map<String, String> endpointOptions = new LinkedHashMap<>();

        // Extract options from options that are supposed to be set at the endpoint
        // level, those options can be overridden and extended using by the query
        // parameters.
        Collection<String> endpointProperties = getDefinition().getEndpointProperties().keySet();
        for (String key : endpointProperties) {
            Object val = options.get(key);
            if (val != null) {
                doAddOption(endpointOptions, key, converter.mandatoryConvertTo(String.class, val));
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

    private <T> void doAddOptions(Map<String, T> destination, Map<String, T> options) {
        options.forEach(
            (k, v) -> doAddOption(destination, k, v)
        );
    }

    private <T> void doAddOption(Map<String, T> options, String name, T value) {
        LOGGER.trace("Adding option: {}={}", name, value);
        T val = options.put(name, value);
        if (val != null) {
            LOGGER.debug("Options {} overridden, old value was {}", name, val);
        }
    }

    protected Object getOption(String key) {
        return configuredOptions.get(key);
    }

    protected CamelCatalog getCatalog() {
        return catalog;
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
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
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
                        @SuppressWarnings("unchecked")
                        Map<String, Object> tmp = Map.class.cast(buildEndpointOptions(null, map));
                        options = tmp;
                    } catch (Exception e) {
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
        } catch (Exception e) {
            return (scope, map) -> {
                return ResultBuilder.withStatusAndScope(ComponentVerifierExtension.Result.Status.OK, scope)
                    .error(ResultErrorBuilder.withException(e).build())
                    .build();
            };
        }
    }
}
