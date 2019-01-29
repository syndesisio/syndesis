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
package io.syndesis.connector.odata.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Component;
import org.apache.camel.Endpoint;
import org.apache.camel.component.olingo4.Olingo4AppEndpointConfiguration;
import org.apache.camel.component.olingo4.Olingo4Component;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import io.syndesis.connector.odata.ODataConstants;
import io.syndesis.connector.odata.ODataUtil;
import io.syndesis.connector.odata.PropertyBuilder;
import io.syndesis.integration.component.proxy.ComponentDefinition;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;

final class ODataComponent extends ComponentProxyComponent implements ODataConstants {

    /**
     * These fields are populated using reflection by the HandlerCustomizer class.
     *
     * The values are resolved then the appropriate setter called, while the original
     * key/value pairs are removed from the options map.
     *
     * Note 1:
     * Should a property be secret then its raw value is the property placeholder and
     * the resolving process converts it accordingly hence the importance of doing it
     * this way rather than using the options map directly.
     *
     * Note 2:
     * apiName not included as not required here but required later in the options map.
     */
    private String methodName;
    private String serviceUri;
    private String basicUserName;
    private String basicPassword;
    private String clientCertificate;
    private String keyPredicate;
    private String queryParams;
    private boolean filterAlreadySeen;

    // Consumer properties
    private long delay = -1;
    private long initialDelay = -1;
    private int backoffIdleThreshold = -1;
    private int backoffMultiplier = -1;

    ODataComponent(String componentId, String componentScheme) {
        super(componentId, componentScheme);
    }

    public String getMethodName() {
        return methodName;
    }


    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }


    public String getServiceUri() {
        return serviceUri;
    }


    public void setServiceUri(String serviceUri) {
        this.serviceUri = serviceUri;
    }


    public String getBasicUserName() {
        return basicUserName;
    }


    public void setBasicUserName(String basicUserName) {
        this.basicUserName = basicUserName;
    }


    public String getBasicPassword() {
        return basicPassword;
    }


    public void setBasicPassword(String basicPassword) {
        this.basicPassword = basicPassword;
    }


    public String getClientCertificate() {
        return clientCertificate;
    }

    public void setClientCertificate(String clientCertificate) {
        this.clientCertificate = clientCertificate;
    }

    public String getKeyPredicate() {
        return keyPredicate;
    }

    public void setKeyPredicate(String keyPredicate) {
        this.keyPredicate = keyPredicate;
    }

    public String getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    @SuppressWarnings("PMD")
    public boolean getFilterAlreadySeen() {
        return filterAlreadySeen ;
    }

    public void setFilterAlreadySeen(boolean filterAlreadySeen) {
        this.filterAlreadySeen = filterAlreadySeen;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public int getBackoffIdleThreshold() {
        return backoffIdleThreshold;
    }

    public void setBackoffIdleThreshold(int backoffIdleThreshold) {
        this.backoffIdleThreshold = backoffIdleThreshold;
    }

    public int getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public void setBackoffMultiplier(int backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
    }

    private Map<String, Object> bundleOptions() {
        PropertyBuilder<Object> builder = new PropertyBuilder<Object>();
        return builder
            .propertyIfNotNull(METHOD_NAME, getMethodName())
            .propertyIfNotNull(SERVICE_URI, getServiceUri())
            .propertyIfNotNull(BASIC_USER_NAME, getBasicUserName())
            .propertyIfNotNull(BASIC_PASSWORD, getBasicPassword())
            .propertyIfNotNull(CLIENT_CERTIFICATE, getClientCertificate())
            .propertyIfNotNull(KEY_PREDICATE, getKeyPredicate())
            .propertyIfNotNull(QUERY_PARAMS, getQueryParams())
            .build();
    }

    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    @Override
    protected Optional<Component> createDelegateComponent(ComponentDefinition definition, Map<String, Object> options) throws Exception {
        Olingo4Component component = new Olingo4Component(getCamelContext());
        Olingo4AppEndpointConfiguration configuration = new Olingo4AppEndpointConfiguration();

        //
        // Ensure at least a blank map exists for this property
        //
        Map<String, String> httpHeaders = new HashMap<>();
        configuration.setEndpointHttpHeaders(httpHeaders);

        Map<String, Object> resolvedOptions = bundleOptions();
        HttpClientBuilder httpClientBuilder = ODataUtil.createHttpClientBuilder(resolvedOptions);
        configuration.setHttpClientBuilder(httpClientBuilder);

        HttpAsyncClientBuilder httpAsyncClientBuilder = ODataUtil.createHttpAsyncClientBuilder(resolvedOptions);
        configuration.setHttpAsyncClientBuilder(httpAsyncClientBuilder);
        configuration.setSslContextParameters(ODataUtil.createSSLContextParameters(resolvedOptions));

        if (getServiceUri() != null) {
            configuration.setServiceUri(getServiceUri());
        }

        //
        // keyPredicate is not supported properly in 2.21.0 but is handled
        // in 2.24.0 by setting it directly on the configuration. Can modify
        // this when component dependencies are upgraded.
        //
        String methodName = getMethodName();
        if (getKeyPredicate() != null) {
            String keyPredicate = getKeyPredicate();
            if (! keyPredicate.startsWith(OPEN_BRACKET)) {
                keyPredicate = OPEN_BRACKET + keyPredicate;
            }
            if (! keyPredicate.endsWith(CLOSE_BRACKET)) {
                keyPredicate = keyPredicate + CLOSE_BRACKET;
            }

            methodName = methodName + keyPredicate;
        }
        options.put(METHOD_NAME, methodName);

        //
        // Modify the query parameters into the expected map
        //
        Map<String, String> queryParams = new HashMap<>();
        if (getQueryParams() != null) {
            String queryString = getQueryParams();
            String[] clauses = queryString.split(AMPERSAND, -1);
            if (clauses.length >= 1) {
                for (String clause : clauses) {
                    String[] parts = clause.split(EQUALS, -1);
                    if (parts.length == 2) {
                        queryParams.put(parts[0], parts[1]);
                    } else if (parts.length < 2) {
                        queryParams.put(parts[0], EMPTY_STRING);
                    }
                    // A clause with more than 1 '=' would be invalid
                }
            }
        }

        configuration.setQueryParams(queryParams);
        configuration.setFilterAlreadySeen(getFilterAlreadySeen());

        component.setConfiguration(configuration);
        return Optional.of(component);
    }

    @Override
    protected Endpoint createDelegateEndpoint(
                                              ComponentDefinition definition, String scheme, Map<String, String> options)
                                              throws Exception {

        Endpoint endpoint = super.createDelegateEndpoint(definition, scheme, options);

        /**
         * Need to apply these consumer properties after the creation
         * of the delegate endpoint since the Olingo4Endpoint swallows
         * properties not explicitly outlined by the olingo4 definition.
         */
        Map<String, Object> properties = new HashMap<>();
        if (getInitialDelay() > -1) {
            properties.put(CONSUMER + DOT + INITIAL_DELAY, Long.toString(getInitialDelay()));
        }

        if (getDelay() > -1) {
            properties.put(CONSUMER + DOT + DELAY, Long.toString(getDelay()));
        }

        if (getBackoffIdleThreshold() > -1) {
            properties.put(CONSUMER + DOT + BACKOFF_IDLE_THRESHOLD, Integer.toString(getBackoffIdleThreshold()));
        }

        if (getBackoffMultiplier() > -1) {
            properties.put(CONSUMER + DOT + BACKOFF_MULTIPLIER, Integer.toString(getBackoffMultiplier()));
        }

        if (! properties.isEmpty()) {
            endpoint.configureProperties(properties);
        }

        return endpoint;
    }
}
