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

import java.io.InputStream;
import java.util.Map;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.DynamicActionMetadata;
import io.syndesis.server.api.generator.ConnectorAndActionGenerator;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;

public class LocalMetadataCommandAction extends HystrixCommand<DynamicActionMetadata> {

    private static final DataShape NO_SHAPE = new DataShape.Builder()
        .kind(DataShapeKinds.NONE)
        .build();

    private final ConnectorAction action;

    private final Connector connector;

    private final ConnectorAndActionGenerator generator;

    private final Map<String, String> parameters;

    private final InputStream specificationStream;

    public LocalMetadataCommandAction(final ConnectorAndActionGenerator generator, final Connector connector, final ConnectorAction action,
        final Map<String, String> parameters, final InputStream specificationStream) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("LocalMeta"))
            .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                .withCoreSize(3))
            .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                .withExecutionTimeoutInMilliseconds(5000)));
        this.generator = generator;
        this.connector = connector;
        this.action = action;
        this.parameters = parameters;
        this.specificationStream = specificationStream;
    }

    @Override
    protected DynamicActionMetadata run() throws Exception {
        try (InputStream in = specificationStream) {
            final ConnectorAction generated = generator.generateAction(connector, action, parameters, in);

            return new DynamicActionMetadata.Builder()
                .inputShape(generated.getInputDataShape().orElse(NO_SHAPE))
                .outputShape(generated.getOutputDataShape().orElse(NO_SHAPE))
                .build();
        }
    }

}
