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
package io.syndesis.server.endpoint.v1.handler.connection;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;
import java.util.Map;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.endpoint.v1.SyndesisRestException;
import io.syndesis.server.endpoint.v1.handler.exception.RestError;
import io.syndesis.server.verifier.MetadataConfigurationProperties;

abstract class MetadataCommand<R> extends HystrixCommand<R> {

    private final Map<String, String> parameters;
    private final Class<R> type;

    MetadataCommand(final MetadataConfigurationProperties configuration, Class<R> type, final Map<String, String> parameters ) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("Meta"))//
            .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()//
                .withCoreSize(configuration.getThreads()))//
            .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()//
                .withExecutionTimeoutInMilliseconds(configuration.getTimeout())));
        this.parameters = parameters;
        this.type = type;
    }

    @Override
    protected R run() {
        Client client = null;

        try {
            client = createClient();

            final WebTarget target = client.target(getMetadataURL());
            final Entity<?> entity = Entity.entity(parameters, MediaType.APPLICATION_JSON);

            return target.request(MediaType.APPLICATION_JSON).post(entity, type);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    protected abstract String getMetadataURL();

    protected Client createClient() {

        return ClientBuilder.newClient().register((ClientResponseFilter) (requestContext, responseContext) -> {
            if (responseContext.getStatusInfo().getFamily() == Family.SERVER_ERROR
                && MediaType.APPLICATION_JSON.equals(responseContext.getHeaderString(HttpHeaders.CONTENT_TYPE))) {
                final RestError error = JsonUtils.reader().forType(RestError.class).readValue(responseContext.getEntityStream());

                throw new SyndesisRestException(
                    error.getDeveloperMsg(),
                    error.getUserMsg(),
                    error.getUserMsgDetail(),
                    error.getErrorCode()
                );
            }
        });
    }

}
