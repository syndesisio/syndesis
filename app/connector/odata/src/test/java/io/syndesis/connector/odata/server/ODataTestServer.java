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
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import io.syndesis.connector.odata.ODataConstants;

public class ODataTestServer extends Server implements ODataConstants {

    public enum Options {
        AUTH_USER,
        SSL
    }

    public static final String USER_PASSWORD = "user1234!";
    public static final String USER = "user";

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

        this.addLifeCycleListener(new AbstractLifeCycleListener() {

            @Override
            public void lifeCycleStopped(LifeCycle event) {
                // Ensure storage is destroyed on stopping
                Storage.dispose();
            }
        });

        initServer(sslContext, userName, password);
    }

    private String serverBaseUrl(Server server) {
        return server.getURI().toString();
    }

    private static KeyStore serverStore() throws Exception {
        final KeyStore store = KeyStore.getInstance("jks");
        URL url = ODataTestServer.class.getResource(KEYSTORE);
        assertNotNull(url);
        InputStream inputStream = url.openStream();
        try {
            store.load(inputStream, KEYPASS_AND_STOREPASS_VALUE);
        } finally {
            inputStream.close();
        }

        return store;
    }

    private static KeyManager[] serverKeyManagers(KeyStore store, final char[] password) throws Exception {
           KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
           keyManagerFactory.init(store, password);
           return keyManagerFactory.getKeyManagers();
       }

    private static TrustManager[] serverTrustManagers(KeyStore store) throws Exception {
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

        if (sslContext == null) {
            // HTTP
            ServerConnector http = new ServerConnector(this);
            http.setPort(8090);
            this.addConnector(http);
        }
        else {
            // HTTPS
            HttpConfiguration httpConfiguration = new HttpConfiguration();
            httpConfiguration.setSecureScheme("https");
            httpConfiguration.setSecurePort(8091);

            final SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setSslContext(sslContext);
            ServerConnector https = new ServerConnector(this, sslContextFactory, new HttpConnectionFactory(httpConfiguration));
            this.addConnector(https);
        }
    }

    public String serviceUrl() {
        return serverBaseUrl(this) + PRODUCTS_SVC;
    }

    public String methodName() {
        return PRODUCTS;
    }
}
