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
package io.syndesis.dv.openshift;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

public class SyndesisHttpClient implements Closeable{
    private CloseableHttpClient client;

    public SyndesisHttpClient() throws IOException {
        this.client = buildHttpClient();
    }

    private CloseableHttpClient buildHttpClient() throws IOException {
        try {
            // no verification of host for now.
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (certificate, authType) -> true)
                    .build();

            CloseableHttpClient client = HttpClients.custom().setSSLContext(sslContext)
                    .setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
            return client;
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new IOException("Failed to create the Http client to access Syndesis server", e);
        }
    }

    private static void addDefaultHeaders(HttpRequestBase request) {
        request.addHeader("Accept", "application/json");
        request.addHeader("X-Forwarded-User", "user");
        request.addHeader("SYNDESIS-XSRF-TOKEN", "awesome");
        request.addHeader("X-Forwarded-Access-Token", "supersecret");
        request.addHeader("Content-Type", "application/json");
    }

    public InputStream executeGET(String url, Header... headers) throws IOException{
        HttpGet request = new HttpGet(url);
        if (headers != null) {
            for (Header header : headers) {
                request.addHeader(header);
            }
        }
        addDefaultHeaders(request);

        HttpResponse response = this.client.execute(request);
        ResponseHandler<InputStream> handler = new AbstractResponseHandler<InputStream>(){
            @Override
            public InputStream handleEntity(final HttpEntity entity) throws IOException {
                return entity.getContent();
            }
        };
        InputStream result = handler.handleResponse(response);
        return result;
    }

    public InputStream executePOST(String url, String payload) throws IOException{
        HttpPost request = new HttpPost(url);
        addDefaultHeaders(request);
        request.setEntity(new StringEntity(payload));
        HttpResponse response = this.client.execute(request);
        ResponseHandler<InputStream> handler = new AbstractResponseHandler<InputStream>(){
            @Override
            public InputStream handleEntity(final HttpEntity entity) throws IOException {
                return entity.getContent();
            }
        };
        InputStream result = handler.handleResponse(response);
        return result;
    }

    public InputStream executeDELETE(String url) throws IOException {
        HttpDelete request = new HttpDelete(url);
        addDefaultHeaders(request);
        HttpResponse response = this.client.execute(request);
        ResponseHandler<InputStream> handler = new AbstractResponseHandler<InputStream>(){
            @Override
            public InputStream handleEntity(final HttpEntity entity) throws IOException {
                return entity.getContent();
            }
        };
        InputStream result = handler.handleResponse(response);
        return result;
    }

    @Override
    public void close() throws IOException {
        if (this.client != null) {
            this.client.close();
        }
    }
}
