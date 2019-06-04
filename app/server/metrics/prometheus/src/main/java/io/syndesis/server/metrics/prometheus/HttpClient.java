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
package io.syndesis.server.metrics.prometheus;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.jboss.resteasy.client.jaxrs.internal.LocalResteasyProviderFactory;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import java.util.concurrent.TimeUnit;

public class HttpClient {

    /**
     * Avoid thread remaining stuck waiting for prometheus to come back.
     */
    private static final int CONNECT_TIMEOUT = 15;
    private static final int READ_TIMEOUT = 15;

    private final Client client;

    public HttpClient() {
        this.client = createClient();

    }
    public QueryResult queryPrometheus(HttpQuery query) {
        return client.target(query.getUriBuilder()).request(MediaType.APPLICATION_JSON).get(QueryResult.class);
    }

    public void close() {
        this.client.close();
    }

    // ************
    // Helper
    // ************

    private static Client createClient() {
        final ObjectMapper mapper = new ObjectMapper().registerModules(new Jdk8Module());
        final ResteasyJackson2Provider resteasyJacksonProvider = new ResteasyJackson2Provider();

        resteasyJacksonProvider.setMapper(mapper);

        final ResteasyProviderFactory providerFactory = new ResteasyProviderFactory();
        providerFactory.register(resteasyJacksonProvider);

        final Configuration configuration = new LocalResteasyProviderFactory(providerFactory);

        return ClientBuilder.newBuilder()
            .withConfig(configuration)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .build();
    }
}
