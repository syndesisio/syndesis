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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.combined.visitors.CombinedAllNodeVisitor;
import io.apicurio.datamodels.core.factories.TraverserFactory;
import io.apicurio.datamodels.core.models.Document;
import io.apicurio.datamodels.core.models.common.Operation;
import io.apicurio.datamodels.core.models.common.Parameter;
import io.apicurio.datamodels.core.visitors.ITraverser;
import io.apicurio.datamodels.core.visitors.TraverserDirection;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v3.models.Oas30Parameter;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import io.syndesis.integration.component.proxy.Processors;
import io.syndesis.integration.runtime.util.SyndesisHeaderStrategy;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Makes sure that header parameters are allowed, i.e. passed from Camel to the
 * API service. We only pass headers that we explicitly allow, to make sure that
 * we do not pass sensitive headers or headers that might interfere with the API
 * service (i.e. setting `Connection: close` or `Content-Length` with
 * inappropriate content length).
 */
public class HeaderParametersCustomizer implements ComponentProxyCustomizer {

    public static class AllowHeaders implements Processor {

        private final Set<String> headerParameters;

        public AllowHeaders(final Set<String> headerParameters) {
            this.headerParameters = headerParameters;
        }

        @Override
        public void process(final Exchange exchange) throws Exception {
            SyndesisHeaderStrategy.allow(exchange, headerParameters);
        }
    }

    private static final class OperationHeaderParameterCollector extends CombinedAllNodeVisitor {

        private String currentOperation;

        private final Set<String> operationHeaderParameters = new HashSet<>();

        private final String operationId;

        public OperationHeaderParameterCollector(final String operationId) {
            this.operationId = operationId;
        }

        @Override
        public void visitOperation(final Operation node) {
            currentOperation = node.operationId;
        }

        @Override
        public void visitParameter(final Parameter node) {
            if (!operationId.equals(currentOperation)) {
                return;
            }

            final String in;
            if (node instanceof Oas20Parameter) {
                final Oas20Parameter oas20Parameter = (Oas20Parameter) node;
                in = oas20Parameter.in;
            } else if (node instanceof Oas30Parameter) {
                final Oas30Parameter oas30Parameter = (Oas30Parameter) node;
                in = oas30Parameter.in;
            } else {
                throw new IllegalArgumentException("Given parameter type is not OpenAPI 2.x or 3.x" + node.getClass().getName());
            }

            if ("header".equals(in)) {
                operationHeaderParameters.add(node.name);
            }
        }
    }

    @Override
    public void customize(final ComponentProxyComponent component, final Map<String, Object> options) {
        // set by SpecificationResourceCustomizer
        final String specification = (String) options.get("specification");

        final String operationId = (String) options.get("operationId");

        final Set<String> headerParameters = findHeaderParametersFor(specification, operationId);

        Processors.addBeforeProducer(component, new AllowHeaders(headerParameters));
    }

    static Set<String> findHeaderParametersFor(final String specification, final String operationId) {
        final Document document = Library.readDocumentFromJSONString(specification);

        final OperationHeaderParameterCollector headerParameterCollector = new OperationHeaderParameterCollector(operationId);
        final ITraverser traverser = TraverserFactory.create(document, headerParameterCollector, TraverserDirection.down);

        traverser.traverse(document);

        return headerParameterCollector.operationHeaderParameters;
    }

}
