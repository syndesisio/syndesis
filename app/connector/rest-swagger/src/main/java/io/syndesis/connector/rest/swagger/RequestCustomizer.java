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
package io.syndesis.connector.rest.swagger;

import java.util.Map;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.InputDataShapeAware;
import io.syndesis.common.model.OutputDataShapeAware;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import io.syndesis.integration.component.proxy.Processors;

public final class RequestCustomizer implements ComponentProxyCustomizer, InputDataShapeAware, OutputDataShapeAware {

    private DataShape inputDataShape;

    private DataShape outputDataShape;

    @Override
    public void customize(final ComponentProxyComponent component, final Map<String, Object> options) {
        Processors.addBeforeProducer(component, new RequestHeaderSetter(inputDataShape, outputDataShape));
        Processors.addBeforeProducer(component, new RequestPayloadConverter(inputDataShape));
    }

    @Override
    public DataShape getInputDataShape() {
        return inputDataShape;
    }

    @Override
    public DataShape getOutputDataShape() {
        return outputDataShape;
    }

    @Override
    public void setInputDataShape(final DataShape dataShape) {
        inputDataShape = dataShape;
    }

    @Override
    public void setOutputDataShape(final DataShape dataShape) {
        outputDataShape = dataShape;
    }

}
