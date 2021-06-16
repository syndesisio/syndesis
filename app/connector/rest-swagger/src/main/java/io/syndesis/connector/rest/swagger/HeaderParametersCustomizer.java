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
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20ParameterDefinition;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Parameter;
import io.apicurio.datamodels.openapi.v3.models.Oas30ParameterDefinition;
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

            final OasParameter resolved;
            if (node instanceof Oas20Parameter) {
                final Oas20Parameter oas20Parameter = (Oas20Parameter) node;

                resolved = resolveParameter(oas20Parameter);
            } else if (node instanceof Oas30Parameter) {
                final Oas30Parameter oas30Parameter = (Oas30Parameter) node;

                resolved = resolveParameter(oas30Parameter);
            } else {
                throw new IllegalArgumentException("Given parameter type is not OpenAPI 2.x or 3.x" + node.getClass().getName());
            }

            if (resolved != null && "header".equals(resolved.in)) {
                operationHeaderParameters.add(resolved.getName());
            }
        }

        private static String nameFromReference(final String ref) {
            return ref.replaceAll("^.*/", "");
        }

        private static OasParameter resolveParameter(final Oas20Parameter parameter) {
            if (parameter.$ref == null) {
                return parameter;
            }

            final Oas20Document document = (Oas20Document) parameter.ownerDocument();

            if (document.parameters == null) {
                return null;
            }

            final String name = nameFromReference(parameter.$ref);

            final Oas20ParameterDefinition resolvedParameter = document.parameters.getItem(name);

            if (resolvedParameter == null) {
                return null;
            }

            return resolvedParameter;
        }

        private static OasParameter resolveParameter(final Oas30Parameter parameter) {
            if (parameter.$ref == null) {
                return parameter;
            }

            final Oas30Document document = (Oas30Document) parameter.ownerDocument();

            if (document.components == null || document.components.parameters == null) {
                return null;
            }

            final String name = nameFromReference(parameter.$ref);

            final Oas30ParameterDefinition resolvedParameter = document.components.parameters.get(name);

            if (resolvedParameter == null) {
                return null;
            }

            return resolvedParameter;
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
