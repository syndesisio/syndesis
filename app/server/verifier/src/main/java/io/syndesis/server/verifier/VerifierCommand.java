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
package io.syndesis.server.verifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import io.syndesis.common.util.json.JsonUtils;

import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;

final class VerifierCommand extends HystrixCommand<List<Verifier.Result>> {

    private static final List<Verifier.Result> ERROR = Collections.singletonList(ImmutableResult.builder()
        .status(Verifier.Result.Status.ERROR)
        .scope(Verifier.Scope.CONNECTIVITY)
        .addErrors(ImmutableVerifierError.builder()
            .code("SYNDESIS000")
            .description("Unable to perform verification")
            .build())
        .build());

    private static final GenericType<List<Verifier.Result>> LIST_OF_RESULT = new GenericType<List<Verifier.Result>>() {
        // type token
    };

    final MetadataConfigurationProperties config;

    final String connectorId;

    final Map<String, String> options;

    final ResteasyProviderFactory providerFactory;

    VerifierCommand(final MetadataConfigurationProperties config, final String connectorId, final Map<String, String> options) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("Meta"))
            .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                .withCoreSize(config.getThreads()))
            .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                .withExecutionTimeoutInMilliseconds(config.getTimeout())));

        this.config = config;
        this.connectorId = connectorId;
        this.options = options;

        final ResteasyJackson2Provider resteasyJacksonProvider = new ResteasyJackson2Provider();

        resteasyJacksonProvider.setMapper(JsonUtils.copyObjectMapperConfiguration());

        providerFactory = ResteasyProviderFactory.newInstance();
        providerFactory.register(resteasyJacksonProvider);
    }

    Client newClient() {
        return ClientBuilder.newClient(providerFactory);
    }

    @Override
    protected List<Verifier.Result> getFallback() {
        return ERROR;
    }

    @Override
    protected List<Verifier.Result> run() throws Exception {
        final String url = String.format("http://%s/api/v1/verifier/%s", config.getService(), connectorId);

        final Client client = newClient();
        try {
            final WebTarget target = client.target(url);

            return target.request(MediaType.APPLICATION_JSON).post(
                Entity.entity(options, MediaType.APPLICATION_JSON),
                LIST_OF_RESULT);
        } finally {
            client.close();
        }
    }
}
