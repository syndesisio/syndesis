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
package io.syndesis.connector.odata.server;

import static org.junit.Assert.assertNotNull;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.ssl.SSLContexts;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.core.ODataClientFactory;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.syndesis.connector.odata.ODataConstants;

public class ODataTestServer extends Server implements ODataConstants {

    public enum Options {
        AUTH_USER,
        SSL,
        HTTP_PORT,
        HTTPS_PORT
    }

    public static final String USER_PASSWORD = "user1234!";
    public static final String USER = "user";

    private static final Logger LOG = LoggerFactory.getLogger(ODataTestServer.class);

    private static final String HTTP_PROPERTY = ODataTestServer.class.getSimpleName() + DOT + "httpPort";
    private static final String HTTPS_PROPERTY = ODataTestServer.class.getSimpleName() + DOT + "httpsPort";

    private static final char[] KEYPASS_AND_STOREPASS_VALUE = "redhat".toCharArray();
    private static final String CERTIFICATE = "certs/server-cert.pem";
    private static final String DIFF_CERTIFICATE = "certs/diffserver-cert.pem";
    private static final String REF_SERVICE_CERTIFICATE = "certs/odata-org.pem";
    private static final String KEYSTORE = "jks/server-keystore.jks";
    private static final String PRODUCTS_SVC = "odata4/Products.svc";
    private static final String PRODUCTS = "Products";

    private static String certificate(String certificateName) throws Exception {
        URL url = ODataTestServer.class.getResource(certificateName);
        assertNotNull(url);
        InputStream srcStream = url.openStream();
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = srcStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            return result.toString("UTF-8");
        } finally {
            if (srcStream != null)
                srcStream.close();
        }
    }

    public static String serverCertificate() throws Exception {
        return certificate(CERTIFICATE);
    }

    public static String differentCertificate() throws Exception {
        return certificate(DIFF_CERTIFICATE);
    }

    public static String referenceServiceCertificate() throws Exception {
        return certificate(REF_SERVICE_CERTIFICATE);
    }

    private List<Options> optionsList;

    private int httpPort;
    private int httpsPort;
    private ServerConnector httpConnector;
    private ServerConnector httpsConnector;

    public ODataTestServer(Options... options) throws Exception {
        super();
        this.optionsList = Arrays.asList(options);

        SSLContext sslContext = null;
        String userName = null;
        char[] password = null;

        if (optionsList.contains(Options.SSL)) {
            sslContext = createServerSSLContext();
        }

        if (optionsList.contains(Options.AUTH_USER)) {
            userName = USER;
            password = KEYPASS_AND_STOREPASS_VALUE;
        }

        if (optionsList.contains(Options.HTTP_PORT)) {
            httpPort = extractPort(HTTP_PROPERTY);
        }

        if (optionsList.contains(Options.HTTPS_PORT)) {
            httpsPort = extractPort(HTTPS_PROPERTY);
        }

        this.addLifeCycleListener(new AbstractLifeCycleListener() {

            @Override
            public void lifeCycleStopped(LifeCycle event) {
                // Ensure storage is destroyed on stopping
                Storage.dispose();
            }
        });

        initServer(sslContext, userName, password);
    }

    private int extractPort(String property) {
        String portProperty = System.getProperty(property);
        if (portProperty == null) {
            LOG.warn("Server port option requested but no port specified using property {}. "
                                    + "Falling back to default of finding next random port",
                                        property);
            return 0;
        }

        try {
            return Integer.parseInt(portProperty);
        } catch (NumberFormatException ex) {
            LOG.error("Cannot format the port from the property {}. "
                                    + "Falling back to default of finding next random port",
                                      property);
            return 0;
        }
    }

    private String serverBaseUri(NetworkConnector connector) {
        if (connector == null) {
            return null;
        }

        ContextHandler context = getChildHandlerByClass(ContextHandler.class);

        try {
            String protocol = connector.getDefaultConnectionFactory().getProtocol();
            String scheme = "http";
            if (protocol.startsWith("SSL-") || protocol.equals("SSL"))
                scheme = "https";

            String host = connector.getHost();
            if (context != null && context.getVirtualHosts() != null && context.getVirtualHosts().length > 0)
                host = context.getVirtualHosts()[0];
            if (host == null)
                host = InetAddress.getLocalHost().getHostAddress();

            String path = context == null ? null : context.getContextPath();
            if (path == null) {
                path = FORWARD_SLASH;
            }

            URI uri = new URI(scheme, null, host, connector.getLocalPort(), path, null, null);
            return uri.toString();
        }
        catch(Exception e) {
            LOG.error("Uri error", e);
            return null;
        }
    }

    private KeyStore serverStore() throws Exception {
        final KeyStore store = KeyStore.getInstance("jks");
        InputStream urlStream = getClass().getResourceAsStream(KEYSTORE);
        assertNotNull(urlStream);
        try {
            store.load(urlStream, KEYPASS_AND_STOREPASS_VALUE);
        } finally {
            urlStream.close();
        }

        return store;
    }

    private KeyManager[] serverKeyManagers(KeyStore store, final char[] password) throws Exception {
           KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
           keyManagerFactory.init(store, password);
           return keyManagerFactory.getKeyManagers();
       }

    private TrustManager[] serverTrustManagers(KeyStore store) throws Exception {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(store);
        return trustManagerFactory.getTrustManagers();
    }

    private SSLContext createServerSSLContext() throws Exception {
        KeyStore serverKeyStore = serverStore();
        KeyManager[] serverKeyManagers = serverKeyManagers(serverKeyStore, KEYPASS_AND_STOREPASS_VALUE);
        TrustManager[] serverTrustManagers = serverTrustManagers(serverKeyStore);

        SSLContext sslContext = SSLContexts.custom().setProtocol("TLS").build();
        sslContext.init(serverKeyManagers, serverTrustManagers, new SecureRandom());

        return sslContext;
    }

    private void initServer(SSLContext sslContext, String userName, char[] password) throws UnknownHostException {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(FORWARD_SLASH);
        this.setHandler(context);

        ServletHandler productsHandler = new ServletHandler();
        productsHandler.addServletWithMapping(
            ProductsServlet.class,
            FORWARD_SLASH + PRODUCTS_SVC + FORWARD_SLASH + STAR);
        productsHandler.addFilterWithMapping(ODataPathFilter.class, FORWARD_SLASH + STAR, FilterMapping.REQUEST);
        context.insertHandler(productsHandler);

        if (userName != null) {
            LoginService loginService = new HashLoginService("MyRealm", "src/test/resources/realm.properties");
            this.addBean(loginService);

            ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
            Constraint constraint = new Constraint();
            constraint.setName("auth");
            constraint.setAuthenticate(true);
            constraint.setRoles(new String[] { USER, "admin" });

            ConstraintMapping mapping = new ConstraintMapping();
            mapping.setPathSpec(FORWARD_SLASH + PRODUCTS_SVC + FORWARD_SLASH + STAR);
            mapping.setConstraint(constraint);

            securityHandler.setConstraintMappings(Collections.singletonList(mapping));
            securityHandler.setAuthenticator(new BasicAuthenticator());

            context.setSecurityHandler(securityHandler);
        }

        httpConnector = new ServerConnector(this);
        httpConnector.setPort(httpPort); // Finds next available port if still 0
        this.addConnector(httpConnector);


        if (sslContext != null) {
            // HTTPS
            HttpConfiguration httpConfiguration = new HttpConfiguration();
            httpConfiguration.setSecureScheme("https");
            httpConfiguration.setSecurePort(httpsPort); // Finds next available port if still 0
            httpConfiguration.addCustomizer(new SecureRequestCustomizer());

            final SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setSslContext(sslContext);
            httpsConnector = new ServerConnector(this, sslContextFactory, new HttpConnectionFactory(httpConfiguration));
            httpsConnector.setPort(httpsPort); // Finds next available port if still 0
            this.addConnector(httpsConnector);
        }
    }

    public String servicePlainUri() {
        if (httpConnector == null) {
            return null;
        }

        return serverBaseUri(httpConnector) + PRODUCTS_SVC;
    }

    public String serviceSSLUri() {
        if (httpsConnector == null) {
            return null;
        }

        return serverBaseUri(httpsConnector) + PRODUCTS_SVC;
    }

    public String resourcePath() {
        return PRODUCTS;
    }

    public int getResultCount() {
        return Storage.getInstance().getCount();
    }

    public ClientEntitySetIterator<ClientEntitySet, ClientEntity> getData() {
        ODataClient client = ODataClientFactory.getClient();

        String serviceUri = serviceSSLUri();
        if (serviceUri == null) {
            serviceUri = servicePlainUri();
        }

        URI customersUri = client.newURIBuilder(serviceUri)
                                                      .appendEntitySetSegment(resourcePath()).build();

        ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response
            = client.getRetrieveRequestFactory().getEntitySetIteratorRequest(customersUri).execute();

        return response.getBody();
    }

    public ClientEntity getData(String keyPredicate) {
        String resourcePath = resourcePath() + OPEN_BRACKET + keyPredicate + CLOSE_BRACKET;

        String serviceUri = serviceSSLUri();
        if (serviceUri == null) {
            serviceUri = servicePlainUri();
        }

        ODataClient client = ODataClientFactory.getClient();
        URI customersUri = client.newURIBuilder(serviceUri)
                                                      .appendEntitySetSegment(resourcePath).build();

        ODataRetrieveResponse<ClientEntity> response
            = client.getRetrieveRequestFactory().getEntityRequest(customersUri).execute();

        return response.getBody();
    }

    public void reset() {
        Storage.getInstance().reload();
    }

    /**
     * Run our own OData Server
     *
     * @param args
     */
    public static void main(String... args) throws Exception {
       ODataTestServer server = new ODataTestServer(Options.HTTP_PORT, Options.HTTPS_PORT, Options.SSL);
       server.start();

       LOG.info("Server running with service urls:\n\t\t* {}\n\t\t* {}", server.servicePlainUri(), server.serviceSSLUri());
    }
}
