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
package io.syndesis.server.controller.integration.camelk.customizer;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.swagger.models.Swagger;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ResourceIdentifier;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.openapi.OpenApiHelper;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.integration.project.generator.ProjectGeneratorHelper;
import io.syndesis.server.controller.ControllersConfigurationProperties;
import io.syndesis.server.controller.integration.camelk.CamelKPublishHandler;
import io.syndesis.server.controller.integration.camelk.CamelKSupport;
import io.syndesis.server.controller.integration.camelk.crd.ConfigurationSpec;
import io.syndesis.server.controller.integration.camelk.crd.DataSpec;
import io.syndesis.server.controller.integration.camelk.crd.Integration;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationSpec;
import io.syndesis.server.controller.integration.camelk.crd.ResourceSpec;
import io.syndesis.server.controller.integration.camelk.crd.SourceSpec;
import io.syndesis.server.openshift.Exposure;
import org.apache.camel.generator.swagger.RestDslXmlGenerator;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

/**
 * Configure OpenApi
 */
@Component
public class OpenApiCustomizer implements CamelKIntegrationCustomizer {
    private final ControllersConfigurationProperties configuration;
    private final IntegrationResourceManager resourceManager;

    public OpenApiCustomizer(
            ControllersConfigurationProperties configuration,
            IntegrationResourceManager resourceManager) {

        this.configuration = configuration;
        this.resourceManager = resourceManager;
    }

    @Override
    public Integration customize(IntegrationDeployment deployment, Integration integration, EnumSet<Exposure> exposure) {
        IntegrationSpec.Builder spec = new IntegrationSpec.Builder();
        if (integration.getSpec() != null) {
            spec = spec.from(integration.getSpec());
        }

        // assuming that we have a single swagger definition for the moment
        Optional<ResourceIdentifier> rid = deployment.getSpec().getResources().stream().filter(Kind.OpenApi::sameAs).findFirst();
        if (!rid.isPresent()) {
            return integration;
        }

        final ResourceIdentifier openApiResource = rid.get();
        final Optional<String> maybeOpenApiResourceId = openApiResource.getId();
        if (!maybeOpenApiResourceId.isPresent()) {
            return integration;
        }

        final String openApiResourceId = maybeOpenApiResourceId.get();
        Optional<OpenApi> res = resourceManager.loadOpenApiDefinition(openApiResourceId);
        if (!res.isPresent()) {
            return integration;
        }

        try {
            spec.addResources(generateOpenAPIResource(res.get()));
            spec.addSources(generateOpenAPIRestDSL(res.get()));
            spec.addSources(generateOpenAPIRestEndpoint());
            spec.addConfiguration(
                new ConfigurationSpec.Builder()
                    .type("property")
                    .value("customizer.servletregistration.enabled=true")
                    .build()
            );
            spec.addConfiguration(
                new ConfigurationSpec.Builder()
                    .type("property")
                    .value("customizer.servletregistration.path=/*")
                    .build()
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        integration.setSpec(spec.build());

        return integration;
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private ResourceSpec generateOpenAPIResource(OpenApi openApi) throws Exception {
        final byte[] openApiBytes = openApi.getDocument();
//        final String content = configuration.getCamelk().isCompression() ? CamelKSupport.compress(openApiBytes) : new String(openApiBytes, UTF_8);

        return new ResourceSpec.Builder()
            .dataSpec(new DataSpec.Builder()
                //we always compress openapi spec document
                .compression(true)
                .name("openapi.json")
                .content(CamelKSupport.compress(openApiBytes))
                .build())
            .type("data")
            .build();
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private SourceSpec generateOpenAPIRestDSL(OpenApi openApi) throws Exception {
        final byte[] openApiBytes = openApi.getDocument();
        final Swagger swagger = OpenApiHelper.parse(new String(openApiBytes, UTF_8));
        final String camelRestDsl = RestDslXmlGenerator.toXml(ProjectGeneratorHelper.normalizePaths(swagger)).generate(new DefaultCamelContext());
        final String content = configuration.getCamelk().isCompression() ? CamelKSupport.compress(camelRestDsl) : camelRestDsl;

        return new SourceSpec.Builder()
            .dataSpec(new DataSpec.Builder()
                .compression(configuration.getCamelk().isCompression())
                .content(content)
                .name("openapi-routes")
                .build())
            .language("xml")
            .build();
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private SourceSpec generateOpenAPIRestEndpoint() throws Exception {
        try (InputStream is = CamelKPublishHandler.class.getResourceAsStream("/expose-openapi-document.xml")) {
            String oepnApiEndpointXml = IOUtils.toString(is, UTF_8);
            final String content = configuration.getCamelk().isCompression() ? CamelKSupport.compress(oepnApiEndpointXml) : oepnApiEndpointXml;
            return new SourceSpec.Builder()
                .dataSpec(new DataSpec.Builder()
                    .compression(configuration.getCamelk().isCompression())
                    .content(content)
                    .name("openapi-endpoint")
                    .build())
                .language("xml")
                .build();
        }
    }
}
