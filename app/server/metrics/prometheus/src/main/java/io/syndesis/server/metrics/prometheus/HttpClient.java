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
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import org.jboss.resteasy.client.jaxrs.internal.LocalResteasyProviderFactory;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class HttpClient {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModules(new Jdk8Module());

    public QueryResult queryPrometheus(HttpQuery query) {
        final Client client = createClient();
//        final WebTarget target = client.target(UriBuilder.fromPath(String.format("http://%s/api/v1/query", prometheusMetricsProviderImpl.getServiceName())).queryParam("query", "%7Bintegration=\"" + integrationId + "\",type=\"context\"%7D", integrationId));
        final WebTarget target = client.target(query.getUriBuilder());
        return target.request(MediaType.APPLICATION_JSON).get(QueryResult.class);
    }

    /* default */
    private Client createClient() {
        final ResteasyJackson2Provider resteasyJacksonProvider = new ResteasyJackson2Provider();
        resteasyJacksonProvider.setMapper(MAPPER);

        final ResteasyProviderFactory providerFactory = ResteasyProviderFactory.newInstance();
        providerFactory.register(resteasyJacksonProvider);
        final Configuration configuration = new LocalResteasyProviderFactory(providerFactory);

        return ClientBuilder.newClient(configuration);
    }
}
